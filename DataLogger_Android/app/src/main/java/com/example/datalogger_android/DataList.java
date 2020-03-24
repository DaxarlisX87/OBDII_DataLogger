package com.example.datalogger_android;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

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
        setContentView(R.layout.activity_data_list);

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

        String[] pathNames = f.list();
        if(pathNames == null){
            //pathNames = new String[]{Environment.getExternalStorageDirectory().toString()+"/LOG_GPS/"};
            pathNames = new String[]{"Could not access directory"};
        }
        for(String s : pathNames){
            Log.d("PATHNAMES", "asdf: "+s);
        }
        ListView listView = (ListView)findViewById( R.id.list );
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                //ListView listv = findViewById(R.id.list);
                //TextView item = (TextView) listv.getChildAt(position);
                //String item_str = item.getText().toString();

                goToMapView(selectedItem);
            }
        });

        final ArrayList<String> listItems = new ArrayList<String>(Arrays.asList(pathNames));
        final ArrayAdapter<String> adapter = new ArrayAdapter<String>( this, android.R.layout.simple_list_item_1, listItems );
        listView.setAdapter( adapter );

    }
    private void goToMapView(String filename) {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("filename", filename);
        startActivity(intent);
    }
}
