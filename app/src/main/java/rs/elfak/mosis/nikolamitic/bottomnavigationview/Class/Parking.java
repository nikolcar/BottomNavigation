package rs.elfak.mosis.nikolamitic.bottomnavigationview.Class;

import com.google.android.gms.maps.model.LatLng;

public class Parking
{
    public String name;
    public LatLng location;
    public String description;
    public String adderId;
    public boolean secret;
    public String pid;

    public Parking(String name, Double lon, Double lat, String uid, boolean secret, String pid)
    {
        this.name = name;
        this.location = new LatLng(lon,lat);
        this.adderId = uid;
        this.secret = secret;
        this.pid = pid;
    }
}
