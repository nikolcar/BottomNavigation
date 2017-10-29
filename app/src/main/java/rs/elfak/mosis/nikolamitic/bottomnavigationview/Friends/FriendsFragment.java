package rs.elfak.mosis.nikolamitic.bottomnavigationview.Friends;

import android.app.Activity;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.view.menu.ActionMenuItemView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import rs.elfak.mosis.nikolamitic.bottomnavigationview.Bluetooth.ChatService;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Bluetooth.DeviceListActivity;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Class.BitmapManipulation;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Class.User;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Home.HomeFragment;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.MainActivity;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.MyLocationService;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.R;

public class FriendsFragment extends Fragment
{
    private static final String FRIEND_REQUEST_CODE = "FRIEND_REQUEST_";
    private static final int BT_DISCOVERABLE_TIME = 120;
    private static final int ADD_POINTS_NEW_FRIEND = 10;
    private FriendListAdapter mAdapter;
    private ArrayList<FriendModel> mFriends;
    private ArrayList<String> friendsList;

    public static boolean pauseWaitingForFriendsList = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.friends_fragment, container, false);

        ListView lvHighscore = (ListView) view.findViewById(R.id.highscore_list);

        mFriends = new ArrayList<>();
        friendsList = new ArrayList<>();

        mAdapter = new FriendListAdapter(getActivity().getApplicationContext(), mFriends);
        lvHighscore.setAdapter(mAdapter);

        lvHighscore.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
           @Override
           public void onItemClick(AdapterView<?> parent, View view, int position, long id)
           {
               MainActivity mainActivity = (MainActivity) getActivity();
               if(mainActivity.getSettingsFragment().friends_status)
               {
                   FriendModel dataModel = mFriends.get(position);
                   String friendsUid = dataModel.getuId();

                   Marker friendMarker = HomeFragment.mapFriendIdMarker.get(friendsUid);
                   friendMarker.showInfoWindow();
                   CameraPosition mCameraPosition = new CameraPosition.Builder().target(friendMarker.getPosition()).zoom(16).build();
                   mainActivity.getHomeFragment().googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));

                   mainActivity.performHomeClick();
               }
               else
               {
                   Toast.makeText(mainActivity, "To find friends on map, you first need to make them visible in settings.", Toast.LENGTH_LONG).show();
               }
           }
        });

        FloatingActionButton btnAddFriend = (FloatingActionButton) view.findViewById(R.id.btn_add_friend);

        btnAddFriend.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter == null) {
                    Toast.makeText(getActivity(),"Bluetooth is not available", Toast.LENGTH_LONG).show();
                    return;
                }
                ensureDiscoverable(bluetoothAdapter);   //onActivityResult checks if discoverability in enabled and then sends friend request
            }
        });
        getFriendsFromServer();
        pauseWaitingForFriendsList = true;

        return view;
    }

    public void getFriendsFromServer()
    {
        friendsList.clear();
        mFriends.clear();
        mAdapter.clear();

        String loggedUserId = MainActivity.loggedUser.getUid();
        getFriendData(loggedUserId);

        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "Please wait...", "Loading markers...",true);
        DatabaseReference userFriends = FirebaseDatabase.getInstance().getReference("users").child(loggedUserId).child("friends");
        userFriends.addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                {
                    String json = singleSnapshot.toString();

                    String friendUid = json.substring(json.indexOf("value = ") + 8, json.length() - 2);

                    if (!friendUid.equals(""))
                    {
                        if (!friendsList.contains(friendUid))
                        {
                            getFriendData(friendUid);
                            friendsList.add(friendUid);
                        }
                    }
                }
                progressDialog.dismiss();
                pauseWaitingForFriendsList =false;
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
            }
        });
    }

    private void getFriendData(final String friendUid)
    {
        FirebaseDatabase.getInstance().getReference("users").child(friendUid).addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "Please wait...", "Loading friends...", true);
                final User friend = dataSnapshot.getValue(User.class);

                if (friend != null)
                {
                    StorageReference storage = FirebaseStorage.getInstance().getReference().child("profile_images/" + friendUid + ".jpg");
                    final long MEMORY = 10 * 1024 * 1024;

                    storage.getBytes(MEMORY).addOnSuccessListener(new OnSuccessListener<byte[]>()
                    {
                        @Override
                        public void onSuccess(final byte[] bytes)
                        {
                            //TODO: don't deserialize like this
                            Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            bitmap = BitmapManipulation.getCroppedBitmap(bitmap);

                            mFriends.add(new FriendModel(friend.getFirstName() + " " +
                                    friend.getLastName() + "\n" + friend.getNickname(), friend.getPoints(), bitmap, friendUid));
                            bitmap = null;
                            updatePoints(friendUid);
                            mAdapter.notifyDataSetChanged();
                        }
                    }).addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception exception)
                        {
                            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.avatar);
                            mFriends.add(new FriendModel(friend.getFirstName() + " " +
                                    friend.getLastName() + "\n" + friend.getNickname(), friend.getPoints(), bitmap, friendUid));
                            bitmap = null;
                            updatePoints(friendUid);
                            mAdapter.notifyDataSetChanged();
                        }
                    });
                }
                else
                {
                    Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.avatar);
                    mFriends.add(new FriendModel("fake user\n" + friendUid, 0, bitmap, friendUid));
                    bitmap = null;

                    Collections.sort(mFriends, new Comparator<FriendModel>()
                    {
                        @Override
                        public int compare(FriendModel o1, FriendModel o2)
                        {
                            return o2.getPoints()-o1.getPoints();
                        }
                    });

                    mAdapter.notifyDataSetChanged();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    void updatePoints(final String friendUid)
    {
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        database.getReference("users").child(friendUid).child("points").addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                int points = dataSnapshot.getValue(Integer.class);
                int i = findModelById(friendUid);
                mFriends.get(i).setPoints(points);

                Collections.sort(mFriends, new Comparator<FriendModel>()
                {
                    @Override
                    public int compare(FriendModel o1, FriendModel o2)
                    {
                        return o2.getPoints()-o1.getPoints();
                    }
                });

                mAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
            }
        });
    }

    private int findModelById(String uId)
    {
        int i = 0;
        while (i < mFriends.size())
        {
            FriendModel model = mFriends.get(i);
            String fId = model.getuId();
            if (fId.equals(uId))
                break;
            i++;
        }
        return i;
    }

    private void addNewFriend()
    {
        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                Intent serverIntent = new Intent(getActivity(), DeviceListActivity.class);
                startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_SECURE);
            }
        };
        Thread btThread = new Thread(r);
        btThread.start();
    }

    //----------------------------------------------------------------------------------------------------------------------------------

    private final static String TAG = "Bluetooth";
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private static final int REQUEST_CONNECT_DEVICE_SECURE = 1;
    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private String connectedDeviceName = null;

    private StringBuffer outStringBuffer;
    private BluetoothAdapter bluetoothAdapter = null;
    private ChatService chatService = null;

    private Handler handler = new Handler(new Handler.Callback()
    {
        @Override
        public boolean handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1)
                    {
                        case ChatService.STATE_CONNECTED:
                            sendFriendRequest();
                            break;
                        case ChatService.STATE_CONNECTING:
                            sendFriendRequest();
                            break;
                        case ChatService.STATE_LISTEN:
                        case ChatService.STATE_NONE:
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;
                    String message = new String(readBuf, 0, msg.arg1);

                    int _char = message.lastIndexOf("_");
                    String messageCheck = message.substring(0,_char+1);
                    final String friendsUid = message.substring(_char+1);

                    if(messageCheck.equals(FRIEND_REQUEST_CODE))
                    {
                        Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                        v.vibrate(500);

                        final String myUid = MainActivity.loggedUser.getUid();
                        final DatabaseReference database = FirebaseDatabase.getInstance().getReference("users").child(myUid);
                        final DatabaseReference dbRef = database.child("friends");

                        getActivity().runOnUiThread(new Runnable()
                        {
                            public void run()
                            {
                                new AlertDialog.Builder(getActivity())
                                        .setTitle("Confirm friend request")
                                        .setMessage("Are you sure you want to become friends with a device\n\n" + connectedDeviceName)
                                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener()
                                        {
                                            public void onClick(DialogInterface dialog, int which)
                                            {
                                                dbRef.addListenerForSingleValueEvent(new ValueEventListener()
                                                {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot)
                                                    {
                                                        List<String> friendsList = new ArrayList<>();

                                                        for (DataSnapshot singleSnapshot : dataSnapshot.getChildren())
                                                        {
                                                            String json = singleSnapshot.toString();

                                                            //TODO: deserialize via class, not like this
                                                            String friendUid = json.substring(json.indexOf("value = ") + 8, json.length() - 2);
                                                            friendsList.add(friendUid);
                                                        }

                                                        if (friendsList.contains(friendsUid))
                                                        {
                                                            Toast.makeText(getActivity(), "You already have this friend!", Toast.LENGTH_SHORT).show();
                                                        }
                                                        else
                                                        {
                                                            friendsList.add(friendsUid);
                                                            dbRef.setValue(friendsList);

                                                            Toast.makeText(getActivity(),"Adding " + ADD_POINTS_NEW_FRIEND + " points!",Toast.LENGTH_SHORT).show();
                                                            MyLocationService.myPoints += ADD_POINTS_NEW_FRIEND;
                                                            database.child("points").setValue(MyLocationService.myPoints);

                                                            getFriendData(friendsUid);

                                                            Marker friendMarker = HomeFragment.mapUserIdMarker.get(friendsUid);
                                                            HomeFragment.mapUserIdMarker.remove(friendsUid);
                                                            friendMarker.setIcon(BitmapDescriptorFactory.fromBitmap(BitmapManipulation.getMarkerBitmapFromView(R.mipmap.friend, getActivity())));
                                                            friendMarker.setVisible(((MainActivity)getActivity()).getSettingsFragment().friends_status);
                                                            HomeFragment.mapFriendIdMarker.put(friendsUid, friendMarker);
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError)
                                                    {
                                                    }
                                                });
                                            }
                                        })
                                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener()
                                        {
                                            public void onClick(DialogInterface dialog, int which)
                                            {
                                                Toast.makeText(getActivity(), "You declined friend request", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .setIcon(R.mipmap.logo)
                                        .show();
                            }
                        });
                    }
                    break;
                case MESSAGE_DEVICE_NAME:
                    connectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getActivity(), "Connected to " + connectedDeviceName + "\nClose upper window and confirm friend request.", Toast.LENGTH_LONG).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getActivity(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    });

    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        switch (requestCode)
        {
            case REQUEST_CONNECT_DEVICE_SECURE:
                if (resultCode == Activity.RESULT_OK)
                {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                if (resultCode == Activity.RESULT_OK)
                {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == BT_DISCOVERABLE_TIME)
                {
                    setupChat();
                    addNewFriend();
                }
                else
                {
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }

    private void connectDevice(Intent data, boolean secure)
    {
        String address = data.getExtras().getString(DeviceListActivity.DEVICE_ADDRESS);
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        try
        {
            chatService.connect(device, secure);
        }
        catch (Exception e)
        {
            Toast.makeText(getActivity(), "Error! Other user must click on + button.", Toast.LENGTH_LONG).show();
            Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(500);
        }
    }

    private void ensureDiscoverable(BluetoothAdapter bluetoothAdapter)
    {
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, BT_DISCOVERABLE_TIME);
        startActivityForResult(discoverableIntent,REQUEST_ENABLE_BT);
    }

    private void sendMessage(String message)
    {
        if (chatService.getState() != ChatService.STATE_CONNECTED)
        {
            return;
        }

        if (message.length() > 0)
        {
            byte[] send = message.getBytes();
            chatService.write(send);
            outStringBuffer.setLength(0);
        }
    }

    private boolean setupChat()
    {
        chatService = new ChatService(getActivity(), handler);
        outStringBuffer = new StringBuffer("");

        if (chatService.getState() == ChatService.STATE_NONE)
        {
            chatService.start();
        }

        return true;
    }

    private void sendFriendRequest()
    {
        String message = FRIEND_REQUEST_CODE + MainActivity.loggedUser.getUid();
        sendMessage(message);
    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    @Override
    public synchronized void onResume()
    {
        super.onResume();
    }

    @Override
    public synchronized void onPause()
    {
        super.onPause();
    }

    @Override
    public void onStop()
    {
        super.onStop();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (chatService != null)
            chatService.stop();
    }

    public ArrayList<String> getFriendsList()
    {
        return friendsList;
    }
}
