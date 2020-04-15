package com.example.datalogger_android;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;

public class DrivingAnalyzer {

    static public ArrayList<DrivingEvent> drivingAnalysis(ArrayList<ArrayList<Double>> IMU_OBDII_Data, ArrayList<ArrayList<Double>> GPS_Data) {

        ArrayList<DrivingEvent> drivingEvents = new ArrayList<DrivingEvent>();

        String thisLine = null;
        final int windowSize = 10;
        final int windowStep = 2; // ensure step evenly divides size
        double[] speedArr = new double[windowSize+windowStep];
        double[] rpmArr = new double[windowSize+windowStep];
        double[] quotientArr = new double[windowSize+windowStep];

        int position = 0;

        double badMPGValue = 105;
        double launchLimit = 500;
        int zeroSpeed = 0;

//        System.out.println("Performing Analysis");

        //Create First Window
        int i;


        for(i = 0; (i < windowSize) && (position < IMU_OBDII_Data.size()); i++){
            speedArr[i] = IMU_OBDII_Data.get(position).get(1);
            rpmArr[i] = IMU_OBDII_Data.get(position).get(2);
            quotientArr[i] = rpmArr[i]/ (speedArr[i] + 0.1);

            position++;

//            System.out.println("Speed: " + speedArr[i] + " RPM: " + rpmArr[i]);
            if(speedArr[i] == 0) {
//                System.out.println("At Zero Speed");
                zeroSpeed = windowSize - windowStep;
            }
            if(zeroSpeed > 0) {
                if (IMU_OBDII_Data.get(i).get(7) > launchLimit) {
                    double eventTime = IMU_OBDII_Data.get(i).get(0);

                    double timeDifference = Integer.MAX_VALUE;
                    int index = 0;
                    int eventIndex = 0;

                    for(ArrayList list: GPS_Data) {
                        double gpsTime = (double) list.get(0);
                        double newDifference = Math.abs(eventTime - gpsTime);
                        if( eventTime - gpsTime < timeDifference) {
                            timeDifference = newDifference;
                            eventIndex = index;
                            if(newDifference < 0) {
                                drivingEvents.add(new DrivingEvent(GPS_Data.get(eventIndex).get(0), GPS_Data.get(eventIndex).get(1), GPS_Data.get(eventIndex).get(2), "Launch"));
                                break;
                            }
                        }
                        else if(newDifference < 0) {
                            drivingEvents.add(new DrivingEvent(GPS_Data.get(eventIndex).get(0), GPS_Data.get(eventIndex).get(1), GPS_Data.get(eventIndex).get(2), "Launch"));
                        }

                        index++;

                    }
                }
            }
        }
        double oldAverage;
        double averageQuotient = windowAverage(quotientArr, windowSize);
        int arrStart = windowStep;

        //Update Window
        while(true){
            //System.out.println("position = " + position);
            if(position >= IMU_OBDII_Data.size()) {
                break;
            }
            for(int j = 0; (j < windowStep) && (position < IMU_OBDII_Data.size()); j++){
                speedArr[i+j] = IMU_OBDII_Data.get(position).get(1);
                rpmArr[i+j] = IMU_OBDII_Data.get(position).get(2);
                quotientArr[i +j] = quotientArr[i+j] = rpmArr[i]/ (speedArr[i+j] + 0.1);
                //System.out.println("Speed: " + speedArr[i+j] + " RPM: " + rpmArr[i+j]);
                position++;

                if(speedArr[i+j] == 0) {
                    zeroSpeed = windowSize / 2;
                }
                if(zeroSpeed > 0) {
                    if (IMU_OBDII_Data.get(i+j).get(7) > launchLimit) {
//                        System.out.println("LaunchDetected");
                        double eventTime = IMU_OBDII_Data.get(position - 1).get(0);
                        double timeDifference = Integer.MAX_VALUE;
                        int index = 0;
                        int eventIndex = 0;

                        for(ArrayList list: GPS_Data) {
                            double gpsTime = (double) list.get(0);
                            //System.out.println("EventTime: " + eventTime + " GPSTime: " + gpsTime);
                            double newDifference = Math.abs(eventTime - gpsTime);
                            if( newDifference < timeDifference) {
                                //System.out.println("TimeDifference:" + timeDifference + " NewDifference: " + newDifference);
                                timeDifference = newDifference;
                                eventIndex = index;
                    			/*if(newDifference < 0) {
                    				System.out.println("Adding Event at: " + eventIndex);
                    				drivingEvents.add(new DrivingEvent(GPS_Data.get(eventIndex).get(0), GPS_Data.get(eventIndex).get(1), GPS_Data.get(eventIndex).get(2), "Launch"));
                    				break;
                    			}*/
                            }
                            else {
//                                System.out.println("Adding Event at: " + eventIndex);
                                drivingEvents.add(new DrivingEvent(GPS_Data.get(eventIndex).get(0), GPS_Data.get(eventIndex).get(1), GPS_Data.get(eventIndex).get(2), "Launch"));
                                break;
                            }

                            index++;

                        }
                    }
                }

            }
            i = (i + windowStep) % (windowSize+windowStep);
            zeroSpeed--;


            oldAverage = averageQuotient;
            averageQuotient = updateWindowAverage(quotientArr, averageQuotient, windowSize, windowStep, arrStart, i);
            arrStart = (arrStart + windowStep) % (windowSize+windowStep);

//            System.out.println("AverageQuotient: " + averageQuotient);
            if(averageQuotient > badMPGValue) {
//                System.out.println("Bad MPG Detected");
                double eventTime = IMU_OBDII_Data.get(position - 1).get(0);
                double timeDifference = Integer.MAX_VALUE;
                int index = 0;
                int eventIndex = 0;

                for(ArrayList list: GPS_Data) {
                    double gpsTime = (double) list.get(0);
                    //System.out.println("EventTime: " + eventTime + " GPSTime: " + gpsTime);
                    double newDifference = Math.abs(eventTime - gpsTime);
                    if( newDifference < timeDifference) {
                        //System.out.println("TimeDifference:" + timeDifference + " NewDifference: " + newDifference);
                        timeDifference = newDifference;
                        eventIndex = index;
            			/*if(newDifference < 0) {
            				System.out.println("Adding Event at: " + eventIndex);
            				drivingEvents.add(new DrivingEvent(GPS_Data.get(eventIndex).get(0), GPS_Data.get(eventIndex).get(1), GPS_Data.get(eventIndex).get(2), "Launch"));
            				break;
            			}*/
                    }
                    else {
//                        System.out.println("Adding Event at: " + eventIndex);
                        //Log.d("DRIVEEVENT", "added at "+eventIndex+" because "+averageQuotient);
                        drivingEvents.add(new DrivingEvent(GPS_Data.get(eventIndex).get(0), GPS_Data.get(eventIndex).get(1), GPS_Data.get(eventIndex).get(2), "BadMPG"));
                        break;
                    }

                    index++;

                }
            }
        }


        return drivingEvents;
    }



    public static double parseLine(String line){
        String[] dataArr = line.split(",", 0);
        double speed = Double.parseDouble(dataArr[1]);
        double rpm = Double.parseDouble(dataArr[2]);
        return rpm/speed;
    }

    public static double windowAverage(double[] dataArr, int windowSize){
        double sum = 0;
        for(int i = 0; i < windowSize; i++){
            sum += dataArr[i];
        }
        return sum/windowSize;
    }

    public static double updateWindowAverage(double[] dataArr, double oldAverage, int windowSize, int windowStep, int arrStart, int replaceStart){
        //Log.d("WINDOWING", "start "+arrStart+" i "+replaceStart);
        double sum = 0;
        for(int i = 0; i < windowStep; i++){
            sum += dataArr[i+replaceStart];
        }
        sum /= windowSize;
        double newAverage = oldAverage - sum;
        sum = 0;
        for(int i = 0; i < windowStep; i++){
            sum += dataArr[i+((arrStart + windowSize - windowStep) % (windowSize + windowStep))];
        }
        sum /= windowSize;
        return newAverage + sum;
    }

    public static double windowAverageQuotient(double[] data1Arr, double[] data2Arr, int windowSize){
        double sum = 0;
        for(int i = 0; i < windowSize; i++){
            sum += data2Arr[i]/ (data1Arr[i] + 0.1);
        }
        return sum/windowSize;
    }

    public static double updateWindowQuotient(double[] data1Arr, double[] data2Arr, double oldAverage, int windowSize, int windowStep, int arrStart, int replaceStart){
        double sum = 0;
        for(int i = 0; i < windowStep; i++){
            sum += data2Arr[i]/ (data1Arr[i] + 0.1);
        }

        sum /= windowSize;
        double newAverage = oldAverage - sum;
        sum = 0;
        for(int i = 0; i < windowStep; i++){
            sum += data2Arr[i+((arrStart + windowSize - windowStep) % (windowSize + windowStep))]/ (data1Arr[i+((arrStart + windowSize - windowStep) % (windowSize + windowStep))] + 0.1);
        }
        sum /= windowSize;
        return newAverage + sum;
    }

}