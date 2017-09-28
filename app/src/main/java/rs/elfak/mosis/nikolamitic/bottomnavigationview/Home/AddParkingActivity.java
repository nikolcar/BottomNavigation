package rs.elfak.mosis.nikolamitic.bottomnavigationview.Home;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;

import rs.elfak.mosis.nikolamitic.bottomnavigationview.Class.Parking;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.Class.User;
import rs.elfak.mosis.nikolamitic.bottomnavigationview.R;

public class AddParkingActivity extends Activity{

    private EditText etName, etDescription, etLongitude, etLatitude;
    private Spinner sType;

    FirebaseAuth mAuth;
    private DatabaseReference parkings;
    private DatabaseReference users;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        TextView title = (TextView)findViewById(android.R.id.title);
        title.setBackgroundColor(Color.BLUE);
        title.setTextColor(Color.WHITE);
        setContentView(R.layout.activity_add_parking);

        mAuth = FirebaseAuth.getInstance();
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        parkings = database.getReference("parkings");
        users = database.getReference("users");

        etName = (EditText) findViewById(R.id.add_parking_name);
        etDescription = (EditText) findViewById(R.id.add_parking_desc);
        etLatitude = (EditText) findViewById(R.id.add_parking_lati);
        etLongitude = (EditText) findViewById(R.id.add_parking_long);
        sType = (Spinner) findViewById(R.id.add_parking_type);

        getGpsCoordinates();
    }

    public void add_parking_click(View v)
    {
        getGpsCoordinates();
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
            latitude=null;
            longitude=null;
        }

        String text = sType.getSelectedItem().toString();

        final boolean secret = (text.equals("Private"));

        if (TextUtils.isEmpty(name))
        {
            Toast.makeText(getApplicationContext(), "Enter parking name!", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(description))
        {
            Toast.makeText(getApplicationContext(), "Enter parking description!", Toast.LENGTH_SHORT).show();
            return;
        }

        Date date = Calendar.getInstance().getTime();

        FirebaseUser user = mAuth.getCurrentUser();
        String uid = user.getUid();

        Parking newParking = new Parking(name, description, longitude, latitude, uid, secret, date);
        String key = parkings.push().getKey();
        parkings.child(key).setValue(newParking);

        if(secret)
        {
            users.child(uid).child("myPrivate").push().setValue(key);
        }

        //users.child(uid).child("points").

        Toast.makeText(getApplicationContext(), "Parking " + name + " has been added!", Toast.LENGTH_SHORT).show();
        this.finish();
    }

    public void cancel_parking_click(View v)
    {
        this.finish();
    }

    private void getGpsCoordinates()
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
            Toast.makeText(this,"Please turn on GPS",Toast.LENGTH_SHORT).show();
            etLatitude.setText("unknown");
            etLongitude.setText("unknown");
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        getGpsCoordinates();
    }
}
