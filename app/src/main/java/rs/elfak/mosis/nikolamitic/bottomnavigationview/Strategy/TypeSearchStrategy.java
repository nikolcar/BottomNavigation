package rs.elfak.mosis.nikolamitic.bottomnavigationview.Strategy;

import com.google.android.gms.maps.model.Marker;
import java.util.HashMap;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Class.Parking;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Strategy.SearchStrategy;

public class TypeSearchStrategy implements SearchStrategy
{
    @Override
    public void search(String query, HashMap<Parking, Marker> mapParkingsMarkers)
    {
        Marker mMarker = null;
        for (Parking parking: mapParkingsMarkers.keySet())
        {
            mMarker = mapParkingsMarkers.get(parking);
            if(parking.isSecret())
            {
                mMarker.setVisible((new String("private")).startsWith(query.toLowerCase()));
            }
            else
            {
                mMarker.setVisible((new String("public")).startsWith(query.toLowerCase()));
            }
        }
    }
}
