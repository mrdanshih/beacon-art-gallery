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
import android.widget.ScrollView;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;


import com.ms.square.android.expandabletextview.ExpandableTextView;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static com.UciStudentCenterAndEventServices.ArtGallery.R.drawable.beacon;
import static com.UciStudentCenterAndEventServices.ArtGallery.R.drawable.nothing_in_range;
import static com.UciStudentCenterAndEventServices.ArtGallery.R.drawable.no_image;


public class ArtBeaconsActivity extends AppCompatActivity implements ExhibitConnection.AsyncResponse {

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
        getSupportActionBar().setElevation(3);

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        }catch(NullPointerException e){
            System.out.println("COULD NOT SET ACTION BAR UP BUTTON");
            e.printStackTrace();
        }

        beaconManager = new BeaconManager(this);
        
        //Download the exhibit and art piece information.
        new ExhibitConnection(this).execute();

        doBeaconSearching();

    }
    
 
    private void doBeaconSearching(){
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            String artistName;
            String artistInfo;
            String pieceTitle;
            String pieceInfo;
            String beaconID;
            String imageURL;
            Beacon currentBeacon, previousClosest;

            int beaconCredibility = 0;
            int emptyCredibility = 0;

            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> beaconList) {
                if(!beaconList.isEmpty()) {
                    emptyCredibility = 0;
                    Beacon nearestBeacon = beaconList.iterator().next();

                    //If discovered new closest Beacon...
                    //Basic nothing_in_range "credibility" test - If same new Beacon is seen for 2 cycles,
                    //that Beacon will be seen as a credible new Beacon.
                    if(!nearestBeacon.equals(currentBeacon)){
                        if(nearestBeacon.equals(previousClosest)){
                            if(beaconCredibility == 2) {
                                beaconCredibility = 0;

                                currentBeacon = nearestBeacon;

                          
                                Log.d(TAG, "Credible new Beacon: " + nearestBeacon.getMajor());

                                ExhibitPiece artPiece = getPieceInfo(nearestBeacon);
                                artistName = artPiece.artistName;
                                artistInfo = artPiece.artistBlurb;
                                pieceTitle = artPiece.title;
                                pieceInfo = artPiece.blurb;
                                beaconID = artPiece.beaconMajorId + "";
                                imageURL = artPiece.pictureUrl;


                                setExpandableInfoVisiblity(View.VISIBLE);
                                setDisplayedPieceInfo(artistName, artistInfo, pieceTitle, pieceInfo, beaconID, imageURL);


                            }else{
                                beaconCredibility++;
                                Log.d(TAG, "Updating credibility of " + nearestBeacon.getMajor() + " to " + beaconCredibility);
                            }

                        }else{
                            Log.d(TAG, "New Beacon: " + nearestBeacon.getMajor());

                            final String newID = "Found " + getPieceInfo(nearestBeacon).beaconMajorId + " as new closest art piece...";

                            setNewBeaconNotification(newID);

                            beaconCredibility = 0;
                            previousClosest = nearestBeacon;
                        }


                        //If it's the same nearest beacon, no need to do anything!
                    }else{
                        Log.d(TAG, "Same beacon: Major ID is " + nearestBeacon.getMajor());

                        setNewBeaconNotification(beaconID);

                        previousClosest = nearestBeacon;

                    }

                }else if(emptyCredibility == 3){
                    emptyCredibility = 0;
                    currentBeacon = null;
                    Log.d(TAG, "No beacons in range for 3 cycles!");

                    artistName = "No art piece in range!";
                    artistInfo = "";
                    pieceTitle = "Walk up to an art piece.";
                    pieceInfo = "Searching for art piece beacons...";
                    beaconID = "";
                    imageURL = null;

                    setExpandableInfoVisiblity(View.GONE);
                    setDisplayedPieceInfo(artistName, artistInfo, pieceTitle, pieceInfo, beaconID, imageURL);

                }else{
                    emptyCredibility++;
                    Log.d(TAG, "No beacons in range... for " + emptyCredibility + " cycles");

                }
            }
        });

        region = new Region("Art Gallery Region", UUID.fromString(artGalleryUUID), null, null);
        
    }

    private void setExpandableInfoVisiblity(final int visibility){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ScrollView expandableInfoView = (ScrollView) (findViewById(R.id.expandable_info));

                expandableInfoView.setVisibility(visibility);
            }
        });
    }

    private void setNewBeaconNotification(final String newID){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                ((TextView) findViewById(R.id.beaconID)).setText(newID);
            }
        });
    }

    private void setDisplayedPieceInfo(final String artistName, final String artistInfo, final String pieceTitle, final String pieceInfo, final String beaconID, final String imageURL){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(imageURL != null){
                    DownloadImageTask task = new DownloadImageTask((ImageView) findViewById(R.id.artPieceImage));
                    task.execute(imageURL);

                }else{
                    ((ImageView) findViewById(R.id.artPieceImage)).setImageResource(nothing_in_range);
                }


                ((TextView) findViewById(R.id.artPieceName)).setText(pieceTitle);
                ((TextView) findViewById(R.id.artistName)).setText(artistName);
                ((TextView) findViewById(R.id.artistInfoView).findViewById(R.id.title)).setText("About the artist");
                ((TextView) findViewById(R.id.artworkInfoView).findViewById(R.id.title)).setText("About the piece");

                ExpandableTextView artistNameExpandable = (ExpandableTextView) findViewById(R.id.artistInfoView).findViewById(R.id.expand_text_view);
                artistNameExpandable.setText(artistInfo);

                ExpandableTextView artInfoExpandable = (ExpandableTextView) findViewById(R.id.artworkInfoView).findViewById(R.id.expand_text_view);
                artInfoExpandable.setText(pieceInfo);

                ((TextView) findViewById(R.id.beaconID)).setText(beaconID);



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

        if(beaconManager != null) {
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

        if(beaconManager != null) {
            beaconManager.stopRanging(region);
            Log.d(TAG, "Stopping Beacon ranging.");
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        beaconManager.disconnect();
        
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



