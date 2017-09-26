package rs.elfak.mosis.nikolamitic.bottomnavigationview.Class;


import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;

public class User
{
    public String uid;
    public String firstName;
    public String lastName;
    public String nickname;
    public Date dateOfBirth;

    public LatLng location;
    public Integer points;
    public ArrayList<User> friends;
    public ArrayList<Parking> myPrivate;
    public ArrayList<Parking> myFriendsPrivate;

    public Integer gpsrefresh;
    public Boolean showfriends;
    public Boolean showplayers;
    public Boolean workback;

    public User(String uid, String firstName, String lastName, String nickname, Date dateOfBirth)
    {
        this.uid = uid;
        this.firstName = firstName;
        this.lastName = lastName;
        this.nickname = nickname;
        this.dateOfBirth=dateOfBirth;

        this.location = new LatLng(0.0,0.0);
        this.points = 0;
        this.friends = new ArrayList<>();
        this.myPrivate = new ArrayList<>();
        this.myFriendsPrivate = new ArrayList<>();

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
}
