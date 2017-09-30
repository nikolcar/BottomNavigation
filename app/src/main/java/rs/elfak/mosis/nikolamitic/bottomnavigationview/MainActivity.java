package rs.elfak.mosis.nikolamitic.bottomnavigationview;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Calendar;
import java.util.Date;

import rs.elfak.mosis.nikolamitic.bottomnavigationview.Class.Parking;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Friends.FriendsFragment;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Home.HomeFragment;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Login.LoginActivity;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Settings.SettingsFragment;

public class MainActivity extends Activity
{
    private static final String TAG = "Locate Parking";

    int clicked = 1, newClicked =0;
    HomeFragment homeFragment = new HomeFragment();
    FriendsFragment friendsFragment = new FriendsFragment();
    SettingsFragment settingsFragment = new SettingsFragment();
    Fragment newFragment = null;

    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth mAuth;
    private FirebaseUser loggedUser;
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

        ft.add(R.id.fragmentContainer, homeFragment);
        ft.hide(homeFragment);

        ft.add(R.id.fragmentContainer, friendsFragment);
        ft.hide(friendsFragment);

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

        Parking newParking = new Parking(name, description, longitude, latitude, uid, secret, date);
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
}
