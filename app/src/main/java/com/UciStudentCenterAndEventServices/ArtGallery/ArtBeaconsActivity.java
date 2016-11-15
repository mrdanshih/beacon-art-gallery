package com.UciStudentCenterAndEventServices.ArtGallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;


import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.UciStudentCenterAndEventServices.ArtGallery.R.drawable.nothing_in_range;
import static com.UciStudentCenterAndEventServices.ArtGallery.R.drawable.no_image;


public class ArtBeaconsActivity extends AppCompatActivity implements ExhibitConnection.AsyncResponse{

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

        new ExhibitConnection(this).execute();
        doBeaconSearching();


    }

    private void doBeaconSearching(){
        beaconManager = new BeaconManager(this);
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            String artistName;
            String pieceTitle;
            String pieceInfo;
            String beaconID;
            String imageURL;
            Beacon currentBeacon, previousClosest;

            int beaconCredibility = 0;

            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> beaconList) {
                if(!beaconList.isEmpty()) {
                    Beacon nearestBeacon = beaconList.get(0);

                    //If discovered new closest Beacon...
                    //Basic nothing_in_range "credibility" test - If same new Beacon is seen for 2 cycles,
                    //that Beacon will be seen as a credible new Beacon.
                    if(!nearestBeacon.equals(currentBeacon)){
                        if(nearestBeacon.equals(previousClosest)){
                            if(beaconCredibility == 2) {
                                beaconCredibility = 0;

                                currentBeacon = nearestBeacon;
                                System.out.println("Credible new Beacon: " + nearestBeacon.getMajor());

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
                                System.out.println("Updating credibility of " + nearestBeacon.getMajor() + " to " + beaconCredibility);
                            }

                        }else{
                            System.out.println("New Beacon: " + nearestBeacon.getMajor());

                            String newID = "Found " + getPieceInfo(nearestBeacon).beaconMajorId + " as new closest art piece...";
                            ((TextView) findViewById(R.id.beaconID)).setText(newID);

                            beaconCredibility = 0;
                            previousClosest = nearestBeacon;
                        }


                        //If it's the same nearest nothing_in_range, no need to do anything!
                    }else{
                        System.out.println("Same nothing_in_range: Major ID is " + nearestBeacon.getMajor());
                        ((TextView) findViewById(R.id.beaconID)).setText(beaconID);
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

        //Beacons to search for are in this region - with the specified UUID.
        region = new Region("Art Gallery Region", UUID.fromString(artGalleryUUID), null, null);
    }


    private void setDisplayedPieceInfo(String artistName, String pieceTitle, String pieceInfo, String beaconID, String imageURL){
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

    @Override
    public void processFinish(ArrayList<ExhibitPiece> result){
        piecesList = result;
    }


    @Override
    protected void onResume() {
        super.onResume();


        if (!SystemRequirementsChecker.checkWithDefaultDialogs(this)) {
            Log.e(TAG, "Can't scan for beacons, some pre-conditions were not met");
            Log.e(TAG, "Read more about what's required at: http://estimote.github.io/Android-SDK/JavaDocs/com/estimote/sdk/SystemRequirementsChecker.html");
            Log.e(TAG, "If this is fixable, you should see a popup on the app's screen right now, asking to enable what's necessary");
        } else if(beaconManager != null) {
            Log.d(TAG, "Starting BeaconManager ranging.");

            beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
                @Override
                //Starts the ranging of the beacons
                public void onServiceReady() {
                    beaconManager.startRanging(region);
                }
            });
        }
    }

    @Override
    protected void onPause() {
        if(beaconManager != null) {
            beaconManager.stopRanging(region);
            Log.d(TAG, "Stopping Beacon ranging.");
        }

        super.onPause();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }




    private ExhibitPiece getPieceInfo(Beacon beaconDetails){
        ExhibitPiece associatedPiece = new ExhibitPiece(0, 0, "Unknown Piece!", 0,
                                                        "Major ID is " + beaconDetails.getMajor(),
                                                        "","","","",true,0);

        for(ExhibitPiece piece: piecesList){
            if (piece.beaconMajorId == beaconDetails.getMajor()){
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



