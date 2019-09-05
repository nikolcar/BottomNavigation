package rs.elfak.mosis.nikolamitic.bottomnavigationview.Strategy;

import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.Objects;

import rs.elfak.mosis.nikolamitic.bottomnavigationview.Class.Parking;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Home.HomeFragment;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Strategy.SearchStrategy;

public class TypeSearchStrategy implements SearchStrategy {
    @Override
    public void search(String query, HashMap<String, Marker> mapParkingIdMarker) {
        for (Marker marker : mapParkingIdMarker.values()) {
            marker.setVisible(Objects.requireNonNull(marker.getTag()).toString().startsWith(query.toLowerCase()));
        }
    }
}
