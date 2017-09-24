package rs.elfak.mosis.nikolamitic.bottomnavigationview.Class;

import rs.elfak.mosis.nikolamitic.bottomnavigationview.Class.Location;

public class Parking
{
    public String name;
    public Location location;
    public String adderId;
    public boolean secret;
    public String pid;

    public Parking(String name, Double lon, Double lat, String uid, boolean secret, String pid)
    {
        this.name = name;
        this.location.setLocation(lon,lat);
        this.adderId = uid;
        this.secret = secret;
        this.pid = pid;
    }
}
