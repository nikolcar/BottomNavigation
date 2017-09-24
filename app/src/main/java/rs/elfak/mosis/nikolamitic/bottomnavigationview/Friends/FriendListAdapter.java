package rs.elfak.mosis.nikolamitic.bottomnavigationview.Friends;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import rs.elfak.mosis.nikolamitic.bottomnavigationview.R;

public class FriendListAdapter extends ArrayAdapter<FriendModel>
{
    private Context mContext;
    private ArrayList<FriendModel> mFriendList;

    // View lookup cache
    private static class ViewHolder
    {
        TextView tvName;
        TextView tvPoints;
        ImageView imAvatar;
    }

    public FriendListAdapter(Context mContext, ArrayList<FriendModel> mFriendList)
    {
        super(mContext, R.layout.item_friend_list, mFriendList);
        this.mContext = mContext;
        this.mFriendList = mFriendList;
    }


    private int lastPosition = -1;

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        // Get the data item for this position
        FriendModel friendModel = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        ViewHolder viewHolder; // view lookup cache stored in tag

        if (convertView == null)
        {
            viewHolder = new ViewHolder();
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.item_friend_list, parent, false);
            viewHolder.tvName = (TextView) convertView.findViewById(R.id.item_friend_name);
            viewHolder.tvPoints = (TextView) convertView.findViewById(R.id.item_friend_points);
            viewHolder.imAvatar = (ImageView) convertView.findViewById(R.id.item_friend_avatar);

            convertView.setTag(viewHolder);
        }
        else
        {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        lastPosition = position;

        viewHolder.tvName.setText(friendModel.getName());
        viewHolder.tvPoints.setText(String.valueOf(friendModel.getPoints()));
        viewHolder.imAvatar.setImageBitmap(friendModel.getAvatar());

        return convertView;
    }

}
