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
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Friends.FriendListAdapter;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Friends.FriendModel;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.MainActivity;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.MyLocationService;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.R;

public class FriendsFragment extends Fragment
{
    public static final String FRIEND_REQUEST_CODE = "FRIEND_REQUEST_";
    private static final int BT_DISCOVERABLE_TIME = 120;
    private static final int ADD_POINTS_NEW_FRIEND = 10;
    private ListView lvHighscore;
    private FriendListAdapter mAdapter;
    public static ArrayList<FriendModel> mFriends;
    public static ArrayList<String> friendsList;
    FloatingActionButton btnAddFriend;

    FirebaseUser loggedUser;
    FirebaseAuth mAuth;

    public static boolean pauseWaitingForFriendsList = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.friends_fragment, container, false);

        mAuth = FirebaseAuth.getInstance();
        loggedUser = mAuth.getCurrentUser();

        lvHighscore = (ListView) view.findViewById(R.id.highscore_list);

        mFriends = new ArrayList<>();
        friendsList = new ArrayList<>();

        mAdapter = new FriendListAdapter(getActivity().getApplicationContext(), mFriends);
        lvHighscore.setAdapter(mAdapter);

        lvHighscore.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
           @Override
           public void onItemClick(AdapterView<?> parent, View view, int position, long id)
           {
               FriendModel dataModel = mFriends.get(position);
               Toast.makeText(getActivity(), "" + dataModel.getName(), Toast.LENGTH_SHORT).show();
           }
        });

        btnAddFriend = (FloatingActionButton) view.findViewById(R.id.btn_add_friend);

        btnAddFriend.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                //Snackbar.make(getView(), "Wait for incoming friend request or send one.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                if (bluetoothAdapter == null) {
                    Toast.makeText(getActivity(),"Bluetooth is not available", Toast.LENGTH_LONG).show();
                    //finish();
                    return;
                }
                ensureDiscoverable(bluetoothAdapter);   //onActivityResult checks if discoverability in enabled and then sends friend request
            }
        });

        return view;
    }

    public void getFriendsFromServer()
    {
        friendsList.clear();
        mFriends.clear();
        mAdapter.clear();
        pauseWaitingForFriendsList =true;

        getFriendData(loggedUser.getUid());

        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "Please wait...", "Loading friends...",true);
        DatabaseReference userFriends = FirebaseDatabase.getInstance().getReference("users").child(loggedUser.getUid()).child("friends");
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

                            mFriends.add(new FriendModel(friend.getFirstName() + " " + friend.getLastName() + "\n" + friend.getNickname(), friend.getPoints(), bitmap, friendUid));
                            bitmap = null;
                            updatePoints(friendUid);
                            mAdapter.notifyDataSetChanged();
                        }
                    }).addOnFailureListener(new OnFailureListener()
                    {
                        @Override
                        public void onFailure(@NonNull Exception exception)
                        {
                            //user exists, but doesn't have profile photo
                            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.avatar);
                            mFriends.add(new FriendModel(friend.getFirstName() + " " + friend.getLastName() + "\n" + friend.getNickname(), friend.getPoints(), bitmap, friendUid));
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


    private void addNewFriend() {
        Log.d(TAG, "Friends addNewFriend started");
        Runnable r = new Runnable() {

            @Override
            public void run() {
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

    private ListView lvMainChat;
    private EditText etMain;
    private Button btnSend;

    private String connectedDeviceName = null;
    //private ArrayAdapter<String> chatArrayAdapter;

    private StringBuffer outStringBuffer;
    private BluetoothAdapter bluetoothAdapter = null;
    private ChatService chatService = null;

    private Handler handler = new Handler(new Handler.Callback() {

        @Override
        public boolean handleMessage(Message msg) {
            Log.d(TAG, "MainActivity: handleMessage started");
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case ChatService.STATE_CONNECTED:
                            Log.d(TAG, "MainActivity: handleMessage MESSAGE_STATE_CHANGE STATE_CONNECTED");     //for new devices
                            //setStatus(getString(R.string.title_connected_to, connectedDeviceName));
                            //chatArrayAdapter.clear();
                            sendFriendRequest();
                            break;
                        case ChatService.STATE_CONNECTING:
                            Log.d(TAG, "MainActivity: handleMessage MESSAGE_STATE_CHANGE STATE_CONNECTING");    //for paired devices??
                            //setStatus(R.string.title_connecting);
                            sendFriendRequest();
                            break;
                        case ChatService.STATE_LISTEN:
                        case ChatService.STATE_NONE:
                            Log.d(TAG, "MainActivity: handleMessage MESSAGE_STATE_CHANGE STATE_NONE");
                            //setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    Log.d(TAG, "MainActivity: handleMessage MESSAGE_WRITE");
                    byte[] writeBuf = (byte[]) msg.obj;

                    String writeMessage = new String(writeBuf);
                    //chatArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case MESSAGE_READ:
                    Log.d(TAG, "MainActivity: handleMessage MESSAGE_READ");
                    byte[] readBuf = (byte[]) msg.obj;

                    String readMessage = new String(readBuf, 0, msg.arg1);

                    Log.d(TAG, "readMessage:" + readMessage);
                    //Toast.makeText(MainActivity, ""+ readMessage, Toast.LENGTH_LONG).show();

                    String message = readMessage;

                    int _char = message.lastIndexOf("_");
                    String messageCheck = message.substring(0,_char+1);
                    final String friendsUid = message.substring(_char+1);
                    Log.d(TAG,"TEMP messageCheck:" + messageCheck); //messageCheck:FRIEND_REQUEST_
                    Log.d(TAG,"TEMP friendsUid:" + friendsUid);
                    Log.d(TAG,"TEMP FRIEND_REQUEST_CODE:" + FRIEND_REQUEST_CODE);//FRIEND_REQUEST_CODE:FRIEND_REQUEST_

                    if(messageCheck.equals(FRIEND_REQUEST_CODE)) {
                        Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
                        v.vibrate(500);

                        final String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        final DatabaseReference database = FirebaseDatabase.getInstance().getReference("users").child(myUid);
                        final DatabaseReference dbRef = database.child("friends");

                        getActivity().runOnUiThread(new Runnable() {
                            public void run() {
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

                                                            Log.d(TAG, "friendUid2: " + friendUid);

                                                        }

                                                        if (friendsList.contains(friendsUid))
                                                        {
                                                            Toast.makeText(getActivity(), "You already have this friend!", Toast.LENGTH_SHORT).show();
                                                        }
                                                        else
                                                        {
                                                            friendsList.add(friendsUid); //adding new friendship
                                                            dbRef.setValue(friendsList);

                                                            getFriendData(friendsUid);

                                                            Log.d(TAG, "friendUid2: " + friendsUid);

                                                            Toast.makeText(getActivity(),"Adding " + ADD_POINTS_NEW_FRIEND + " points!",Toast.LENGTH_SHORT).show();
                                                            MyLocationService.myPoints += ADD_POINTS_NEW_FRIEND;
                                                            database.child("points").setValue(MyLocationService.myPoints);
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError)
                                                    {
                                                        //Log.e(TAG, "onCancelled", databaseError.toException());
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
                                        }).setIcon(R.mipmap.logo).show();
                            }
                        });
                    }
                    break;
                case MESSAGE_DEVICE_NAME:
                    Log.d(TAG, "MainActivity: handleMessage MESSAGE_DEVICE_NAME");
                    connectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getActivity(), "Connected to " + connectedDeviceName + "\nClose upper window and confirm friend request.", Toast.LENGTH_LONG).show();
                    break;
                case MESSAGE_TOAST:
                    Log.d(TAG, "MainActivity: handleMessage MESSAGE_TOAST");
                    Toast.makeText(getActivity(), msg.getData().getString(TOAST), Toast.LENGTH_SHORT).show();
                    break;
            }
            return false;
        }
    });

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "MainActivity: onActivityResult started");
        Log.d(TAG, "requestCode=" + requestCode + " resultCode=" + resultCode);
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_SECURE:
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, true);
                }
                break;
            case REQUEST_CONNECT_DEVICE_INSECURE:
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
            case REQUEST_ENABLE_BT:
                if (resultCode == BT_DISCOVERABLE_TIME) {
                    //Toast.makeText(this,"Setup chat", Toast.LENGTH_SHORT).show();
                    setupChat();
                    addNewFriend();
                } else {
                    Toast.makeText(getActivity(), R.string.bt_not_enabled_leaving, Toast.LENGTH_SHORT).show();
                    //finish();
                }
                break;
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        Log.d(TAG, "MainActivity: connectDevice started");
        String address = data.getExtras().getString(DeviceListActivity.DEVICE_ADDRESS);
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        try{
            chatService.connect(device, secure);
        }catch (Exception e){
            Toast.makeText(getActivity(), "Error! Other user must click on + button.", Toast.LENGTH_LONG).show();
            Vibrator v = (Vibrator) getActivity().getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(500);
        }
    }

    private void ensureDiscoverable(BluetoothAdapter bluetoothAdapter) {
        Log.d(TAG, "MainActivity: ensureDiscoverable started");
        //if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) { //this must be commented because then onActivityResult is not called when BT is enabled before enterin Friends activity
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, BT_DISCOVERABLE_TIME);
        startActivityForResult(discoverableIntent,REQUEST_ENABLE_BT);
        //}
    }

    private void sendMessage(String message) {
        Log.d(TAG, "MainActivity: sendMessage started");
        if (chatService.getState() != ChatService.STATE_CONNECTED) {
            //Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT).show();
            return;
        }

        if (message.length() > 0) {
            byte[] send = message.getBytes();
            chatService.write(send);

            outStringBuffer.setLength(0);
            //etMain.setText(outStringBuffer);
        }
    }

    private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_NULL
                    && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            return true;
        }
    };

//    private final void setStatus(int resId) {
//        Log.d(TAG, "MainActivity: setStatus1 started");
//        final ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
//        actionBar.setSubtitle(resId);
//    }
//
//    private final void setStatus(CharSequence subTitle) {
//        Log.d(TAG, "MainActivity: setStatus2 started");
//        final ActionBar actionBar = ((AppCompatActivity)getActivity()).getSupportActionBar();
//        actionBar.setSubtitle(subTitle);
//    }

    private boolean setupChat() {
        Log.d(TAG, "MainActivity: setupChat started");
        //chatArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        //lvMainChat.setAdapter(chatArrayAdapter);

        chatService = new ChatService(getActivity(), handler);

        outStringBuffer = new StringBuffer("");

        if (chatService.getState() == ChatService.STATE_NONE) {
            chatService.start();
        }
        return true;
    }

    private void sendFriendRequest(){
        String message = FRIEND_REQUEST_CODE + FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG, "MainActivity: addNewFriend sendingMessage:" + message);
        sendMessage(message);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "MainActivity: onStart started");
    }

    @Override
    public synchronized void onResume() {
        super.onResume();
        Log.d(TAG, "MainActivity: onResume started");
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
        Log.d(TAG, "MainActivity: onPause started");
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (chatService != null)
            chatService.stop();
    }
}
