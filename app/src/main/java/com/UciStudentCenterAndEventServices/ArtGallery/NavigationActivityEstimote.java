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


import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;



class PositionBeacon{
    private int majorID;
    private double x;
    private double y;


    public PositionBeacon(int majorID, double x, double y){
        this.majorID = majorID;
        this.x = x;
        this.y = y;

    }

    public int getMajorID(){
        return majorID;
    }

    public double getX(){
        return x;
    }

    public double getY(){
        return y;
    }
}

class Point{
    double x, y;

    public Point(double x, double y){
        this.x = x;
        this.y = y;
    }

    public double getX(){
        return x;
    }

    public double getY(){
        return y;
    }
}

public class NavigationActivityEstimote extends AppCompatActivity {
    private static final String TAG = "NavigationActivity";
    String artGalleryUUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    PositionBeacon b = new PositionBeacon(27031, 0, 0);

    private PositionBeacon[] beaconList = {new PositionBeacon(27031, 0, 0), new PositionBeacon(30586, -1, -1),
                                            new PositionBeacon(65193, -1, -2.4), new PositionBeacon(34821, 0, 3.2),
                                            new PositionBeacon(52757, -0.5, 5), new PositionBeacon(7847, -1.7, 3.8),
                                            new PositionBeacon(43243, -3, 3.8)};

    private BeaconManager beaconManager;
    private Region region;

    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation);

        beaconManager = new BeaconManager(this);

        doBeaconSearching();


    }

    private void doBeaconSearching() {
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> beaconList) {
                if (!beaconList.isEmpty()) {
                    Iterator<Beacon> iterator = beaconList.iterator();
                    Beacon nearestBeacon = beaconList.get(0);
                    System.out.println("BEGIN");
                    while (iterator.hasNext()){
                        Beacon b = iterator.next();
                        System.out.println(b.getMajor() + " " + Utils.computeAccuracy(b));

                    }

                    System.out.println("END");
                    Log.d(TAG, "BEACON 1!: " + nearestBeacon.getMajor());

                    String major1 = nearestBeacon.getMajor() + "";
                    String distance1 = new DecimalFormat("#.###").format(Utils.computeAccuracy(nearestBeacon));

                    setInfo(R.id.navBeaconID, R.id.navDistance, major1, distance1);


                    if(beaconList.size() >= 2){
                        Beacon secondNearest = iterator.next();
                        Log.d(TAG, "BEACON 2!: " + secondNearest.getMajor());
                        String major2 = secondNearest.getMajor() + "";
                        String distance2 = new DecimalFormat("#.###").format(secondNearest.getMajor());
                        setInfo(R.id.navBeacon2ID, R.id.navDistance2, major2, distance2);

                    }else{
                        setInfo(R.id.navBeacon2ID, R.id.navDistance2, "NONE", "NONE");
                    }


                    Beacon[] threeBeacons;
                    //TODO - make 3 beacon sublist, pass in to the getCoordinate method, and test.


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


    private Point getCoordinate(Beacon[] threeBeaconsList){
        double distanceA, distanceB, distanceC;
        double pointA1, pointA2, pointB1, pointB2, pointC1, pointC2;

        ArrayList<PositionBeacon> selectedBeacons = new ArrayList<PositionBeacon>();

        double[] distancesList = {0,0,0};

        for(int i = 0; i < threeBeaconsList.length; i++){
            selectedBeacons.add(getCorrespondingPositionBeacon(threeBeaconsList[i]));
            distancesList[i] = Utils.computeAccuracy(threeBeaconsList[i]);
        }

        if(selectedBeacons.size() == 3) {
            distanceA = distancesList[0];
            distanceB = distancesList[1];
            distanceC = distancesList[2];

            pointA1 = selectedBeacons.get(0).getX();
            pointA2 = selectedBeacons.get(0).getY();

            pointB1 = selectedBeacons.get(0).getX();
            pointB2 = selectedBeacons.get(0).getY();

            pointC1 = selectedBeacons.get(0).getX();
            pointC2 = selectedBeacons.get(0).getY();


            double w, z, x, y, y2;
            w = distanceA * distanceA - distanceB * distanceB - pointA1 * pointA1 - pointA2 * pointA2 + pointB1 * pointB1 + pointB2 * pointB2;

            z = distanceB * distanceB - distanceC * distanceC - pointB1 * pointB1 - pointB2 * pointB2 + pointC1 * pointC1 + pointC2 * pointC2;

            x = (w * (pointC2 - pointB2) - z * (pointB2 - pointA2)) / (2 * ((pointB1 - pointA1) * (pointC1 - pointB2) - (pointC1 - pointB1) * (pointB2 - pointA2)));

            y = (w - 2 * x * (pointB1 - pointA1)) / (2 * (pointB2 - pointA2));

            y2 = (z - 2 * x * (pointC1 - pointB1)) / (2 * (pointC1 - pointB2));

            y = (y + y2) / 2;

            return new Point(x, y);
        }else{
            return new Point(-999,-999);
        }
    }

    private PositionBeacon getCorrespondingPositionBeacon(Beacon beacon){
        for(PositionBeacon positionBeacon: beaconList){
            if(positionBeacon.getMajorID() == beacon.getMajor()){
                return positionBeacon;
            }
        }
    }

}



