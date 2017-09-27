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

        Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.avatar);

        mFriends = new ArrayList<>();

        mAdapter = new FriendListAdapter(getActivity().getApplicationContext(), mFriends);
        lvHighscore.setAdapter(mAdapter);

        mFriends.add(new FriendModel("Pera Perić", 5, bm));
        mFriends.add(new FriendModel("Mika Mikić", 15, bm));
        mFriends.add(new FriendModel("Pera Perić", 5, bm));
        mFriends.add(new FriendModel("Mika Mikić", 15, bm));
        mFriends.add(new FriendModel("Pera Perić", 5, bm));
        mFriends.add(new FriendModel("Mika Mikić", 15, bm));
        mFriends.add(new FriendModel("Pera Perić", 5, bm));
        mFriends.add(new FriendModel("Mika Mikić", 15, bm));

        return view;
    }
}
