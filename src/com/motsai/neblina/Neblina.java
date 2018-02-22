package com.motsai.neblina;

/**********************************************************************************/

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;
import java.util.UUID;
import java.lang.String;

import static android.bluetooth.BluetoothGattCharacteristic.*;

/**********************************************************************************/

public class Neblina  {
    //NEBLINA CUSTOM UUIDs
    public static final UUID NEB_SERVICE_UUID  = UUID.fromString("0df9f021-1532-11e5-8960-0002a5d5c51b");
    public static final UUID NEB_DATACHAR_UUID = UUID.fromString("0df9f022-1532-11e5-8960-0002a5d5c51b");
    public static final UUID NEB_CTRLCHAR_UUID = UUID.fromString("0df9f023-1532-11e5-8960-0002a5d5c51b");

    // Packet types
    public static final byte NEBLINA_PACKET_TYPE_RESPONSE		    = 0;		// Response
    public static final byte NEBLINA_PACKET_TYPE_ACK		        = 1;		// Ack
    public static final byte NEBLINA_PACKET_TYPE_COMMAND		    = 2;		// Command
    public static final byte NEBLINA_PACKET_TYPE_DATA	            = 3;
    public static final byte NEBLINA_PACKET_TYPE_ERROR		        = 4;		// Error response
    public static final byte NEBLINA_PACKET_TYPE_RESERVE_2	        = 5;		//
    public static final byte NEBLINA_PACKET_TYPE_REQUEST_LOG	    = 6;		// Request status/error log
    public static final byte NEBLINA_PACKET_TYPE_RESERVE_3	        = 7;

    // Subsystem values
    public static final byte NEBLINA_SUBSYSTEM_GENERAL		        = 0;		// Status & logging
    public static final byte NEBLINA_SUBSYSTEM_FUSION	            = 1;		// Motion Engine
    public static final byte NEBLINA_SUBSYSTEM_POWER	            = 2;		// Power management
    public static final byte NEBLINA_SUBSYSTEM_GPIO		            = 3;		// GPIO control
    public static final byte NEBLINA_SUBSYSTEM_LED		            = 4;		// LED control
    public static final byte NEBLINA_SUBSYSTEM_ADC		            = 5;		// ADC control
    public static final byte NEBLINA_SUBSYSTEM_DAC		            = 6;		// DAC control
    public static final byte NEBLINA_SUBSYSTEM_I2C		            = 7;		// I2C control
    public static final byte NEBLINA_SUBSYSTEM_SPI		            = 8;		// SPI control
    public static final byte NEBLINA_SUBSYSTEM_DEBUG                = 9;
    public static final byte NEBLINA_SUBSYSTEM_TEST                 = 10;
    public static final byte NEBLINA_SUBSYSTEM_RECORDER             = 11;		//NOR flash memory recorder
    public static final byte NEBLINA_SUBSYSTEM_EEPROM		        = 12;		//small EEPROM storage
    public static final byte NEBLINA_SUBSYSTEM_SENSOR               = 13;

    // ***
    // General subsystem commands
    public static final byte NEBLINA_COMMAND_GENERAL_AUTHENTICATION              = 0x00;
    public static final byte NEBLINA_COMMAND_GENERAL_SYSTEM_STATUS               = 0x01;
    public static final byte NEBLINA_COMMAND_GENERAL_FUSION_STATUS               = 0x02;
    public static final byte NEBLINA_COMMAND_GENERAL_RECORDER_STATUS             = 0x03;
    public static final byte NEBLINA_COMMAND_GENERAL_FIRMWARE_VERSION            = 0x05;
    public static final byte NEBLINA_COMMAND_GENERAL_DEVICE_SHUTDOWN             = 0x06;
    public static final byte NEBLINA_COMMAND_GENERAL_RSSI                        = 0x07;
    public static final byte NEBLINA_COMMAND_GENERAL_INTERFACE_STATUS            = 0x08;
    public static final byte NEBLINA_COMMAND_GENERAL_INTERFACE_STATE             = 0x09;
    public static final byte NEBLINA_COMMAND_GENERAL_POWER_STATUS                = 0x0A;
    public static final byte NEBLINA_COMMAND_GENERAL_SENSOR_STATUS               = 0x0B;
    public static final byte NEBLINA_COMMAND_GENERAL_DISABLE_STREAMING           = 0x0C;
    public static final byte NEBLINA_COMMAND_GENERAL_RESET_TIMESTAMP             = 0x0D;
    public static final byte NEBLINA_COMMAND_GENERAL_FIRMWARE_UPDATE             = 0x0E;
    public static final byte NEBLINA_COMMAND_GENERAL_DEVICE_NAME_GET             = 0x0F;
    public static final byte NEBLINA_COMMAND_GENERAL_DEVICE_NAME_SET             = 0x10;
    public static final byte NEBLINA_COMMAND_GENERAL_SET_UNIX_TIMESTAMP          = 0x11;
    public static final byte NEBLINA_COMMAND_GENERAL_GET_UNIX_TIMESTAMP          = 0x12;
// Reserved command 0x13, 0x14, 0x15 and 0x16 for configurator
    public static final byte NEBLINA_COMMAND_GENERAL_DEVICE_RESET                = 0x17;

    // ***
    // Subsystem fusion commands
    public static final byte NEBLINA_COMMAND_FUSION_RATE                                       = 0x00;
    public static final byte NEBLINA_COMMAND_FUSION_DOWNSAMPLE                                 = 0x01;
    public static final byte NEBLINA_COMMAND_FUSION_MOTION_STATE_STREAM                        = 0x02;
    public static final byte NEBLINA_COMMAND_FUSION_QUATERNION_STREAM                          = 0x04;
    public static final byte NEBLINA_COMMAND_FUSION_EULER_ANGLE_STREAM                         = 0x05;
    public static final byte NEBLINA_COMMAND_FUSION_EXTERNAL_FORCE_STREAM                      = 0x06;
    public static final byte NEBLINA_COMMAND_FUSION_FUSION_TYPE                                = 0x07;
    public static final byte NEBLINA_COMMAND_FUSION_TRAJECTORY_RECORD                          = 0x08;
    public static final byte NEBLINA_COMMAND_FUSION_TRAJECTORY_INFO_STREAM                     = 0x09;
    public static final byte NEBLINA_COMMAND_FUSION_PEDOMETER_STREAM                           = 0x0A;
    public static final byte NEBLINA_COMMAND_FUSION_SITTING_STANDING_STREAM                    = 0x0C;
    public static final byte NEBLINA_COMMAND_FUSION_LOCK_HEADING_REFERENCE                     = 0x0D; /// obsolete
    public static final byte NEBLINA_COMMAND_FUSION_FINGER_GESTURE_STREAM                      = 0x11;
    public static final byte NEBLINA_COMMAND_FUSION_ROTATION_INFO_STREAM                       = 0x12;
    public static final byte NEBLINA_COMMAND_FUSION_EXTERNAL_HEADING_CORRECTION                = 0x13;
    public static final byte NEBLINA_COMMAND_FUSION_ANALYSIS_RESET                             = 0x14;
    public static final byte NEBLINA_COMMAND_FUSION_ANALYSIS_CALIBRATE                         = 0x15;
    public static final byte NEBLINA_COMMAND_FUSION_ANALYSIS_CREATE_POSE                       = 0x16;
    public static final byte NEBLINA_COMMAND_FUSION_ANALYSIS_SET_ACTIVE_POSE                   = 0x17;
    public static final byte NEBLINA_COMMAND_FUSION_ANALYSIS_GET_ACTIVE_POSE                   = 0x18;
    public static final byte NEBLINA_COMMAND_FUSION_ANALYSIS_STREAM                            = 0x19;
    public static final byte NEBLINA_COMMAND_FUSION_ANALYSIS_POSE_INFO                         = 0x1A;
    public static final byte NEBLINA_COMMAND_FUSION_CALIBRATE_FORWARD_POSITION                 = 0x1B;
    public static final byte NEBLINA_COMMAND_FUSION_CALIBRATE_DOWN_POSITION                    = 0x1C;
    public static final byte NEBLINA_COMMAND_FUSION_MOTION_DIRECTION_STREAM                    = 0x1E;
    public static final byte NEBLINA_COMMAND_FUSION_SHOCK_SEGMENT_STREAM                       = 0x1F;
    public static final byte NEBLINA_COMMAND_FUSION_ACCELEROMETER_CALIBRATION_RESET            = 0x20;
    public static final byte NEBLINA_COMMAND_FUSION_ACCELEROMETER_CALIBRATION_SET_NEW_POSITION = 0x21;
    public static final byte NEBLINA_COMMAND_FUSION_CALIBRATED_ACCELEROMETER_STREAM            = 0x22;
    public static final byte NEBLINA_COMMAND_FUSION_INCLINOMETER_CALIBRATE                     = 0x23;
    public static final byte NEBLINA_COMMAND_FUSION_INCLINOMETER_STREAM                        = 0x24;
    public static final byte NEBLINA_COMMAND_FUSION_MAGNETOMETER_AC_STREAM                     = 0x25;
    public static final byte NEBLINA_COMMAND_FUSION_MOTION_INTENSITY_TREND_STREAM              = 0x26;
    public static final byte NEBLINA_COMMAND_FUSION_SET_GOLFSWING_ANALYSIS_MODE                = 0x27;
    public static final byte NEBLINA_COMMAND_FUSION_SET_GOLFSWING_MAXIMUM_ERROR                = 0x28;

    // Power management subsystem command code
    public static final byte NEBLINA_COMMAND_POWER_BATTERY		    = 0;	// Get battery level
    public static final byte NEBLINA_COMMAND_POWER_TEMPERATURE	    = 1;	// Get battery temperature
    public static final byte NEBLINA_COMMAND_POWER_CHARGE_CURRENT   = 2;	// Set battery charge current

    // LED Commands
    public static final byte NEBLINA_COMMAND_LED_STATE              = 1;    // Set LED state
    public static final byte NEBLINA_COMMAND_LED_STATUS             = 2;    // Get LED state

    // Debug subsystem command code
    public static final byte NEBLINA_COMMAND_DEBUG_PRINTF           = 0;	// The infamous printf thing.
    public static final byte NEBLINA_COMMAND_DEBUG_DUMP_DATA        = 1;

    // Eeprom subsystem
    public static final byte NEBLINA_COMMAND_EEPROM_READ            = 1;    // reads 8-byte chunks of data
    public static final byte NEBLINA_COMMAND_EEPROM_WRITE           = 2;    // write 8-bytes of data to the EEPROM

    // Recorder subsystem commands
    public static final byte NEBLINA_COMMAND_RECORDER_ERASE_ALL         = 1;     // erases the whole NOR flash
    public static final byte NEBLINA_COMMAND_RECORDER_RECORD            = 2;     // start or stop recording in a new session
    public static final byte NEBLINA_COMMAND_RECORDER_PLAYBACK          = 3;     // playing back a pre-recorded session: either start or stop
    public static final byte NEBLINA_COMMAND_RECORDER_SESSION_COUNT     = 4;     // a command to get the total number of sessions in the NOR flash recorder
    public static final byte NEBLINA_COMMAND_RECORDER_SESSION_INFO      = 5;     // get the session length of a session ID. The session IDs start from 0 to n-1, where n is the total number of sessions in the NOR flash
    public static final byte NEBLINA_COMMAND_RECORDER_SESSION_READ      = 6;
    public static final byte NEBLINA_COMMAND_RECORDER_SESSION_DOWNLOAD  = 7;
    public static final byte NEBLINA_COMMAND_RECORDER_SESSION_OPEN      = 8;
    public static final byte NEBLINA_COMMAND_RECORDER_SESSION_CLOSE     = 9;
    public static final byte NEBLINA_COMMAND_RECORDER_SESSION_NAME      = 0x0A;


    public static final byte NEBLINA_RECORDER_STATUS_IDLE   = 0x0,
            NEBLINA_RECORDER_STATUS_READ     = 0x01,
            NEBLINA_RECORDER_STATUS_RECORD   = 0x02,
            NEBLINA_RECORDER_STATUS_ERASE    = 0x03,
            NEBLINA_RECORDER_STATUS_DOWNLOAD = 0x04;

    // Sensor subsystem commands
    public static final byte NEBLINA_COMMAND_SENSOR_SET_DOWNSAMPLE                      = 0;
    public static final byte NEBLINA_COMMAND_SENSOR_SET_RANGE                           = 1;
    public static final byte NEBLINA_COMMAND_SENSOR_SET_RATE                            = 2;
    public static final byte NEBLINA_COMMAND_SENSOR_GET_DOWNSAMPLE                      = 3;
    public static final byte NEBLINA_COMMAND_SENSOR_GET_RANGE                           = 4;
    public static final byte NEBLINA_COMMAND_SENSOR_GET_RATE                            = 5;
    public static final byte NEBLINA_COMMAND_SENSOR_ACCELEROMETER_STREAM                = 10;
    public static final byte NEBLINA_COMMAND_SENSOR_GYROSCOPE_STREAM                    = 11;
    public static final byte NEBLINA_COMMAND_SENSOR_HUMIDITY_STREAM                     = 12;
    public static final byte NEBLINA_COMMAND_SENSOR_MAGNETOMETER_STREAM                 = 13;
    public static final byte NEBLINA_COMMAND_SENSOR_PRESSURE_STREAM                     = 14;
    public static final byte NEBLINA_COMMAND_SENSOR_TEMPERATURE_STREAM                  = 15;
    public static final byte NEBLINA_COMMAND_SENSOR_ACCELEROMETER_GYROSCOPE_STREAM      = 16;
    public static final byte NEBLINA_COMMAND_SENSOR_ACCELEROMETER_MAGNETOMETER_STREAM   = 17;

    //
    // Data port control
    public static final byte DATAPORT_MAX	= 2;	// Max number of data port

    public static final byte DATAPORT_BLE	= 0; 	// streaming data port BLE
    public static final byte DATAPORT_UART	= 1;	//

    public static final byte DATAPORT_OPEN	= 1;	// Open streaming data port
    public static final byte DATAPORT_CLOSE	= 0;	// Close streaming data port

    //
    //
    //
    public static final byte NEBLINA_INTERFACE_STATUS_BLE = (1 << DATAPORT_BLE);
    public static final byte NEBLINA_INTERFACE_STATUS_UART = (1 << DATAPORT_UART);

    //
    public static final byte NEBLINA_FUSION_STREAM_CALIBRATED_ACCEL = 0x00,
            NEBLINA_FUSION_STREAM_EULER                     = 0x01,
            NEBLINA_FUSION_STREAM_EXTERNAL_FORCE            = 0x02,
            NEBLINA_FUSION_STREAM_FINGER_GESTURE            = 0x03,
            NEBLINA_FUSION_STREAM_INCLINOMETER              = 0x04,
            NEBLINA_FUSION_STREAM_MAGNETOMETER_AC           = 0x05,
            NEBLINA_FUSION_STREAM_MOTION_ANALYSIS           = 0x06,
            NEBLINA_FUSION_STREAM_MOTION_DIRECTION          = 0x07,
            NEBLINA_FUSION_STREAM_MOTION_INTENSITY_TREND    = 0x08,
            NEBLINA_FUSION_STREAM_MOTION_STATE              = 0x09,
            NEBLINA_FUSION_STREAM_PEDOMETER                 = 0x0a,
            NEBLINA_FUSION_STREAM_QUATERNION                = 0x0b,
            NEBLINA_FUSION_STREAM_ROTATION_INFO             = 0x0c,
            NEBLINA_FUSION_STREAM_SHOCK_SEGMENT             = 0x0d,
            NEBLINA_FUSION_STREAM_SITTING_STANDING          = 0x0e,
            NEBLINA_FUSION_STREAM_TRAJECTORY_INFO           = 0x0f;

    public static final int NEBLINA_FUSION_STATUS_CALIBRATED_ACCEL     = ( 1 << NEBLINA_FUSION_STREAM_CALIBRATED_ACCEL ),
            NEBLINA_FUSION_STATUS_EULER                     = (int)( 1 << NEBLINA_FUSION_STREAM_EULER ),
            NEBLINA_FUSION_STATUS_EXTERNAL_FORCE            = (int)( 1 << NEBLINA_FUSION_STREAM_EXTERNAL_FORCE ),
            NEBLINA_FUSION_STATUS_FINGER_GESTURE            = (int)( 1 << NEBLINA_FUSION_STREAM_FINGER_GESTURE ),
            NEBLINA_FUSION_STATUS_INCLINOMETER              = (int)( 1 << NEBLINA_FUSION_STREAM_INCLINOMETER ),
            NEBLINA_FUSION_STATUS_MAGNETOMETER_AC           = (int)( 1 << NEBLINA_FUSION_STREAM_MAGNETOMETER_AC ),
            NEBLINA_FUSION_STATUS_MOTION_ANALYSIS           = (int)( 1 << NEBLINA_FUSION_STREAM_MOTION_ANALYSIS ),
            NEBLINA_FUSION_STATUS_MOTION_DIRECTION          = (int)( 1 << NEBLINA_FUSION_STREAM_MOTION_DIRECTION ),
            NEBLINA_FUSION_STATUS_MOTION_INTENSITY_TREND    = (int)( 1 << NEBLINA_FUSION_STREAM_MOTION_INTENSITY_TREND ),
            NEBLINA_FUSION_STATUS_MOTION_STATE              = (int)( 1 << NEBLINA_FUSION_STREAM_MOTION_STATE ),
            NEBLINA_FUSION_STATUS_PEDOMETER                 = (int)( 1 << NEBLINA_FUSION_STREAM_PEDOMETER ),
            NEBLINA_FUSION_STATUS_QUATERNION                = (int)( 1 << NEBLINA_FUSION_STREAM_QUATERNION ),
            NEBLINA_FUSION_STATUS_ROTATION_INFO             = (int)( 1 << NEBLINA_FUSION_STREAM_ROTATION_INFO),
            NEBLINA_FUSION_STATUS_SHOCK_SEGMENT             = (int)( 1 << NEBLINA_FUSION_STREAM_SHOCK_SEGMENT ),
            NEBLINA_FUSION_STATUS_SITTING_STANDING          = (int)( 1 << NEBLINA_FUSION_STREAM_SITTING_STANDING),
            NEBLINA_FUSION_STATUS_TRAJECTORY_INFO           = (int)( 1 << NEBLINA_FUSION_STREAM_TRAJECTORY_INFO );

    public static final byte NEBLINA_SENSOR_STREAM_ACCELEROMETER = 0x00,
        NEBLINA_SENSOR_STREAM_ACCELEROMETER_GYROSCOPE    = 0x01,
        NEBLINA_SENSOR_STREAM_ACCELEROMETER_MAGNETOMETER = 0x02,
        NEBLINA_SENSOR_STREAM_GYROSCOPE                  = 0x03,
        NEBLINA_SENSOR_STREAM_HUMIDITY                   = 0x04,
        NEBLINA_SENSOR_STREAM_MAGNETOMETER               = 0x05,
        NEBLINA_SENSOR_STREAM_PRESSURE                   = 0x06,
        NEBLINA_SENSOR_STREAM_TEMPERATURE                = 0x07;

    public static final byte     NEBLINA_SENSOR_STATUS_ACCELEROMETER    = (byte) ( 1 << NEBLINA_SENSOR_STREAM_ACCELEROMETER ),
            NEBLINA_SENSOR_STATUS_ACCELEROMETER_GYROSCOPE    = (byte) ( 1 << NEBLINA_SENSOR_STREAM_ACCELEROMETER_GYROSCOPE ),
            NEBLINA_SENSOR_STATUS_ACCELEROMETER_MAGNETOMETER = (byte) ( 1 << NEBLINA_SENSOR_STREAM_ACCELEROMETER_MAGNETOMETER ),
            NEBLINA_SENSOR_STATUS_GYROSCOPE                  = (byte) ( 1 << NEBLINA_SENSOR_STREAM_GYROSCOPE ),
            NEBLINA_SENSOR_STATUS_HUMIDITY                   = (byte) ( 1 << NEBLINA_SENSOR_STREAM_HUMIDITY ),
            NEBLINA_SENSOR_STATUS_MAGNETOMETER               = (byte) ( 1 << NEBLINA_SENSOR_STREAM_MAGNETOMETER ),
            NEBLINA_SENSOR_STATUS_PRESSURE                   = (byte) ( 1 << NEBLINA_SENSOR_STREAM_PRESSURE ),
            NEBLINA_SENSOR_STATUS_TEMPERATURE                = (byte) ( 1 << NEBLINA_SENSOR_STREAM_TEMPERATURE );
}

/**********************************************************************************/
