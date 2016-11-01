package com.UciStudentCenterAndEventServices.ArtGallery;

import com.google.gson.*;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class ExhibitDatabaseTester {
    public static void main (String[] args) throws Exception{
        URL databaseURL = new URL("http://aladdin.studentcenter.uci.edu/ArtWebApi/api/Artwork/GetAssociatedArtwork");
        URLConnection connection = databaseURL.openConnection();

        InputStream in = connection.getInputStream();
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));

        String response = "";
        String inputLine;

        while((inputLine = reader.readLine()) != null){
            response += inputLine;

        }

        System.out.println(response);

        JsonElement jelement = new JsonParser().parse(response);
        JsonArray jarray = jelement.getAsJsonArray();
        JsonObject artpiece = jarray.get(0).getAsJsonObject();


        System.out.println(artpiece.get("ArtworkId"));
        reader.close();


    }



}
