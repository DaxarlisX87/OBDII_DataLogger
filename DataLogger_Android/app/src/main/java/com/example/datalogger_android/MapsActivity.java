package com.example.datalogger_android;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
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
        mMap = googleMap;
        Intent intent = getIntent();
        String filename = intent.getStringExtra("filename");
        File f = new File(getFilesDir().toString()+"/LOG_GPS/"+filename);
        double first_lat = 0, first_lon = 0;
        boolean firstLoop = true;
        if(f.exists()) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(f));
                PolylineOptions plo = (new PolylineOptions()).clickable(true);

                String thisLine = br.readLine(); // read in header line
                double lat, lon;
                String[] dataArr;
                while ((thisLine = br.readLine()) != null) {
                    dataArr = thisLine.split(",", 0);
                    lat = convertDMS2Dec(dataArr[1]);
                    lon = convertDMS2Dec(dataArr[2]);
                    lon = -lon;
                    plo.add(new LatLng(lat, lon));
                    if(firstLoop){
                        first_lat = lat;
                        first_lon = lon;
                        firstLoop = false;
                    }
                }
                // Add a marker in Sydney and move the camera
                //LatLng sydney = new LatLng(-34, 151);
                //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
                //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));

                // use PolylineOptions's .add() or addAll() to add points before displaying
                // override onPolylineClick() method to make something happen when clicked
                Polyline polyline1 = googleMap.addPolyline(plo);

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
