package com.UciStudentCenterAndEventServices.ArtGallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.SystemRequirementsChecker;

import org.w3c.dom.Text;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.UciStudentCenterAndEventServices.ArtGallery.R.drawable.beacon;
import static com.UciStudentCenterAndEventServices.ArtGallery.R.drawable.blueberries;
import static com.UciStudentCenterAndEventServices.ArtGallery.R.drawable.ice;
import static com.UciStudentCenterAndEventServices.ArtGallery.R.drawable.mint;


public class MainActivity extends AppCompatActivity implements ExhibitConnection.AsyncResponse{

    private static final String TAG = "MainActivity";

    private BeaconManager beaconManager;
    private Region region;

    //IceMintMajor represents a fictional "Ice-Mint Exhibit"
    //Individual exhibits have art pieces - this is represented by majorID = exhibit, minorID = piece
    final int iceMintMajor = 12345;

    //Hardcoded UUID/IDs for the test app
    String blueberryUUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    int blueberryMajorID = 20522, blueberryMinorID = 62874;

    String iceUUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    int iceMajorID = 12345, iceMinorID = 62387;

    String mintUUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    int mintMajorID = 12345, mintMinorID = 45368;

    ArrayList<ExhibitPiece> piecesList;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Does the fetching of the database (network process) using Async task
        new ExhibitConnection(this).execute();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        beaconManager = new BeaconManager(this);
        beaconManager.setRangingListener(new BeaconManager.RangingListener() {
            @Override
            public void onBeaconsDiscovered(Region region, List<Beacon> beaconList) {
                String artistName;
                String pieceTitle;
                String pieceInfo;
                String beaconID;
                String imageURL;

                if(!beaconList.isEmpty()){
                    Beacon nearestBeacon = beaconList.get(0);

                    ExhibitPiece artPiece = getPieceInfo(nearestBeacon);
                    artistName = artPiece.artistName;
                    pieceTitle = artPiece.title;
                    pieceInfo = artPiece.blurb;
                    beaconID = artPiece.beaconMajorId + "";
                    imageURL = artPiece.pictureUrl;

                    System.out.println(imageURL);

                    ((TextView) findViewById(R.id.artistName)).setText(artistName);
                    ((TextView) findViewById(R.id.beaconID)).setText(beaconID);
                    ((TextView) findViewById(R.id.artPieceName)).setText(pieceTitle);
                    ((TextView) findViewById(R.id.artPieceInfo)).setText(pieceInfo);

                    //Download and set image.
                    DownloadImageTask task = new DownloadImageTask((ImageView) findViewById(R.id.artPieceImage));
                    task.execute(imageURL);

                }
            }
        });

        region = new Region("Art Gallery Region", UUID.fromString(blueberryUUID), null, null);

    }

    @Override
    public void processFinish(ArrayList<ExhibitPiece> result){
        piecesList = result;
    }

    public void setCurrentImage(int imageNum){
        final ImageView imageView = (ImageView) findViewById(R.id.artPieceImage);
        imageView.setImageResource(imageNum);
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (!SystemRequirementsChecker.checkWithDefaultDialogs(this)) {
            Log.e(TAG, "Can't scan for beacons, some pre-conditions were not met");
            Log.e(TAG, "Read more about what's required at: http://estimote.github.io/Android-SDK/JavaDocs/com/estimote/sdk/SystemRequirementsChecker.html");
            Log.e(TAG, "If this is fixable, you should see a popup on the app's screen right now, asking to enable what's necessary");
        } else {
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
        beaconManager.stopRanging(region);
        Log.d(TAG, "Stopping Beacon ranging.");

        super.onPause();


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private String getExhibitInfo(Beacon beaconDetails){
        if(beaconDetails.getMajor() == iceMintMajor){
            return "You are in the Ice-Mint Exhibit.";

        }else if(beaconDetails.getMajor() == blueberryMajorID){
            return "You are in the Blueberry exhibit.";

        }else{
            return "Unknown exhibit! The ID is " + beaconDetails.getMajor();
        }
    }

    private String getPieceName(Beacon beaconDetails){
        if(beaconDetails.getMinor() == blueberryMinorID){
            return "Blueberry";

        }else if(beaconDetails.getMinor() == mintMinorID){
            return "Mint";

        }else if(beaconDetails.getMinor() == iceMinorID) {
            return "Ice";

        }else{
            return "UNKNOWN";
        }
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
            bmImage.setImageBitmap(result);
        }
    }

}



