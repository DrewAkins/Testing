package com.example.maxdr_000.miniproject1;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Chronometer;
import android.widget.TextView;

import com.example.maxdr_000.miniproject1.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    Marker myMarker = null;
    Chronometer chronometers;


    private double totalDistance; //total distance
    private long totalSeconds; //total seconds passed
    LatLng oldPos;
    boolean doesTripStarted;

    Timer timer;
    TimerTask timerTask;
    final Handler timeHandler = new Handler();

    int markerDensity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link } once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                LatLng tmpPosX = new LatLng(30.094494, -95.989462); //Center of PVAMU
                myMarker = mMap.addMarker(new MarkerOptions().position(tmpPosX).title("Meee"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(tmpPosX, 17));
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    mMap.setMyLocationEnabled(true);
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID); //MAP_TYPE_HYBRID, MAP_TYPE_NORMAL
                }
            }

            //Update my location when I am moving around  (callback function)
            mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {
                @Override
                public void onMyLocationChange(Location arg0) {
                    // TODO Auto-generated method stub
                    LatLng currentPos = new LatLng(arg0.getLatitude(), arg0.getLongitude());
                    myMarker.setPosition(currentPos); //Meeee

                    if (!doesTripStarted) {
                        oldPos = currentPos;
                        totalDistance = 0;
                        myMarker.setPosition(currentPos);
                        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPos, 17));
                    }
                    else {
                        totalDistance = totalDistance + myDist(oldPos.latitude, oldPos.longitude,
                                currentPos.latitude, currentPos.longitude, 'M');
                        //display the total
                        TextView myDistText=(TextView)findViewById(R.id.Distancetext);
                        DecimalFormat df = new DecimalFormat(); //display the distance
                        df.setMaximumFractionDigits(4);
                        myDistText.setText(String.valueOf(df.format(totalDistance)));

                        //place marker
                        markerDensity++;
                        if (markerDensity >= 5) {
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPos, 17));
                            mMap.addMarker(new MarkerOptions().position(currentPos));
                            markerDensity = 0;
                        }
                        oldPos = currentPos;
                    }
                }
            });
        }
    }

    public void startTracking(View view) {//button callback function
        // Initializing
        doesTripStarted = true;
        TextView myTimeText = (TextView)findViewById(R.id.timers );
        TextView myDistText = (TextView)findViewById(R.id.Distancetext );
        myTimeText.setText("00000");
        myDistText.setText("0.000000");
        totalDistance = 0.0f;
        totalSeconds = 0;
        markerDensity = 0;
        mMap.clear(); //clear all the markers.
        doesTripStarted = true;

        // After the first 0ms the TimerTask will run every 1000ms
        timer = new Timer(); //set a new Timer
        initializeTimerTask(); //initialize the TimerTask's job
        timer.schedule(timerTask, 500, 1000);
    }

    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {//use a handler to run a toast that shows the current timestamp
                timeHandler.post(new Runnable() {
                    public void run() {
                        //get the current timeStamp
                        totalSeconds++;
                        TextView myTimeText=(TextView)findViewById(R.id.timers);
                        myTimeText.setText(String.valueOf(totalSeconds));
                    }
                });
            }
        };
    }
    public void stopTracking(View view) {//button callback function
        // Do something in response to button click
        timer.cancel();
        doesTripStarted = false;
    }

    //latitude and longitude must be in decimal degree, such as 30.094494, -95.989462
    //unit: ‘M’ for miles, ‘K’ for Kilometers, ‘N’ for nautical miles
    //       200 feet = 0.0378788 miles
    double myDist( double lat1, double lon1, double lat2, double lon2, char unit)
    {
        double theta, dist;
        theta = lon1 - lon2;
        dist = Math.sin(lat1*3.14/ 180) * Math.sin(lat2*3.14/180) +
                Math.cos(lat1*3.14/ 180) * Math.cos(lat2*3.14/ 180) * Math.cos(theta*3.14/180);

        dist = Math.acos(dist);
        dist = dist * 180 / 3.1415926535;
        dist = dist * 60 * 1.1515;
        switch(unit) {
            case 'M':
                break;
            case 'K':
                dist = dist * 1.609344;
                break;
            case 'N':
                dist = dist * 0.8684;
                break;
        }
        return dist;
    }
}
