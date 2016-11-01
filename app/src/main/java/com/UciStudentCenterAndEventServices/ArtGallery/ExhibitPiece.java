package com.UciStudentCenterAndEventServices.ArtGallery;

/**
 * Created by danielshih on 10/31/16.
 */

public class ExhibitPiece {
    final int artworkId, exhibitId, artistId, beaconMajorId;
    final String title, artistName, artistBlurb, artistLogo,
                blurb, pictureUrl;
    final boolean isObselete;


    public ExhibitPiece(int artworkId, int exhibitId, String title,
                        int artistId, String artistName, String artistBlurb,
                        String artistLogo, String blurb, String pictureUrl,
                        boolean isObselete, int beaconMajorId){
        this.artworkId = artworkId;
        this.exhibitId = exhibitId;
        this.title = title;
        this.artistId = artistId;
        this.artistName = artistName;
        this.artistBlurb = artistBlurb;
        this.artistLogo = artistLogo;
        this.blurb = blurb;
        this.pictureUrl = pictureUrl;
        this.isObselete = isObselete;
        this.beaconMajorId = beaconMajorId;

    }
}
