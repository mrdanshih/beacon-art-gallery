package com.UciStudentCenterAndEventServices.ArtGallery;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.UciStudentCenterAndEventServices.ArtGallery.estimote.BeaconID;
import com.UciStudentCenterAndEventServices.ArtGallery.estimote.EstimoteCloudBeaconDetails;
import com.UciStudentCenterAndEventServices.ArtGallery.estimote.EstimoteCloudBeaconDetailsFactory;
import com.UciStudentCenterAndEventServices.ArtGallery.estimote.ProximityContentManager;
import com.estimote.sdk.SystemRequirementsChecker;
import com.estimote.sdk.cloud.model.Color;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import static com.UciStudentCenterAndEventServices.ArtGallery.R.drawable.beacon;
import static com.UciStudentCenterAndEventServices.ArtGallery.R.drawable.blueberries;
import static com.UciStudentCenterAndEventServices.ArtGallery.R.drawable.ice;
import static com.UciStudentCenterAndEventServices.ArtGallery.R.drawable.mint;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final Map<Color, Integer> BACKGROUND_COLORS = new HashMap<>();

    static {
        BACKGROUND_COLORS.put(Color.ICY_MARSHMALLOW, android.graphics.Color.rgb(109, 170, 199));
        BACKGROUND_COLORS.put(Color.BLUEBERRY_PIE, android.graphics.Color.rgb(98, 84, 158));
        BACKGROUND_COLORS.put(Color.MINT_COCKTAIL, android.graphics.Color.rgb(155, 186, 160));
    }

    private static final int BACKGROUND_COLOR_NEUTRAL = android.graphics.Color.rgb(160, 169, 172);

    private ProximityContentManager proximityContentManager;

    final int iceMintMajor = 12345;

    String blueberryUUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    int blueberryMajorID = 20522, blueberryMinorID = 62874;

    String iceUUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    int iceMajorID = 12345, iceMinorID = 62387;

    String mintUUID = "B9407F30-F5F8-466E-AFF9-25556B57FE6D";
    int mintMajorID = 12345, mintMinorID = 45368;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        proximityContentManager = new ProximityContentManager(this,
                Arrays.asList(
                        new BeaconID(blueberryUUID, blueberryMajorID, blueberryMinorID),
                        new BeaconID(iceUUID, iceMajorID, iceMinorID),
                        new BeaconID(mintUUID, mintMajorID, mintMinorID)),
                new EstimoteCloudBeaconDetailsFactory());
        proximityContentManager.setListener(new ProximityContentManager.Listener() {
            @Override
            public void onContentChanged(Object content) {
                String name;
                String exhibitInfo = "";
                String pieceInfo = "";
                Integer backgroundColor;

                if (content != null) {
                    EstimoteCloudBeaconDetails beaconDetails = (EstimoteCloudBeaconDetails) content;
                    name = "You're in " + beaconDetails.getBeaconName() + "'s range!";
                    //backgroundColor = BACKGROUND_COLORS.get(beaconDetails.getBeaconColor());

                    exhibitInfo = getExhibitInfo(beaconDetails);
                    pieceInfo = getPieceInfo(beaconDetails).description;
                    setCurrentImage(getPieceInfo(beaconDetails).image);

                } else {
                    name = "No art pieces in range.";
                    backgroundColor = null;
                    pieceInfo = "";
                }
                ((TextView) findViewById(R.id.artExhibitTitle)).setText(exhibitInfo);
                ((TextView) findViewById(R.id.artPieceName)).setText(name);
                ((TextView) findViewById(R.id.artPieceInfo)).setText(pieceInfo);
                /*findViewById(R.id.relativeLayout).setBackgroundColor(
                        backgroundColor != null ? backgroundColor : BACKGROUND_COLOR_NEUTRAL);*/
            }
        });
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
            Log.d(TAG, "Starting ProximityContentManager content updates");
            proximityContentManager.startContentUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Stopping ProximityContentManager content updates");
        proximityContentManager.stopContentUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        proximityContentManager.destroy();
    }

    private String getExhibitInfo(EstimoteCloudBeaconDetails beaconDetails){
        if(beaconDetails.getMajor() == iceMintMajor){
            return "You are in the Ice-Mint Exhibit.";
        }else if(beaconDetails.getMajor() == blueberryMajorID){
            return "You are in the Blueberry exhibit.";
        }else{
            return "Unknown exhibit! The ID is " + beaconDetails.getMajor();
        }
    }

    private PieceInfo getPieceInfo(EstimoteCloudBeaconDetails beaconDetails){
        if(beaconDetails.getMinor() == blueberryMinorID){
            return new PieceInfo("Blueberries are blue.", blueberries);

        }else if(beaconDetails.getMinor() == mintMinorID){
            return new PieceInfo("Mint is minty.", mint);

        }else if(beaconDetails.getMinor() == iceMinorID) {
            return new PieceInfo("Ice is frozen water.", ice);

        }else{
            return new PieceInfo("Unknown piece! The ID is " + beaconDetails.getMajor(), beacon);
        }

    }




}
