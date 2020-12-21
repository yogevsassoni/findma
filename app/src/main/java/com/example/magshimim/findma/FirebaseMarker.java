package com.example.magshimim.findma;

public class FirebaseMarker {


    public double longitude;
    public double latitude;
    public String str;



    //required empty constructor
    public FirebaseMarker() {
    }


    public void setStr(String str) {
        this.str = str;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }
}