package com.example.gpsexample;


import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.model.LatLng;

public class MainActivityCopy extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, com.google.android.gms.location.LocationListener {

    private static final String TAG = "MainActivity";
    private TextView mLatitudeTextView;
    private TextView mLongitudeTextView;

    //mode
    private RadioButton googleApi;
    private RadioButton locationApi;

    //criteria
    private TextView timeGap;
    private TextView distanceGap;

    //accuracy
    private RadioButton highAccuracy;
    private RadioButton balancedAccuracy;

    //gps config
    private CheckBox deleteGPS;
    private CheckBox xtraTime;
    private CheckBox xtraData;

    //buttons
    private Button startGPS;

    //Layout
    private LinearLayout xtra;



    private GoogleApiClient mGoogleApiClient;
    private Location mLocation;
    private LocationManager mLocationManager;

    private LocationRequest mLocationRequest;
    private com.google.android.gms.location.LocationListener listener;
    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */
    private float DISTANCE_INTERVAL = 10; /* 10 m */
    private boolean HIGH_ACCURACY = true; /* 2 sec */

    private LocationManager locationManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mLatitudeTextView = (TextView) findViewById((R.id.latitude_textview));
        mLongitudeTextView = (TextView) findViewById((R.id.longitude_textview));

        //mode
        googleApi=(RadioButton)findViewById(R.id.googleAPI);
        locationApi=(RadioButton)findViewById(R.id.androidAPI);

        //criteria
        timeGap=(TextView) findViewById(R.id.timeGap);;
        distanceGap=(TextView) findViewById(R.id.distanceGap);

        //accuracy
        highAccuracy=(RadioButton)findViewById(R.id.highAccuracy);;
        balancedAccuracy=(RadioButton)findViewById(R.id.balancedAccuracy);

        //gps config
        deleteGPS=(CheckBox) findViewById(R.id.resetGPS);
        xtraTime=(CheckBox) findViewById(R.id.xtraTime);
        xtraData=(CheckBox) findViewById(R.id.xtraData);

        //buttons
        startGPS=(Button) findViewById(R.id.startLoc);

        //layout
        xtra=(LinearLayout) findViewById(R.id.xtra);

        mGoogleApiClient = new GoogleApiClient.Builder(MainActivityCopy.this)
                .addConnectionCallbacks(MainActivityCopy.this)
                .addOnConnectionFailedListener(MainActivityCopy.this)
                .addApi(LocationServices.API)
                .build();

        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

        mLocationManager = (LocationManager) MainActivityCopy.this.getSystemService(Context.LOCATION_SERVICE);

        startGPS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(googleApi.isChecked()) {
                    if (mGoogleApiClient != null) {
                        mGoogleApiClient.connect();
                    }
                }else if (locationApi.isChecked()){
                    if (mGoogleApiClient.isConnected()) {
                        mGoogleApiClient.disconnect();
                    }
                }
            }
        });

        googleApi.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    xtra.setVisibility(View.INVISIBLE);
                }else {
                    xtra.setVisibility(View.VISIBLE);
                }
            }
        });


//        checkLocation(); //check whether location service is enable or not in your  phone
    }

    private void populateConstants() {
        UPDATE_INTERVAL= Long.parseLong(timeGap.getText().toString());
        DISTANCE_INTERVAL= Float.parseFloat(distanceGap.getText().toString());
        HIGH_ACCURACY = highAccuracy.isChecked();
    }

    private void initLocationManger(){
        LocationProvider provider =
                locationManager.getProvider(LocationManager.GPS_PROVIDER);

    }

    @Override
    public void onConnected(Bundle bundle) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        startLocationUpdates();

        mLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

        if(mLocation == null){
            startLocationUpdates();
        }
        if (mLocation != null) {

            // mLatitudeTextView.setText(String.valueOf(mLocation.getLatitude()));
            //mLongitudeTextView.setText(String.valueOf(mLocation.getLongitude()));
        } else {
            Toast.makeText(this, "Location not Detected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Connection Suspended");
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.i(TAG, "Connection failed. Error: " + connectionResult.getErrorCode());
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mGoogleApiClient != null) {
            mGoogleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    protected void startLocationUpdates() {
        populateConstants();
        // Create the location request
        mLocationRequest = LocationRequest.create()
                .setPriority(((HIGH_ACCURACY)?LocationRequest.PRIORITY_HIGH_ACCURACY:LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY))
                .setSmallestDisplacement(DISTANCE_INTERVAL)
                .setInterval(UPDATE_INTERVAL)
                .setFastestInterval(FASTEST_INTERVAL);
        // Request location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient,
                mLocationRequest, this);
        Log.d("reque", "--->>>>");

        xtra();
    }

    private void xtra(){
//        mLocationManager.sendExtraCommand(LocationManager.GPS_PROVIDER,"delete_aiding_data", null);
//        Bundle bundle = new Bundle();
//        mLocationManager.sendExtraCommand("gps", "force_xtra_injection", bundle);
//        mLocationManager.sendExtraCommand("gps", "force_time_injection", bundle);
    }

    @Override
    public void onLocationChanged(Location location) {

        String msg = "Updated Location: " +
                Double.toString(location.getLatitude()) + "," +
                Double.toString(location.getLongitude());
        mLatitudeTextView.setText(String.valueOf(location.getLatitude()));
        mLongitudeTextView.setText(String.valueOf(location.getLongitude() ));
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        // You can now create a LatLng Object for use with maps
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());

        xtra();
    }

    private boolean checkLocation() {
        if(!isLocationEnabled())
            showAlert();
        return isLocationEnabled();
    }

    private void showAlert() {
        final AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setTitle("Enable Location")
                .setMessage("Your Locations Settings is set to 'Off'.\nPlease Enable Location to " +
                        "use this app")
                .setPositiveButton("Location Settings", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                        Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(myIntent);
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {

                    }
                });
        dialog.show();
    }

    private boolean isLocationEnabled() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

}
