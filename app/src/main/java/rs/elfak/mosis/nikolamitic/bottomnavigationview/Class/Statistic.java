package rs.elfak.mosis.nikolamitic.bottomnavigationview.Class;

public class Statistic
{
    private String userId;
    private String parkingId;
    private String dateTime;

    public Statistic()
    {
    }

    public Statistic(String userId, String parkingId, String dateTime)
    {
        this.userId = userId;
        this.parkingId = parkingId;
        this.dateTime = dateTime;
    }

    public String getUserId()
    {
        return userId;
    }

    public void setUserId(String userId)
    {
        this.userId = userId;
    }

    public String getParkingId()
    {
        return parkingId;
    }

    public void setParkingId(String parkingId)
    {
        this.parkingId = parkingId;
    }

    public String getDateTime()
    {
        return dateTime;
    }

    public void setDateTime(String dateTime)
    {
        this.dateTime = dateTime;
    }
}