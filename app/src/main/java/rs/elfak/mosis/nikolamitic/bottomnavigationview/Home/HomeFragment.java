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
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
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

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import rs.elfak.mosis.nikolamitic.bottomnavigationview.Class.Parking;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.MainActivity;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Strategy.DistanceSearchStrategy;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Strategy.NameSearchStrategy;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.R;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Strategy.SearchStrategy;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Strategy.TypeSearchStrategy;

import static rs.elfak.mosis.nikolamitic.bottomnavigationview.MyLocationService.latitude;
import static rs.elfak.mosis.nikolamitic.bottomnavigationview.MyLocationService.longitude;

public class HomeFragment extends Fragment
{
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    FloatingActionButton btnAddNewParking;

    public GoogleMap googleMap;
    MapView mMapView;
    public static HashMap<Parking, Marker> mapMarkersParkings = new HashMap<Parking, Marker>();
    public static HashMap<String, Marker> mapUserIdMarker = new HashMap<String, Marker>();
    public static HashMap<String, Marker> mapFriendIdMarker = new HashMap<String, Marker>();

    private Circle distanceCircle;
    private SearchStrategy searchStrategy = null;

    public Dialog dialog;
    private SearchView search;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.home_fragment, container, false);

        final Rect displayRectangle = new Rect();
        Window window = getActivity().getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        final LayoutInflater layoutInflater = (LayoutInflater)getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);

        btnAddNewParking = (FloatingActionButton) view.findViewById(R.id.btn_add_new_parking);

        btnAddNewParking.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                dialog = new Dialog(getActivity(),R.style.dialog_no_tytle);

                View layout = layoutInflater.inflate(R.layout.dialog_add_parking, null);
                layout.setMinimumWidth((int)(displayRectangle.width() * 0.8f));

                dialog.setContentView(layout);

                TextView tvTitle = (TextView) dialog.findViewById(R.id.add_parking_title);
                tvTitle.setText(R.string.Add_new_parking);

                final EditText etName = (EditText) dialog.findViewById(R.id.add_parking_name);
                final EditText etDescription = (EditText) dialog.findViewById(R.id.add_parking_desc);
                final EditText etLatitude = (EditText) dialog.findViewById(R.id.add_parking_lati);
                final EditText etLongitude = (EditText) dialog.findViewById(R.id.add_parking_long);
                final Spinner sType = (Spinner) dialog.findViewById(R.id.add_parking_type);

                Button btnAddParking = (Button) dialog.findViewById(R.id.btn_add_parking);

                btnAddParking.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        final String name = etName.getText().toString();
                        final String description = etDescription.getText().toString();

                        Double longitude, latitude;
                        try
                        {
                            latitude = Double.parseDouble(etLatitude.getText().toString());
                            longitude = Double.parseDouble(etLongitude.getText().toString());
                        }
                        catch (Throwable t)
                        {
                            Toast.makeText(getActivity(),"Turn on GPS first!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        String text = sType.getSelectedItem().toString();

                        final boolean secret = (text.equals("Private"));

                        if (TextUtils.isEmpty(name))
                        {
                            Toast.makeText(getActivity(), "Enter parking name!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (TextUtils.isEmpty(description))
                        {
                            Toast.makeText(getActivity(), "Enter parking description!", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        //Date date = Calendar.getInstance().getTime();

                        String uid = MainActivity.loggedUser.getUid();
                        DatabaseReference parkings = FirebaseDatabase.getInstance().getReference("parkings");
                        DatabaseReference users = FirebaseDatabase.getInstance().getReference("users");

                        Parking newParking = new Parking(name, description, longitude, latitude, uid, secret);
                        String key = parkings.push().getKey();
                        parkings.child(key).setValue(newParking);

                        if(secret)
                        {
                            users.child(uid).child("myPrivate").push().setValue(key);
                        }

                        //TODO add points
                        //users.child(uid).child("points").

                        Toast.makeText(getActivity(), "Parking " + name + " has been added!", Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                    }
                });

                Button btnCancelParking = (Button) dialog.findViewById(R.id.btn_cancel_parking);
                btnCancelParking.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        dialog.dismiss();
                    }
                });

                if(latitude!=null && longitude!=null)
                {
                    etLatitude.setText(String.valueOf(latitude));
                    etLongitude.setText(String.valueOf(longitude));
                }
                else
                {
                    Toast.makeText(getActivity(),"Please turn on GPS!",Toast.LENGTH_SHORT).show();
                    etLatitude.setText("unknown");
                    etLongitude.setText("unknown");
                }

                dialog.show();
            }
        });

        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);

        mMapView.onResume(); // needed to get the map to display immediately

        try
        {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        mMapView.getMapAsync(new OnMapReadyCallback()
        {
            @Override
            public void onMapReady(GoogleMap mMap)
            {
                googleMap = mMap;

                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                googleMap.getUiSettings().setMyLocationButtonEnabled(true);

                // For showing a move to my location button
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.


                    //checkLocationPermission();
                    return;
                }
                googleMap.setMyLocationEnabled(false);
                googleMap.getUiSettings().setMapToolbarEnabled(false);

                //LatLng currentLocation = new LatLng(43.318731, 21.891143);

                // For dropping a marker at a point on the Map
                //MarkerOptions markerOptions = new MarkerOptions();
                //markerOptions.position(currentLocation);

                //Bitmap markImage = BitmapFactory.decodeResource(getResources(), R.mipmap.me);
                //markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmapSizeByScall(markImage,0.1f)));
                //markerOptions.title("You");

                //googleMap.addMarker(markerOptions);

                // For zooming automatically to the location of the marker
                //CameraPosition mCameraPosition = new CameraPosition.Builder().target(currentLocation).zoom(15).build();
                //googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
            }
        });

        Spinner spinner = (Spinner) view.findViewById(R.id.spinnerMapSearchCategory);

        ArrayAdapter<CharSequence> spinnerAdapter = new ArrayAdapter<CharSequence>(getActivity(), android.R.layout.simple_spinner_item) {

            @Override
            public View getView(int position, View convertView, ViewGroup parent)
            {
                View v = super.getView(position, convertView, parent);
                if (position == getCount())
                {
                    ((TextView)v.findViewById(android.R.id.text1)).setText("");
                    ((TextView)v.findViewById(android.R.id.text1)).setHint(getItem(getCount())); //"Hint to be displayed"
                }

                return v;
            }

            @Override
            public int getCount()
            {
                return super.getCount()-1; // you dont display last item. It is used as hint.
            }

        };

        String[] myResArray = getResources().getStringArray(R.array.search_type);
        spinnerAdapter.addAll(myResArray);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setSelection(spinnerAdapter.getCount());
        spinner.getBackground().setColorFilter(getResources().getColor(R.color.blue), PorterDuff.Mode.SRC_ATOP);


        search = (SearchView) view.findViewById(R.id.searchMap);
        search.setQueryHint("Select type first");
        EditText searchEditText = (EditText)search.findViewById(android.support.v7.appcompat.R.id.search_src_text);
        searchEditText.setTextColor(getResources().getColor(R.color.blue));
        searchEditText.setTextSize(15);

        if(!search.isFocused())
        {
            search.clearFocus();
        }

        search.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query)
            {
                searchMarker(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                if (newText.length() > 1)
                    searchMarker(newText);
                return false;
            }
        });

        search.setOnCloseListener(new SearchView.OnCloseListener()
        {
            @Override
            public boolean onClose()
            {
                for (Parking parking: mapMarkersParkings.keySet())
                {
                    mapMarkersParkings.get(parking).setVisible(true);
                }

                if (distanceCircle != null)
                    distanceCircle.remove();

                return false;
            }
        });

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                if (distanceCircle != null & position!=1)
                    distanceCircle.remove();

                search.setQuery("",false);
                switch (position)
                {
                    case 0:
                        setSearchStrategy(new NameSearchStrategy());
                        setSearch("Enter name");
                        break;
                    case 1:
                        setSearchStrategy(new DistanceSearchStrategy());
                        setSearch("In meters");
                        break;
                    case 2:
                        setSearchStrategy(new TypeSearchStrategy());
                        setSearch("Private or Public");
//                        setSearch("Enter type");
//                        search.setQuery("Private/Public", false);
                        break;
                }

                for (Parking parking: mapMarkersParkings.keySet())
                {
                    mapMarkersParkings.get(parking).setVisible(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent)
            {
            }
        });

        return view;
    }

    private void setSearch(String hint)
    {
        search.setQueryHint(hint);
        search.setIconified(false);
        search.requestFocusFromTouch();
    }

    private void searchMarker(String query)
    {
        if (searchStrategy != null)
        {
            searchStrategy.search(query, mapMarkersParkings);
        }
        else
        {
            Toast.makeText(getActivity(),"Please first select type of search!",Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume()
    {
        super.onResume();
        /*
        if (checkLocationPermission()) {
            if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission. ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {

            //Request location updates:
            //locationManager.requestLocationUpdates(provider, 400, 1, this);

            }
        }
        */
        mMapView.onResume();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        mMapView.onDestroy();
    }

    @Override
    public void onLowMemory()
    {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    public boolean checkLocationPermission()
    {
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission. ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission. ACCESS_FINE_LOCATION))
            {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.title_location_permission)
                        .setMessage(R.string.text_location_permission)
                        .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i)
                            {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(getActivity(),
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION);
                            }
                        }).create().show();
            }
            else
            {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission. ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION);
            }
            return false;
        }
        else
        {
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case MY_PERMISSIONS_REQUEST_LOCATION:
            {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {
                    // permission was granted, yay! Do the
                    // location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(getActivity(),
                            Manifest.permission. ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED)
                    {
                        //Request location updates:
                        //locationManager.requestLocationUpdates(provider, 400, 1, this);
                    }
                }
                else
                {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }
        }
    }


    private void setSearchStrategy(SearchStrategy newSearchStrategy)
    {
        this.searchStrategy = newSearchStrategy;
    }

    public void setCircle(LatLng latLng, float q_distance)
    {
        if (distanceCircle != null)
            distanceCircle.remove();
        // Drawing circle
        this.distanceCircle = this.googleMap.addCircle(new CircleOptions()
                .center(latLng)
                .radius(q_distance)
                .strokeColor(Color.rgb(0,0,255))
                .strokeWidth(5)
                .fillColor(Color.argb(128,255,255,255)));
    }
}
