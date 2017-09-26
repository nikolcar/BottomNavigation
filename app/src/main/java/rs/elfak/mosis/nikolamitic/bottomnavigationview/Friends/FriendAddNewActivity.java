package rs.elfak.mosis.nikolamitic.bottomnavigationview.Friends;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Window;

import rs.elfak.mosis.nikolamitic.bottomnavigationview.R;

public class FriendAddNewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_friend_add_new);
    }
}
