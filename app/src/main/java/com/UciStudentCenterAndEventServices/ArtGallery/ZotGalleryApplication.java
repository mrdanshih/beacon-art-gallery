package com.UciStudentCenterAndEventServices.ArtGallery;

import android.Manifest;
import android.app.Application;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;


//
// Running into any issues? Drop us an email to: contact@estimote.com
//

public class ZotGalleryApplication extends Application {
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;



    @Override
    public void onCreate() {
        super.onCreate();


        // uncomment to enable debug-level logging
        // it's usually only a good idea when troubleshooting issues with the Estimote SDK
//        EstimoteSDK.enableDebugLogging(true);
    }
}
