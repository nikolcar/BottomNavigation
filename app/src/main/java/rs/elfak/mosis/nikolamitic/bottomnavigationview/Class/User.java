package rs.elfak.mosis.nikolamitic.bottomnavigationview.Class;


import android.util.ArraySet;

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

    public User()
    {
        this.friends = new ArrayList<String>();
        this.friends.add("");
        this.myPrivate = new ArrayList<String>();
        this.myPrivate.add("");
        this.myFriendsPrivate = new ArrayList<String>();
        this.myFriendsPrivate.add("");
    }

    public User(String firstName, String lastName, String nickname, String dateOfBirth)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.nickname = nickname;
        this.dateOfBirth=dateOfBirth;

        this.location = new LatLng(0.0,0.0);
        this.points = 0;
        this.friends = new ArrayList<String>();
        this.friends.add("");
        this.myPrivate = new ArrayList<String>();
        this.myPrivate.add("");
        this.myFriendsPrivate = new ArrayList<String>();
        this.myFriendsPrivate.add("");

        this.gpsrefresh = 10;
        this.showfriends = true;
        this.showplayers = true;
        this.workback = true;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public String getNickname()
    {
        return nickname;
    }

    public String getDateOfBirth()
    {
        return dateOfBirth;
    }

    public ArrayList<String> getFriends()
    {
        return friends;
    }

    public ArrayList<String> getMyPrivate()
    {
        return myPrivate;
    }

    public ArrayList<String> getMyFriendsPrivate()
    {
        return myFriendsPrivate;
    }

    public Integer getGpsrefresh()
    {
        return gpsrefresh;
    }

    public Boolean getShowfriends()
    {
        return showfriends;
    }

    public Boolean getShowplayers()
    {
        return showplayers;
    }

    public Boolean getWorkback()
    {
        return workback;
    }

    public LatLng getLocation()
    {
        return this.location;
    }

    public Integer getPoints()
    {
        return this.points;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setDateOfBirth(String dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public void setPoints(Integer points) {
        this.points = points;
    }

    public void setFriends(ArrayList<String> friends) {
        this.friends = friends;
    }

    public void setMyPrivate(ArrayList<String> myPrivate) {
        this.myPrivate = myPrivate;
    }

    public void setMyFriendsPrivate(ArrayList<String> myFriendsPrivate) {
        this.myFriendsPrivate = myFriendsPrivate;
    }

    public void setGpsrefresh(Integer gpsrefresh) {
        this.gpsrefresh = gpsrefresh;
    }

    public void setShowfriends(Boolean showfriends) {
        this.showfriends = showfriends;
    }

    public void setShowplayers(Boolean showplayers) {
        this.showplayers = showplayers;
    }

    public void setWorkback(Boolean workback) {
        this.workback = workback;
    }
}
