package rs.elfak.mosis.nikolamitic.bottomnavigationview;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.location.LocationManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.maps.model.Marker;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import rs.elfak.mosis.nikolamitic.bottomnavigationview.Class.Parking;
import static rs.elfak.mosis.nikolamitic.bottomnavigationview.Home.HomeFragment.mapFriendIdMarker;
import static rs.elfak.mosis.nikolamitic.bottomnavigationview.Home.HomeFragment.mapParkingsMarkers;

public class MyLocationService extends Service
{
    private LocationManager mLocationManager = null;
    public static final int NOTIFY_DISTANCE = 500;
    private static final int LOCATION_INTERVAL = 1000;
    private static final float LOCATION_DISTANCE = 10f;
    private static final long TIME_BETWEEN_NOTIFICATIONS = 60L;

    public static Double longitude;
    public static Double latitude;
    private FirebaseDatabase database;
    private String loggedUserUid;
    public static int myPoints = 0;
    private Long timeLastNotification = 0L;


    public static float distanceBetween(float lat1, float lng1, float lat2, float lng2)
    {
        double earthRadius = 6371000; //meters
        double dLat = Math.toRadians(lat2-lat1);
        double dLng = Math.toRadians(lng2-lng1);
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLng/2) * Math.sin(dLng/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        float dist = (float) (earthRadius * c);

        return dist;
    }

    private class LocationListener implements android.location.LocationListener
    {
        Location mLastLocation;

        public LocationListener(String provider)
        {
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location)
        {
            mLastLocation.set(location);
            mLastLocation.set(location);

            longitude = location.getLongitude();
            latitude = location.getLatitude();

            DatabaseReference users = FirebaseDatabase.getInstance().getReference("users");

            users.child(loggedUserUid).child("latitude").setValue(latitude);
            users.child(loggedUserUid).child("longitude").setValue(longitude);

            deleteAllNotifications(getApplicationContext());
            showFriendsInRadius();
            showParkingInRadius();
        }


        @Override
        public void onProviderDisabled(String provider)
        {
        }

        @Override
        public void onProviderEnabled(String provider)
        {
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras)
        {
        }
    }

    private void showParkingInRadius()
    {
        double myNewLat, myNewLon;
        myNewLat = latitude;
        myNewLon = longitude;

        float minDistance = NOTIFY_DISTANCE;
        Marker minDistanceMarker = null;
        boolean minDistanceSecret = false;

        for (Parking key : mapParkingsMarkers.keySet())
        {
            Marker marker = mapParkingsMarkers.get(key);
            Float distanceFromMarker = distanceBetween((float) myNewLat, (float) myNewLon, (float) marker.getPosition().latitude, (float) marker.getPosition().longitude);

            if (distanceFromMarker < NOTIFY_DISTANCE)
            {
                if (distanceFromMarker < minDistance)
                {
                    minDistance = distanceFromMarker;
                    minDistanceMarker = marker;
                    minDistanceSecret = key.isSecret();
                }
            }
        }

        if(minDistanceMarker!=null)
        {
            int type = 2;
            if(minDistanceSecret)
                type = 3;

            showNotification(type,minDistanceMarker.getTitle() + " is " + Math.round(minDistance) + " meters away from you!");
        }
    }

    public void showFriendsInRadius(/*Marker marker*/)
    {
        double myNewLat, myNewLon;
        myNewLat = latitude;
        myNewLon = longitude;

//        if (marker == null)
//        {
            for (String key : mapFriendIdMarker.keySet())
            {
                Marker marker = mapFriendIdMarker.get(key);
                Float distanceFromMarker = distanceBetween((float) myNewLat, (float) myNewLon, (float) marker.getPosition().latitude, (float) marker.getPosition().longitude);
                if (distanceFromMarker < NOTIFY_DISTANCE)
                {
                    showNotification(1, marker.getTitle() + " is " + Math.round(distanceFromMarker) + " meters away from you!");
                }
                else
                {
                    //deleteNotification(this,1);
                }
            }
//        }
//        else
//        {
//            Float distanceFromMarker = distanceBetween((float) myNewLat, (float) myNewLon, (float) marker.getPosition().latitude, (float) marker.getPosition().longitude);
//            if (distanceFromMarker < NOTIFY_DISTANCE)
//            {
//                showNotification(1, marker.getTitle() + " is " + Math.round(distanceFromMarker) + " meters away from you!");
//            }
//            else
//            {
//                //deleteNotification(this,1);
//            }
//        }
    }



    //    LocationListener[] mLocationListeners = new LocationListener[]
//    {
//        new LocationListener(LocationManager.GPS_PROVIDER),
//        new LocationListener(LocationManager.NETWORK_PROVIDER)
//    };

    LocationListener[] mLocationListeners = new LocationListener[]
    {
        new LocationListener(LocationManager.PASSIVE_PROVIDER)
    };

    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        super.onStartCommand(intent, flags, startId);

        loggedUserUid = MainActivity.loggedUser.getUid();

        database.getReference("users").child(loggedUserUid).child("points").addListenerForSingleValueEvent(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                myPoints = dataSnapshot.getValue(Integer.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError)
            {
            }
        });

        return START_STICKY;
    }

    @Override
    public void onCreate()
    {
        initializeLocationManager();

        database = FirebaseDatabase.getInstance();

        try
        {
            mLocationManager.requestLocationUpdates(
                    LocationManager.PASSIVE_PROVIDER,
                    LOCATION_INTERVAL/*SettingsFragment.gpsRefresh*/,
                    LOCATION_DISTANCE,
                    mLocationListeners[0]
            );
        }
        catch (java.lang.SecurityException ex)
        {
            Log.i("", "fail to request location update, ignore", ex);
        }
        catch (IllegalArgumentException ex)
        {
            Log.d("", "network provider does not exist, " + ex.getMessage());
        }

        //        try
//        {
//            mLocationManager.requestLocationUpdates(
//                    LocationManager.GPS_PROVIDER,
//                    LOCATION_INTERVAL,
//                    LOCATION_DISTANCE,
//                    mLocationListeners[1]
//            );
//        }
//        catch (java.lang.SecurityException ex)
//        {
//            Log.i("", "fail to request location update, ignore", ex);
//        }
//        catch (IllegalArgumentException ex)
//        {
//            Log.d("", "gps provider does not exist " + ex.getMessage());
//        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (mLocationManager != null)
        {
            for (int i = 0; i < mLocationListeners.length; i++)
            {
                try
                {
                    if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
                    {
                        return;
                    }
                    mLocationManager.removeUpdates(mLocationListeners[i]);
                }
                catch (Exception ex)
                {
                    Log.i("", "fail to remove location listener, ignore", ex);
                }
            }
        }
    }

    private void initializeLocationManager()
    {
        if (mLocationManager == null)
        {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    private int mNotificationId;
    //Some things we only have to set the first time.
    private boolean firstNotification = true;
    NotificationCompat.Builder mBuilder = null;

    public void showNotification(int uid, String text)
    {
        vibrationAndSoundNotification();

        mNotificationId = uid;
        NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if(firstNotification)
        {
            firstNotification = false;
            mBuilder = new NotificationCompat.Builder(this)
                    .setOnlyAlertOnce(true)
                    .setPriority(Notification.PRIORITY_MAX);

            // Creates an explicit intent for an Activity in your app
            Intent resultIntent = new Intent(this, MainActivity.class);

            // The stack builder object will contain an artificial back stack for the started Activity.
            // This ensures that navigating backward from the Activity leads out of your application to the Home screen.
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
            // Adds the back stack for the Intent (but not the Intent itself)
            stackBuilder.addParentStack(MainActivity.class);
            // Adds the Intent that starts the Activity to the top of the stack
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
        }

        Bitmap icon = null;
        String what = "";

        if(uid==1)
        {
            icon = BitmapFactory.decodeResource(getResources(), R.mipmap.friend);
            what = "friend";
        }
        else
            if(uid==2)
            {
                icon = BitmapFactory.decodeResource(getResources(), R.mipmap.occupied);
                what ="public parking";
            }
            else
            {
                icon = BitmapFactory.decodeResource(getResources(), R.mipmap.free);
                what ="private parking";
            }

        mBuilder.setSmallIcon(R.mipmap.privateparking).setLargeIcon(icon).setContentTitle("The "+what+" is near");

        mBuilder.setContentText(text).setAutoCancel(true);
        mNotificationManager.notify(mNotificationId, mBuilder.build());
        System.gc(); //force garbage collector
    }

    private void vibrationAndSoundNotification()
    {
        Long time = System.currentTimeMillis()/1000;

        if(time-timeLastNotification>TIME_BETWEEN_NOTIFICATIONS)
        {   //notify user only every TIME_BETWEEN_NOTIFICATIONS seconds
            timeLastNotification = time;

            Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            v.vibrate(500);

            try
            {
                Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                r.play();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
    }

    public static void deleteAllNotifications(Context ctx)
    {
        String ns = Context.NOTIFICATION_SERVICE;
        NotificationManager nMgr = (NotificationManager) ctx.getSystemService(ns);
        for(int i =1; i<4; i++)
            nMgr.cancel(i);
    }
}