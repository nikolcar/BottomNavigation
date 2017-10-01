package rs.elfak.mosis.nikolamitic.bottomnavigationview;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import rs.elfak.mosis.nikolamitic.bottomnavigationview.Class.BitmapManipulation;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Class.Parking;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Class.User;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Friends.FriendsFragment;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Home.HomeFragment;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Login.LoginActivity;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Settings.SettingsFragment;

public class MainActivity extends Activity
{
    private static final String TAG = "Locate Parking";

    int clicked = 1, newClicked =0;
    static HomeFragment homeFragment;
    FriendsFragment friendsFragment;
    SettingsFragment settingsFragment;
    Fragment newFragment;

    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth mAuth;
    static public FirebaseUser loggedUser;
    private DatabaseReference parkings, users;
    private StorageReference storage;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener()
    {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item)
        {
            switch (item.getItemId())
            {
                case R.id.navigation_home:
                    Log.d(TAG, "navigation_home");
                    OnResume();
                    newClicked = 1;
                    break;
                case R.id.navigation_friends:
                    Log.d(TAG, "navigation_friends");
                    newClicked = 2;
                    break;
                case R.id.navigation_settings:
                    Log.d(TAG, "navigation_settings");
                    newClicked = 3;
                    break;
            }

            if( 0 < newClicked && newClicked < 4 && clicked != newClicked)
            {
                changeFragment(newClicked, true);
                changeFragment(clicked, false);
                clicked = newClicked;
                return true;
            }

            return false;
        }

    };

    void addFragments()
    {
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        homeFragment = new HomeFragment();
        ft.add(R.id.fragmentContainer, homeFragment);
        ft.hide(homeFragment);

        friendsFragment = new FriendsFragment();
        ft.add(R.id.fragmentContainer, friendsFragment);
        ft.hide(friendsFragment);


        Bundle fragment = new Bundle();
        Bundle extras = getIntent().getExtras();
        String display_name = loggedUser.getDisplayName();
        if (extras != null)
            display_name= getIntent().getStringExtra("DISPLAY_NAME");

        fragment.putString("display_name", display_name);
        settingsFragment = new SettingsFragment();
        settingsFragment.setArguments(fragment);

        ft.add(R.id.fragmentContainer, settingsFragment);
        ft.hide(settingsFragment);

        ft.commit();
    }

    void changeFragment(int position, boolean show)
    {
        switch (position)
        {
            case 1:
                newFragment = homeFragment;
                break;
            case 2:
                newFragment = friendsFragment;
                break;
            case 3:
                newFragment = settingsFragment;
                break;
        }

        FragmentTransaction ft = getFragmentManager().beginTransaction();

        if(show)
            ft.show(newFragment);
        else
            ft.hide(newFragment);

        ft.commit();
    }

    public void disableDoubleSelect(int i)
    {
        Menu menu = null;

        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.navigation, menu);

        menu.getItem(1).setCheckable(true);
        menu.getItem(2).setCheckable(true);
        menu.getItem(3).setCheckable(true);

        menu.getItem(i).setCheckable(false);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        mAuth = FirebaseAuth.getInstance();

        authListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                loggedUser = firebaseAuth.getCurrentUser();
                if (loggedUser == null) {
                    // user auth state is changed - user is null
                    // launch login activity
                    startActivity(new Intent(MainActivity.this, LoginActivity.class));
                    finish();
                }
            }
        };

        loggedUser = mAuth.getCurrentUser();

        if(loggedUser!=null)
        {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            parkings = database.getReference("parkings");
            users = database.getReference("users");

            //remove title bar
            requestWindowFeature(Window.FEATURE_NO_TITLE);

            setContentView(R.layout.activity_main);

            BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
            //select home on start
            navigation.setSelectedItemId(R.id.navigation_home);

            addFragments();
            changeFragment(1, true);

            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        }
        else
        {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    }

    public void cancel_parking_click(View v)
    {
        homeFragment.dialog.dismiss();
    }

    public void add_parking_click(View v)
    {
        homeFragment.getGpsCoordinates();
        final String name = homeFragment.etName.getText().toString();
        final String description = homeFragment.etDescription.getText().toString();

        Double longitude, latitude;
        try
        {
            latitude = Double.parseDouble(homeFragment.etLatitude.getText().toString());
            longitude = Double.parseDouble(homeFragment.etLongitude.getText().toString());
        }
        catch (Throwable t)
        {
            latitude=null;
            longitude=null;
        }

        String text = homeFragment.sType.getSelectedItem().toString();

        final boolean secret = (text.equals("Private"));

        if (TextUtils.isEmpty(name))
        {
            Toast.makeText(this, "Enter parking name!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(description))
        {
            Toast.makeText(this, "Enter parking description!", Toast.LENGTH_SHORT).show();
            return;
        }

        Date date = Calendar.getInstance().getTime();

        loggedUser = mAuth.getCurrentUser();
        String uid = loggedUser.getUid();

        Parking newParking = new Parking(name, description, longitude, latitude, uid, secret);
        String key = parkings.push().getKey();
        parkings.child(key).setValue(newParking);

        if(secret)
        {
            users.child(uid).child("myPrivate").push().setValue(key);
        }

        //TODO add points
        //users.child(uid).child("points").

        Toast.makeText(this, "Parking " + name + " has been added!", Toast.LENGTH_SHORT).show();
        homeFragment.dialog.dismiss();
    }

    public void change_password_click(View v)
    {
        final String newPassword = settingsFragment.newPassword.getText().toString();
        final String repeatPassword = settingsFragment.repetePassword.getText().toString();



        if (TextUtils.isEmpty(newPassword))
        {
            Toast.makeText(getApplicationContext(), "Enter new password!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (newPassword.trim().length() < 6)
        {
            Toast.makeText(getApplicationContext(), "Password too short, enter minimum 6 characters", Toast.LENGTH_SHORT).show();
        }

        if (TextUtils.isEmpty(repeatPassword))
        {
            Toast.makeText(getApplicationContext(), "Repeat new password!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!newPassword.equals(repeatPassword))
        {
            Toast.makeText(getApplicationContext(), "Password and repeat password are not the same!", Toast.LENGTH_SHORT).show();
            return;
        }

        final ProgressDialog progressDialog = ProgressDialog.show(this, "Please wait...", "Processing...",true);

        try {
            loggedUser.updatePassword(newPassword.trim())
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(MainActivity.this, "Password is updated, sign in with new password!", Toast.LENGTH_SHORT).show();

                                    } else {
                                        Toast.makeText(MainActivity.this, "In order to change password you need to sing out and then sign in again :(", Toast.LENGTH_SHORT).show();
                                    }
                                    progressDialog.dismiss();
                                    settingsFragment.dialog.dismiss();
                                    settingsFragment.signOut();
                                }
                            });
        }catch (Throwable t){
            progressDialog.dismiss();
            Toast.makeText(MainActivity.this, "Error, please try again.",Toast.LENGTH_SHORT).show();
        }
    }

    public void cancel_account_edit_click(View v)
    {
        settingsFragment.dialog.dismiss();
    }

    public void camera_click(View v)
    {
        Intent imageIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

        File imagesFolder = new File(Environment.getExternalStorageDirectory(), "WorkingWithPhotosApp");
        imagesFolder.mkdirs();

        File image = new File(imagesFolder, "QR_1.png");
        settingsFragment.savedURI = Uri.fromFile(image);

        imageIntent.putExtra(MediaStore.EXTRA_OUTPUT, settingsFragment.savedURI);

        startActivityForResult(imageIntent, 0);
    }

    public void gallery_click(View v)
    {
        Intent pickPhoto = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(pickPhoto , 1);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0 || requestCode == 1)
        {
            if (resultCode == RESULT_OK)
            {
                final ProgressDialog progressDialog = ProgressDialog.show(this, "Please wait...", "Processing...",true);

                if (data != null)
                    settingsFragment.savedURI = data.getData();
/*
                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setPhotoUri(settingsFragment.savedURI).build();
                progressDialog.dismiss();
                loggedUser.updateProfile(profileUpdates).addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(MainActivity.this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
                    }
                });
*/
                storage = FirebaseStorage.getInstance().getReference().child("profile_images/" + loggedUser.getUid() + ".jpg");
                storage.putFile(settingsFragment.savedURI).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        settingsFragment.setProfilePhoto();
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Profile picture updated!", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, "Failed to upload picture, please try again!", Toast.LENGTH_SHORT).show();
                        progressDialog.dismiss();
                    }
                });
                settingsFragment.dialog.dismiss();
            }
            else {
                Toast.makeText(MainActivity.this, "Action canceled!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart");
        mAuth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop");
        if (authListener != null) {
            mAuth.removeAuthStateListener(authListener);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "onPause");
    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume");
        OnResume();
    }

    public void OnResume() {
        if(loggedUser!=null){
            friendsFragment.friendsList.clear();
            friendsFragment.mFriends.clear();

            if(homeFragment.googleMap!=null){
                homeFragment.googleMap.clear();
            }

            friendsFragment.getFriendsFromServer();
            friendsFragment.pauseWaitingForFriendsList=true;

            Runnable r2 = new Runnable() {
                @Override
                public void run() {
                    homeFragment.mapMarkersParkings.clear();
                    homeFragment.mapUserIdMarker.clear();
                    homeFragment.mapMarkerUser.clear();

                    while(friendsFragment.pauseWaitingForFriendsList){
                        synchronized (this) {
                            try {
                                wait(100);
                                //Log.d(TAG,"Waiting 100ms");
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    while(homeFragment.googleMap==null){
                        synchronized (this) {
                            try {
                                wait(100);
                                //Log.d(TAG,"Waiting 100ms");
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    settingsFragment.getSettingsFromServer();   //loadAllPlayersFromServer() must be inside this function
                    loadParkingsFromServer();
                }
            };
            Thread loadEverythingFromServer = new Thread(r2);
            loadEverythingFromServer.start();
        }
    }


    private void loadParkingsFromServer()
    {
        //https://firebase.google.com/docs/database/android/lists-of-data
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());
                final Parking parking = dataSnapshot.getValue(Parking.class);
                Log.d(TAG, "onChildAdded:" + parking.getName());
                Marker marker = addMarkers(parking.getLatitude(), parking.getLongitude(), parking.getName(), parking.getDescription(), null, "", dataSnapshot.getKey(), parking.isSecret());

                //Add to searchable HashMap
                homeFragment.mapMarkersParkings.put(parking, marker);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());
                //We don't have a ability to change a landmark
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());
                //We don't have a ability to change a landmark
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());
                //We don't have a ability to move a landmark in DB.
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
            }
        };
        parkings.addChildEventListener(childEventListener);
    }

    public void loadAllPlayersFromServer() {

        //https://firebase.google.com/docs/database/android/lists-of-data
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                if(settingsFragment.players_status){
                    //Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());
                    final User user = dataSnapshot.getValue(User.class);
                    //Log.d(TAG, "onChildAdded:" + user.firstName + " uid:" + user.uid);
                    String uid = dataSnapshot.getKey();
                    Marker marker = addMarkers(user.getLatitude(), user.getLongitude(),user.getNickname(), "", null, uid, "", false);
                    homeFragment.mapUserIdMarker.put(uid, marker);
                    homeFragment.mapMarkerUser.put(marker, user);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                //Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());
                User user = dataSnapshot.getValue(User.class);
                //Log.d(TAG, "onChildChanged:" + user.firstName + " uid:" + user.uid);

                String uid = dataSnapshot.getKey();

                Marker mMarker;
                mMarker = homeFragment.mapUserIdMarker.get(uid);

                if(mMarker!=null) {
                    //Log.d(TAG,"Brisem marker");
                    mMarker.setPosition(new LatLng(user.getLatitude(), user.getLongitude()));
                    /*
                    if (loggedUser.getUid().equals(uid))
                    {
                        CameraPosition mCameraPosition = new CameraPosition.Builder().target(new LatLng(user.getLatitude(),user.getLongitude())).zoom(15).build();
                        homeFragment.googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
                    }
                    */
                }else{
                    //Log.d(TAG,"Ne brisem marker");
                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
            }
        };
        users.addChildEventListener(childEventListener);
    }

    private Marker addMarkers(double lat, double lng, String title, String snippet, Bitmap icon, String uId, String parkingId, boolean secret){
        Log.d(TAG,"addMarkers uid:" + uId);
        Marker marker = null;
        Float factor = 0.7f;

        MarkerOptions mo = new MarkerOptions();
        mo.position(new LatLng(lat, lng));
        mo.title(title);
        if(snippet!=null && !snippet.equals("")){
            mo.snippet(snippet);
        }
        if(uId==null || uId.equals("")){
            if(secret)
            {
                mo.icon(BitmapDescriptorFactory.fromBitmap(bitmapSizeByScall(BitmapManipulation.getMarkerBitmapFromView(R.mipmap.free, MainActivity.this),factor)));
            }
            else
            {
                mo.icon(BitmapDescriptorFactory.fromBitmap(bitmapSizeByScall(BitmapManipulation.getMarkerBitmapFromView(R.mipmap.occupied, MainActivity.this),factor)));
            }
        }else{
            if(uId.equals(loggedUser.getUid())){
                mo.icon(BitmapDescriptorFactory.fromBitmap(bitmapSizeByScall(BitmapManipulation.getMarkerBitmapFromView(R.mipmap.me, MainActivity.this),factor)));
                /*
                CameraPosition mCameraPosition = new CameraPosition.Builder().target(new LatLng(lat,lng)).zoom(15).build();
                homeFragment.googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
                */
            }else{
                if(friendsFragment.friendsList.contains(uId)){
                    mo.icon(BitmapDescriptorFactory.fromBitmap(bitmapSizeByScall(BitmapManipulation.getMarkerBitmapFromView(R.mipmap.friend, MainActivity.this),0.6f)));
                }else{
                    mo.icon(BitmapDescriptorFactory.fromBitmap(bitmapSizeByScall(BitmapManipulation.getMarkerBitmapFromView(R.mipmap.user, MainActivity.this),0.5f)));
                }
            }
        }

        marker = homeFragment.googleMap.addMarker(mo);

        if(friendsFragment.friendsList.contains(uId)){
            if(homeFragment.friendsMarker.containsKey(uId)){
                homeFragment.friendsMarker.remove(uId);
            }
            homeFragment.friendsMarker.put(uId, marker);
        }

        if(uId==null || uId.equals("")){
            if(homeFragment.parkingsMarker.containsKey(parkingId)){
                homeFragment.parkingsMarker.remove(parkingId);
            }
            homeFragment.parkingsMarker.put(parkingId, marker);
        }
        return marker;
    }

    public Bitmap bitmapSizeByScall(Bitmap bitmapIn, float scall_zero_to_one_f)
    {
        Bitmap bitmapOut = Bitmap.createScaledBitmap(bitmapIn,
                Math.round(bitmapIn.getWidth() * scall_zero_to_one_f),
                Math.round(bitmapIn.getHeight() * scall_zero_to_one_f), false);

        return bitmapOut;
    }

    public void loadPlayersFromServer(Boolean players_status, Boolean friends_status) {
        if(players_status & friends_status)
        {
            loadAllPlayersFromServer();
        }
        else if(players_status)
        {
            loadUsersFromServer(false);
        }
        else
        {
            loadUsersFromServer(friends_status);
            users.child(loggedUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    User user = dataSnapshot.getValue(User.class);

                    String uid = dataSnapshot.getKey();
                    Marker marker = addMarkers(user.getLatitude(), user.getLongitude(), user.getNickname(), "", null, uid, "", false);
                    homeFragment.mapUserIdMarker.put(uid, marker);
                    homeFragment.mapMarkerUser.put(marker, user);
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    Toast.makeText(MainActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void loadUsersFromServer(final boolean friends) {

        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                //Log.d(TAG, "onChildAdded:" + dataSnapshot.getKey());
                final User user = dataSnapshot.getValue(User.class);
                //Log.d(TAG, "onChildAdded:" + user.firstName + " uid:" + user.uid);
                String uid = dataSnapshot.getKey();
                if (friendsFragment.friendsList.contains(uid)==friends) {
                    Marker marker = addMarkers(user.getLatitude(), user.getLongitude(), user.getNickname(), "", null, uid, "", false);
                    homeFragment.mapUserIdMarker.put(uid, marker);
                    homeFragment.mapMarkerUser.put(marker, user);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                //Log.d(TAG, "onChildChanged:" + dataSnapshot.getKey());
                User user = dataSnapshot.getValue(User.class);
                //Log.d(TAG, "onChildChanged:" + user.firstName + " uid:" + user.uid);

                String uid = dataSnapshot.getKey();

                Marker mMarker;
                mMarker = homeFragment.mapUserIdMarker.get(uid);

                if(mMarker!=null) {
                    //Log.d(TAG,"Brisem marker");
                    mMarker.setPosition(new LatLng(user.getLatitude(), user.getLongitude()));
                    /*
                    if (loggedUser.getUid().equals(uid))
                    {
                        CameraPosition mCameraPosition = new CameraPosition.Builder().target(new LatLng(user.getLatitude(),user.getLongitude())).zoom(15).build();
                        homeFragment.googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
                    }
                    */
                }else{
                    //Log.d(TAG,"Ne brisem marker");
                }

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                Log.d(TAG, "onChildRemoved:" + dataSnapshot.getKey());
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
                Log.d(TAG, "onChildMoved:" + dataSnapshot.getKey());
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "postComments:onCancelled", databaseError.toException());
            }
        };
        users.addChildEventListener(childEventListener);
    }
}
