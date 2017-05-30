package com.motsai.neblina;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.*;
import android.content.Context;
import android.content.Intent;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.SystemClock;
import android.util.Log;
import android.app.Service;
import android.widget.AdapterView;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.UUID;
import java.lang.String;


import static android.bluetooth.BluetoothGattCharacteristic.*;
import static android.content.Context.*;
import static java.lang.Thread.yield;

/**
 * Created by hoanmotsai on 2016-06-10.
 */
//public class Neblina extends BluetoothGattCallback implements Parcelable {
public class Neblina extends BluetoothGattCallback implements Parcelable {
    //NEBLINA CUSTOM UUIDs
    public static final UUID NEB_SERVICE_UUID = UUID.fromString("0df9f021-1532-11e5-8960-0002a5d5c51b");
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
    public static final byte NEBLINA_COMMAND_GENERAL_SYSTEM_STATUS      = 1;
    public static final byte NEBLINA_COMMAND_GENERAL_FUSION_STATUS      = 2;
    public static final byte NEBLINA_COMMAND_GENERAL_RECORDER_STATUS	= 3;	// asks for the streaming status of the motion engine, as well as the flash recorder state
    public static final byte NEBLINA_COMMAND_GENERAL_FIRMWARE_VERSION	= 5;    // Get firmware version
    public static final byte NEBLINA_COMMAND_GENERAL_RSSI				= 7;	// get the BLE signal strength in db
    public static final byte NEBLINA_COMMAND_GENERAL_INTERFACE_STATUS   = 8;    // Get streaming data interface port state.
    public static final byte NEBLINA_COMMAND_GENERAL_INTERFACE_STATE	= 9;	// Enable/Disable streaming data interface port
    public static final byte NEBLINA_COMMAND_GENERAL_POWER_STATUS       = 10;
    public static final byte NEBLINA_COMMAND_GENERAL_SENSOR_STATUS	    = 11;
    public static final byte NEBLINA_COMMAND_GENERAL_DISABLE_STREAMING	= 12;
    public static final byte NEBLINA_COMMAND_GENERAL_RESET_TIMESTAMP	= 13;
    public static final byte NEBLINA_COMMAND_GENERAL_FIRMWARE_UPDATE	= 14;
    public static final byte NEBLINA_COMMAND_GENERAL_DEVICE_NAME_GET	= 15;
    public static final byte NEBLINA_COMMAND_GENERAL_DEVICE_NAME_SET    = 16;

    // ***
    // Subsystem fusion commands
    public static final byte NEBLINA_COMMAND_FUSION_RATE                        = 0;
    public static final byte NEBLINA_COMMAND_FUSION_DOWNSAMPLE                  = 1;
    public static final byte NEBLINA_COMMAND_FUSION_MOTION_STATE_STREAM         = 2;
    public static final byte NEBLINA_COMMAND_FUSION_QUATERNION_STREAM           = 4;
    public static final byte NEBLINA_COMMAND_FUSION_EULER_ANGLE_STREAM          = 5;
    public static final byte NEBLINA_COMMAND_FUSION_EXTERNAL_FORCE_STREAM       = 6;
    public static final byte NEBLINA_COMMAND_FUSION_FUSION_TYPE                 = 7;
    public static final byte NEBLINA_COMMAND_FUSION_TRAJECTORY_RECORD           = 8;
    public static final byte NEBLINA_COMMAND_FUSION_TRAJECTORY_INFO_STREAM	    = 9;
    public static final byte NEBLINA_COMMAND_FUSION_PEDOMETER_STREAM            = 10;
    public static final byte NEBLINA_COMMAND_FUSION_SITTING_STANDING_STREAM     = 12;
    public static final byte NEBLINA_COMMAND_FUSION_LOCK_HEADING_REFERENCE	    = 13;
    public static final byte NEBLINA_COMMAND_FUSION_FINGER_GESTURE_STREAM       = 17;
    public static final byte NEBLINA_COMMAND_FUSION_ROTATION_INFO_STREAM   	    = 18;
    public static final byte NEBLINA_COMMAND_FUSION_EXTERNAL_HEADING_CORRECTION = 19;
    public static final byte NEBLINA_COMMAND_FUSION_ANALYSIS_RESET              = 20;
    public static final byte NEBLINA_COMMAND_FUSION_ANALYSIS_CALIBRATE          = 21;
    public static final byte NEBLINA_COMMAND_FUSION_ANALYSIS_CREATE_POSE		= 22;
    public static final byte NEBLINA_COMMAND_FUSION_ANALYSIS_SET_ACTIVE_POSE    = 23;
    public static final byte NEBLINA_COMMAND_FUSION_ANALYSIS_GET_ACTIVE_POSE    = 24;
    public static final byte NEBLINA_COMMAND_FUSION_ANALYSIS_STREAM             = 25;
    public static final byte NEBLINA_COMMAND_FUSION_ANALYSIS_POSE_INFO          = 26;
    public static final byte NEBLINA_COMMAND_FUSION_CALIBRATE_FORWARD_POSITION  = 27;
    public static final byte NEBLINA_COMMAND_FUSION_CALIBRATE_DOWN_POSITION     = 28;
    public static final byte NEBLINA_COMMAND_FUSION_MOTION_DIRECTION_STREAM     = 30;

    // ***
    // Power management subsystem command code
    public static final byte NEBLINA_COMMAND_POWER_BATTERY		    = 0;	// Get battery level
    public static final byte NEBLINA_COMMAND_POWER_TEMPERATURE	    = 1;	// Get battery temperature
    public static final byte NEBLINA_COMMAND_POWER_CHARGE_CURRENT   = 2;	// Set battery charge current

    // ***
    // LED Commands
    public static final byte NEBLINA_COMMAND_LED_STATE              = 1;    // Set LED state
    public static final byte NEBLINA_COMMAND_LED_STATUS             = 2;    // Get LED state

    // ***
    // Debug subsystem command code
    public static final byte NEBLINA_COMMAND_DEBUG_PRINTF           = 0;	// The infamous printf thing.
    public static final byte NEBLINA_COMMAND_DEBUG_DUMP_DATA        = 1;

    // ***
    // Eeprom subsystem
    public static final byte NEBLINA_COMMAND_EEPROM_READ            = 1;    // reads 8-byte chunks of data
    public static final byte NEBLINA_COMMAND_EEPROM_WRITE           = 2;    // write 8-bytes of data to the EEPROM

    // ***
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

    public static final byte NEBLINA_RECORDER_STATUS_IDLE   = 0x0,
            NEBLINA_RECORDER_STATUS_READ     = 0x01,
            NEBLINA_RECORDER_STATUS_RECORD   = 0x02,
            NEBLINA_RECORDER_STATUS_ERASE    = 0x03,
            NEBLINA_RECORDER_STATUS_DOWNLOAD = 0x04;

    // ***
    // Sensore subsystem commands
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
    public static final byte NEBLINA_FUSION_STREAM_EULER = 0x00,
            NEBLINA_FUSION_STREAM_EXTERNAL_FORCE   = 0x01,
        NEBLINA_FUSION_STREAM_FINGER_GESTURE   = 0x02,
        NEBLINA_FUSION_STREAM_MOTION_ANALYSIS  = 0x03,
        NEBLINA_FUSION_STREAM_MOTION_STATE     = 0x04,
        NEBLINA_FUSION_STREAM_PEDOMETER        = 0x05,
        NEBLINA_FUSION_STREAM_QUATERNION       = 0x06,
        NEBLINA_FUSION_STREAM_ROTATION_INFO    = 0x07,
        NEBLINA_FUSION_STREAM_SITTING_STANDING = 0x08,
        NEBLINA_FUSION_STREAM_TRAJECTORY_INFO  = 0x09;

    public static final byte NEBLINA_FUSION_STATUS_EULER  = (byte)( 1 << NEBLINA_FUSION_STREAM_EULER ),
            NEBLINA_FUSION_STATUS_EXTERNAL_FORCE   = (byte)( 1 << NEBLINA_FUSION_STREAM_EXTERNAL_FORCE ),
            NEBLINA_FUSION_STATUS_FINGER_GESTURE   = (byte)( 1 << NEBLINA_FUSION_STREAM_FINGER_GESTURE ),
            NEBLINA_FUSION_STATUS_MOTION_ANALYSIS  = (byte)( 1 << NEBLINA_FUSION_STREAM_MOTION_ANALYSIS ),
            NEBLINA_FUSION_STATUS_MOTION_STATE     = (byte)( 1 << NEBLINA_FUSION_STREAM_MOTION_STATE ),
            NEBLINA_FUSION_STATUS_PEDOMETER        = (byte)( 1 << NEBLINA_FUSION_STREAM_PEDOMETER ),
            NEBLINA_FUSION_STATUS_QUATERNION       = (byte)( 1 << NEBLINA_FUSION_STREAM_QUATERNION ),
            NEBLINA_FUSION_STATUS_ROTATION_INFO    = (byte)( 1 << NEBLINA_FUSION_STREAM_ROTATION_INFO),
            NEBLINA_FUSION_STATUS_SITTING_STANDING = (byte)( 1 << NEBLINA_FUSION_STREAM_SITTING_STANDING),
            NEBLINA_FUSION_STATUS_TRAJECTORY_INFO  = (byte)( 1 << NEBLINA_FUSION_STREAM_TRAJECTORY_INFO );

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

    BluetoothDevice Nebdev;
    String Name;
    long DevId;
    BluetoothGatt mBleGatt;
    NeblinaDelegate mDelegate;
    BluetoothGattCharacteristic mCtrlChar;
    private Boolean mCharWritCompleted = true;
    Context mCtx = null;

    Queue<byte[]> mCmdQue = new LinkedList<byte[]>();

    public void SetDelegate(NeblinaDelegate neblinaDelegate) {
        mDelegate = neblinaDelegate;
    }

    public byte crc8(byte data[], int Len) {
        int i = 0;
        int e = 0;
        int f = 0;
        int crc = 0;

        for (i = 0; i < Len; i++) {
        //while (i < Len) {
            e = (crc ^ ((int)data[i] & 0xFF));
            f = (e ^ (e >> 4) ^ (e >> 7));
            crc = (((f << 1) ^ (f << 4)) & 0xff);
          //  i += 1;
        }

        Log.i("CRC8 : ", String.valueOf(crc));
        return (byte)crc;
    }

    @Override
    public String toString() {
        return Name + "_" + Long.toHexString(DevId).toUpperCase();
    }

    public Neblina(String name, long id, BluetoothDevice dev) {
        Nebdev = dev;
        Name = name;
        DevId = id;
        mDelegate = null;
        mBleGatt = null;
        mCtrlChar = null;
        mCharWritCompleted = true;
       // IntentFilter filter;
       // filter = new IntentFilter("com.motsai.NebCtrlPanel");

        //MyReceiver receiver = new MyReceiver();
        //registerReceiver(mBroadcastReceiver, filter);
    }

    @Override
    public boolean equals(Object obj) {
        if (DevId == ((Neblina)obj).DevId)
            return true;
        return false;
    }

    public boolean isDeviceReady() {
        if (Nebdev == null)
            return false;

       // if (Nebdev.getBondState() == BluetoothDevice.BOND_BONDED)
            return true;

        //return false;
    }

    public boolean Connect(Context ctext) {
        mBleGatt = Nebdev.connectGatt(ctext, false, this);
        mCtx = ctext;
        return mBleGatt != null;
    }

    public void Disconnect() {
        mBleGatt.disconnect();
        mBleGatt = null;
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        mCtx.sendBroadcast(intent);
    }

    // MARK : **** BluetoothGattCallback
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        String intentAction;
        if (newState == BluetoothProfile.STATE_CONNECTED) {
            gatt.discoverServices();
        }
        else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
        }
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            //Get the characteristic from the discovered gatt server
            BluetoothGattService service = gatt.getService(NEB_SERVICE_UUID);
            BluetoothGattCharacteristic data_characteristic = service.getCharacteristic(NEB_DATACHAR_UUID);
            mCtrlChar = service.getCharacteristic(NEB_CTRLCHAR_UUID);
            mCtrlChar.setWriteType(WRITE_TYPE_NO_RESPONSE);
            List<BluetoothGattDescriptor> descriptors = data_characteristic.getDescriptors();
            synchronized (mCharWritCompleted) {
                mCharWritCompleted = false;
            }
            //for (BluetoothGattDescriptor descriptor : data_characteristic.getDescriptors()) {
                BluetoothGattDescriptor descriptor = descriptors.get(0);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                mBleGatt.writeDescriptor(descriptor);
            //}
            gatt.setCharacteristicNotification(data_characteristic, true);
            //descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
            if (mDelegate != null)
                mDelegate.didConnectNeblina(this);
        }
    }

    /**
     * Callback indicating the result of a descriptor write operation.
     *
     * @param gatt GATT client invoked {@link BluetoothGatt#writeDescriptor}
     * @param descriptor Descriptor that was writte to the associated
     *                   remote device.
     * @param status The result of the write operation
     *               {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
     */
    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor,
                                  int status) {
//        if (mDelegate != null)
//            mDelegate.didConnectNeblina(this);
        if (mCmdQue.isEmpty() == false) {
            byte[] pkbuf = mCmdQue.remove();

            //BluetoothGattService service = mBleGatt.getService(NEB_SERVICE_UUID);
            //BluetoothGattCharacteristic lCtrlChar = service.getCharacteristic(NEB_CTRLCHAR_UUID);

            boolean res = mCtrlChar.setValue(pkbuf);
            Log.i("onCharacteristicWrite - lsetValue", String.valueOf(res));
            res = mBleGatt.writeCharacteristic(mCtrlChar);

            Log.i("onCharacteristicWrite - lwriteCharacteristic", String.valueOf(res));
        }
        else {
            synchronized (mCharWritCompleted) {
                mCharWritCompleted = true;
            }
        }
    }

    @Override
    public void onCharacteristicWrite (BluetoothGatt gatt,
                                       BluetoothGattCharacteristic characteristic,
                                       int status) {
        Log.i("onCharacteristicWrite", String.valueOf(status));
        if (mCmdQue.isEmpty() == false) {
            byte[] pkbuf = mCmdQue.remove();

            //BluetoothGattService service = mBleGatt.getService(NEB_SERVICE_UUID);
            //BluetoothGattCharacteristic lCtrlChar = service.getCharacteristic(NEB_CTRLCHAR_UUID);

            boolean res = mCtrlChar.setValue(pkbuf);
            Log.i("onCharacteristicWrite - lsetValue", String.valueOf(res));
            res = mBleGatt.writeCharacteristic(mCtrlChar);

            Log.i("onCharacteristicWrite - lwriteCharacteristic", String.valueOf(res));
        }
        else {
            synchronized (mCharWritCompleted) {
                mCharWritCompleted = true;
            }
        }
    }
    @Override
    // Result of a characteristic read operation
    public void onCharacteristicRead(BluetoothGatt gatt,
                                     BluetoothGattCharacteristic characteristic,
                                     int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
           // broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
        }
    }

    @Override
    public void onCharacteristicChanged (BluetoothGatt gatt,
                                  BluetoothGattCharacteristic characteristic) {
        if (mDelegate == null)
            return;

        final byte[] pkt =  characteristic.getValue();
        int subsys = pkt[0] & 0x1f;
        final int pktype = pkt[0] >> 5;
        byte[] data = new byte[16];
        boolean errFlag = false;

        Log.i("onCharacteristicChanged", "called");

        if (pktype == NEBLINA_PACKET_TYPE_ACK)
            return;

        if ((subsys & 0x80) == 0x80)
        {
            subsys &= 0x7F;
            errFlag = true;
        }

        int datalen = pkt.length - 4;

        for (int i = 0; i < datalen; i++)
            data[i] = pkt[i+4];

        if (pktype == NEBLINA_PACKET_TYPE_RESPONSE) {
            mDelegate.didReceiveResponsePacket(this, subsys, pkt[3], data, datalen);

            return;
        }

        switch (subsys) {
            case NEBLINA_SUBSYSTEM_GENERAL:
                mDelegate.didReceiveGeneralData(this, pktype, pkt[3], data, datalen, errFlag);
                break;
            case NEBLINA_SUBSYSTEM_FUSION:  // Motion Engine
                mDelegate.didReceiveFusionData(this, pktype, pkt[3], data, datalen, errFlag);
                break;
            case NEBLINA_SUBSYSTEM_POWER:	// Power management
                mDelegate.didReceivePmgntData(this, pktype, pkt[3], data, datalen, errFlag);
                break;
            case NEBLINA_SUBSYSTEM_LED:		// LED control
                mDelegate.didReceiveLedData(this, pktype, pkt[3], data, datalen, errFlag);
                break;
            case NEBLINA_SUBSYSTEM_DEBUG:		// Status & logging
                mDelegate.didReceiveDebugData(this, pktype, pkt[3], data, datalen, errFlag);
                break;
            case NEBLINA_SUBSYSTEM_RECORDER:	//NOR flash memory recorder
                mDelegate.didReceiveRecorderData(this, pktype, pkt[3], data, datalen, errFlag);
                break;
            case NEBLINA_SUBSYSTEM_EEPROM:	//small EEPROM storage
                mDelegate.didReceiveEepromData(this, pktype, pkt[3], data, datalen, errFlag);
                break;
            case NEBLINA_SUBSYSTEM_SENSOR:
                mDelegate.didReceiveSensorData(this, pktype, pkt[3], data, datalen, errFlag);
        }
    }

    public void sendCommand(byte SubSystem, byte CmdId, int ParamLen, byte[] ParamData) {
        if (isDeviceReady() == false) {
            return;
        }

        byte[] pkbuf = new byte[4 + ParamLen];

        pkbuf[0] = (byte)((NEBLINA_PACKET_TYPE_COMMAND << 5) | SubSystem);
        pkbuf[1] = (byte)ParamLen;	// Data len
        pkbuf[2] = (byte)0xFF;
        pkbuf[3] = CmdId;	// Cmd

        for (int i = 0; i < ParamLen; i++) {
            pkbuf[4 + i] = ParamData[i];
        }

        pkbuf[2] = crc8(pkbuf, 4 + ParamLen);

        Log.i("sendCommand - ", String.valueOf(pkbuf));

/*
        mCtrlChar.setWriteType(WRITE_TYPE_DEFAULT);//WRITE_TYPE_NO_RESPONSE);
        boolean res = mCtrlChar.setValue(pkbuf);
        Log.i("sendCommand - setValue", String.valueOf(res));

        synchronized (mCharWritCompleted) {
            mCharWritCompleted = false;
        }

        final Intent intent = new Intent(mCtx, Neblina.class);
        intent.putExtra("NeblinaSendCmd", pkbuf);

        mCtx.sendBroadcast(intent);

        //broadcastUpdate(Intent.ACTION_SEND, );
        do {
            res = mBleGatt.writeCharacteristic(mCtrlChar);
            Log.i("sendCommand - writeCharacteristic", String.valueOf(res));
            SystemClock.sleep(1000);
        } while (res == false);
       // while (mCharWritCompleted == false) {
       //     yield();
        //}
*/
        if (mCmdQue.isEmpty() && mCharWritCompleted == true) {
            synchronized (mCharWritCompleted) {
                mCharWritCompleted = false;
            }
           // BluetoothGattService service = mBleGatt.getService(NEB_SERVICE_UUID);
           // BluetoothGattCharacteristic lCtrlChar = service.getCharacteristic(NEB_CTRLCHAR_UUID);

            boolean res = mCtrlChar.setValue(pkbuf);
            Log.i("sendCommand - lsetValue", String.valueOf(res));
            res = mBleGatt.writeCharacteristic(mCtrlChar);
            if (res == false)
                mCmdQue.add(pkbuf);
            Log.i("sendCommand - lwriteCharacteristic", String.valueOf(res));
        }
        else {
            mCmdQue.add(pkbuf);
        }
    }

    // ********************************
    // * Neblina Command API
    // ********************************
    //
    // ***
    // *** Subsystem General
    // ***

    public void getSystemStatus() {
        sendCommand(NEBLINA_SUBSYSTEM_GENERAL, NEBLINA_COMMAND_GENERAL_SYSTEM_STATUS, 0, null);
    }

    public void getFusionStatus() {
        sendCommand(NEBLINA_SUBSYSTEM_GENERAL, NEBLINA_COMMAND_GENERAL_FUSION_STATUS, 0, null);
    }

    public void getRecorderStatus() {
        sendCommand(NEBLINA_SUBSYSTEM_GENERAL, NEBLINA_COMMAND_GENERAL_RECORDER_STATUS, 0, null);
    }

    public void getFirmwareVersion() {
        sendCommand(NEBLINA_SUBSYSTEM_GENERAL, NEBLINA_COMMAND_GENERAL_FIRMWARE_VERSION, 0, null);
    }

    public void getDataPortState() {
        sendCommand(NEBLINA_SUBSYSTEM_GENERAL, NEBLINA_COMMAND_GENERAL_INTERFACE_STATUS, 0, null);
    }

    public void setDataPort(int PortIdx, int Ctrl) {
        byte[] param = new byte[2];

        param[0] = (byte)PortIdx;
        param[1] = (byte)Ctrl;		// 1 - Open, 0 - Close

        sendCommand(NEBLINA_SUBSYSTEM_GENERAL, NEBLINA_COMMAND_GENERAL_INTERFACE_STATE, param.length, param);
    }

    public void getPowerStatus() {
        sendCommand(NEBLINA_SUBSYSTEM_GENERAL, NEBLINA_COMMAND_GENERAL_POWER_STATUS, 0, null);
    }

    public void getSensorStatus() {
        sendCommand(NEBLINA_SUBSYSTEM_GENERAL, NEBLINA_COMMAND_GENERAL_SENSOR_STATUS, 0, null);
    }

    public void disableStreaming() {
        sendCommand(NEBLINA_SUBSYSTEM_GENERAL, NEBLINA_COMMAND_GENERAL_DISABLE_STREAMING, 0, null);
    }

    public void resetTimeStamp( boolean Delayed) {
        byte[] param = new byte[1];

        if (Delayed == true) {
            param[0] = 1;
        }
		else {
            param[0] = 0;
        }

        sendCommand(NEBLINA_SUBSYSTEM_GENERAL, NEBLINA_COMMAND_GENERAL_RESET_TIMESTAMP, param.length, param);
    }

    public void firmwareUpdate() {
        sendCommand(NEBLINA_SUBSYSTEM_GENERAL, NEBLINA_COMMAND_GENERAL_FIRMWARE_UPDATE, 0, null);
    }

    public void getDeviceName() {
        sendCommand(NEBLINA_SUBSYSTEM_GENERAL, NEBLINA_COMMAND_GENERAL_DEVICE_NAME_GET, 0, null);
    }

    public void setDeviceName(String name) {
        byte[] param = name.getBytes();

        sendCommand(NEBLINA_SUBSYSTEM_GENERAL, NEBLINA_COMMAND_GENERAL_DEVICE_NAME_SET, param.length, param);
    }

    // ***
    // *** EEPROM
    // ***
    public void eepromRead(short pageNo) {
        byte[] param = new byte[2];

        param[0] = (byte)(pageNo & 0xff);
        param[1] = (byte)((pageNo >> 8) & 0xff);

        sendCommand(NEBLINA_SUBSYSTEM_EEPROM, NEBLINA_COMMAND_EEPROM_READ, param.length, param);
    }

    public void eepromWrite(short pageNo, byte[] data) {
        byte[] param = new byte[2 + data.length];

        param[0] = (byte)(pageNo & 0xff);
        param[1] = (byte)((pageNo >> 8) & 0xff);

        for (int i = 0; i < 8; i++) {
            param[i + 2] = data[i];
        }

        sendCommand(NEBLINA_SUBSYSTEM_EEPROM, NEBLINA_COMMAND_EEPROM_WRITE, param.length, param);
    }

    // *** LED subsystem commands
    public void getLed() {
        sendCommand(NEBLINA_SUBSYSTEM_LED, NEBLINA_COMMAND_LED_STATUS, 0, null);
    }

    public void setLed(byte LedNo, byte Value) {
        byte[] param = new byte[2];

        param[0] = LedNo;
        param[1] = Value;

        sendCommand(NEBLINA_SUBSYSTEM_LED, NEBLINA_COMMAND_LED_STATE, param.length, param);
    }

    // *** Power management sybsystem commands
    public void getTemperature() {
        sendCommand(NEBLINA_SUBSYSTEM_POWER, NEBLINA_COMMAND_POWER_TEMPERATURE, 0, null);
    }

    public void setBatteryChargeCurrent(short Current) {
        byte[] param = new byte[2];

        param[0] = (byte)(Current & 0xFF);
        param[1] = (byte)((Current >> 8) & 0xFF);

        sendCommand(NEBLINA_SUBSYSTEM_POWER, NEBLINA_COMMAND_POWER_CHARGE_CURRENT, param.length, param);
    }

    // ***
    // *** Fusion subsystem commands
    // ***
    public void setFusionRate(short Rate) {
        byte[] param = new byte[2];

        param[0] = (byte)(Rate & 0xFF);
        param[1] = (byte)((Rate >> 8) & 0xFF);

        sendCommand(NEBLINA_SUBSYSTEM_FUSION, NEBLINA_COMMAND_FUSION_RATE, param.length, param);
    }

    public void setFusionDownSample(short Rate) {
        byte[] param = new byte[2];

        param[0] = (byte)(Rate & 0xFF);
        param[1] = (byte)((Rate >> 8) & 0xFF);

        sendCommand( NEBLINA_SUBSYSTEM_FUSION, NEBLINA_COMMAND_FUSION_DOWNSAMPLE, param.length, param);
    }

    public void streamMotionState(boolean Enable)
    {
        byte[] param= new byte[1];

        if (Enable == true)
        {
            param[0] = 1;
        }
		else
        {
            param[0] = 0;
        }

        sendCommand(NEBLINA_SUBSYSTEM_FUSION, NEBLINA_COMMAND_FUSION_MOTION_STATE_STREAM, param.length, param);
    }

    public void streamQuaternion(boolean Enable)
    {
        byte[] param = new byte[1];

        if (Enable == true)
        {
            param[0] = 1;
        }
		else
        {
            param[0] = 0;
        }

        sendCommand(NEBLINA_SUBSYSTEM_FUSION, NEBLINA_COMMAND_FUSION_QUATERNION_STREAM, param.length, param);
    }

    public void streamEulerAngle(boolean Enable)
    {
        byte[] param = new byte[1];

        if (Enable == true)
        {
            param[0] = 1;
        }
		else
        {
            param[0] = 0;
        }

        sendCommand(NEBLINA_SUBSYSTEM_FUSION, NEBLINA_COMMAND_FUSION_EULER_ANGLE_STREAM, param.length, param);
    }

    public void streamExternalForce(boolean Enable)
    {
        byte[] param = new byte[1];

        if (Enable == true)
        {
            param[0] = 1;
        }
		else
        {
            param[0] = 0;
        }

        sendCommand(NEBLINA_SUBSYSTEM_FUSION, NEBLINA_COMMAND_FUSION_EXTERNAL_FORCE_STREAM, param.length, param);
    }

    public void setFusionType(byte Mode) {
        byte[] param = new byte[1];

        param[0] = Mode;

        sendCommand(NEBLINA_SUBSYSTEM_FUSION, NEBLINA_COMMAND_FUSION_FUSION_TYPE, param.length, param);
    }

    public void recordTrajectory(boolean Enable)
    {
        byte[] param = new byte[1];

        if (Enable == true)
        {
            param[0] = 1;
        }
		else
        {
            param[0] = 0;
        }

        sendCommand(NEBLINA_SUBSYSTEM_FUSION, NEBLINA_COMMAND_FUSION_TRAJECTORY_RECORD, param.length, param);
    }

    public void streamTrajectoryInfo(boolean Enable)
    {
        byte[] param = new byte[1];

        if (Enable == true)
        {
            param[0] = 1;
        }
		else
        {
            param[0] = 0;
        }

        sendCommand(NEBLINA_SUBSYSTEM_FUSION, NEBLINA_COMMAND_FUSION_TRAJECTORY_INFO_STREAM, param.length, param);
    }

    public void streamPedometer(boolean Enable)
    {
        byte[] param = new byte[1];

        if (Enable == true)
        {
            param[0] = 1;
        }
		else
        {
            param[0] = 0;
        }

        sendCommand(NEBLINA_SUBSYSTEM_FUSION, NEBLINA_COMMAND_FUSION_PEDOMETER_STREAM, param.length, param);
    }

    public void streamSittingStanding(boolean Enable) {
        byte[] param = new byte[1];

        if (Enable == true)
        {
            param[0] = 1;
        }
		else
        {
            param[0] = 0;
        }

        sendCommand(NEBLINA_SUBSYSTEM_FUSION, NEBLINA_COMMAND_FUSION_SITTING_STANDING_STREAM, param.length, param);
    }

    public void lockHeadingReference() {
        sendCommand(NEBLINA_SUBSYSTEM_FUSION, NEBLINA_COMMAND_FUSION_LOCK_HEADING_REFERENCE, 0, null);
    }

    public void streamFingerGesture(boolean Enable) {
        byte[] param = new byte[1];

        if (Enable == true)
        {
            param[0] = 1;
        }
		else
        {
            param[0] = 0;
        }

        sendCommand(NEBLINA_SUBSYSTEM_FUSION, NEBLINA_COMMAND_FUSION_FINGER_GESTURE_STREAM, param.length, param);
    }

    public void streamRotationInfo(boolean Enable) {
        byte[] param = new byte[1];

        if (Enable == true)
        {
            param[0] = 1;
        }
		else
        {
            param[0] = 0;
        }

        sendCommand(NEBLINA_SUBSYSTEM_FUSION, NEBLINA_COMMAND_FUSION_ROTATION_INFO_STREAM, param.length, param);
    }

    public void externalHeadingCorrection(short yaw, short error) {
        byte[] param = new byte[4];

        param[0] = (byte)(yaw & 0xFF);
        param[1] = (byte)((yaw >> 8) & 0xFF);
        param[2] = (byte)(error & 0xFF);
        param[3] = (byte)((error >> 8) & 0xFF);

        sendCommand(NEBLINA_SUBSYSTEM_FUSION, NEBLINA_COMMAND_FUSION_EXTERNAL_HEADING_CORRECTION, param.length, param);
    }

    public void resetAnalysis() {
        sendCommand(NEBLINA_SUBSYSTEM_FUSION, NEBLINA_COMMAND_FUSION_ANALYSIS_RESET, 0, null);
    }

    public void calibrateAnalysis() {
        sendCommand(NEBLINA_SUBSYSTEM_FUSION, NEBLINA_COMMAND_FUSION_ANALYSIS_CALIBRATE, 0, null);
    }

    public void createPoseAnalysis(byte id, short[] qtf) {
        byte[] param = new byte[1 + qtf.length * 2];

        param[0] = id;

        for (int i = 0; i < qtf.length; i++) {
            param[1 + (i << 1)] = (byte)(qtf[i] & 0xFF);
            param[2 + (i << 1)] = (byte)((qtf[i] >> 8) & 0xFF);
        }

        sendCommand(NEBLINA_SUBSYSTEM_FUSION, NEBLINA_COMMAND_FUSION_ANALYSIS_CREATE_POSE, param.length, param);
    }

    public void setActivePoseAnalysis(byte id) {
        byte[] param = new byte[1];

        param[0] = id;

        sendCommand(NEBLINA_SUBSYSTEM_FUSION, NEBLINA_COMMAND_FUSION_ANALYSIS_SET_ACTIVE_POSE, param.length, param);
    }

    public void getActivePoseAnalysis() {
        sendCommand(NEBLINA_SUBSYSTEM_FUSION, NEBLINA_COMMAND_FUSION_ANALYSIS_GET_ACTIVE_POSE, 0, null);
    }

    public void streamAnalysis(boolean Enable) {
        byte[] param = new byte[1];

        if (Enable == true)
        {
            param[0] = 1;
        }
		else
        {
            param[0] = 0;
        }

        sendCommand(NEBLINA_SUBSYSTEM_FUSION, NEBLINA_COMMAND_FUSION_ANALYSIS_STREAM, param.length, param);
    }

    public void getPoseAnalysisInfo(byte id) {
        byte[] param = new byte[1];

        param[0] = id;

        sendCommand(NEBLINA_SUBSYSTEM_FUSION, NEBLINA_COMMAND_FUSION_ANALYSIS_POSE_INFO, param.length, param);
    }

    public void calibrateForwardPosition() {
        sendCommand(NEBLINA_SUBSYSTEM_FUSION, NEBLINA_COMMAND_FUSION_CALIBRATE_FORWARD_POSITION, 0, null);
    }

    public void calibrateDownPosition() {
        sendCommand(NEBLINA_SUBSYSTEM_FUSION, NEBLINA_COMMAND_FUSION_CALIBRATE_DOWN_POSITION, 0, null);
    }

    public void streamMotionDirection(boolean Enable) {
        byte[] param = new byte[1];

        if (Enable == true)
        {
            param[0] = 1;
        }
		else
        {
            param[0] = 0;
        }

        sendCommand(NEBLINA_SUBSYSTEM_FUSION, NEBLINA_COMMAND_FUSION_MOTION_DIRECTION_STREAM, param.length, param);
    }

    // ***
    // *** Storage subsystem commands
    // ***
    public void getSessionCount() {
        sendCommand(NEBLINA_SUBSYSTEM_RECORDER, NEBLINA_COMMAND_RECORDER_SESSION_COUNT, 0, null);
    }

    public void getSessionInfo(short sessionId) {
        byte[] param = new byte[2];

        param[0] = (byte)(sessionId & 0xFF);
        param[1] = (byte)((sessionId >> 8) & 0xFF);

        sendCommand(NEBLINA_SUBSYSTEM_RECORDER, NEBLINA_COMMAND_RECORDER_SESSION_INFO, param.length, param);
    }

    public void eraseStorage(boolean quickErase) {
        byte[] param = new byte[1];

        if (quickErase == true)
        {
            param[0] = 1;
        }
		else
        {
            param[0] = 0;
        }
        sendCommand(NEBLINA_SUBSYSTEM_RECORDER, NEBLINA_COMMAND_RECORDER_ERASE_ALL, param.length, param);

    }

    public void sessionPlayback(boolean Enable, short sessionId) {
        byte[] param = new byte[3];

        if (Enable == true)
        {
            param[0] = 1;
        }
		else
        {
            param[0] = 0;
        }

        param[1] = (byte)(sessionId & 0xff);
        param[2] = (byte)((sessionId >> 8) & 0xff);

        sendCommand(NEBLINA_SUBSYSTEM_RECORDER, NEBLINA_COMMAND_RECORDER_PLAYBACK, param.length, param);
    }

    public void sessionRecord(boolean Enable) {
        byte[] param = new byte[1];

        if (Enable == true)
        {
            param[0] = 1;
        }
		else
        {
            param[0] = 0;
        }

        sendCommand(NEBLINA_SUBSYSTEM_RECORDER, NEBLINA_COMMAND_RECORDER_RECORD, param.length, param);
    }

    public void sessionRead(short SessionId, short Len, int Offset) {
        byte[] param = new byte[8];


        // Command parameter
        param[0] = (byte)(SessionId & 0xFF);
        param[1] = (byte)((SessionId >> 8) & 0xFF);
        param[2] = (byte)(Len & 0xFF);
        param[3] = (byte)((Len >> 8) & 0xFF);
        param[4] = (byte)(Offset & 0xFF);
        param[5] = (byte)((Offset >> 8) & 0xFF);
        param[6] = (byte)((Offset >> 16) & 0xFF);
        param[7] = (byte)((Offset >> 24) & 0xFF);

        sendCommand(NEBLINA_SUBSYSTEM_RECORDER, NEBLINA_COMMAND_RECORDER_SESSION_READ, param.length, param);
    }

    public void sessionDownload(boolean Start, short SessionId, short Len, int Offset) {
        byte[] param = new byte[9];

        // Command parameter
        if (Start == true) {
            param[0] = 1;
        }
		else {
            param[0] = 0;
        }
        param[1] = (byte)(SessionId & 0xFF);
        param[2] = (byte)((SessionId >> 8) & 0xFF);
        param[3] = (byte)(Len & 0xFF);
        param[4] = (byte)((Len >> 8) & 0xFF);
        param[5] = (byte)(Offset & 0xFF);
        param[6] = (byte)((Offset >> 8) & 0xFF);
        param[7] = (byte)((Offset >> 16) & 0xFF);
        param[8] = (byte)((Offset >> 24) & 0xFF);

        sendCommand(NEBLINA_SUBSYSTEM_RECORDER, NEBLINA_COMMAND_RECORDER_SESSION_DOWNLOAD, param.length, param);
    }

    // ***
    // *** Sensor subsystem commands
    // ***
    public void sensorSetDownsample(short stream, short factor) {
        byte[] param = new byte[4];

        param[0] = (byte)(stream & 0xFF);
        param[1] = (byte)(stream >> 8);
        param[2] = (byte)(factor & 0xFF);
        param[3] = (byte)(factor >> 8);

        sendCommand(NEBLINA_SUBSYSTEM_SENSOR, NEBLINA_COMMAND_SENSOR_SET_DOWNSAMPLE, param.length, param);
    }

    public void sensorSetRange(short type, short range) {
        byte[] param = new byte[4];

        param[0] = (byte)(type & 0xFF);
        param[1] = (byte)(type >> 8);
        param[2] = (byte)(range & 0xFF);
        param[3] = (byte)(range >> 8);

        sendCommand(NEBLINA_SUBSYSTEM_SENSOR, NEBLINA_COMMAND_SENSOR_SET_RANGE, param.length, param);
    }

    public void sensorSetRate(short type, short rate) {
        byte[] param = new byte[4];

        param[0] = (byte)(type & 0xFF);
        param[1] = (byte)(type >> 8);
        param[2] = (byte)(rate & 0xFF);
        param[3] = (byte)(rate >> 8);

        sendCommand(NEBLINA_SUBSYSTEM_SENSOR, NEBLINA_COMMAND_SENSOR_SET_RATE, param.length, param);
    }

    public void sensorGetDownsample(byte streamId) {
        byte[] param = new byte[1];

        param[0] = streamId;

        sendCommand(NEBLINA_SUBSYSTEM_SENSOR, NEBLINA_COMMAND_SENSOR_GET_DOWNSAMPLE, param.length, param);
    }

    public void sensorGetRange(byte type) {
        byte[] param = new byte[1];

        param[0] = type;

        sendCommand(NEBLINA_SUBSYSTEM_SENSOR, NEBLINA_COMMAND_SENSOR_GET_RANGE, param.length, param);
    }

    public void sensorGetRate(byte type) {
        byte[] param = new byte[1];

        param[0] = type;

        sendCommand(NEBLINA_SUBSYSTEM_SENSOR, NEBLINA_COMMAND_SENSOR_GET_RATE, param.length, param);
    }

    public void sensorStreamAccelData(boolean Enable) {
        byte[] param = new byte[1];

        if (Enable == true)
        {
            param[0] = 1;
        }
		else
        {
            param[0] = 0;
        }

        sendCommand(NEBLINA_SUBSYSTEM_SENSOR, NEBLINA_COMMAND_SENSOR_ACCELEROMETER_STREAM, param.length, param);
    }

    public void sensorStreamGyroData(boolean Enable) {
        byte[] param = new byte[1];

        if (Enable == true)
        {
            param[0] = 1;
        }
		else
        {
            param[0] = 0;
        }

        sendCommand(NEBLINA_SUBSYSTEM_SENSOR, NEBLINA_COMMAND_SENSOR_GYROSCOPE_STREAM, param.length, param);
    }

    public void sensorStreamHumidityData(boolean Enable) {
        byte[] param = new byte[1];

        if (Enable == true)
        {
            param[0] = 1;
        }
		else
        {
            param[0] = 0;
        }

        sendCommand(NEBLINA_SUBSYSTEM_SENSOR, NEBLINA_COMMAND_SENSOR_HUMIDITY_STREAM, param.length, param);
    }

    public void sensorStreamMagData(boolean Enable) {
        byte[] param = new byte[1];

        if (Enable == true)
        {
            param[0] = 1;
        }
		else
        {
            param[0] = 0;
        }

        sendCommand(NEBLINA_SUBSYSTEM_SENSOR, NEBLINA_COMMAND_SENSOR_MAGNETOMETER_STREAM, param.length, param);
    }

    public void sensorStreamPressureData(boolean Enable) {
        byte[] param = new byte[1];

        if (Enable == true)
        {
            param[0] = 1;
        }
		else
        {
            param[0] = 0;
        }

        sendCommand(NEBLINA_SUBSYSTEM_SENSOR, NEBLINA_COMMAND_SENSOR_PRESSURE_STREAM, param.length, param);
    }

    public void sensorStreamTemperatureData(boolean Enable) {
        byte[] param = new byte[1];

        if (Enable == true)
        {
            param[0] = 1;
        }
		else
        {
            param[0] = 0;
        }

        sendCommand(NEBLINA_SUBSYSTEM_SENSOR, NEBLINA_COMMAND_SENSOR_TEMPERATURE_STREAM, param.length, param);
    }

    public void sensorStreamAccelGyroData(boolean Enable) {
        byte[] param = new byte[1];

        if (Enable == true)
        {
            param[0] = 1;
        }
        else
        {
            param[0] = 0;
        }

        sendCommand(NEBLINA_SUBSYSTEM_SENSOR, NEBLINA_COMMAND_SENSOR_ACCELEROMETER_GYROSCOPE_STREAM, param.length, param);
    }

    public void sensorStreamAccelMagData(boolean Enable) {
        byte[] param = new byte[1];

        if (Enable == true)
        {
            param[0] = 1;
        }
        else
        {
            param[0] = 0;
        }

        sendCommand(NEBLINA_SUBSYSTEM_SENSOR, NEBLINA_COMMAND_SENSOR_ACCELEROMETER_MAGNETOMETER_STREAM, param.length, param);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeValue(Nebdev);
        out.writeString(Name);
        out.writeLong(DevId);
        out.writeValue(mBleGatt);
        out.writeValue(mDelegate);
        out.writeValue(mCtrlChar);

    }

    public static final Parcelable.Creator<Neblina> CREATOR
            = new Parcelable.Creator<Neblina>() {
        public Neblina createFromParcel(Parcel in) {
            return new Neblina(in);
        }

        public Neblina[] newArray(int size) {
            return new Neblina[size];
        }
    };

    private Neblina(Parcel in) {
        Nebdev = (BluetoothDevice) in.readValue(null);
        Name = in.readString();
        DevId = in.readLong();
        mBleGatt = (BluetoothGatt) in.readValue(null);
        mDelegate = (NeblinaDelegate) in.readValue(null);
        mCtrlChar = (BluetoothGattCharacteristic) in.readValue(null);
    }

}
