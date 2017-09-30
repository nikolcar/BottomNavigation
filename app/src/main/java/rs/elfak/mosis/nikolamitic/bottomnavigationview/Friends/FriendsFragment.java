package rs.elfak.mosis.nikolamitic.bottomnavigationview.Friends;

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
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
import java.util.List;

import rs.elfak.mosis.nikolamitic.bottomnavigationview.Class.BitmapManipulation;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Class.User;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Friends.FriendListAdapter;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Friends.FriendModel;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.R;

public class FriendsFragment extends Fragment
{
    private ListView lvHighscore;
    private FriendListAdapter mAdapter;
    private ArrayList<FriendModel> mFriends;
    FloatingActionButton btnAddFriend;

    FirebaseDatabase database;
    FirebaseUser loggedUser;
    FirebaseAuth mAuth;
    DatabaseReference userFriends;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.friends_fragment, container, false);

        mAuth = FirebaseAuth.getInstance();
        loggedUser = mAuth.getCurrentUser();
        database = FirebaseDatabase.getInstance();

        lvHighscore = (ListView) view.findViewById(R.id.highscore_list);
        
        mFriends = new ArrayList<>();

        mAdapter = new FriendListAdapter(getActivity().getApplicationContext(), mFriends);
        lvHighscore.setAdapter(mAdapter);

        lvHighscore.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
           @Override
           public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
               FriendModel dataModel = mFriends.get(position);
               Toast.makeText(getActivity(), "" + dataModel.getName(), Toast.LENGTH_SHORT).show();
           }
        });

        getFriendsFromServer();

        btnAddFriend = (FloatingActionButton) view.findViewById(R.id.btn_add_friend);

        btnAddFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                    addFriend("5236ee");
            }
        });

        return view;
    }

    private void getFriendsFromServer()
    {
        userFriends = database.getReference("users").child(loggedUser.getUid()).child("friends");
        //Toast.makeText(getActivity(), "Getting friends from server", Toast.LENGTH_SHORT).show();
        userFriends.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                    String json = singleSnapshot.toString();

                    final String friendUid = json.substring(json.indexOf("value = ") + 8, json.length() - 2);

                    //Toast.makeText(getActivity(), friendUid, Toast.LENGTH_SHORT).show();

                    if (!friendUid.equals("")) {
                        database.getReference("users").child(friendUid).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "Please wait...", "Loading friends...", true);
                                final User friend = dataSnapshot.getValue(User.class);
                               // Toast.makeText(getActivity(), friend.getFirstName(), Toast.LENGTH_SHORT).show();

                            if (friend != null) {
                                StorageReference storage = FirebaseStorage.getInstance().getReference().child("profile_images/" + friendUid + ".jpg");
                                final long MEMORY = 10 * 1024 * 1024;

                                storage.getBytes(MEMORY).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(final byte[] bytes)
                                    {
                                        //TODO: don't deserialize like this
                                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                        bitmap = BitmapManipulation.getCroppedBitmap(bitmap);

                                        mFriends.add(new FriendModel(friend.getFirstName() + " " + friend.getLastName() + "\n" + friend.getNickname(), friend.getPoints(), bitmap));
                                        mAdapter.notifyDataSetChanged();

                                        bitmap = null;
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception exception) {
                                        //user exists, but doesn't have profile photo
                                        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.avatar);
                                        mFriends.add(new FriendModel(friend.getFirstName() + " " + friend.getLastName() + "\n" + friend.getNickname(), friend.getPoints(), bitmap));
                                        mAdapter.notifyDataSetChanged();
                                        bitmap = null;
                                    }
                                });
                            } else {
                                Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.mipmap.avatar);
                                mFriends.add(new FriendModel("fake user\n" + friendUid, 0, bitmap));
                                mAdapter.notifyDataSetChanged();
                                bitmap = null;
                            }

                            progressDialog.dismiss();
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                Toast.makeText(getActivity(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                //Log.e(TAG, "onCancelled", databaseError.toException());
            }
        });
    }


    private void addFriend(final String friendsUid)
    {
        new AlertDialog.Builder(getActivity())
                .setTitle("Confirm friend request")
                .setMessage("Are you sure you want to become friends with a device\n + connectedDeviceName + \nUserID( + friendsUid + )")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        final String myUid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        final FirebaseDatabase database = FirebaseDatabase.getInstance();
                        final DatabaseReference dbRef = database.getReference("users").child(myUid).child("friends");
                        dbRef.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                List<String> friendsList = new ArrayList<>();
                                for (DataSnapshot singleSnapshot : dataSnapshot.getChildren()) {
                                    String json = singleSnapshot.toString();
                                    //TODO: deserialize via class, not like this
                                    String friendUid = json.substring(json.indexOf("value = ") + 8, json.length() - 2);
                                    friendsList.add(friendUid);
                                }
                                if (friendsList.contains(friendsUid)) {
                                    Toast.makeText(getActivity(), "You already have this friend!", Toast.LENGTH_SHORT).show();
                                } else {
                                    friendsList.add(friendsUid); //adding new friendship
                                    database.getReference("users").child(myUid).child("friends").setValue(friendsList);

                                        /*
                                        dbAdapter.open();
                                        if (!dbAdapter.checkFriendship(friendsUid))
                                        {
                                            dbAdapter.insertFriendship(friendsUid);
                                            BackgroundService.myPoints += 5;
                                            FirebaseDatabase.getInstance().getReference("scoreTable").child(myUid).child("points").setValue(BackgroundService.myPoints);
                                        }
                                        dbAdapter.close();
                                        */

                                    Toast.makeText(getActivity(),"Adding  + ADD_POINTS_NEW_FRIEND +  points!",Toast.LENGTH_SHORT).show();
                                    //BackgroundService.myPoints += ADD_POINTS_NEW_FRIEND;
                                    //FirebaseDatabase.getInstance().getReference("scoreTable").child(myUid).child("points").setValue(BackgroundService.myPoints);

                                    //Snackbar.make(findViewById(android.R.id.content), "You are now friends with " + friendsUid, Snackbar.LENGTH_LONG).show();
                                    //adapter.clear();
                                    //getFriendsFromServer(dbRef, adapter);
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                //Log.e(TAG, "onCancelled", databaseError.toException());
                            }

                        });
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(getActivity(), "You declined friend request", Toast.LENGTH_SHORT).show();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
