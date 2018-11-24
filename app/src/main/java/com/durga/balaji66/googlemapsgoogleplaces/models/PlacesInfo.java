package com.durga.balaji66.googlemapsgoogleplaces.models;

import android.net.Uri;

import com.google.android.gms.maps.model.LatLng;

public class PlacesInfo {

    private String name;
    private String address;
    private String phoneNumber;
    private String id;
    private Uri webSiteUri;
    private LatLng latLng;
    private float rating;
    private String attributions;

    public PlacesInfo() {
    }

    @Override
    public String toString() {
        return "PlacesInfo{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", id='" + id + '\'' +
                ", webSiteUri=" + webSiteUri +
                ", latLng=" + latLng +
                ", rating=" + rating +
                ", attributions='" + attributions + '\'' +
                '}';
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Uri getWebSiteUri() {
        return webSiteUri;
    }

    public void setWebSiteUri(Uri webSiteUri) {
        this.webSiteUri = webSiteUri;
    }

    public LatLng getLatLng() {
        return latLng;
    }

    public void setLatLng(LatLng latLng) {
        this.latLng = latLng;
    }

    public float getRating() {
        return rating;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public String getAttributions() {
        return attributions;
    }

    public void setAttributions(String attributions) {
        this.attributions = attributions;
    }

    public PlacesInfo(String name, String address, String phoneNumber, String id, Uri webSiteUri, LatLng latLng, float rating, String attributions) {

        this.name = name;
        this.address = address;
        this.phoneNumber = phoneNumber;
        this.id = id;
        this.webSiteUri = webSiteUri;
        this.latLng = latLng;
        this.rating = rating;
        this.attributions = attributions;
    }
}
