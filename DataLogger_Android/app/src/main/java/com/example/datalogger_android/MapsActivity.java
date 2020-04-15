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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private GoogleMap mMap;
    private ArrayList<ArrayList<Double>> gpsData = new ArrayList<ArrayList<Double>>();
    private ArrayList<ArrayList<Double>> imuObdData = new ArrayList<ArrayList<Double>>();
    private ArrayList<Polyline> routeData = new ArrayList<Polyline>();
    private ArrayList<Marker> launchMarkers = new ArrayList<>();
    private ArrayList<Marker> mpgMarkers = new ArrayList<>();
    private ArrayList<Marker> aggressiveMarkers = new ArrayList<>();
    private final double minSpeed = 0.0, maxSpeed = 60.0;
    private final double minMPG = 0.0, maxMPG = 60.0;
    private final int[] colorValues = {0xFF0000FF, 0xFF0080FF, 0xFF00FFFF, 0xFF00FF80, 0xFF00FF00, 0xFF80FF00, 0xFFFFFF00, 0xFFFF8000, 0xFFFF0000};
    private final int numColorLevels = colorValues.length;
    private final double colorSpeedStep = (maxSpeed-minSpeed)/numColorLevels;
    private final double colorMPGStep = (maxMPG-minMPG)/numColorLevels;
    private boolean analysisDone = false;
    private static final int window = 10;
    private static final int shift = 2;

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
        Button mpgBtn = findViewById(R.id.colorMPG);
        mpgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayMPG();
            }
        });
        Button mpgTButton = findViewById(R.id.toggleMpg);
        mpgTButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleMarkers(mpgMarkers);
            }
        });
        Button launchTButton = findViewById(R.id.toggleLaunch);
        launchTButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { toggleMarkers(launchMarkers);
            }
        });
        Button aggressiveTButton = findViewById(R.id.toggleAggressive);
        aggressiveTButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { toggleMarkers(aggressiveMarkers);
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
        String imuFilename = getFilesDir().toString()+"/LOG_IMU_OBD/"+filename.substring(0,filename.length()-7)+"IMU_OBD.txt";
        File fImuObd = new File(imuFilename);
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
                br.close();

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

                    for (String s : dataArr) {
                        data = Double.parseDouble(s);
                        oneAL.add(data);
                    }
                    imuObdData.add(oneAL);
                }
                br2.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        mMap.moveCamera(CameraUpdateFactory.zoomTo((float) 15));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(first_lat, first_lon)));
        ArrayList<DrivingEvent> drivingEvents = DrivingAnalyzer.drivingAnalysis(imuObdData, gpsData);

        MarkerOptions mrko;
        Marker aMarker;
        for(DrivingEvent event: drivingEvents) {
            mrko = new MarkerOptions();
            mrko.position(new LatLng(event.getLatitude(), event.getLongitude()));
//            mrko.flat(true);
            mrko.visible(false);
            if(event.getType().equals("BadMPG")) {
                mrko.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
                aMarker = mMap.addMarker(mrko);
                mpgMarkers.add(aMarker);
            }else if(event.getType().equals("Launch")){
                mrko.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE));
                aMarker = mMap.addMarker(mrko);
                launchMarkers.add(aMarker);
//                mrko.zIndex(999999);
//                mMap.addMarker(mrko);
            }
            //mMap.addMarker(mrko);
        }
        svm_model loadedModel;
        try {
            loadedModel = svm.svm_load_model(getFilesDir().toString()+"/model.model");
            ArrayList<ArrayList<svm_node>> dataset = makeDataset(imuFilename);
//            nodeList.toArray(new svm_node[nodeList.size()]);
//            ArrayList<Double> predictions = new ArrayList<>();
            svm_node[] oneNodeSet;
//            Log.d("PREDICTIONS",dataset.size()+" "+dataset.get(0).size());
            assert dataset != null;
            for(int i = 0; i < dataset.size(); i++) {
                oneNodeSet = dataset.get(i).toArray(new svm_node[dataset.get(i).size()]);
                double d = svm.svm_predict(loadedModel, oneNodeSet);
//                Log.d("PREDICTIONS","i = "+ i +" Predict: "+d);
//                predictions.add(d);
                if(d == 1.0){
                    //found aggressive driving
                    double aTime = imuObdData.get(i*shift).get(0);// time aggressive event occurred
                    for(int index = 0; index < routeData.size(); index++){
                        if(gpsData.get(index).get(0) > aTime){
                            mrko = new MarkerOptions();
                            mrko.visible(false);
                            mrko.position(routeData.get(index).getPoints().get(0));
//                            mrko.flat(true);
                            mrko.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                            aMarker = mMap.addMarker(mrko);
                            aggressiveMarkers.add(aMarker);
                            break;
                        }
                    }
                }
            }
        } catch (IOException e) {
//            Log.d("PREDICTIONS","error");
            e.printStackTrace();
        }

        analysisDone = true;
    }
    public static ArrayList<ArrayList<svm_node>> makeDataset(String filen) {
        String thisLine;
        ArrayList<ArrayList<svm_node>> nodeList = new ArrayList<>();
        ArrayList<svm_node> oneList = new ArrayList<>();
        double[][] windowdata = new double[window][10];
        String[] Lines = new String[shift];
//        String classifier = "-1";
//
//        if(args[0].toLowerCase().equals("aggressive")) {
//            classifier = "+1";
//        }

        try{

            File file=new File(filen);
            File outfile=new File("drivingDataset");
            BufferedReader br=new BufferedReader(new FileReader(file));
//            BufferedWriter bw = new BufferedWriter(new FileWriter(outfile));

            int i = 0;
            thisLine = br.readLine(); // read in header line
            while((thisLine = br.readLine()) != null && i < window){

                windowdata[i] = (toDoubleArr(thisLine.split(",")));
                //System.out.println(windowdata[i].toString());

                i++;
            }

            if(i < window) {
                Log.d("PREDICTIONS","File Not big enough to create dataset");
                return null;
            }
            else {
//                bw.write(classifier);
                int index = 1;
                for(int r = 0; r < window; r++) {
                    for(int c = 1; c < 10; c++) {
//                        String output = " " + (index) + ":" + windowdata[r][c];
//                        System.out.println(output);
//                        bw.append(output);
                        svm_node thisNode = new svm_node();
                        thisNode.index = index;
                        thisNode.value = windowdata[r][c];
                        oneList.add(thisNode);
                        index++;
                    }
                }
                nodeList.add(oneList);
                oneList = new ArrayList<>();
//                bw.newLine();
                //bw.write("This is a Test");
            }
            boolean shiftWindow = true;
            while(shiftWindow){
                for(int s = 0; s < shift; s++) {
                    if((Lines[s] = br.readLine()) == null) {
                        shiftWindow = false;
                        break;
                    }
                }
                if(!shiftWindow) break;

                for(int s = 0; s < (window - shift); s++) {
                    windowdata[s] = windowdata[s + shift];
                }

                for(int s = 0; s < shift; s++) {
                    windowdata[window + s - shift] = toDoubleArr(Lines[s].split(","));
                }

	        	/*windowdata[0] = windowdata[2];
	        	windowdata[1] = windowdata[3];
	        	windowdata[2] = windowdata[4];
	        	windowdata[3] = windowdata[5];
	        	windowdata[4] = windowdata[6];
	        	windowdata[5] = windowdata[7];
	        	windowdata[6] = windowdata[8];
	        	windowdata[7] = windowdata[9];
	        	windowdata[8] = (toDoubleArr(thisLine.split(",")));
	        	windowdata[9] = (toDoubleArr(nextLine.split(",")));*/
//                bw.write(classifier);
                int index = 1;
                for(int r = 0; r < window; r++) {
                    for(int c = 1; c < 10; c++) {
//                        String output = " " + (index) + ":" + windowdata[r][c];
//                        System.out.println(output);
//                        bw.append(output);
                        svm_node thisNode = new svm_node();
                        thisNode.index = index;
                        thisNode.value = windowdata[r][c];
                        oneList.add(thisNode);
                        index++;
                    }
                }
//                bw.newLine();
                nodeList.add(oneList);
                oneList = new ArrayList<>();
            }

            System.out.println("Closing Files");
            br.close();
//            bw.close();
        }catch(Exception e){
            System.out.println(e);
        }

        return nodeList;

    }

    public static double[] toDoubleArr(String[] values) {
        double[] doubles = new double[values.length];
        for(int i = 0; i < values.length; i++) {
            doubles[i] = Double.parseDouble(values[i]);
        }

        return doubles;
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
//            for(int k = numColorLevels-1; k >= 0; k--){
//                if(speed > k*colorSpeedStep){
//                    routeData.get(i).setColor(colorValues[k]);
//                    break;
//                }
//            }
            double speedLimit = 35.0;
            if(speed < speedLimit){
                routeData.get(i).setColor(Color.BLUE);
            }else if(speed < speedLimit+5){
                routeData.get(i).setColor(Color.YELLOW);
            }else if(speed < speedLimit+10){
                routeData.get(i).setColor(0xFFffa500);//hex orange
            }else{
                routeData.get(i).setColor(Color.RED);
            }
        }
    }
    private void displayMPG(){
        if(!analysisDone){
            return;
        }
        //for each segment of the route
        ArrayList<Double> oneAL;
        double estMPG, maf, speed;
        int prevIndex = 0;// next gps data point will always map to same imuObd data point or later because both lists are sorted by time
        for(int i = 0; i < routeData.size(); i++){
            //determine time to use as lookup
            double time = gpsData.get(i).get(0);
            //lookup in obd AL
            estMPG = minMPG;
            for(int j = prevIndex; j < imuObdData.size(); j++){
                oneAL = imuObdData.get(j);
                if(oneAL.get(0) > time){
                    speed = oneAL.get(1);
                    maf = oneAL.get(6);
                    estMPG = (maf/(14.7*454*6.701)*2600)*speed;
                    prevIndex = j;
                    break;
                }
            }
            if(time > imuObdData.get(imuObdData.size()-1).get(0)){
                speed = imuObdData.get(imuObdData.size()-1).get(1);
                maf = imuObdData.get(imuObdData.size()-1).get(6);
                estMPG = (maf/(14.7*454*6.701)*2600)*speed;
            }
            //color threshold
            for(int k = numColorLevels-1; k >= 0; k--){
                if(estMPG > k*colorMPGStep){
                    routeData.get(i).setColor(colorValues[numColorLevels-1-k]);
                    break;
                }
            }
        }
    }
    private void toggleMarkers(ArrayList<Marker> al){
        if(!analysisDone){
            return;
        }
        for(Marker m : al){
            m.setVisible(!m.isVisible());
        }
    }
}
