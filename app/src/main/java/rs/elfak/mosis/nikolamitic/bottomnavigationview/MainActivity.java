package rs.elfak.mosis.nikolamitic.bottomnavigationview;

import android.app.Activity;
import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.MenuItem;
import android.view.Window;

import rs.elfak.mosis.nikolamitic.bottomnavigationview.Friends.FriendsFragment;

public class MainActivity extends Activity {

    private static final String TAG = "Locate Parking";

    //FragmentManager fragmentManager = getFragmentManager();
    //Fragment newFragment = null;
    //FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();


    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            boolean result = false;
            switch (item.getItemId()) {
                case R.id.navigation_home:
                    Log.d(TAG, "navigation_home");
                    changeFragment("home");
                    result = true;
                    break;
                case R.id.navigation_friends:
                    Log.d(TAG, "navigation_friends");
                    changeFragment("friends");
                    result = true;
                    break;
                case R.id.navigation_settings:
                    Log.d(TAG, "navigation_settings");
                    changeFragment("settings");
                    result = true;
                    break;
            }
            return result;
        }

    };

    void changeFragment(String name)
    {
        Fragment newFragment = null;

        switch (name)
        {
            case "home":
                newFragment = new HomeFragment();
                break;
            case "friends":
                newFragment = new FriendsFragment();
                break;
            case "settings":
                newFragment = new FragmentSettings();
                break;
        }

        getFragmentManager().beginTransaction().replace(R.id.fragmentContainer, newFragment).commit();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //remove title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        //select home on start
        navigation.setSelectedItemId(R.id.navigation_home);
        changeFragment("home");
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }



}
