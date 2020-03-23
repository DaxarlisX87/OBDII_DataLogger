package com.example.datalogger_android;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
//import android.support.v7.app.AppCompatActivity;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.content.Intent;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

public class DownloadActivity<DownloadActvity> extends AppCompatActivity {

    BluetoothSocket mmSocket;
    BluetoothDevice mmDevice = null;

    final byte delimiter = 33;
    int readBufferPosition = 0;
    private String downloadStatus;

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
        setContentView(R.layout.activity_download);

        final ListView list = findViewById(R.id.list);
        ArrayList<String> arrayList = getIntent().getStringArrayListExtra("Files");
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, arrayList);
        list.setAdapter(arrayAdapter);

        final Handler handler = new Handler();
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

//        final class downloadThread implements Runnable {
//
//            private String btMsg;
//            private String filename;
//            BufferedWriter IMU_File;
//            BufferedWriter GPS_File;
//            public downloadThread(String msg, String Filename) {
//                btMsg = msg;
//                filename = Filename;
//            }
//
//            @RequiresApi(api = Build.VERSION_CODES.O)
//            public void run()
//            {
//                sendBtMsg(btMsg + filename);
//                while(!Thread.currentThread().isInterrupted())
//                {
//                    int bytesAvailable;
//                    boolean workDone = false;
//
//                    try {
//
//
//                        for(int download_count = 0; download_count < 2; download_count++) {
//                            final InputStream mmInputStream;
//                            mmInputStream = mmSocket.getInputStream();
//                            OutputStream mmOutputStream = mmSocket.getOutputStream();
//                            String msg = "Continue";
//
//                            bytesAvailable = mmInputStream.available();
//                            if(bytesAvailable > 0) {
//
//                                byte[] packetBytes = new byte[bytesAvailable];
//                                Log.e("DataLogger recv bt", "bytes available" + bytesAvailable);
//                                byte[] readBuffer = new byte[1024];
//                                mmInputStream.read(packetBytes);
//
//
//                                for (int i = 0; i < bytesAvailable; i++) {
//                                    byte b = packetBytes[i];
//                                    if (b == delimiter) {
//                                        download_count++;
//                                        Log.e("DataLogger recv bt", "delimiter reached at:" + i);
//                                        byte[] encodedBytes = new byte[readBufferPosition];
//                                        System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
//                                        final String data = new String(encodedBytes, "US-ASCII");
//                                        readBufferPosition = 0;
//
//                                        //The variable data now contains our full command
//
//                                        //Store String To File
//                                        if(download_count == 0) {
//                                            try {
//                                                if(!data.equals("IMU File Not Found")) {
//                                                    Log.e("DataLogger IMU_LOG", "Creating LogFile");
//                                                    //Path path = Paths.get("LOG_IMU_OBD");
//                                                    //if (!Files.exists(path)) {
//                                                    //    Files.createDirectory(path);
//                                                    //}
//                                                    generateNoteOnSD(filename + "_IMU_OBD.txt", data);
//                                                    //IMU_File = new BufferedWriter(new FileWriter("LOG_IMU_OBD/" + filename + "_IMU_OBD.txt"));
//                                                    //IMU_File.write(data);
//                                                    //IMU_File.close();
//                                                }
//                                            }
//                                            catch(Exception e) {
//                                                Log.e("DataLogger IMU_LOG", "Failed to Create File:" + "LOG_IMU_OBD/" + filename + "_IMU_OBD.txt");
//                                            }
//
//                                            break;
//
//                                            //mmOutputStream.write(msg.getBytes());
//                                        }
//                                        else if(download_count == 1) {
//                                            try {
//                                                Log.e("DataLogger GPS_LOG", "Creating LogFile");
//                                                if(!data.equals("GPS File Not Found")) {
//                                                    //Path path = Paths.get("GPS");
//                                                    //if (!Files.exists(path)) {
//                                                    //    Files.createDirectory(path);
//                                                    //}
//                                                    //GPS_File = new BufferedWriter(new FileWriter("LOG_GPS/" + filename + "_GPS.txt"));
//                                                    //GPS_File.write(data);
//                                                    //GPS_File.close();
//                                                    generateNoteOnSD(filename + "_GPS.txt", data);
//                                                }
//                                            }
//                                            catch(Exception e) {
//                                                Log.e("DataLogger GPS_LOG", "Failed to Create File:" + "LOG_GPS/" + filename + "_GPS.txt");
//                                            }
//                                            break;
//                                            //mmOutputStream.write(msg.getBytes());
//
//                                        }
//                                        else {
//                                            downloadStatus = data;
//                                            handler.post(new Runnable() {
//                                                public void run() {
//                                                    Toast.makeText(DownloadActivity.this,data,Toast.LENGTH_SHORT).show();
//                                                }
//                                            });
//                                            workDone = true;
//
//                                            break;
//                                        }
//
//
//
//
//
//
//                                    } else {
//                                        readBuffer[readBufferPosition++] = b;
//                                    }
//                                }
//
//                                if (workDone == true) {
//                                    mmSocket.close();
//                                    break;
//                                }
//                            }
//
//                        }
//                    } catch (IOException e) {
//                        // TODO Auto-generated catch block
//                        e.printStackTrace();
//                    }
//
//                }
//            }
//        }

        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String clickedItem=(String) list.getItemAtPosition(position);
                Toast.makeText(DownloadActivity.this,clickedItem,Toast.LENGTH_SHORT).show();
                //(new Thread(new downloadThread("download! ", clickedItem))).start();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("result", clickedItem);
                setResult(RESULT_OK, resultIntent);
                finish();

            }
        });

//        if(mBluetoothAdapter != null)
//        {
//            if(!mBluetoothAdapter.isEnabled())
//            {
//                Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                startActivityForResult(enableBluetooth, 0);
//            }
//
//            Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
//            if(pairedDevices.size() > 0)
//            {
//                for(BluetoothDevice device : pairedDevices)
//                {
//                    if(device.getName().equals("DataLoggerPi")) //Note, you will need to change this to match the name of your device
//                    {
//                        Log.e("Aquarium",device.getName());
//                        mmDevice = device;
//                        break;
//                    }
//                }
//            }
//        }

    }

}