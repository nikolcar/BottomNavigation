package rs.elfak.mosis.nikolamitic.bottomnavigationview.Class;


import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class User
{
    public String email;
    public String firstName;
    public String lastName;
    public LatLng location;
    public Integer points;
    public ArrayList<User> friends;
    public ArrayList<Parking> myPrivate;
    public ArrayList<Parking> myFriendsPrivate;

    public Integer gpsrefresh;
    public Boolean showfriends;
    public Boolean showplayers;
    public Boolean workback;

    public User(String firstName, String lastName, String email)
    {
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;

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

    public void addPoints(int bonus)
    {
        this.points+=bonus;
    }

    public String toString()
    {
        return this.firstName+" "+this.lastName;
    }
}
