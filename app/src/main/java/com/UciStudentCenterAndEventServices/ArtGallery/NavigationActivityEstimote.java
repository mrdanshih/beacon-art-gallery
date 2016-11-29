package com.UciStudentCenterAndEventServices.ArtGallery;

import android.os.Bundle;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import static org.altbeacon.beacon.Identifier.fromUuid;


public class NavigationActivityEstimote extends AppCompatActivity {
    private static final String TAG = "NavigationActivity";
    String artGalleryUUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    private BeaconManager beaconManager;
    private Region region;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        beaconManager = new BeaconManager(this);

        doBeaconSearching();

        //

    }

    private void doBeaconSearching() {
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> beaconList) {
                if (!beaconList.isEmpty()) {
                    Iterator<Beacon> iterator = beaconList.iterator();
                    Beacon nearestBeacon = iterator.next();
                    System.out.println("BEGIN");
                    System.out.println(nearestBeacon.getMajor() + " " + Utils.computeAccuracy(nearestBeacon));
                    while (iterator.hasNext()){
                        Beacon b = iterator.next();
                        System.out.println(b.getMajor() + " " + Utils.computeAccuracy(b));

                    }

                    System.out.println("END");
                    Log.d(TAG, "BEACON 1!: " + nearestBeacon.getMajor());

                    String major1 = nearestBeacon.getMajor() + "";
                    String distance1 = new DecimalFormat("#.###").format(Utils.computeAccuracy(nearestBeacon));

                    setInfo(R.id.navBeaconID, R.id.navDistance, major1, distance1);


                    if(iterator.hasNext()){
                        Beacon secondNearest = iterator.next();
                        Log.d(TAG, "BEACON 2!: " + secondNearest.getMajor());
                        String major2 = secondNearest.getMajor() + "";
                        String distance2 = new DecimalFormat("#.###").format(secondNearest.getMajor());
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
        region = new Region("Art Gallery Region", (UUID.fromString(artGalleryUUID)), null, null);

    }

    @Override
    protected void onResume() {
        super.onResume();


        if(beaconManager != null && region != null) {
            beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
                @Override
                //Starts the ranging of the beacons
                public void onServiceReady() {
                    beaconManager.startRanging(region);
                }
            });

        }

        Log.d(TAG, "Starting Beacon ranging.");

    }

    @Override
    protected void onPause(){
        super.onPause();

        if(beaconManager != null && region != null) {
            beaconManager.stopRanging(region);
            Log.d(TAG, "Stopping Beacon ranging.");
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.disconnect();

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

