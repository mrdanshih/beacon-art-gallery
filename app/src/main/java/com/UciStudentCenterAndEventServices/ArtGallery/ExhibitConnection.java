package com.UciStudentCenterAndEventServices.ArtGallery;

import android.os.AsyncTask;

import com.google.gson.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public class ExhibitConnection extends AsyncTask<Void, Void, ArrayList<ExhibitPiece>>{
    final static String DATABASE_URL = "http://aladdin.studentcenter.uci.edu/ArtWebApi/api/Artwork/GetAssociatedArtwork";

    public interface AsyncResponse{
        void processFinish(ArrayList<ExhibitPiece> output);

    }

    public AsyncResponse delegate = null;

    public ExhibitConnection(AsyncResponse delegate){
        this.delegate = delegate;
    }

    @Override
    protected ArrayList<ExhibitPiece> doInBackground(Void... params) {
        return getExhibitInfo();
    }

    @Override
    protected void onPostExecute(ArrayList<ExhibitPiece>result) {
        System.out.println("Got exhibit info!");
        delegate.processFinish(result);
    }

    private static ArrayList<ExhibitPiece> getExhibitInfo() {
        ArrayList<ExhibitPiece> piecesList = new ArrayList<ExhibitPiece>();

        JsonArray exhibitArray = getJsonArray();

        for (JsonElement dict : exhibitArray){
            JsonObject artPiece = dict.getAsJsonObject();
            int resultId = artPiece.get("ArtworkId").getAsInt();
            int resultExhibitId = artPiece.get("ExhibitId").getAsInt();
            String resultTitle = artPiece.get("Title").getAsString();
            int resultArtistId = artPiece.get("Artist").getAsJsonObject().get("Id").getAsInt();
            String resultArtistName = artPiece.get("Artist").getAsJsonObject().get("Name").getAsString();
            String resultArtistBlurb = artPiece.get("Artist").getAsJsonObject().get("Blurb").getAsString();
            String resultArtistLogo = artPiece.get("Artist").getAsJsonObject().get("Logo").getAsString();
            String resultBlurb = artPiece.get("Blurb").getAsString();
            String resultPictureUrl = artPiece.get("PictureUrl").getAsString();
            boolean isObselete = artPiece.get("IsObsolete").getAsBoolean();
            int resultMajorId = artPiece.get("BeaconMajorId").getAsInt();

            ExhibitPiece piece = new ExhibitPiece(resultId, resultExhibitId, resultTitle,
                                             resultArtistId, resultArtistName,
                                             resultArtistBlurb,  resultArtistLogo, resultBlurb,
                                             resultPictureUrl,   isObselete,  resultMajorId);

            piecesList.add(piece);

        }

        return piecesList;
    }

    private static JsonArray getJsonArray() {
        JsonArray jarray = null;

        try {
            URL databaseURL = new URL(DATABASE_URL);
            URLConnection connection = databaseURL.openConnection();

            InputStream in = connection.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(in));

            String response = "";
            String inputLine;

            while ((inputLine = reader.readLine()) != null) {
                response += inputLine;

            }

            JsonElement jelement = new JsonParser().parse(response);
            jarray = jelement.getAsJsonArray();


        } catch (Exception e) {
            System.out.println("COULD NOT LOAD EXHIBIT.");
            System.out.println(e.getLocalizedMessage());
            e.printStackTrace();

        }

        return jarray;

    }


}


