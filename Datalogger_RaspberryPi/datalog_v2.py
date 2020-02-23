import obd
import os
import time
import IMU
import datetime
import threading
import serial

port = "/dev/serial0"

def parseGPS(data):
    global logfile2
#    print(data)
#    time.sleep(1)
    if data[3:6] == "GGA":
#        print("GGA")
        _, _, lat, _, lon, _, _, _, _, altitude, _, _, _, _, _ = data.split(",")
        logfile2.write(str(time.time()) + "," + lat + "," + lon + "," + altitude + "\n")
        #logfile2.write("GPS Lat " + lat + " degrees\n")
        #logfile2.write("GPS Lon " + lon + " degrees\n")
        #logfile2.write("GPS Altitude " + altitude + " degrees\n")

cmdSpeed = obd.commands.SPEED
cmdRPM = obd.commands.RPM
cmdTPos = obd.commands.THROTTLE_POS
cmdEngLoad = obd.commands.ENGINE_LOAD
cmdTimingAdv = obd.commands.TIMING_ADVANCE
cmdMAF = obd.commands.MAF
period = 0.75
ACC_SCALE = 0.244144

def logData():
    global logfile
    global carPort
    global thread_h
    global period
    global ACC_SCALE

    global cmdSpeed
    global cmdRPM
    global cmdTPos
    global cmdEngLoad
    global cmdTimingAdv
    global cmdMAF

    thread_h = threading.Timer(period,logData)
    thread_h.start()

    #Get obd2 data
    speed = carPort.query(cmdSpeed).value.to("mph").magnitude
    rpm = carPort.query(cmdRPM).value.magnitude
    throttlePos = carPort.query(cmdTPos).value.magnitude
    engineLoad = carPort.query(cmdEngLoad).value.magnitude
    #fuelLevel = carPort.query(cmdFuelLevel).value
    #fuelPress = carPort.query(cmdFuelPress).value
    #intakePress = carPort.query(cmdIntakePress).value
    timingAdv = carPort.query(cmdTimingAdv).value.magnitude
    MAF = carPort.query(cmdMAF).value.magnitude
    #fuelRate = carPort.query(cmdFuelRate).value
    #Get IMU Data
    ACCx = ACC_SCALE * IMU.readACCx()
    ACCy = ACC_SCALE * IMU.readACCy()
    ACCz = ACC_SCALE * IMU.readACCz()
    #Write Logfile
    logfile.write(str(time.time()) + "," + str(speed) + "," + str(rpm) + "," + str(throttlePos) + "," + str(engineLoad) + "," + str(timingAdv) + "," + str(MAF) + "," + str(ACCx) + "," str(ACCy) + "," + str(ACCz) + "\n")
#    logfile.write("Pi Time " + str(time.time()) + " seconds\n")
#    logfile.write("OBD Speed " + str(speed) + "\n")
#    logfile.write("OBD RPM " + str(rpm) + "\n")
#    logfile.write("OBD TPS " + str(throttlePos) + "\n")
#    logfile.write("OBD EngineLoad: " + str(engineLoad) + "\n")
    #logfile.write("OBD FuelLevel: " + str(fuelLevel) + "\n")
    #logfile.write("OBD FuelPressure: " + str(fuelPress) + "\n")
    #logfile.write("OBD IntakePressure: " + str(intakePress) + "\n")
#    logfile.write("OBD TimingAdvance " + str(timingAdv) + "\n")
#    logfile.write("OBD MAF " + str(MAF) + "\n")
    #logfile.write("OBD FuelRate: " + str(fuelRate) + "\n")
#    logfile.write("IMU ACCx " + str(ACCx) + " milli-Gs\n")
#    logfile.write("IMU ACCy " + str(ACCy) + " milli-Gs\n")
#    logfile.write("IMU ACCz " + str(ACCz) + " milli-Gs\n")
    #print("IMU ACCx " + str(ACCx) + " milli-Gs\n")
    #print("IMU ACCy " + str(ACCy) + " milli-Gs\n")
    #print("IMU ACCz " + str(ACCz) + " milli-Gs\n")


#sampling options
#num_loops = 100
#num_commands = 6

#OpenCarport and OutputFiles
carPort = obd.OBD()
if not carPort.is_connected():
    exit()

datetime_str = str(datetime.datetime.now())
logfile = open("logfile_IMU_OBD" + datetime_str + ".txt","w")
logfile2 = open("logfile_GPS" + datetime_str + ".txt","w")
logfile.write("Time,Speed,RPM,Throttle Position,Engine Load,Timing Advance,MAF,ACCx,ACCy,ACCz\n")
logfile2.write("Time,Latitude,Longitude,Altitude\n")


#Initialize IMU
IMU.detectIMU()
IMU.initIMU()

print("start\n")
#start_time = time.time()
thread_h = threading.Timer(period,logData)
thread_h.start()
time.sleep(1)
ser = serial.Serial(port, baudrate = 9600)

#duration = time.time() - start_time
#commands_per_sec = num_loops*num_commands/duration
#print("commands per sec = " + str(commands_per_sec) + "\n")
#logfile.write("commands per sec = " + str(commands_per_sec) + "\n")

try:
    while(True):
        data = ser.readline()
        parseGPS(data)
except KeyboardInterrupt:
    thread_h.cancel()
    time.sleep(period)
    logfile.close()
    logfile2.close()

