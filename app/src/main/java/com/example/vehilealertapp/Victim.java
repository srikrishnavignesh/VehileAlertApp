package com.example.vehilealertapp;

import android.net.Uri;

import java.net.URL;

class location
{
    public double latitude;
    public double longitude;
    public location()
    {

    }
    public location(double latitude,double longitude)
    {
        this.latitude=latitude;
        this.longitude=longitude;
    }
}
public class Victim {
    public String registration_number;
    public String name;
    public String details;
    public String contact_no;
    public String imageurl;
    public Victim()
    {

    }
    public Victim(String registration_number,String name,String details,String contact_no)
    {
        this.registration_number=registration_number;
        this.name=name;
        this.details=details;
        this.contact_no=contact_no;
    }


}
