package rs.elfak.mosis.nikolamitic.bottomnavigationview.Strategy;

import com.google.android.gms.maps.model.Marker;
import java.util.HashMap;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Class.Parking;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Home.HomeFragment;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Strategy.SearchStrategy;

public class TypeSearchStrategy implements SearchStrategy
{
    @Override
    public void search(String query, HashMap<String, Marker> mapSearchIdMarker)
    {
        for (Marker marker: mapSearchIdMarker.values())
        {
            String secret = HomeFragment.getKeyFromValue(mapSearchIdMarker,marker);
            marker.setVisible(secret.startsWith(query.toLowerCase()));
        }
    }
}
