package com.UciStudentCenterAndEventServices.ArtGallery;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class MainActivity extends AppCompatActivity implements ActivityCompat.OnRequestPermissionsResultCallback {
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationPermissionCheck();
        turnOnBluetooth();
        turnOnLocation();
        internetCheck();
    }



    public void startArtGallery(View view) {
        Intent artGalleryIntent = new Intent(this, ArtBeaconsActivity.class);
        startActivity(artGalleryIntent);
    }

    public void startNavigationActivity(View view){
        Intent navIntent = new Intent(this, NavigationActivity.class);
        startActivity(navIntent);
    }

    private void locationPermissionCheck(){


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

                final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("Grant location access permissions");
                builder.setMessage("This app uses Bluetooth and location services to detect beacons.");
                builder.setPositiveButton(android.R.string.ok, null);
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
                    }

                });
                builder.show();
            }
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        View mainView = findViewById(R.id.activity_main2);

        if (requestCode == PERMISSION_REQUEST_FINE_LOCATION) {
            // Request for location
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission has been granted.
                Snackbar.make(mainView, "Location permission granted.",
                        Snackbar.LENGTH_SHORT)
                        .show();

            } else {
                // Permission request was denied.
                final AlertDialog noLocationDialog = new AlertDialog.Builder(this).create();
                noLocationDialog.setTitle("Location permission denied");
                noLocationDialog.setMessage("This app requires location permissions to detect nearby Beacons for its functionality.");
                noLocationDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Try again", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        locationPermissionCheck();
                    }
                });
                noLocationDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Quit app", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });

                noLocationDialog.show();

            }
        }

    }

    private void internetCheck(){
        //Check if app has network connectivity...
        if(isNetworkAvailable()){
            System.out.println("CONNECTED TO INTERNET");
            //Does the fetching of the database (network process) using Async task


        }else{
            System.out.println("NO INTERNET CONNECTION");
            final AlertDialog noInternetDialog = new AlertDialog.Builder(this).create();
            noInternetDialog.setTitle("No internet connection.");
            noInternetDialog.setMessage("This app requires an internet connection to function. Connect to the internet and try again.");
            noInternetDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Try again", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    internetCheck();
                }
            });
            noInternetDialog.setButton(AlertDialog.BUTTON_NEGATIVE, "Quit app", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });

            noInternetDialog.show();

        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

        return activeNetworkInfo != null;
    }

    private void turnOnBluetooth(){
        final View mainView = findViewById(R.id.activity_main2);

        final BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (!adapter.isEnabled()){
            final AlertDialog locationOffDialog = new AlertDialog.Builder(this).create();
            locationOffDialog.setTitle("Bluetooth is off");
            locationOffDialog.setMessage("This app uses Bluetooth for Beacon detection. Turn it on?");
            locationOffDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    adapter.enable();
                    Snackbar.make(mainView, "Turned on Bluetooth.",
                            Snackbar.LENGTH_SHORT)
                            .show();
                }
            });

            locationOffDialog.show();


        }
    }

    private void turnOnLocation(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        boolean gps_enabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

        if(!gps_enabled){
            final AlertDialog locationOffDialog = new AlertDialog.Builder(this).create();
            locationOffDialog.setTitle("Location is off");
            locationOffDialog.setMessage("This app uses location for Beacon detection. Turn it on?");
            locationOffDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Go to Settings", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    Intent myIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    startActivity(myIntent);
                }
            });

            locationOffDialog.show();
        }

    }

}
