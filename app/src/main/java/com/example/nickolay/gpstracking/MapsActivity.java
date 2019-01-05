package com.example.nickolay.gpstracking;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.security.Permission;

import static java.lang.StrictMath.abs;

public class MapsActivity extends FragmentActivity implements
        GoogleMap.OnMyLocationButtonClickListener,
        GoogleMap.OnMyLocationClickListener,
        OnMapReadyCallback,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private static final String FINE_LOCATION = Manifest.permission.ACCESS_FINE_LOCATION;
    private static final String COURSE_LOCATION = Manifest.permission.ACCESS_COARSE_LOCATION;
    private static final float DEFAULT_ZOOM = 15f;
    private GoogleMap mMap;

    private boolean mPermissionDenied = false;
    private boolean mLocationPermissionsGranted = false;

    private FusedLocationProviderClient mFusedLocationClient;
    private LocationCallback locationCallback;
    private LocationRequest locationRequest;


    private boolean locationIsActive = false;
    double CLat=0,CLng=0;
    int Point=1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        getLocationPermission();

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ContextCompat.checkSelfPermission(
                this.getApplicationContext(),
                FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED) {
            locationRequest = LocationRequest.create();
            locationRequest.setInterval(10000);
            locationRequest.setFastestInterval(5000);




            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);

                    Location lastLocation = locationResult.getLastLocation();


                    if (lastLocation != null) {
                        double lat = lastLocation.getLatitude();
                        double lng = lastLocation.getLongitude();

                        Log.d("TAG", "Lat: " + abs(CLat-lat) + ", Long: " + abs(CLng-lng));
                        Log.d("TAG", "Lat: " + lat + ", Long: " + lng);

                        if (mMap != null&&(
                        abs(CLat-lat)>0.0005||
                        abs(CLng-lng)>0.0005))
                        {
                                Log.d("TAG", "Маркер добавлен");
                                // Logic to handle location object
                                LatLng sydney = new LatLng(lat, lng);

                                mMap.addMarker(new MarkerOptions().position(sydney)
                                        .title("Lat: " + lat + ", Long: " + lng+
                                                " Point:"+Point));
                                mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
                                Point++;
                                CLat=lat;
                                CLng=lng;
                        }

                    }
                }

                @Override
                public void onLocationAvailability(LocationAvailability locationAvailability) {
                    super.onLocationAvailability(locationAvailability);
                }
            };
        }

        FloatingActionButton Floc = findViewById(R.id.Floc);
        Floc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (locationIsActive == false && mFusedLocationClient != null) {
                    mFusedLocationClient.requestLocationUpdates(
                            locationRequest,
                            locationCallback,
                            null

                    );
                    Snackbar.make(view, "Tracking active", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    locationIsActive = true;
                } else if (locationIsActive == true) {
                    mFusedLocationClient.removeLocationUpdates(locationCallback);
                    locationIsActive = false;
                    Snackbar.make(view, "Tracking INACTIVE", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mFusedLocationClient != null) {
            mFusedLocationClient.removeLocationUpdates(locationCallback);
        }
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.d("TAG", "onMapReady");

        mMap = googleMap;

        // Add a marker in Sydney and move the camera
        if (ActivityCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);
    }


    @Override
    public void onMyLocationClick(@NonNull Location location) {
        Toast.makeText(this, "Current location:\n" + location, Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onMyLocationButtonClick() {
        Toast.makeText(this, "MyLocation button clicked", Toast.LENGTH_SHORT).show();
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position)/home/sergey.
        return false;
    }

    private void getLocationPermission() {
        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION};

        boolean fineLocationIsGranted = ContextCompat.checkSelfPermission(
                this.getApplicationContext(),
                FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED;


        if (fineLocationIsGranted) {
            boolean courseLocationIsGranted = ContextCompat.checkSelfPermission(
                    this.getApplicationContext(),
                    COURSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED;

            if (courseLocationIsGranted) {
                mLocationPermissionsGranted = true;
                initMap();
            } else {
                ActivityCompat.requestPermissions(
                        this,
                        permissions,
                        LOCATION_PERMISSION_REQUEST_CODE
                );
            }
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    permissions,
                    LOCATION_PERMISSION_REQUEST_CODE
            );
        }
    }

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);

    }


}