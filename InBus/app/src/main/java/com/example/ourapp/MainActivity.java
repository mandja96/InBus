package com.example.ourapp;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.telesign.MessagingClient;
import com.telesign.RestClient;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    GoogleMap map;

    private static final int REQUEST_LOCATION = 1;
    Button btnDone;
    Button btnDone2;

    LatLng pickedCoordinates;

    LocationManager locationManager;
    String latitude, longitude;

    double theLat;
    double theLong;

    EditText phoneNumber;
    EditText message;

    EditText minText;
    EditText kmText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        requestPermission();

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);

        btnDone = findViewById(R.id.btnDone);
        btnDone2 = findViewById(R.id.btnDone2);

        minText = findViewById(R.id.editText6);
        kmText = findViewById(R.id.editText5);

        message = findViewById(R.id.textMessage);
        phoneNumber = findViewById(R.id.phoneNumber);

        btnDone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    OnGPS();
                }
                else {
                    doWork();
                }
            }

        });


        btnDone2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

                if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    OnGPS();
                }
                else {
                    definedTime();
                }
            }

        });
    }

    private void definedTime() {
        getLocation();

        if(phoneNumber != null && message != null && pickedCoordinates != null && minText!=null){
            double currentTime = System.currentTimeMillis(); // current time

            double maxSpeed =  35;

            while (true) {
                double lastLat = theLat;
                double lastLong = theLong;
                getLocation();

                double currentSpeed = getDistanceFromLatLonInKm(lastLat, lastLong, theLat, theLong)*1000 / 1;

                if (currentSpeed == 0){
                    currentSpeed = 1;
                }

                double currentDistanceToEnd = getDistanceFromLatLonInKm(theLat, theLong, pickedCoordinates.latitude, pickedCoordinates.longitude);

                double currentTimeEstimated = currentDistanceToEnd / currentSpeed;

                double minutes = Double.valueOf(minText.getText().toString().trim());

                if(currentTimeEstimated < minutes * 60 * 1000) {
                    sendMessage();
                    break;
                }
                try {
                    sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void doWork() {

        getLocation();

        if(phoneNumber != null && message != null && pickedCoordinates != null && kmText != null){

            double kilometers = Double.parseDouble(kmText.getText().toString().trim());
            System.out.println(kilometers);

            double distance = getDistanceFromLatLonInKm(theLat, theLong, pickedCoordinates.latitude, pickedCoordinates.longitude);
            if (distance < kilometers){
                sendMessage();
            }

        }

    }

    double getDistanceFromLatLonInKm(double lat1, double lon1, double lat2, double lon2) {
        double R = 6371; // Radius of the earth in km
        double dLat = Math.toRadians(lat2-lat1);  // deg2rad below
        double dLon = Math.toRadians(lon2-lon1);
        double a =
                Math.sin(dLat/2) * Math.sin(dLat/2) +
                        Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                                Math.sin(dLon/2) * Math.sin(dLon/2)
                ;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c; // Distance in km
        return d;
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(MainActivity.this, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, REQUEST_LOCATION);
        }
        else {
            Location LocationGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location LocationNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            Location LocationPassive = locationManager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER);


            if (LocationGps != null){
                double lat = LocationGps.getLatitude();
                double longi = LocationGps.getLongitude();


                latitude = String.valueOf(lat);
                longitude = String.valueOf(longi);

                theLat = lat;
                theLong = longi;
                //sendMessage(latitude, longitude);

                // System.out.println("Lokacija1 je: " + latitude + " " + longitude);
                Toast.makeText(this, "GPS location is: " + latitude + ", " + longitude, Toast.LENGTH_SHORT).show();
            }
            else if (LocationNetwork != null){
                double lat = LocationNetwork.getLatitude();
                double longi = LocationNetwork.getLongitude();


                latitude = String.valueOf(lat);
                longitude = String.valueOf(longi);

                theLat = lat;
                theLong = longi;

                Toast.makeText(this, "Network location is: " + latitude + ", " + longitude, Toast.LENGTH_SHORT).show();
            }
            else if (LocationPassive != null){
                double lat = LocationPassive.getLatitude();
                double longi = LocationPassive.getLongitude();


                latitude = String.valueOf(lat);
                longitude = String.valueOf(longi);

                theLat = lat;
                theLong = longi;

                Toast.makeText(this, "Passive location is: " + latitude + ", " + longitude, Toast.LENGTH_SHORT).show();
            }
            else {
                Toast.makeText(this, "Can't get your location!", Toast.LENGTH_SHORT).show();
            }



        }
    }

    private void sendMessage() {

        try {
            Thread thread = new Thread(new Runnable(){
                public void run() {
                    try {

                        String customerId = "183FFD0F-8B04-4CF0-B6DE-59ED28A69A26";
                        String apiKey = "FSYoKltd76JstereAm5iLeqJbRLBzPkb9NM907z5lOy7Dpf53Dvgq4r6gSESQfvKy/1L+J5rGB9S5dPqrxQNRQ==";
                        //String phoneNumber = "381628595076";

                        String number = phoneNumber.getText().toString().trim();
                        //String message = "You are far away from your location: " + distance + ":)";
                        String msg = message.getText().toString().trim();
                        String messageType = "ARN";
                        MessagingClient messagingClient = new MessagingClient(customerId, apiKey);
                        RestClient.TelesignResponse telesignResponse = messagingClient.message(number, msg, messageType, null);
                        //System.out.println("Your reference id is: " + telesignResponse.json.get("reference_id"));
                        System.out.println("Response: " + telesignResponse.json);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

            thread.start();



        } catch (Exception e) {
            System.out.println("HEHE! ");
            e.printStackTrace();
                }
            }

        private void OnGPS() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage("Enable GPS").setCancelable(false).setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void requestPermission(){
        ActivityCompat.requestPermissions(this, new String []{ACCESS_FINE_LOCATION}, 1);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        map = googleMap;


//        LatLng NeZnam = new LatLng(44.806790, 20.477363);
//        map.addMarker(new MarkerOptions().position(NeZnam).title("Ne Znam"));
//        map.moveCamera(CameraUpdateFactory.newLatLng(NeZnam));

        map.setOnMapClickListener(new GoogleMap.OnMapClickListener()
        {
            @Override
            public void onMapClick(LatLng arg0)
            {
//                android.util.Log.i("onMapClick", "Horray!");

                if (pickedCoordinates != null){
                    map.clear();
                }
                pickedCoordinates = arg0;
                map.addMarker(new MarkerOptions().position(pickedCoordinates).title("End point"));
                System.out.println(pickedCoordinates);
            }
        });
    }


}
