package com.UciStudentCenterAndEventServices.ArtGallery.estimote;

import com.estimote.sdk.cloud.model.Color;

import java.util.UUID;

public class EstimoteCloudBeaconDetails {

    private String beaconName;
    private Color beaconColor;
    private UUID uuid;
    private int major, minor;

    public EstimoteCloudBeaconDetails(String beaconName, Color beaconColor, UUID uuid, int major, int minor) {
        this.beaconName = beaconName;
        this.beaconColor = beaconColor;
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;
    }

    public String getBeaconName() {
        return beaconName;
    }

    public Color getBeaconColor() {
        return beaconColor;
    }

    public UUID getUuid(){
        return uuid;
    }

    public int getMajor(){
        return major;
    }

    public int getMinor(){
        return minor;
    }

    @Override
    public String toString() {
        return "[beaconName: " + getBeaconName() + ", beaconColor: " + getBeaconColor() + "]";
    }
}
