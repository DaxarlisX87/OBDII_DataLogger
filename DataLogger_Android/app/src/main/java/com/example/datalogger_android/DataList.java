package com.example.datalogger_android;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

@RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
public class DataList extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
//            Log.d("PERMISSIONS", "aaaa");
//        }else{
//            Log.d("PERMISSIONS", "bbbb");
//        }
//
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
//            Log.d("PERMISSIONS", "aaaaaaaa");
//        }else{
//            Log.d("PERMISSIONS", "bbbbbbbb");
//        }
        setContentView(R.layout.activity_data_list);

//        requestForPermission();
        
        // Here, thisActivity is the current activity
//        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//
//            // Permission is not granted
//            // Should we show an explanation?
//            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                // Show an explanation to the user *asynchronously* -- don't block
//                // this thread waiting for the user's response! After the user
//                // sees the explanation, try again to request the permission.
//            } else {
//                // No explanation needed; request the permission
//                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 69);
//
//                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
//                // app-defined int constant. The callback method gets the
//                // result of the request.
//            }
//        } else {
//            // Permission has already been granted
//        }

        //File f = new File("/storage/self/primary/LOG_GPS/");
//        File f = getFilesDir();
        File f = new File(getFilesDir().toString()+"/LOG_GPS/");
//        log_dir.mkdir();
//        File sample_file = new File(getFilesDir().toString()+"/test.txt");
//        try {
//            sample_file.createNewFile();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        //Log.d("JKL",getFilesDir().toString());
        /*FilenameFilter filter = new FilenameFilter() {
            @Override
            public boolean accept(File f, String name) {
                return name.endsWith(".csv");
            }
        };
        // apply the filter
        String[] pathNames = f.list(filter);
        */

        String[] pathNames = f.list();
        if(pathNames == null){
            //pathNames = new String[]{Environment.getExternalStorageDirectory().toString()+"/LOG_GPS/"};
            pathNames = new String[]{"Could not access directory"};
        }
        for(String s : pathNames){
            Log.d("PATHNAMES", "asdf: "+s);
        }
        ListView listView = (ListView)findViewById( R.id.list );
        final ArrayList<String> listItems = new ArrayList<String>(Arrays.asList(pathNames));
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>( this, android.R.layout.simple_list_item_1, listItems );
        listView.setAdapter( adapter );

    }
//    public final String[] EXTERNAL_PERMS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
//
//    public final int EXTERNAL_REQUEST = 138;
//
//    public boolean requestForPermission() {
//
//        boolean isPermissionOn = true;
//        final int version = Build.VERSION.SDK_INT;
//        if (version >= 23) {
//            if (!canAccessExternalSd()) {
//                isPermissionOn = false;
//                requestPermissions(EXTERNAL_PERMS, EXTERNAL_REQUEST);
//            }
//        }
//        return isPermissionOn;
//    }
//
//    public boolean canAccessExternalSd() {
//        String msg = "asdjlkfld";
//        if(hasPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)){
//            msg = "iojeiorj";
//        }
//        Log.d("PERMISSIONS", "klasklfdklasdhfklhasdnkjlfhn");
//        return (hasPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE));
//    }
//
//    private boolean hasPermission(String perm) {
//        return (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, perm));
//    }
}
