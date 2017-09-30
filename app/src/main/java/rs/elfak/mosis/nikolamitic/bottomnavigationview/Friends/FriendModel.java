package rs.elfak.mosis.nikolamitic.bottomnavigationview.Friends;

import android.graphics.Bitmap;

public class FriendModel
{
    private String name;
    private int points;
    private Bitmap avatar;
    private String uId;

    public FriendModel(String name, int points, Bitmap avatar, String uId)
    {
        this.name = name;
        this.points = points;
        this.avatar = avatar;
        this.uId = uId;
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

    public String getuId() {
        return uId;
    }
}


