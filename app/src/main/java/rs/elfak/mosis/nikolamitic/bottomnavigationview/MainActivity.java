package rs.elfak.mosis.nikolamitic.bottomnavigationview;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.android.ui.IconGenerator;

import java.util.ArrayList;
import java.util.Objects;

import rs.elfak.mosis.nikolamitic.bottomnavigationview.Class.BitmapManipulation;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Class.Parking;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Class.User;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Friends.FriendsFragment;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Home.HomeFragment;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Login.LoginActivity;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Settings.SettingsFragment;

public class MainActivity extends Activity {
    private BottomNavigationView navigation;
    private int clicked = 1, newClicked = 0;
    private HomeFragment homeFragment;
    private FriendsFragment friendsFragment;
    private SettingsFragment settingsFragment;
    private Fragment newFragment;

    private FirebaseAuth.AuthStateListener authListener;
    private FirebaseAuth mAuth;
    static public FirebaseUser loggedUser;
    private DatabaseReference parkings, users;

    private Intent backgroundService;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = item -> {
                switch (item.getItemId()) {
                    case R.id.navigation_home:
                        newClicked = 1;
                        break;
                    case R.id.navigation_friends:
                        newClicked = 2;
                        break;
                    case R.id.navigation_settings:
                        newClicked = 3;
                        break;
                }

                if (0 < newClicked && newClicked < 4 && clicked != newClicked) {
                    changeFragment(newClicked, true);
                    changeFragment(clicked, false);
                    clicked = newClicked;
                    return true;
                }

                return false;
            };

    void addFragments() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();

        homeFragment = new HomeFragment();
        ft.add(R.id.fragmentContainer, homeFragment);
        ft.hide(homeFragment);

        friendsFragment = new FriendsFragment();
        ft.add(R.id.fragmentContainer, friendsFragment);
        ft.hide(friendsFragment);

        settingsFragment = new SettingsFragment();
        ft.add(R.id.fragmentContainer, settingsFragment);
        ft.hide(settingsFragment);

        ft.commit();
    }

    void changeFragment(int position, boolean show) {
        switch (position) {
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

        if (show)
            ft.show(newFragment);
        else
            ft.hide(newFragment);

        ft.commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        mAuth = FirebaseAuth.getInstance();

        authListener = firebaseAuth -> {
            loggedUser = firebaseAuth.getCurrentUser();
            if (loggedUser == null) {
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                finish();
            }
        };

        loggedUser = mAuth.getCurrentUser();

        if (loggedUser != null) {
            FirebaseDatabase database = FirebaseDatabase.getInstance();
            parkings = database.getReference("parkings");
            users = database.getReference("users");

            //remove title bar
            requestWindowFeature(Window.FEATURE_NO_TITLE);

            setContentView(R.layout.activity_main);

            addFragments();
            changeFragment(1, true);

            navigation = findViewById(R.id.navigation);
            navigation.setItemIconTintList(null);

            navigation.setSelectedItemId(R.id.navigation_home);

            backgroundService = new Intent(this, MyLocationService.class);
            navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        } else {
            startActivity(new Intent(MainActivity.this, LoginActivity.class));
            finish();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(authListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (authListener != null) {
            mAuth.removeAuthStateListener(authListener);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!isMyServiceRunning(MyLocationService.class)) {
            startService(backgroundService);
            //customToast(this,"Starting background service",Toast.LENGTH_SHORT);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!settingsFragment.workback_status && isMyServiceRunning(MyLocationService.class)) {
            stopService(backgroundService);
            //customToast(this,"Stopping background service",Toast.LENGTH_SHORT);
        }
    }

    public boolean isMyServiceRunning(Class<?> serviceClass) {
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
        if (backgroundService != null)
            stopService(backgroundService);
    }


    public void loadParkingsFromServer() {
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                final Parking parking = dataSnapshot.getValue(Parking.class);
                assert parking != null;
                Marker marker = addMarkers(parking.getLatitude(), parking.getLongitude(),
                        parking.getName(), parking.getDescription(), "", false, false,
                        parking.isSecret(), parking.getAdderId());

                if (marker != null) {
                    marker.setTag(parking.isSecret() ? "private" : "public");
                    HomeFragment.mapParkingIdMarker.put(parking.getPid(), marker);
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                //We don't have a ability to change a parking
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        parkings.addChildEventListener(childEventListener);
    }

    private Marker addMarkers(double lat, double lng, String title, String snippet, String uId, boolean friends_status, boolean player_status, boolean secret, String adderId) {
        ArrayList<String> friendsList = friendsFragment.getFriendsList();

        Marker marker = null;

        MarkerOptions mo = new MarkerOptions();
        mo.position(new LatLng(lat, lng));
        mo.title(title);
        mo.anchor(0.5f, 0.9f);

        if (snippet != null && !snippet.equals("")) {
            mo.snippet(snippet);
        }

        if (uId == null || uId.equals("")) {
            if (secret) {
                if (adderId.equals(loggedUser.getUid()) || friendsList.contains(adderId)) {
                    mo.icon(BitmapDescriptorFactory.fromBitmap(BitmapManipulation.getMarkerBitmapFromView(R.mipmap.free, MainActivity.this)));
                    marker = homeFragment.googleMap.addMarker(mo);
                }
            } else {
                mo.icon(BitmapDescriptorFactory.fromBitmap(BitmapManipulation.getMarkerBitmapFromView(R.mipmap.occupied, MainActivity.this)));
                marker = homeFragment.googleMap.addMarker(mo);
            }
        } else {
            if (uId.equals(loggedUser.getUid())) {
                mo.icon(BitmapDescriptorFactory.fromBitmap(BitmapManipulation.getMarkerBitmapFromView(R.mipmap.me, MainActivity.this)));
                CameraPosition mCameraPosition = new CameraPosition.Builder().target(new LatLng(lat, lng)).zoom(16).build();
                homeFragment.googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));

                marker = homeFragment.googleMap.addMarker(mo);
                HomeFragment.mapFriendIdMarker.put(uId, marker);
            } else {
                if (friendsList.contains(uId)) {
                    mo.icon(BitmapDescriptorFactory.fromBitmap(BitmapManipulation.getMarkerBitmapFromView(R.mipmap.friend, MainActivity.this)));
                    mo.visible(friends_status);
                    mo.alpha(0.9f);

                    marker = homeFragment.googleMap.addMarker(mo);
                    HomeFragment.mapFriendIdMarker.put(uId, marker);
                } else {
                    mo.icon(BitmapDescriptorFactory.fromBitmap(BitmapManipulation.getMarkerBitmapFromView(R.mipmap.user, MainActivity.this)));
                    mo.visible(player_status);
                    mo.alpha(0.8f);

                    marker = homeFragment.googleMap.addMarker(mo);
                    HomeFragment.mapUserIdMarker.put(uId, marker);
                }
            }
        }

        return marker;
    }

    public void changeVisibility(boolean playersOptionChanged, boolean friendsOptionChanged) {
        if (playersOptionChanged) {
            for (Marker mMarker : HomeFragment.mapUserIdMarker.values()) {
                mMarker.setVisible(!mMarker.isVisible());
            }
        }

        if (friendsOptionChanged) {
            for (Marker mMarker : HomeFragment.mapFriendIdMarker.values()) {
                mMarker.setVisible(!mMarker.isVisible());
            }

            Objects.requireNonNull(HomeFragment.mapFriendIdMarker.get(loggedUser.getUid())).setVisible(true);
        }
    }

    public void loadUsersFromServer(final Boolean players_status, final Boolean friends_status) {
        ChildEventListener childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
                final User user = dataSnapshot.getValue(User.class);
                String uid = dataSnapshot.getKey();
                assert user != null;
                Marker marker = addMarkers(user.getLatitude(), user.getLongitude(),
                        user.getFirstName() + " " + user.getLastName(), user.getNickname(), uid, friends_status, players_status, false, "");
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
                User user = dataSnapshot.getValue(User.class);
                String uid = dataSnapshot.getKey();

                Marker mMarker = HomeFragment.mapUserIdMarker.get(uid);

                if (mMarker == null) {
                    mMarker = HomeFragment.mapFriendIdMarker.get(uid);
                }
                assert user != null;
                assert mMarker != null;
                mMarker.setPosition(new LatLng(user.getLatitude(), user.getLongitude()));
//                if(uid == loggedUser.getUid())
//                {
//                    CameraPosition mCameraPosition = new CameraPosition.Builder().target(new LatLng(user.getLatitude(), user.getLongitude())).build();
//                    HomeFragment.googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
//                }

//                myLocationService.showFriendsInRadius(mMarker);
            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
        users.addChildEventListener(childEventListener);
    }

    public HomeFragment getHomeFragment() {
        return homeFragment;
    }

    public FriendsFragment getFriendsFragment() {
        return friendsFragment;
    }

    public SettingsFragment getSettingsFragment() {
        return settingsFragment;
    }

    public void performHomeClick() {
        navigation.setSelectedItemId(R.id.navigation_home);
    }

    public static void customToast(Context context, String message, int duration) {
        Toast toast = Toast.makeText(context, message, duration);
        View view = toast.getView();

        //Gets the actual oval background of the Toast then sets the colour filter
        view.getBackground().setColorFilter(context.getResources().getColor(R.color.dark), PorterDuff.Mode.SRC_IN);

        //Gets the TextView from the Toast so it can be editted
        TextView text = view.findViewById(android.R.id.message);
        text.setTextColor(context.getResources().getColor(R.color.white));

        toast.show();
    }
}