package rs.elfak.mosis.nikolamitic.bottomnavigationview.Home;

import android.Manifest;
import android.app.Dialog;
import android.app.Fragment;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.os.Bundle;
import com.getbase.floatingactionbutton.FloatingActionButton;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.maps.DirectionsApi;
import com.google.maps.GeoApiContext;
import com.google.maps.errors.ApiException;
import com.google.maps.model.DirectionsResult;
import com.google.maps.model.TravelMode;
import com.google.maps.android.PolyUtil;

import org.joda.time.DateTime;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import rs.elfak.mosis.nikolamitic.bottomnavigationview.Class.Parking;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Class.Statistic;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.MainActivity;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.MyLocationService;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Strategy.DistanceSearchStrategy;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Strategy.NameSearchStrategy;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.R;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Strategy.SearchStrategy;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Strategy.TypeSearchStrategy;

import static android.content.Context.LAYOUT_INFLATER_SERVICE;
import static rs.elfak.mosis.nikolamitic.bottomnavigationview.MainActivity.customToast;
import static rs.elfak.mosis.nikolamitic.bottomnavigationview.MyLocationService.latitude;
import static rs.elfak.mosis.nikolamitic.bottomnavigationview.MyLocationService.longitude;

public class HomeFragment extends Fragment {
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private static final int ADD_POINTS_NEW_PARKING = 5;

    public GoogleMap googleMap;
    private MapView mMapView;
    public static HashMap<String, Marker> mapParkingIdMarker = new HashMap<>();
    public static HashMap<String, Marker> mapUserIdMarker = new HashMap<>();
    public static HashMap<String, Marker> mapFriendIdMarker = new HashMap<>();

    private Circle distanceCircle;
    private SearchStrategy searchStrategy = null;

    private Dialog dialog;
    private Polyline direction;

    private SearchView search;
    private Spinner sSearchType;
    private com.google.android.material.floatingactionbutton.FloatingActionButton btnCancelDirections;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);

        final Rect displayRectangle = new Rect();
        Window window = getActivity().getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        final LayoutInflater layoutInflater = (LayoutInflater) getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);

        FloatingActionButton btnAddNewParking = view.findViewById(R.id.btn_add_new_parking);
        btnCancelDirections = view.findViewById(R.id.btn_cancel_direction_mode);
        sSearchType = view.findViewById(R.id.spinnerMapSearchCategory);
        search = view.findViewById(R.id.searchMap);

        btnAddNewParking.setOnClickListener(v -> {
            dialog = new Dialog(getActivity(), R.style.dialog_no_tytle);

            View layout = layoutInflater.inflate(R.layout.dialog_add_parking, null);
            layout.setMinimumWidth((int) (displayRectangle.width() * 0.8f));

            dialog.setContentView(layout);

            TextView tvTitle = dialog.findViewById(R.id.add_parking_title);
            tvTitle.setText(R.string.Add_new_parking);

            final EditText etName = dialog.findViewById(R.id.add_parking_name);
            final EditText etDescription = dialog.findViewById(R.id.add_parking_desc);
            final EditText etLatitude = dialog.findViewById(R.id.add_parking_lati);
            final EditText etLongitude = dialog.findViewById(R.id.add_parking_long);
            final Spinner sType = dialog.findViewById(R.id.add_parking_type);

            Button btnAddParking = dialog.findViewById(R.id.btn_add_parking);

            btnAddParking.setOnClickListener(v1 -> {
                final String name = etName.getText().toString();
                final String description = etDescription.getText().toString();

                Double longitude, latitude;
                try {
                    latitude = Double.parseDouble(etLatitude.getText().toString());
                    longitude = Double.parseDouble(etLongitude.getText().toString());
                } catch (Throwable t) {
                    customToast(getActivity(), "Turn on GPS and try again", Toast.LENGTH_SHORT);
                    return;
                }

                String text = sType.getSelectedItem().toString();

                final boolean secret = (text.equals("Private"));

                if (TextUtils.isEmpty(name)) {
                    customToast(getActivity(), "Enter parking name!", Toast.LENGTH_SHORT);
                    return;
                }

                if (TextUtils.isEmpty(description)) {
                    customToast(getActivity(), "Enter parking description!", Toast.LENGTH_SHORT);
                    return;
                }

                String uid = MainActivity.loggedUser.getUid();
                DatabaseReference parkings = FirebaseDatabase.getInstance().getReference("parkings");
                DatabaseReference database = FirebaseDatabase.getInstance().getReference("users").child(uid);

                Parking newParking = new Parking(name, description, longitude, latitude, uid, secret);
                String key = parkings.push().getKey();
                newParking.setPid(key);
                assert key != null;
                parkings.child(key).setValue(newParking);

                customToast(getActivity(), "Adding " + ADD_POINTS_NEW_PARKING + " points!", Toast.LENGTH_SHORT);
                MyLocationService.myPoints += ADD_POINTS_NEW_PARKING;
                database.child("points").setValue(MyLocationService.myPoints);

                customToast(getActivity(), "Parking " + name + " has been added!", Toast.LENGTH_SHORT);
                dialog.dismiss();
            });

            Button btnCancelParking = dialog.findViewById(R.id.btn_cancel_parking);
            btnCancelParking.setOnClickListener(v12 -> dialog.dismiss());

            if (latitude != null && longitude != null) {
                etLatitude.setText(String.valueOf(latitude));
                etLongitude.setText(String.valueOf(longitude));
            } else {
                customToast(getActivity(), "Turn on GPS and try again", Toast.LENGTH_SHORT);
                etLatitude.setText("unknown");
                etLongitude.setText("unknown");
            }

            dialog.show();
        });


        btnCancelDirections.setOnClickListener(v -> {
            direction.remove();
            for (Marker marker : mapParkingIdMarker.values()) {
                marker.setVisible(true);
            }

            CameraPosition mCameraPosition = new CameraPosition.Builder().target(new LatLng(latitude, longitude)).zoom(15).build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));

            changeVisibility(false);
        });

        mMapView = view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            e.printStackTrace();
        }

        mMapView.getMapAsync(mMap -> {
            googleMap = mMap;

            googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            googleMap.getUiSettings().setMyLocationButtonEnabled(true);

            // For showing a move to my location button
            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.

                return;
            }
            googleMap.setMyLocationEnabled(false);
            googleMap.getUiSettings().setMapToolbarEnabled(false);

            googleMap.setOnInfoWindowClickListener(mMarker -> {
                final String parkingId = getKeyFromValue(mapParkingIdMarker, mMarker);
                if (parkingId != null) {
                    if (latitude != null && longitude != null) {
                        dialog = new AlertDialog.Builder(getActivity())
                                .setTitle("Get direction to " + mMarker.getTitle() + " parking?")
                                .setMessage("Are you sure you want to get direction to \n\n" + mMarker.getTitle() + "\n" + mMarker.getSnippet())
                                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                                    dialog.dismiss();

                                    for (Marker marker : mapParkingIdMarker.values()) {
                                        marker.setVisible(false);
                                    }

                                    mMarker.setVisible(true);

                                    changeVisibility(true);

                                    getDirection(latitude, longitude, mMarker.getPosition().latitude, mMarker.getPosition().longitude);

                                    CameraPosition mCameraPosition = new CameraPosition.Builder().target(new LatLng(latitude, longitude)).zoom(20).tilt(90).build();
                                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));

                                    DateTime now = new DateTime();
                                    DatabaseReference statistic = FirebaseDatabase.getInstance().getReference("statistic");
                                    Statistic stat = new Statistic(MainActivity.loggedUser.getUid(), parkingId, now.toString());
                                    String key = statistic.push().getKey();
                                    assert key != null;
                                    statistic.child(key).setValue(stat);
                                })
                                .setNegativeButton(android.R.string.no, (dialog, which) -> customToast(getActivity(), "You declined parking direction!", Toast.LENGTH_SHORT))
                                .setIcon(R.mipmap.logo)
                                .show();
                    } else {
                        customToast(getActivity(), "Turn on GPS and try again", Toast.LENGTH_SHORT);
                    }
                }
            });
        });


        ArrayAdapter<CharSequence> spinnerAdapter = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                if (position == getCount()) {
                    ((TextView) v.findViewById(android.R.id.text1)).setText("");
                    ((TextView) v.findViewById(android.R.id.text1)).setHint(getItem(getCount())); //"Hint to be displayed"
                }

                return v;
            }

            @Override
            public int getCount() {
                return super.getCount() - 1; // you dont display last item. It is used as hint.
            }

        };

        String[] myResArray = getResources().getStringArray(R.array.search_type);
        spinnerAdapter.addAll(myResArray);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sSearchType.setAdapter(spinnerAdapter);
        sSearchType.setSelection(spinnerAdapter.getCount());
        sSearchType.getBackground().setColorFilter(getResources().getColor(R.color.white), PorterDuff.Mode.SRC_ATOP);

        sSearchType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (distanceCircle != null & position != 1)
                    distanceCircle.remove();

                search.setQuery("", false);
                switch (position) {
                    case 0:
                        setSearchStrategy(new NameSearchStrategy((MainActivity) getActivity()));
                        setSearch("Enter name");
                        break;
                    case 1:
                        setSearchStrategy(new DistanceSearchStrategy((MainActivity) getActivity()));
                        setSearch("In meters");
                        break;
                    case 2:
                        setSearchStrategy(new TypeSearchStrategy());
                        setSearch("Private or Public");
                        break;
                }

                for (Marker marker : mapParkingIdMarker.values()) {
                    marker.setVisible(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        search.setQueryHint("Select type first");
        EditText searchEditText = search.findViewById(R.id.search_src_text);
        searchEditText.setTextColor(getResources().getColor(R.color.white));
        searchEditText.setTextSize(15);

        if (!search.isFocused()) {
            search.clearFocus();
        }

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                searchMarker(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 1)
                    searchMarker(newText);
                return false;
            }
        });

        search.setOnCloseListener(() -> {
            for (Marker marker : mapParkingIdMarker.values()) {
                marker.setVisible(true);
            }

            if (distanceCircle != null)
                distanceCircle.remove();

            return false;
        });

        return view;
    }

    private void changeVisibility(boolean b) {
        if (b) {
            btnCancelDirections.setVisibility(View.VISIBLE);
            search.setVisibility(View.INVISIBLE);
        } else {
            btnCancelDirections.setVisibility(View.GONE);
            search.setVisibility(View.VISIBLE);
        }

        sSearchType.setEnabled(!b);
    }

    private void setSearch(String hint) {
        search.setQueryHint(hint);
        search.setIconified(false);
        search.requestFocusFromTouch();
    }

    private void searchMarker(String query) {
        if (searchStrategy != null) {
            searchStrategy.search(query, mapParkingIdMarker);
        } else {
            customToast(getActivity(), "Please first select type of search!", Toast.LENGTH_SHORT);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
//        if (checkLocationPermission()) {
//            if (ContextCompat.checkSelfPermission(getActivity(),
//                Manifest.permission. ACCESS_FINE_LOCATION)
//                == PackageManager.PERMISSION_GRANTED) {
//
//            //Request location updates:
//            //locationManager.requestLocationUpdates(provider, 400, 1, this);
//
//            }
//        }
        mMapView.onResume();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

//    public boolean checkLocationPermission()
//    {
//        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission. ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
//        {
//            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission. ACCESS_FINE_LOCATION))
//            {
//
//                // Show an explanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//                new AlertDialog.Builder(getActivity())
//                        .setTitle(R.string.title_location_permission)
//                        .setMessage(R.string.text_location_permission)
//                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
//                        {
//                            @Override
//                            public void onClick(DialogInterface dialogInterface, int i)
//                            {
//                                //Prompt the user once explanation has been shown
//                                ActivityCompat.requestPermissions(getActivity(),
//                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                                        MY_PERMISSIONS_REQUEST_LOCATION);
//                            }
//                        }).create();
//            }
//            else
//            {
//                // No explanation needed, we can request the permission.
//                ActivityCompat.requestPermissions(getActivity(),
//                        new String[]{Manifest.permission. ACCESS_FINE_LOCATION},
//                        MY_PERMISSIONS_REQUEST_LOCATION);
//            }
//            return false;
//        }
//        else
//        {
//            return true;
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
//    {
//        switch (requestCode)
//        {
//            case MY_PERMISSIONS_REQUEST_LOCATION:
//            {
//                // If request is cancelled, the result arrays are empty.
//                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
//                {
//                    // permission was granted, yay! Do the
//                    // location-related task you need to do.
//                    if (ContextCompat.checkSelfPermission(getActivity(),
//                            Manifest.permission. ACCESS_FINE_LOCATION)
//                            == PackageManager.PERMISSION_GRANTED)
//                    {
//                        //Request location updates:
//                        //locationManager.requestLocationUpdates(provider, 400, 1, this);
//                    }
//                }
//                else
//                {
//                    // permission denied, boo! Disable the
//                    // functionality that depends on this permission.
//                }
//                return;
//            }
//        }
//    }

    private void setSearchStrategy(SearchStrategy newSearchStrategy) {
        this.searchStrategy = newSearchStrategy;
    }

    public void setCircle(LatLng latLng, float q_distance) {
        if (distanceCircle != null)
            distanceCircle.remove();

        this.distanceCircle = this.googleMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(q_distance)
                .strokeColor(Color.rgb(0, 0, 255))
                .strokeWidth(5)
                .fillColor(Color.argb(128, 255, 255, 255)));

        CameraPosition mCameraPosition = new CameraPosition.Builder().target(new LatLng(latitude, longitude)).zoom(15).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
    }

    private GeoApiContext getGeoContext() {
        GeoApiContext geoApiContext = new GeoApiContext();
        return geoApiContext.setQueryRateLimit(3)
                .setApiKey(getString(R.string.directionsApiKey))
                .setConnectTimeout(1, TimeUnit.SECONDS)
                .setReadTimeout(1, TimeUnit.SECONDS)
                .setWriteTimeout(1, TimeUnit.SECONDS);
    }

    public void getDirection(double latOrig, double lonOrig, double latDest, double lonDest) {
        DateTime now = new DateTime();
        DirectionsResult result = null;

        try {
            result = DirectionsApi.newRequest(getGeoContext())
                    .mode(TravelMode.DRIVING)
                    .origin(new com.google.maps.model.LatLng(latOrig, lonOrig))
                    .destination(new com.google.maps.model.LatLng(latDest, lonDest))
                    .departureTime(now).await();
        } catch (ApiException | InterruptedException | IOException e) {
            e.printStackTrace();
        }

        addPolyline(result, this.googleMap);
    }

    private void addPolyline(DirectionsResult results, GoogleMap mMap) {
        if (direction != null)
            direction.remove();

        List<LatLng> decodedPath = PolyUtil.decode(results.routes[0].overviewPolyline.getEncodedPath());
        direction = mMap.addPolyline(new PolylineOptions().addAll(decodedPath).width(10).color(Color.BLUE));
    }


    public static String getKeyFromValue(HashMap<String, Marker> hm, Marker value) {
        for (String o : hm.keySet()) {
            if (Objects.requireNonNull(hm.get(o)).equals(value)) {
                return o;
            }
        }
        return null;
    }
}
