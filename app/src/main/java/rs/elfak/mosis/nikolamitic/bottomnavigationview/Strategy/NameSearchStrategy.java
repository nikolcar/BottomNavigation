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
    public void search(String query, HashMap<String, Marker> mapSearchIdMarker) {
        for (Marker marker: mapSearchIdMarker.values())
        {
            if(marker.getTitle().toLowerCase().contains(query.toLowerCase()))
            {
                marker.setVisible(true);
                marker.showInfoWindow();

                CameraPosition mCameraPosition = new CameraPosition.Builder().target(marker.getPosition()).zoom(16).build();
                mainActivity.getHomeFragment().googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
            }
            else marker.setVisible(false);
        }
    }

    public NameSearchStrategy(MainActivity mainActivity)
    {
        this.mainActivity = mainActivity;
    }
}
