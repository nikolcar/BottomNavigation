package rs.elfak.mosis.nikolamitic.bottomnavigationview.Friends;

import android.graphics.Bitmap;

public class FriendModel
{
    private String name;
    private int points;
    private Bitmap avatar;

    public FriendModel(String name, int points, Bitmap avatar)
    {
        this.name = name;
        this.points = points;
        this.avatar = avatar;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public int getPoints()
    {
        return points;
    }

    public void setPoints(int points)
    {
        this.points = points;
    }

    public Bitmap getAvatar()
    {
        return avatar;
    }

    public void setAvatar (Bitmap avatar)
    {
        this.avatar = avatar;
    }
}


