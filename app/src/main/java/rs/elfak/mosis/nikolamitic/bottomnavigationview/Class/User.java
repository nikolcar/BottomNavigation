package rs.elfak.mosis.nikolamitic.bottomnavigationview.Class;


import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;

public class User
{
    //public String uid;
    public String firstName;
    public String lastName;
    public String nickname;
    public String dateOfBirth;

    public LatLng location;
    public Integer points;
    public ArrayList<String> friends;
    public ArrayList<String> myPrivate;
    public ArrayList<String> myFriendsPrivate;

    public Integer gpsrefresh;
    public Boolean showfriends;
    public Boolean showplayers;
    public Boolean workback;

    public User(String firstName, String lastName, String nickname, String dateOfBirth)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.nickname = nickname;
        this.dateOfBirth=dateOfBirth;

        this.location = new LatLng(0.0,0.0);
        this.points = 0;
        this.friends = new ArrayList<String>();
        this.myPrivate = new ArrayList<String>();
        this.myFriendsPrivate = new ArrayList<String>();

        this.gpsrefresh = 10;
        this.showfriends = true;
        this.showplayers = true;
        this.workback = true;
    }

    public LatLng getLocation()
    {
        return this.location;
    }

    public Integer getPoints()
    {
        return this.points;
    }

    public String toString()
    {
        return this.firstName+", "+this.nickname+", "+this.lastName;
    }

    public void addPrivateParking (String pid)
    {
        myPrivate.add(pid);
    }
}
