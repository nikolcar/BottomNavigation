package rs.elfak.mosis.nikolamitic.bottomnavigationview.Class;

import android.util.ArraySet;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Date;

public class User
{
    //public String uid;
    private String firstName;
    private String lastName;
    private String nickname;
    private String dateOfBirth;

    private Double longitude;
    private Double latitude;

    //private LatLng location;
    private Integer points;
    private ArrayList<String> friends;
    //private ArrayList<String> myPrivate;
    //private ArrayList<String> myFriendsPrivate;

    private Integer gpsrefresh;
    private Boolean showfriends;
    private Boolean showplayers;
    private Boolean workback;

    public User()
    {
    }

    public User(String firstName, String lastName, String nickname, String dateOfBirth, Double longitude, Double latitude)
    {
        this.firstName = firstName;
        this.lastName = lastName;
        this.nickname = nickname;
        this.dateOfBirth = dateOfBirth;

        this.longitude = longitude;
        this.latitude = latitude;
        this.points = 0;

        this.friends = new ArrayList<>();
        friends.add("");

        this.gpsrefresh = 10;
        this.showfriends = true;
        this.showplayers = true;
        this.workback = true;
    }

    public String getFirstName()
    {
        return firstName;
    }

    public void setFirstName(String firstName)
    {
        this.firstName = firstName;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    public String getNickname()
    {
        return nickname;
    }

    public void setNickname(String nickname)
    {
        this.nickname = nickname;
    }

    public String getDateOfBirth()
    {
        return dateOfBirth;
    }

    public void setDateOfBirth(String dateOfBirth)
    {
        this.dateOfBirth = dateOfBirth;
    }

    public Double getLongitude()
    {
        return longitude;
    }

    public void setLongitude(Double longitude)
    {
        this.longitude = longitude;
    }

    public Double getLatitude()
    {
        return latitude;
    }

    public void setLatitude(Double latitude)
    {
        this.latitude = latitude;
    }

    public Integer getPoints()
    {
        return this.points;
    }

    public void setPoints(Integer points)
    {
        this.points = points;
    }

    public ArrayList<String> getFriends()
    {
        return friends;
    }

    public void setFriends(ArrayList<String> friends)
    {
        this.friends = friends;
    }

    public Integer getGpsrefresh()
    {
        return gpsrefresh;
    }

    public void setGpsrefresh(Integer gpsrefresh)
    {
        this.gpsrefresh = gpsrefresh;
    }

    public Boolean getShowfriends()
    {
        return showfriends;
    }

    public void setShowfriends(Boolean showfriends)
    {
        this.showfriends = showfriends;
    }

    public Boolean getShowplayers()
    {
        return showplayers;
    }

    public void setShowplayers(Boolean showplayers)
    {
        this.showplayers = showplayers;
    }

    public Boolean getWorkback()
    {
        return workback;
    }

    public void setWorkback(Boolean workback)
    {
        this.workback = workback;
    }
}
