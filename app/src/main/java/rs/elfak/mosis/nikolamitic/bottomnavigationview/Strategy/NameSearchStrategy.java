package rs.elfak.mosis.nikolamitic.bottomnavigationview.Strategy;

import com.google.android.gms.maps.model.Marker;
import java.util.HashMap;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Class.Parking;

public class NameSearchStrategy implements SearchStrategy
{
    @Override
    public void search(String query, HashMap<Parking, Marker> mapMarkersParkings) {
        for (Parking parking: mapMarkersParkings.keySet())
        {
            Marker mMarker = null;

            mMarker = mapMarkersParkings.get(parking);
            mMarker.setVisible(parking.getName().toLowerCase().startsWith(query.toLowerCase()));
            mMarker.showInfoWindow();
        }
    }
}
