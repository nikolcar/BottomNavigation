package rs.elfak.mosis.nikolamitic.bottomnavigationview.Friends;

import android.graphics.Bitmap;

class FriendModel
{
    private String name;
    private int points;
    private Bitmap avatar;
    private String uId;

    FriendModel(String name, int points, Bitmap avatar, String uId)
    {
        this.name = name;
        this.points = points;
        this.avatar = avatar;
        this.uId = uId;
    }

    String getName()
    {
        return name;
    }

    void setName(String name)
    {
        this.name = name;
    }

    int getPoints()
    {
        return points;
    }

    void setPoints(int points)
    {
        this.points = points;
    }

    Bitmap getAvatar()
    {
        return avatar;
    }

    void setAvatar (Bitmap avatar)
    {
        this.avatar = avatar;
    }

    String getuId()
    {
        return uId;
    }

    void setuId(String uId)
    {
        this.uId = uId;
    }
}
