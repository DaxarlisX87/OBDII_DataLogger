package com.example.datalogger_android;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        final double minSpeed = 0.0, maxSpeed = 100.0;
        final int[] colorValues = {0xFF0000FF, 0xFF0080FF, 0xFF00FFFF, 0xFF00FF80, 0xFF00FF00, 0xFF80FF00, 0xFFFFFF00, 0xFFFF8000, 0xFFFF0000};
        final int numColorLevels = colorValues.length;
        final double colorStep = (maxSpeed-minSpeed)/numColorLevels;
        mMap = googleMap;
        Intent intent = getIntent();
        String filename = intent.getStringExtra("filename");
        File f = new File(getFilesDir().toString()+"/LOG_GPS/"+filename);
        File fImuObd = new File(getFilesDir().toString()+"/LOG_IMU_OBD/"+filename);
        double first_lat = 0, first_lon = 0;
        boolean firstLoop = true;
        Polyline polyline1;
        if(f.exists()) {
            BufferedReader br = null;
            BufferedReader br2 = null;
            try {
                br = new BufferedReader(new FileReader(f));
                br2 = new BufferedReader(new FileReader(fImuObd));
                PolylineOptions plo = (new PolylineOptions()).clickable(true);

                String thisLine = br.readLine(); // read in header line
                String thisLine2 = br2.readLine(); // read in header line
                double lat, lon, lastLat = 0, lastLon = 0, thisSpeed;
                String[] dataArr, mmuObdArr;
                while ((thisLine = br.readLine()) != null) {
                    thisLine2 = br2.readLine();
                    dataArr = thisLine.split(",", 0);
                    mmuObdArr = thisLine2.split(",",0);
                    thisSpeed = Double.parseDouble(mmuObdArr[0]);

                    lat = convertDMS2Dec(dataArr[1]);
                    lon = convertDMS2Dec(dataArr[2]);
                    lon = -lon;
                    //plo.add(new LatLng(lat, lon));
                    if(firstLoop){
                        first_lat = lat;
                        first_lon = lon;
                        firstLoop = false;
                    }else{
                        plo.add(new LatLng(lastLat, lastLon), new LatLng(lat, lon));
                        for(int i = numColorLevels-1; i >= 0; i--){
                            if(thisSpeed > i*colorStep){
                                plo.color(colorValues[i]);
                                break;
                            }
                        }
//                        plo.color(Color.BLUE);
                        polyline1 = googleMap.addPolyline(plo);
                        plo = (new PolylineOptions()).clickable(true);
                    }
                    lastLat = lat;
                    lastLon = lon;
                }
                // Add a marker in Sydney and move the camera
                //LatLng sydney = new LatLng(-34, 151);
                //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

                // use PolylineOptions's .add() or addAll() to add points before displaying
                // override onPolylineClick() method to make something happen when clicked
//                Polyline polyline1 = googleMap.addPolyline(plo);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mMap.moveCamera(CameraUpdateFactory.zoomTo((float) 15));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(first_lat, first_lon)));

    }
    private double convertDMS2Dec(String s){
        String[] arr = s.split("\\.", 0);
        double degrees = Double.parseDouble(arr[0].substring(0, arr[0].length()-2));
        double minutes = Double.parseDouble(s.substring(arr[0].length()-2));
        double decMinutes = minutes/60.0;
        return degrees + decMinutes;
    }
}
