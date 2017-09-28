package rs.elfak.mosis.nikolamitic.bottomnavigationview.Home;

import android.Manifest;
import android.app.Dialog;
import android.app.Fragment;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;

import rs.elfak.mosis.nikolamitic.bottomnavigationview.Class.Parking;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.R;


public class HomeFragment extends Fragment
{
    FloatingActionButton btnAddParking;

    private GoogleMap googleMap;
    MapView mMapView;

    public Dialog dialog;
    public EditText etName, etDescription, etLongitude, etLatitude;
    public Spinner sType;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.home_fragment, container, false);

        final Rect displayRectangle = new Rect();
        Window window = getActivity().getWindow();
        window.getDecorView().getWindowVisibleDisplayFrame(displayRectangle);
        final LayoutInflater layoutInflater = (LayoutInflater)getActivity().getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);

        btnAddParking = (FloatingActionButton) view.findViewById(R.id.btn_add_new_parking);

        btnAddParking.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //startActivity(new Intent(getActivity(), AddParkingActivity.class));

                dialog = new Dialog(getActivity(),R.style.dialog_no_tytle);

                //TODO add parking
                View layout = layoutInflater.inflate(R.layout.dialog_add_parking, null);
                layout.setMinimumWidth((int)(displayRectangle.width() * 0.8f));

                dialog.setContentView(layout);

                TextView tvTitle = (TextView) dialog.findViewById(R.id.add_parking_title);
                tvTitle.setText(R.string.Add_new_parking);

                etName = (EditText) dialog.findViewById(R.id.add_parking_name);
                etDescription = (EditText) dialog.findViewById(R.id.add_parking_desc);
                etLatitude = (EditText) dialog.findViewById(R.id.add_parking_lati);
                etLongitude = (EditText) dialog.findViewById(R.id.add_parking_long);
                sType = (Spinner) dialog.findViewById(R.id.add_parking_type);

                getGpsCoordinates();
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
                    return;
                }
                googleMap.setMyLocationEnabled(false);

                LatLng currentLocation = new LatLng(43.318731, 21.891143);

                // For dropping a marker at a point on the Map
                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(currentLocation);

                Bitmap markImage = BitmapFactory.decodeResource(getResources(), R.mipmap.me);
                markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmapSizeByScall(markImage,0.1f)));
                markerOptions.title("You");

                googleMap.addMarker(markerOptions);

                // For zooming automatically to the location of the marker
                CameraPosition mCameraPosition = new CameraPosition.Builder().target(currentLocation).zoom(15).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(mCameraPosition));
            }
        });

        return view;
    }

    public void getGpsCoordinates()
    {
        //TODO get user location
        Double currentLat = 43.3151881;
        Double currentLon = 21.9199866;

        if(currentLat!=null && currentLon!=null)
        {
            etLatitude.setText(String.valueOf(currentLat));
            etLongitude.setText(String.valueOf(currentLon));
        }
        else
        {
            Toast.makeText(getActivity(),"Please turn on GPS",Toast.LENGTH_SHORT).show();
            etLatitude.setText("unknown");
            etLongitude.setText("unknown");
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState)
    {
        super.onSaveInstanceState(outState);
    }

    public Bitmap bitmapSizeByScall(Bitmap bitmapIn, float scall_zero_to_one_f)
    {
        Bitmap bitmapOut = Bitmap.createScaledBitmap(bitmapIn,
                Math.round(bitmapIn.getWidth() * scall_zero_to_one_f),
                Math.round(bitmapIn.getHeight() * scall_zero_to_one_f), false);

        return bitmapOut;
    }

    @Override
    public void onResume()
    {
        super.onResume();
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
}
