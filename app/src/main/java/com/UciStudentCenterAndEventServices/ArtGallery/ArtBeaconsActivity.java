package com.UciStudentCenterAndEventServices.ArtGallery;

import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
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
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.UciStudentCenterAndEventServices.ArtGallery.R.drawable.nothing_in_range;
import static com.UciStudentCenterAndEventServices.ArtGallery.R.drawable.no_image;
import static com.UciStudentCenterAndEventServices.ArtGallery.R.id.beaconID;
import static com.indooratlas.android.sdk._internal.ff.L;


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
        getSupportActionBar().setElevation(5);

        try {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        } catch (NullPointerException e) {
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
                //If the list of nearby Beacons is not empty, grab the first nearest beacon.

                if(!beaconList.isEmpty()) {
                    findViewById(R.id.initialSearchSpinner).setVisibility(View.GONE);
                    emptyCredibility = 0;
                    Beacon nearestBeacon = beaconList.iterator().next();



                    if(!nearestBeacon.equals(currentBeacon)){
                    /*If discovered new closest Beacon...
                    Basic nothing_in_range "credibility" test - If same new Beacon is seen for 2 cycles,
                    that Beacon will be seen as a credible new Beacon.
                    */

                        ExhibitPiece artPiece = getPieceInfo(nearestBeacon);

                        /* Get the art piece info for the closest Beacon
                        If there is no art piece association, then it will get an ExhibitPiece with
                        prespecified values for unknown Beacons, allowing for the detection of such
                        */


                        if(nearestBeacon.equals(previousClosest)){
                            /* Credibility check. If the closest Beacon in this cycle is the same as the
                            Beacon in the last cycle, then increase the credibility rating.
                            If the credibility rating is high enough (seen same new Beacon for 3 cycles),
                            then it is now the current art piece.
                             */
                            if(beaconCredibility == 2) {
                                beaconCredibility = 0;

                                currentBeacon = nearestBeacon;

                          
                                Log.d(TAG, "Credible new Beacon: " + nearestBeacon.getMajor());

                                artistName = artPiece.artistName;
                                artistInfo = artPiece.artistBlurb;
                                pieceTitle = artPiece.title;
                                pieceInfo = artPiece.blurb;
                                beaconID = nearestBeacon.getMajor() + "";
                                imageURL = artPiece.pictureUrl;

                                //Sets the displayed Beacon info on the UI
                                setExpandableInfoVisiblity(View.VISIBLE);
                                setDisplayedPieceInfo(artistName, artistInfo, pieceTitle, pieceInfo, beaconID, imageURL);


                            }else{
                                beaconCredibility++;
                                Log.d(TAG, "Updating credibility of " + nearestBeacon.getMajor() + " to " + beaconCredibility);
                            }

                        }else{
                            /* Detection of new Beacons different from the last seen closest Beacon.
                                If it's not an art piece, then the app will display the major ID at
                                the bottom of the screen. If it's an art piece, the app will display
                                the name (up to 10 chars) of the new art piece that was detected
                             */

                            Log.d(TAG, "New Beacon: " + nearestBeacon.getMajor());
                            String beaconNameOrID = "";

                            if(artPiece.title.equals("Unknown Piece")){
                                beaconNameOrID = nearestBeacon.getMajor() + "";
                            }else{
                                if(artPiece.title.length() < 10){
                                    beaconNameOrID = "\"" + artPiece.title + "\"";
                                }else{
                                    beaconNameOrID = "\"" + artPiece.title.substring(0, 10) +"...\"";
                                }

                            }

                            final String newID = "Found " + beaconNameOrID + " as new closest art piece...";
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
                    /*If no Beacons seen for 4 cycles, then update the current display
                        to show that no beacons have been detected.
                     */
                    findViewById(R.id.initialSearchSpinner).setVisibility(View.VISIBLE);
                    beaconCredibility = 0;
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
                    setNewBeaconNotification("Searching for beacons...");

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
                ((TextView) findViewById(beaconID)).setText(newID);
            }
        });
    }

    private void setDisplayedPieceInfo(final String artistName, final String artistInfo, final String pieceTitle, final String pieceInfo, final String beaconID, final String imageURL){
        /* Update the displayed piece info on the UI thread. Updates all the text and collapsible 
            information, as well as executing the task for downloading the image and displaying it.
         */
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if(imageURL != null){
//                    DownloadDisplayImageTask task = new DownloadDisplayImageTask((ImageView) findViewById(R.id.artPieceImage));
//                    task.execute(imageURL);
                    loadImage((ImageView) findViewById(R.id.artPieceImage), imageURL);


                }else{
                    ImageView imageView = (ImageView) findViewById(R.id.artPieceImage);
                    imageView.setImageResource(nothing_in_range);
                }


                ((TextView) findViewById(R.id.artPieceName)).setText(pieceTitle);
                ((TextView) findViewById(R.id.artistName)).setText(artistName);
                ((TextView) findViewById(R.id.artistInfoView).findViewById(R.id.title)).setText("About the artist");
                ((TextView) findViewById(R.id.artworkInfoView).findViewById(R.id.title)).setText("About the piece");

                ExpandableTextView artistNameExpandable = (ExpandableTextView) findViewById(R.id.artistInfoView).findViewById(R.id.expand_text_view);
                artistNameExpandable.setText(artistInfo);

                ExpandableTextView artInfoExpandable = (ExpandableTextView) findViewById(R.id.artworkInfoView).findViewById(R.id.expand_text_view);
                artInfoExpandable.setText(pieceInfo);

                setBeaconIDText(beaconID);

            }
        });

    }

    private void setBeaconIDText(String beaconIDText){
        ((TextView) findViewById(beaconID)).setText(beaconIDText);
    }

    @Override
    public void processFinish(ArrayList<ExhibitPiece> result){
        /* Callback method for downloading the database from the UCI server.
            If the database gives no art pieces, then display an error. Else, the database
            has been successfully downloaded.
         */
        piecesList = result;

        if(piecesList.isEmpty()){
            Log.d(TAG, "Unexpected database result - returned pieces list was empty...");
            final AlertDialog databaseFailureDialog = new AlertDialog.Builder(this).create();
            databaseFailureDialog.setCancelable(false);
            databaseFailureDialog.setTitle("SCES Art Gallery Database Failure");
            databaseFailureDialog.setMessage("A problem occurred while obtaining the gallery database. Either the database sever could not" +
                    " reached or the application received unexpected results. \n\nThere maybe an issue with the connection (make sure to be using the UCI network or a VPN), or the database." +
                    "\n\nThis app cannot display art piece information without obtaining gallery data from the database.");
            databaseFailureDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Exit", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });

            databaseFailureDialog.show();

        }else{
            Log.d(TAG, "SCES Art Database downloaded!");
        }
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

        if(beaconManager != null && region != null) {
            beaconManager.stopRanging(region);

        }

        beaconManager.disconnect();

        
    }


    private ExhibitPiece getPieceInfo(Beacon beaconDetails){
        /* Returns an ExhibitPiece object associated with the given Beacon.

            Searches through the stored art piece list (from the database) and
            finds the match with the given Beacon.
         */
        ExhibitPiece associatedPiece = new ExhibitPiece(0, 0, "Unknown Piece", 0,
                                                        "Major ID is " + beaconDetails.getMajor(),
                                                        "","","","",true,0);
        if(piecesList != null) {
            for (ExhibitPiece piece : piecesList) {
                if (piece.beaconMajorId == beaconDetails.getMajor()) {
                    associatedPiece = piece;
                    break;
                }
            }
        }

        return associatedPiece;

    }

    private void loadImage(final ImageView view, final String imageURL){
        Target target = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                view.setImageBitmap(bitmap);

                view.setVisibility(View.VISIBLE);
                Transitions.fadeInImage(view);
                findViewById(R.id.loadImageSpinner).setVisibility(View.INVISIBLE);

            }
            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
                Transitions.fadeOutImage(view);
                view.setVisibility(View.INVISIBLE);

                findViewById(R.id.loadImageSpinner).setVisibility(View.VISIBLE);
            }
        };

        /*Uses Picasso library instead of raw image downloading since the library
            automatically cancels and background downloads should a new download
            be instantiated whilst one is currently in progress - i.e. if new image
            is detected one that is currently downloading will have its download cancelled.

            Also, Picasso caches images automatically, so they don't need to be redownloaded
            each time.
         */
        if(imageURL == ""){
            Picasso.with(getApplicationContext()).load(no_image).into(target);
        }else{
            Picasso.with(getApplicationContext()).load(imageURL).error(no_image).into(target);
        }
    }
    /*
    OLD CODE -- previously used for downloading and displaying images - replaced with Picasso library use
    private class DownloadDisplayImageTask extends AsyncTask<String, Void, Bitmap>  {
        // An AsynchronousTask for downloading and displaying art piece images.

        ImageView bmImage;

        public DownloadDisplayImageTask(ImageView bmImage) {
            this.bmImage = bmImage;
        }

        protected void onPreExecute() {
            Transitions.fadeOutImage(bmImage);
            bmImage.setVisibility(View.INVISIBLE);

            findViewById(R.id.loadImageSpinner).setVisibility(View.VISIBLE);
        }

        protected Bitmap doInBackground(String[] url) {
            //There's only one URL - the one passed in.
            String urldisplay = url[0];

            Bitmap imageBitmap = null;

            try {
                URL imageURL = new URL(urldisplay);
                URLConnection connection = imageURL.openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                InputStream in = connection.getInputStream();
                imageBitmap = BitmapFactory.decodeStream(in);

            } catch (Exception e) {
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

            bmImage.setVisibility(View.VISIBLE);
            Transitions.fadeInImage(bmImage);
            findViewById(R.id.loadImageSpinner).setVisibility(View.INVISIBLE);

        }
    }
    */




}



