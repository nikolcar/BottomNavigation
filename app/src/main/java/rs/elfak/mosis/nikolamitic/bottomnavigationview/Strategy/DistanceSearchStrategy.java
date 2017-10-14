package rs.elfak.mosis.nikolamitic.bottomnavigationview.Strategy;

import android.widget.Toast;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import java.util.HashMap;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Class.Parking;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.MyLocationService;

import static rs.elfak.mosis.nikolamitic.bottomnavigationview.MainActivity.homeFragment;
import static rs.elfak.mosis.nikolamitic.bottomnavigationview.MyLocationService.distanceBetween;
import static rs.elfak.mosis.nikolamitic.bottomnavigationview.MyLocationService.latitude;
import static rs.elfak.mosis.nikolamitic.bottomnavigationview.MyLocationService.longitude;

public class DistanceSearchStrategy implements SearchStrategy
{
    @Override
    public void search(String query, HashMap<Parking, Marker> mapParkingsMarkers)
    {
        Marker mMarker = null;
        double mLatitude;
        double mLongitude;
        float q_distance;
        try
        {
            q_distance = Float.parseFloat(query);
        }
        catch (Exception e)
        {
            Toast.makeText(homeFragment.getActivity(),"Please enter the float number!",Toast.LENGTH_SHORT).show();
            return;
        }

        if(latitude!=null && longitude!=null)
        {
            mLatitude = latitude;
            mLongitude = longitude;
        }
        else
        {
            Toast.makeText(homeFragment.getActivity(),"Please turn on GPS!",Toast.LENGTH_SHORT).show();
            return;
        }

        homeFragment.setCircle(new LatLng(mLatitude, mLongitude), q_distance);

        for (Parking parking : mapParkingsMarkers.keySet())
        {
            mMarker = mapParkingsMarkers.get(parking);
            float distance = distanceBetween((float) mLatitude, (float) mLongitude, (float) mMarker.getPosition().latitude, (float) mMarker.getPosition().longitude);
            mMarker.setVisible(distance <= q_distance);
        }
    }
}
