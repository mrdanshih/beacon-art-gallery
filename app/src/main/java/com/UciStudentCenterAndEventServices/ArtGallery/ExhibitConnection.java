package com.UciStudentCenterAndEventServices.ArtGallery;

import com.google.gson.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

import static com.estimote.sdk.repackaged.okhttp_v2_2_0.com.squareup.okhttp.Protocol.get;

public class ExhibitConnection {
    public static void main (String[] args) throws Exception{
        ArrayList<ExhibitPiece> piecesList = getExhibitInfo();

        for (ExhibitPiece piece: piecesList){
            System.out.println(piece.artistName);
            System.out.println(piece.title);
            System.out.println(piece.blurb);
            System.out.println(piece.beaconMajorId);
            System.out.println();
        }
    }

    public static ArrayList<ExhibitPiece> getExhibitInfo() {
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
            URL databaseURL = new URL("http://aladdin.studentcenter.uci.edu/ArtWebApi/api/Artwork/GetAssociatedArtwork");
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

