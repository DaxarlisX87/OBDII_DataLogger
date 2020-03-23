import os
import glob
import time
import RPi.GPIO as GPIO
import datalog_lib
from bluetooth import *

os.system('modprobe w1-gpio')
os.system('modprobe w1-therm')

#GPIO.setmode(GPIO.BCM)
#GPIO.setup(17, GPIO.OUT)

#base_dir = '/sys/bus/w1/devices/'
#device_folder = glob.glob(base_dir + '28*')[0]
#device_file = device_folder + '/w1_slave'

#def read_temp_raw():
#	f = open(device_file, 'r')
#	lines = f.readlines()
#	f.close()
#	return lines

#def read_temp():
#	lines = read_temp_raw()
#	while lines[0].strip()[-3:] != 'YES':
#		time.sleep(0.2)
#		lines = read_temp_raw()
#	equals_pos = lines[1].find('t=')
#	if equals_pos != -1:
#		temp_string = lines[1][equals_pos+2:]
#		temp_c = float(temp_string) / 1000.0
#		temp_f = temp_c * 9.0 / 5.0 + 32.0
#		return temp_c
	
#while True:
#	print(read_temp())	
#	time.sleep(1)


server_sock=BluetoothSocket( RFCOMM )
server_sock.bind(("",PORT_ANY))
server_sock.listen(1)

port = server_sock.getsockname()[1]

uuid = "94f39d29-7d6d-437d-973b-fba39e49d4ee"

advertise_service( server_sock, "DataLoggerPiServer",
				   service_id = uuid,
				   service_classes = [ uuid, SERIAL_PORT_CLASS ],
				   profiles = [ SERIAL_PORT_PROFILE ], 
#				   protocols = [ OBEX_UUID ] 
					)
while True:		  
	print("Waiting for connection on RFCOMM channel " + str(port) + "\n")

	client_sock, client_info = server_sock.accept()
	print("Accepted connection from " + str(client_info))

	try:
		data = client_sock.recv(1024)
		if len(data) == 0: break
		data = data.decode()
		print("received ", data)
		
		#if data == 'temp':
		#	data = str(read_temp())+'!'
		#elif data == 'lightOn':
		#	GPIO.output(17,False)
		#	data = 'light on!'
		#elif data == 'lightOff':
		#	GPIO.output(17,True)
		#	data = 'light off!'
		
		data = data.split()
		command = data[0]
		if command == 'connect!':
			print("Connecting to Car\n")
			if datalog_lib.initializeOBD():
				data = 'car connected!'
			else:
				data = 'connection failed!'
			print(data + "\n")
		elif command == 'start!':
			print("Starting Data Logging\n")
			datalog_lib.startLogging()
			data = 'started!'
		elif command == 'stop!' :
			print("Stopping Data Logging\n")
			datalog_lib.stopLogging()
			data = 'stopped!'
		elif command == 'logfiles!':
			print("Retrieving File List\n")
			filelist = glob.glob("LOG_IMU_OBD/*.txt")
			filenames = []
			for file in filelist:
				file = file.replace("_IMU_OBD.txt", '')
				file = file.replace("LOG_IMU_OBD/", '')
				filenames.append(file)
			data = ' '.join(filenames) + '!'
		elif command == 'download!':
			if(len(data) > 1):
				filename = data[1]
				print("Dowloading File:" + filename + "\n")
				#Download IMU File
				IMU_filename = "LOG_IMU_OBD/" + filename + "_IMU_OBD.txt"
				try:
					downloadFile = open(IMU_filename, "r")
					if downloadFile.mode == 'r':
						IMU_data = downloadFile.read() + "#"
						IMU_data = IMU_data.replace("\EOF", '')
						print(IMU_data)
						#client_sock.send(IMU_data.encode())
					IMU_Found = True
				except IOError:
					print("IMU File Not Found")
					IMU_Found = False
					IMU_data = 'IMU File Not Found#'
					#client_sock.send(data.encode())
				finally:
					downloadFile.close()

				#Recieve Continue
				#print(client_sock.recv(1024))
				#Download GPS File
				GPS_filename = "LOG_GPS/" + filename + "_GPS.txt"
				try:
					downloadFile = open(GPS_filename, "r")
					if downloadFile.mode == 'r':
						GPS_data = downloadFile.read() + "#"
						GPS_data = GPS_data.replace("\EOF", '')
						print(GPS_data)
						#client_sock.send(GPS_data.encode())
						GPS_Found = True
				except IOError:
					print("GPS File Not Found")
					GPS_data = 'GPS File Not Found#'
					GPS_Found = False
					#client_sock.send(data.encode())
				finally:
					downloadFile.close()
				
				#print(client_sock.recv(1024))
				if(GPS_Found and IMU_Found):
					data = IMU_data + GPS_data + 'Files Downloaded Sucessfully!'
				else:
					data = 'Files Downloaded Failed!'
			else:
				data = 'No File Requested!'
			
		elif command == 'exit!':
			print("Stopping Server")
			datalog_lib.exitProgram()
			exit()
		else:
			data = 'WTF!' 
		print(str(data.encode()) + "\n")
		client_sock.send(data.encode())
		print ("sending " + str(data) + "\n")

	except IOError:
		pass

	except KeyboardInterrupt:

		print("disconnected\n")

		client_sock.close()
		server_sock.close()
		print("all done\n")

		break
