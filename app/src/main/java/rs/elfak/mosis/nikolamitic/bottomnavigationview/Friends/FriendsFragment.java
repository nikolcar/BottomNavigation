package rs.elfak.mosis.nikolamitic.bottomnavigationview.Friends;

import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;

import rs.elfak.mosis.nikolamitic.bottomnavigationview.Friends.FriendListAdapter;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Friends.FriendModel;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.R;

public class FriendsFragment extends Fragment
{
    private ListView lvHighscore;
    private FriendListAdapter mAdapter;
    private ArrayList<FriendModel> mFriends;
    FloatingActionButton btnAddFriend;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.friends_fragment, container, false);

        lvHighscore = (ListView) view.findViewById(R.id.highscore_list);
        
        mFriends = new ArrayList<>();

        mAdapter = new FriendListAdapter(getActivity().getApplicationContext(), mFriends);
        lvHighscore.setAdapter(mAdapter);

        //TODO get friends
        Bitmap bm1 = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_blue_round);
        Bitmap bm2 = BitmapFactory.decodeResource(getResources(), R.mipmap.icon_white_round);

        mFriends.add(new FriendModel("Pera Perić", 5, null));
        mFriends.add(new FriendModel("Mika Mikić", 15, bm2));
        mFriends.add(new FriendModel("Pera Perić", 5, bm1));
        mFriends.add(new FriendModel("Mika Mikić", 15, null));
        mFriends.add(new FriendModel("Pera Perić", 5, bm1));
        mFriends.add(new FriendModel("Mika Mikić", 15, bm2));
        mFriends.add(new FriendModel("Pera Perić", 5, bm1));
        mFriends.add(new FriendModel("Mika Mikić", 15, bm1));
        mFriends.add(new FriendModel("Pera Perić", 5, null));
        mFriends.add(new FriendModel("Mika Mikić", 15, bm2));
        mFriends.add(new FriendModel("Pera Perić", 5, bm1));
        mFriends.add(new FriendModel("Mika Mikić", 15, null));
        mFriends.add(new FriendModel("Pera Perić", 5, bm1));
        mFriends.add(new FriendModel("Mika Mikić", 15, bm2));
        mFriends.add(new FriendModel("Pera Perić", 5, bm1));
        mFriends.add(new FriendModel("Mika Mikić", 15, bm1));

        return view;
    }
}
