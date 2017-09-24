package rs.elfak.mosis.nikolamitic.bottomnavigationview;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;

import rs.elfak.mosis.nikolamitic.bottomnavigationview.Friends.FriendsFragment;

public class MainActivity extends Activity {

    private static final String TAG = "Locate Parking";

    int clicked = 0, newClicked =0;
    HomeFragment homeFragment = new HomeFragment();
    FriendsFragment friendsFragment = new FriendsFragment();
    SettingsFragment settingsFragment = new SettingsFragment();
    Fragment newFragment = null;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
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
                changeFragment(newClicked);
                clicked = newClicked;
                return true;
            }

            return false;
        }

    };

    void changeFragment(int position)
    {
        switch (position)
        {
            case 1:
                newFragment = getHomeFragment();
                break;
            case 2:
                newFragment = getFriendsFragment();
                break;
            case 3:
                newFragment = getSettingsFragment();
                break;
        }

        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.replace(R.id.fragmentContainer, newFragment);
        ft.commit();
    }

    public HomeFragment getHomeFragment()
    {
        return homeFragment;
    }

    public FriendsFragment getFriendsFragment()
    {
        return friendsFragment;
    }

    public SettingsFragment getSettingsFragment()
    {
        return settingsFragment;
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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //remove title bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        //select home on start
        navigation.setSelectedItemId(R.id.navigation_home);
        changeFragment(1);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }



}
