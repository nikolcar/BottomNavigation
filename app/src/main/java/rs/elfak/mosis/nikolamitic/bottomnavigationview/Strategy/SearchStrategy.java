package rs.elfak.mosis.nikolamitic.bottomnavigationview.Strategy;

import com.google.android.gms.maps.model.Marker;
import java.util.HashMap;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Class.Parking;


public interface SearchStrategy
{
    public void search(String query, HashMap<Parking, Marker> mapMarkersParkings);
}
