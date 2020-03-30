package com.example.datalogger_android;

import androidx.fragment.app.FragmentActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

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
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<ArrayList<Double>> gpsData = new ArrayList<ArrayList<Double>>();
    private ArrayList<ArrayList<Double>> imuObdData = new ArrayList<ArrayList<Double>>();
    private ArrayList<Polyline> routeData = new ArrayList<Polyline>();
    private final double minSpeed = 0.0, maxSpeed = 60.0;
    private final int[] colorValues = {0xFF0000FF, 0xFF0080FF, 0xFF00FFFF, 0xFF00FF80, 0xFF00FF00, 0xFF80FF00, 0xFFFFFF00, 0xFFFF8000, 0xFFFF0000};
    private final int numColorLevels = colorValues.length;
    private final double colorStep = (maxSpeed-minSpeed)/numColorLevels;
    private boolean analysisDone = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        Button speedBtn = findViewById(R.id.colorSpeed);
        speedBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displaySpeed();
            }
        });
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
        File fImuObd = new File(getFilesDir().toString()+"/LOG_IMU_OBD/"+filename);
//        ArrayList<ArrayList<Double>> gpsData = new ArrayList<ArrayList<Double>>();
//        ArrayList<ArrayList<Double>> imuObdData = new ArrayList<ArrayList<Double>>();
//        ArrayList<PolylineOptions> routeData = new ArrayList<PolylineOptions>();
        ArrayList<Double> oneAL;
        PolylineOptions plo;
        double first_lat = 0, first_lon = 0;
        boolean firstLoop = true;
        //Polyline polyline1;
        if(f.exists()) {
            BufferedReader br = null;
            try {
                br = new BufferedReader(new FileReader(f));

                String thisLine = br.readLine(); // read in header line
                double time, lat, lon, alt, lastLat = 0, lastLon = 0;
                String[] dataArr;
                while ((thisLine = br.readLine()) != null) {
                    plo = (new PolylineOptions()).clickable(true);
                    oneAL = new ArrayList<Double>();
                    dataArr = thisLine.split(",", 0);

                    time = Double.parseDouble(dataArr[0]);
                    lat = convertDMS2Dec(dataArr[1]);
                    lon = convertDMS2Dec(dataArr[2]);
                    lon = -lon;// hardcode NW hemisphere
                    alt = Double.parseDouble(dataArr[3]);
                    oneAL.add(time);
                    oneAL.add(lat);
                    oneAL.add(lon);
                    oneAL.add(alt);
                    gpsData.add(oneAL);

                    if(firstLoop){
                        first_lat = lat;
                        first_lon = lon;
                        firstLoop = false;
                    }else{
                        plo.add(new LatLng(lastLat, lastLon), new LatLng(lat, lon));
                        routeData.add(googleMap.addPolyline(plo));
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
        if(fImuObd.exists()) {
            BufferedReader br2 = null;
            try {
                br2 = new BufferedReader(new FileReader(fImuObd));

                String thisLine = br2.readLine(); // read in header line
                double data, speed, rpm, maf, tps, load, adv, accX, accY, accZ;
                String[] dataArr;
                while ((thisLine = br2.readLine()) != null) {
                    oneAL = new ArrayList<Double>();
                    dataArr = thisLine.split(",", 0);

                    for(int i = 0; i < dataArr.length; i++) {
                        data = Double.parseDouble(dataArr[i]);
                        oneAL.add(data);
                    }
                    imuObdData.add(oneAL);
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mMap.moveCamera(CameraUpdateFactory.zoomTo((float) 15));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(first_lat, first_lon)));

        analysisDone = true;
    }
    private double convertDMS2Dec(String s){
        String[] arr = s.split("\\.", 0);
        double degrees = Double.parseDouble(arr[0].substring(0, arr[0].length()-2));
        double minutes = Double.parseDouble(s.substring(arr[0].length()-2));
        double decMinutes = minutes/60.0;
        return degrees + decMinutes;
    }
    private void displaySpeed(){
        if(!analysisDone){
            return;
        }
        //for each segment of the route
        ArrayList<Double> oneAL;
        int prevIndex = 0;// next gps data point will always map to same imuObd data point or later because both lists are sorted by time
        for(int i = 0; i < routeData.size(); i++){
            //determine time to use as lookup
            double time = gpsData.get(i).get(0);
            //lookup in obd AL
            double speed = minSpeed;
            for(int j = prevIndex; j < imuObdData.size(); j++){
                oneAL = imuObdData.get(j);
                if(oneAL.get(0) > time){
                    speed = oneAL.get(1);
                    prevIndex = j;
                    break;
                }
            }
            if(time > imuObdData.get(imuObdData.size()-1).get(0)){
                speed = imuObdData.get(imuObdData.size()-1).get(1);
            }
            //color threshold
            for(int k = numColorLevels-1; k >= 0; k--){
                if(speed > k*colorStep){
                    routeData.get(i).setColor(colorValues[k]);
                    break;
                }
            }
        }
    }
}
