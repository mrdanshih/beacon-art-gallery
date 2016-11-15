package com.UciStudentCenterAndEventServices.ArtGallery;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        internetCheck();


    }

    public void startArtGallery(View view){
        Intent artGalleryIntent = new Intent(this, ArtBeaconsActivity.class);
        startActivity(artGalleryIntent);

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


}
