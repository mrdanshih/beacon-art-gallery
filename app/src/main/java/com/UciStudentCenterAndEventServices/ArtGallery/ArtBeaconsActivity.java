package com.UciStudentCenterAndEventServices.ArtGallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.RemoteException;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.estimote.sdk.SystemRequirementsChecker;

import org.altbeacon.beacon.BeaconConsumer;
import org.altbeacon.beacon.BeaconManager;
import org.altbeacon.beacon.BeaconParser;
import org.altbeacon.beacon.RangeNotifier;
import org.altbeacon.beacon.Beacon;
import org.altbeacon.beacon.Region;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static com.UciStudentCenterAndEventServices.ArtGallery.R.drawable.nothing_in_range;
import static com.UciStudentCenterAndEventServices.ArtGallery.R.drawable.no_image;
import static com.UciStudentCenterAndEventServices.ArtGallery.R.id.artistName;
import static com.UciStudentCenterAndEventServices.ArtGallery.R.id.beaconID;
import static org.altbeacon.beacon.Identifier.fromUuid;


public class ArtBeaconsActivity extends AppCompatActivity implements ExhibitConnection.AsyncResponse, BeaconConsumer{

    private static final String TAG = "ArtBeaconsActivity";

    private BeaconManager beaconManager;
    private Region region;


    String artGalleryUUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";

    ArrayList<ExhibitPiece> piecesList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_art_gallery);

        Toolbar toolbar = (Toolbar) findViewById(R.id.artGalleryToolbar);
        setSupportActionBar(toolbar);

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        }catch(NullPointerException e){
            System.out.println("COULD NOT SET ACTION BAR UP BUTTON");
            e.printStackTrace();
        }

        beaconManager = BeaconManager.getInstanceForApplication(this);
        beaconManager.getBeaconParsers().add(new BeaconParser().
                setBeaconLayout("m:0-3=4c000215,i:4-19,i:20-21,i:22-23,p:24-24"));
        beaconManager.bind(this);
        new ExhibitConnection(this).execute();
        //onBeaconServiceConnect();


    }
    @Override
    public void onBeaconServiceConnect() {
        beaconManager.addRangeNotifier(new RangeNotifier() {
            String artistName;
            String pieceTitle;
            String pieceInfo;
            String beaconID;
            String imageURL;
            Beacon currentBeacon, previousClosest;

            int beaconCredibility = 0;

            @Override
            public void didRangeBeaconsInRegion(Collection<Beacon> beaconList, Region region) {
                if(!beaconList.isEmpty()) {
                    Beacon nearestBeacon = beaconList.iterator().next();

                    //If discovered new closest Beacon...
                    //Basic nothing_in_range "credibility" test - If same new Beacon is seen for 2 cycles,
                    //that Beacon will be seen as a credible new Beacon.
                    if(!nearestBeacon.equals(currentBeacon)){
                        if(nearestBeacon.equals(previousClosest)){
                            if(beaconCredibility == 2) {
                                beaconCredibility = 0;

                                currentBeacon = nearestBeacon;

                                //ID 2 is the MajorID
                                System.out.println("Credible new Beacon: " + nearestBeacon.getId2());

                                ExhibitPiece artPiece = getPieceInfo(nearestBeacon);
                                artistName = artPiece.artistName;
                                pieceTitle = artPiece.title;
                                pieceInfo = artPiece.blurb;
                                beaconID = artPiece.beaconMajorId + "";
                                imageURL = artPiece.pictureUrl;

                                System.out.println(imageURL);

                                setDisplayedPieceInfo(artistName, pieceTitle, pieceInfo, beaconID, imageURL);


                            }else{
                                beaconCredibility++;
                                System.out.println("Updating credibility of " + nearestBeacon.getId2() + " to " + beaconCredibility);
                            }

                        }else{
                            System.out.println("New Beacon: " + nearestBeacon.getId2());

                            final String newID = "Found " + getPieceInfo(nearestBeacon).beaconMajorId + " as new closest art piece...";

                            setNewBeaconNotification(newID);

                            beaconCredibility = 0;
                            previousClosest = nearestBeacon;
                        }


                        //If it's the same nearest beacon, no need to do anything!
                    }else{
                        System.out.println("Same beacon: Major ID is " + nearestBeacon.getId2());

                        setNewBeaconNotification(beaconID);

                        previousClosest = nearestBeacon;

                    }

                }else{
                    System.out.println("No beacons in range.");

                    artistName = "No art piece in range!";
                    pieceTitle = "Walk up to an art piece.";
                    pieceInfo = "Searching for art piece beacons...";
                    beaconID = "";
                    imageURL = null;

                    setDisplayedPieceInfo(artistName, pieceTitle, pieceInfo, beaconID, imageURL);

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

    private void setNewBeaconNotification(final String newID){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.beaconID)).setText(newID);
            }
        });
    }

    private void setDisplayedPieceInfo(final String artistName, final String pieceTitle, final String pieceInfo, final String beaconID, final String imageURL){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(imageURL != null){
                    DownloadImageTask task = new DownloadImageTask((ImageView) findViewById(R.id.artPieceImage));
                    task.execute(imageURL);

                }else{
                    ((ImageView) findViewById(R.id.artPieceImage)).setImageResource(nothing_in_range);
                }

                ((TextView) findViewById(R.id.artistName)).setText(artistName);
                ((TextView) findViewById(R.id.beaconID)).setText(beaconID);
                ((TextView) findViewById(R.id.artPieceName)).setText(pieceTitle);
                ((TextView) findViewById(R.id.artPieceInfo)).setText(pieceInfo);

            }
        });

    }

    @Override
    public void processFinish(ArrayList<ExhibitPiece> result){
        piecesList = result;
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




    private ExhibitPiece getPieceInfo(Beacon beaconDetails){
        ExhibitPiece associatedPiece = new ExhibitPiece(0, 0, "Unknown Piece!", 0,
                                                        "Major ID is " + beaconDetails.getId2(),
                                                        "","","","",true,0);

        for(ExhibitPiece piece: piecesList){
            if (piece.beaconMajorId == beaconDetails.getId2().toInt()){
                associatedPiece = piece;
                break;
            }
        }

        return associatedPiece;

    }


    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;

        public DownloadImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected void onPreExecute() {
            bmImage.setVisibility(View.INVISIBLE);
            findViewById(R.id.loadImageSpinner).setVisibility(View.VISIBLE);
        }

        protected Bitmap doInBackground(String[] url) {
            //There's only one URL - the one passed in.
            String urldisplay = url[0];

            Bitmap imageBitmap = null;

            try {
                InputStream in = new java.net.URL(urldisplay).openStream();
                imageBitmap = BitmapFactory.decodeStream(in);

            } catch (Exception e) {
                Log.e("Error", e.getMessage());
                e.printStackTrace();

            }
            return imageBitmap;
        }

        protected void onPostExecute(Bitmap result) {
            if(result != null) {
                bmImage.setImageBitmap(result);
            }else{
                bmImage.setImageResource(no_image);
            }

            findViewById(R.id.loadImageSpinner).setVisibility(View.INVISIBLE);
            bmImage.setVisibility(View.VISIBLE);
        }
    }




}



