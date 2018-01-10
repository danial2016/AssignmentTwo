package com.example.daniel.assignmenttwo;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Locale;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBar;

    public static ServerConnection sc;
    boolean bound = false;
    private ServiceConnection serviceConn;

    private GoogleMap mMap;
    private SupportMapFragment mapFragment;
    public static Protocol mProtocol;

    boolean mapReady = false;
    boolean addMarker = false;
    boolean boundToService = false;
    private Intent serviceIntent;
    Resources res;

    private int count = 0;

    private LocationManager locationManager;
    private LocationListener locList = new LocList();
    private double latitude;
    private double longitude;
    private Location location ;
    public static final int REQUEST_ACCESS_FINE_LOCATION = 99;
    public static final int REQUEST_ACCESS_COARSE_LOCATION = 98;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mProtocol = new Protocol();
        mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map_fragment);
        mapFragment.getMapAsync(MainActivity.this);
        Locale.setDefault(Locale.ENGLISH);
        initializeComponents();
    }


    @Override
    protected void onRestart() {
        super.onRestart();
        if(sc.newMarker == true){
            String[][] mat = sc.getLocationsMatrix();
            if(mat != null){
                for(int i = 0; i < mat.length; i++){
                    LatLng newMarker = new LatLng(Double.parseDouble(mat[i][2]), Double.parseDouble(mat[i][1]));
                    addMarker(newMarker, mat[i][0]);
                }
                Toast.makeText(getApplicationContext(), R.string.newMarker, Toast.LENGTH_SHORT).show();
                sc.newMarker = false;
            }else{
                Toast.makeText(getApplicationContext(), R.string.nullMatrix, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private class LocList implements LocationListener {
        // Called when the location has changed.
        public void onLocationChanged(Location location) {
            latitude = location.getLatitude();
            longitude = location.getLongitude();
            Log.d("onLocChanged", "Lng=" + longitude + ",Lat=" + latitude);
        }

        // Called when the provider is disabled by the user.
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        // Called when the provider is enabled by the user.
        public void onProviderEnabled(String provider) {
        }

        // Called when the provider status changes.
        public void onProviderDisabled(String provider) {
        }
    }

    public Location getLocation() {
        try {
            locationManager = (LocationManager) getSystemService(MainActivity.this.LOCATION_SERVICE);
            boolean gpsStatus = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean networkStatus = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
            Log.i("GPS status: ", gpsStatus + "");
            Log.i("Network status: ", networkStatus + "");


            if (gpsStatus == false && networkStatus == false) {
                return null;
            } else {
                //Use NETWORK_PROVIDER to get location because many times GPS will give null latitude and longitude
                if (networkStatus == true) {
                    if (locationManager != null) {
                        if (ContextCompat.checkSelfPermission(this,
                                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                                    REQUEST_ACCESS_COARSE_LOCATION);
                        } else {
                            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locList);
                            Log.d("Network Enabled", "Network Enabled");
                            location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }else if (gpsStatus == true) {
                    if (locationManager != null) {
                        if (ContextCompat.checkSelfPermission(this,
                                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_DENIED) {
                            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                                    REQUEST_ACCESS_FINE_LOCATION);
                        } else {
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locList);
                            Log.d("GPS Enabled", "GPS Enabled");
                            location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return location;
    }

    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ACCESS_FINE_LOCATION:
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locList);
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locList);
                }
                break;
            case REQUEST_ACCESS_COARSE_LOCATION:
                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) ==
                        PackageManager.PERMISSION_GRANTED) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, locList);
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locList);
                }
                break;

        }
    }

    private void initializeComponents() {
        drawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        actionBar = new ActionBarDrawerToggle(this, drawerLayout, R.string.Open, R.string.Close);
        actionBar.setDrawerIndicatorEnabled(true);
        drawerLayout.addDrawerListener(actionBar);
        actionBar.syncState();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        final NavigationView navView = (NavigationView) findViewById(R.id.navView);
        navView.setNavigationItemSelectedListener
                (new NavigationView.OnNavigationItemSelectedListener() {

                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        int id = item.getItemId();
                        if(id == R.id.itemConnect){
                            serviceConn = new ServiceConn();
                            serviceIntent = new Intent(getApplicationContext(), ServerConnection.class);
                            boundToService = bindService(serviceIntent, serviceConn, 0);
                            startService(serviceIntent);
                        }
                        if(id == R.id.itemRegister){
                            if(sc.connectedToServer == true){
                                if (boundToService) {
                                    sc.sendMessage(mProtocol.getCurrentGroups());
                                    Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
                                    int nbrOfGroups = sc.getNbrOfGroups();
                                    intent.putExtra("nbrOfGroups", nbrOfGroups);
                                    String[] array = sc.getListOfGroups();
                                    intent.putExtra("listOfGroups", array);
                                    location = getLocation();
                                    latitude = location.getLatitude();
                                    longitude = location.getLongitude();
                                    intent.putExtra("latitude", latitude);
                                    intent.putExtra("longitude", longitude);
                                    startActivity(intent);

                                    //sc.sendMessage(mProtocol.sendMyPosition(""+loc.getLongitude(), ""+loc.getLatitude()));
                                }else{
                                    Toast.makeText(getApplicationContext(), R.string.notBound, Toast.LENGTH_SHORT).show();
                                }
                            }else{
                                Toast.makeText(getApplicationContext(), R.string.firstConnectToServer, Toast.LENGTH_SHORT).show();
                            }
                        }
                        if(id == R.id.itemUnregister){
                            res = getResources();
                            if(RegistrationActivity.ableToUnregister == true){
                                sc.sendMessage(mProtocol.unregisterFromGroup(RegistrationActivity.currentID));
                                Toast.makeText(getApplicationContext(), res.getString(R.string.youHaveUnregistered), Toast.LENGTH_SHORT).show();
                                RegistrationActivity.ableToUnregister = false;
                                mMap.clear();
                                Location location = getLocation();
                                LatLng home = new LatLng(location.getLatitude(), location.getLongitude());
                                res = getResources();
                                addMarker(home, res.getString(R.string.myPosition));
                            }else{
                                StringBuilder builder = new StringBuilder();
                                builder.append(res.getString(R.string.youAreNotRegistered));
                                showMessage(res.getString(R.string.note), builder.toString());
                            }
                        }
                        if(id == R.id.itemEnglish){
                            setLocale("en");
                            Toast.makeText(getApplicationContext(), R.string.languageChanged, Toast.LENGTH_SHORT).show();
                        }
                        if(id == R.id.itemSwedish){
                            setLocale("sv");
                            Toast.makeText(getApplicationContext(), R.string.languageChanged, Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                }
        );
    }

    @Override
    public void onBackPressed() {

        android.os.Process.killProcess(android.os.Process.myPid());
        // This above line close correctly
    }


    public void setLocale(String lang) {
        Log.i("LanguageTag", lang);
        Locale myLocale = new Locale(lang); //Locale.forLanguageTag(lang);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        finish();
        Intent refresh = new Intent(this, MainActivity.class);
        startActivity(refresh);
    }


    public void showMessage(String title, String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return actionBar.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mapReady = true;
        Location location = getLocation();
        LatLng home = new LatLng(location.getLatitude(), location.getLongitude());
        res = getResources();
        addMarker(home, res.getString(R.string.myPosition));
    }

    private void addMarker(LatLng latLng, String name) {
        MarkerOptions mo = new MarkerOptions().position(latLng).title(name);
        addMarker = true;
        mMap.addMarker(mo);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 9));
    }

    private class ServiceConn implements ServiceConnection {
        public void onServiceConnected(ComponentName arg0, IBinder binder) {
            ServerConnection.LocalBinder ls = (ServerConnection.LocalBinder) binder;
            sc = ls.getService();
            bound = true;
        }

        public void onServiceDisconnected(ComponentName arg0) {
            bound = false;
        }
    }
}
