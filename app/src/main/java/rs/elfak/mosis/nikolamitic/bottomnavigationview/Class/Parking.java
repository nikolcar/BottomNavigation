package rs.elfak.mosis.nikolamitic.bottomnavigationview.Class;

import com.google.android.gms.maps.model.LatLng;

import java.util.Date;

public class Parking
{
    private String name;
    private Double longitude;
    private Double latitude;

    private String description;
    private String adderId;
    private boolean secret;
    //private Date date;
    public String pid;

    public Parking()
    {
    }

    public Parking(String name, String description, Double lon, Double lat, String uid, boolean secret)
    {
        this.name = name;
        this.description = description;

        this.longitude = lon;
        this.latitude = lat;

        this.adderId = uid;
        this.secret = secret;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
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

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getAdderId()
    {
        return adderId;
    }

    public void setAdderId(String adderId)
    {
        this.adderId = adderId;
    }

    public boolean isSecret()
    {
        return secret;
    }

    public void setSecret(boolean secret)
    {
        this.secret = secret;
    }

    public String getPid()
    {
        return pid;
    }

    public void setPid(String pid)
    {
        this.pid = pid;
    }
}