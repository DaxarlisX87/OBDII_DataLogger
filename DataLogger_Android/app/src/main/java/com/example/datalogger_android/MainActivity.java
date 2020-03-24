package com.example.datalogger_android;

import android.os.Build;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Environment;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.content.Intent;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

// For Bluetooth Connectivity
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Set;
import java.util.UUID;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice = null;

    final byte delimiter = 33;
    int readBufferPosition = 0;
    private ArrayList<String> Filelist;
    private String[] DownloadMessages;
    private String downloadFilename;


    public void sendBtMsg(String msg2send){
        //UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb"); //Standard SerialPortService ID
        UUID uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee"); //Standard SerialPortService ID
        try {

            mmSocket = mmDevice.createRfcommSocketToServiceRecord(uuid);
            if (!mmSocket.isConnected()){
                mmSocket.connect();
            }

            String msg = msg2send;
            //msg += "\n";
            OutputStream mmOutputStream = mmSocket.getOutputStream();
            mmOutputStream.write(msg.getBytes());

        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final Handler handler = new Handler();
        final TextView myLabel = (TextView) findViewById(R.id.btResult);


        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Button files = findViewById(R.id.list_button);
        Button download = findViewById(R.id.download_button);
        download.setEnabled(false);
        Button connect = findViewById(R.id.connect_button);
        Button mapBttn = findViewById(R.id.mapButton);
        Button viewDownloaded = findViewById(R.id.viewDownloaded);

        final class workerThread implements Runnable {

            private String btMsg;

            public workerThread(String msg) {
                btMsg = msg;
            }

            public void run()
            {
                sendBtMsg(btMsg);
                while(!Thread.currentThread().isInterrupted())
                {
                    int bytesAvailable;
                    boolean workDone = false;

                    try {



                        final InputStream mmInputStream;
                        mmInputStream = mmSocket.getInputStream();
                        bytesAvailable = mmInputStream.available();
                        if(bytesAvailable > 0)
                        {

                            byte[] packetBytes = new byte[bytesAvailable];
                            Log.e("DataLogger recv bt","bytes available" + bytesAvailable);
                            byte[] readBuffer = new byte[1024];
                            mmInputStream.read(packetBytes);

                            for(int i=0;i<bytesAvailable;i++)
                            {
                                byte b = packetBytes[i];
                                if(b == delimiter)
                                {
                                    Log.e("DataLogger recv bt","delimiter reached at:" + i);
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    final String data = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;

                                    //The variable data now contains our full command

                                    if(btMsg.equals("logfiles!")) Filelist = new ArrayList<String>(Arrays.asList(data.split(" ")));
                                    if(btMsg.contains("download")) {
                                        DownloadMessages = data.split("#");
                                        String filename =  downloadFilename + "_IMU_OBD.txt";
                                        String filepath = "/Download/DataLoggerPi/";

                                        File file = new File(filepath, "LOG_IMU_OBD");

                                        if (!file.exists()) {
                                            file.mkdirs();
                                            Log.e("DataLogger mkdirs", filepath + "LOG_IMU_OBD");
                                        }
                                        try {
                                            File gpxfile = new File(file, filename);
                                            FileWriter writer = new FileWriter(gpxfile);
                                            writer.append(DownloadMessages[0]);
                                            writer.flush();
                                            writer.close();
                                        } catch (Exception e) { }

                                        //Write GPS Data
                                        filepath =  downloadFilename + "_GPS.txt";

                                        file = new File(filepath, "LOG_GPS");
                                        if (!file.exists()) {
                                            file.mkdirs();
                                        }
                                        try {
                                            File gpxfile = new File(file, filename);
                                            FileWriter writer = new FileWriter(gpxfile);
                                            writer.append(DownloadMessages[1]);
                                            writer.flush();
                                            writer.close();
                                        } catch (Exception e) { }

                                    }
                                    handler.post(new Runnable()
                                    {
                                        public void run()
                                        {
                                            if(btMsg.equals("logfiles!")) {
                                                if(Filelist == null) {
                                                    myLabel.setText(R.string.no_files_found);
                                                    Log.e("DataLogger Crt List", "No Files Found");
                                                }
                                                else Log.e("DataLogger Crt List", Filelist.toString());
                                            }
                                            else if(btMsg.contains("download!")) {
                                                myLabel.setText(DownloadMessages[2]);
                                            }
                                            else myLabel.setText(data);
                                        }
                                    });

                                    workDone = true;
                                    break;


                                }
                                else
                                {
                                    readBuffer[readBufferPosition++] = b;
                                }
                            }

                            if (workDone == true){
                                mmSocket.close();
                                break;
                            }

                        }
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                }
            }
        }

        viewDownloaded.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToDownloadListView();
            }
        });

        files.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread fileThread = (new Thread(new workerThread("logfiles!")));
                fileThread.start();
                try {
                    fileThread.join();
                }
                catch (Exception e){
                }
                goToFileDownload(Filelist);
            }
        });

        download.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Thread fileThread = (new Thread(new workerThread("download! " + downloadFilename)));
                fileThread.start();
                try {
                    fileThread.join();
                }
                catch (Exception e){
                }

            }
        });
        mapBttn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                goToMapView();
            }
        });

        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                (new Thread(new workerThread("connect!"))).start();
            }
        });

        ToggleButton toggle = (ToggleButton) findViewById(R.id.toggleButton);
        toggle.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    // The toggle is enabled
                    (new Thread(new workerThread("start!"))).start();
                } else {
                    // The toggle is disabled
                    (new Thread(new workerThread("stop!"))).start();

                }
            }
        });

        if(mBluetoothAdapter != null)
        {
            if(!mBluetoothAdapter.isEnabled())
            {
                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBluetooth, 0);
            }

            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
            if(pairedDevices.size() > 0)
            {
                for(BluetoothDevice device : pairedDevices)
                {
                    if(device.getName().equals("DataLoggerPi")) //Note, you will need to change this to match the name of your device
                    {
                        Log.e("Aquarium",device.getName());
                        mmDevice = device;
                        break;
                    }
                }
            }
        }



    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void goToFileDownload(ArrayList<String> availableFiles) {
        Log.e("DataLogger FileList", availableFiles.toString());
        Intent intent = new Intent(this, DownloadActivity.class);
        intent.putStringArrayListExtra("Files", availableFiles);

        startActivityForResult(intent, 1);
    }

    private void goToMapView() {
        Intent intent = new Intent(this, MapsActivity.class);

        startActivity(intent);
    }

    private void goToDownloadListView() {
        Intent intent = new Intent(this, DataList.class);

        startActivity(intent);
    }

    @Override
    protected void onActivityResult (int requestCode, int resultCode, Intent data){//..code}
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if(resultCode == RESULT_OK) {
                final TextView myLabel = (TextView) findViewById(R.id.btResult);
                String result = data.getStringExtra("result");
                Log.e("DataLogger FileList", result);
                myLabel.setText(result);
                downloadFilename = result;
                Button download = findViewById(R.id.download_button);
                download.setEnabled(true);
            }
            if(resultCode == RESULT_CANCELED) {
                downloadFilename = null;
            }

        }
    }

    public void generateNoteOnSD(String sFileName, String sBody) {
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "Notes");
            if (!root.exists()) {
                root.mkdirs();
            }
            File gpxfile = new File(root, sFileName);
            FileWriter writer = new FileWriter(gpxfile);
            writer.append(sBody);
            writer.flush();
            writer.close();
            Toast.makeText(this, "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
