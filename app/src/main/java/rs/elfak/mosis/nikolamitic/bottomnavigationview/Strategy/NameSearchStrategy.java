package rs.elfak.mosis.nikolamitic.bottomnavigationview.Strategy;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Marker;
import java.util.HashMap;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Class.Parking;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.MainActivity;

public class NameSearchStrategy implements SearchStrategy
{
    MainActivity mainActivity;

    @Override
    public void search(String query, HashMap<Parking, Marker> mapParkingsMarkers) {
        for (Parking parking: mapParkingsMarkers.keySet())
        {
            Marker mMarker = null;
            mMarker = mapParkingsMarkers.get(parking);
            if(parking.getName().toLowerCase().contains(query.toLowerCase()))
            {
                mMarker.setVisible(true);
                mMarker.showInfoWindow();

                CameraPosition mCameraPosition = new CameraPosition.Builder().target(mMarker.getPosition()).zoom(16).build();
                mainActivity.getHomeFragment().googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
            }
            else mMarker.setVisible(false);
        }
    }

    public NameSearchStrategy(MainActivity mainActivity)
    {
        this.mainActivity = mainActivity;
    }
}
