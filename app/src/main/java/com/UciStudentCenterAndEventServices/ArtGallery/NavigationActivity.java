package com.UciStudentCenterAndEventServices.ArtGallery;

import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Region;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.UUID;

import static android.os.Build.ID;
import static org.altbeacon.beacon.Identifier.fromUuid;


public class NavigationActivity extends AppCompatActivity implements BeaconConsumer {
    private static final String TAG = "NavigationActivity";
    String artGalleryUUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    private BeaconManager beaconManager;
    private Region region;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        beaconManager = BeaconManager.getInstanceForApplication(this);
        //This parser code allows for the detection of Estimote beacons.
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));

        beaconManager.setForegroundScanPeriod(1000);
        beaconManager.bind(this);
        //

    }

    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(new RangeNotifier() {

            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beaconList, Region region) {
                if (!beaconList.isEmpty()) {
                    Iterator<Beacon> iterator = beaconList.iterator();
                    Beacon nearestBeacon = iterator.next();
                    System.out.println("BEGIN");
                    System.out.println(nearestBeacon.getId2() + " " + nearestBeacon.getDistance());
                    while (iterator.hasNext()){
                        Beacon b = iterator.next();
                        System.out.println(b.getId2() + " " + b.getDistance());

                    }

                    System.out.println("END");
                    Log.d(TAG, "BEACON 1!: " + nearestBeacon.getId2());

                    String major1 = nearestBeacon.getId2().toString();
                    String distance1 = new DecimalFormat("#.###").format(nearestBeacon.getDistance());

                    setInfo(R.id.navBeaconID, R.id.navDistance, major1, distance1);


                    if(iterator.hasNext()){
                        Beacon secondNearest = iterator.next();
                        Log.d(TAG, "BEACON 2!: " + secondNearest.getId2());
                        String major2 = secondNearest.getId2().toString();
                        String distance2 = new DecimalFormat("#.###").format(secondNearest.getDistance());
                        setInfo(R.id.navBeacon2ID, R.id.navDistance2, major2, distance2);

                    }else{
                        setInfo(R.id.navBeacon2ID, R.id.navDistance2, "NONE", "NONE");
                    }

                }else{
                    setInfo(R.id.navBeaconID, R.id.navDistance, "NONE", "NONE");
                    setInfo(R.id.navBeacon2ID, R.id.navDistance2, "NONE", "NONE");
                }
            }


        });

        region = new Region("Art Gallery Region", fromUuid(UUID.fromString(artGalleryUUID)), null, null);

        try{
            beaconManager.startRangingBeaconsInRegion(region);
        }catch(RemoteException e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();


        if(beaconManager.isBound(this)){
            beaconManager.setBackgroundMode(false);
            Log.d(TAG, "Starting Beacon ranging.");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if(beaconManager.isBound(this)){
            beaconManager.setBackgroundMode(true);
            Log.d(TAG, "Stopping Beacon ranging.");
        }




    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.unbind(this);
    }




    private void setInfo(final int idViewID, final int distanceViewID, final String ID, final String distance){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(idViewID)).setText(ID);
                ((TextView) findViewById(distanceViewID)).setText(distance);
            }
        });
    }


}

