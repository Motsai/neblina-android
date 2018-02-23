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

public class NeblinaDevice extends BluetoothGattCallback implements Parcelable {
    BluetoothDevice Nebdev;
    String Name;
    long DevId;
    BluetoothGatt mBleGatt;
    BluetoothGattCharacteristic mCtrlChar;
    private Boolean mCharWritCompleted = true;
    Context mCtx = null;
    NeblinaCallback mCallback = null;

    Queue<byte[]> mCmdQue = new LinkedList<byte[]>();

    @Override
    public String toString() {
        return Name + "_" + Long.toHexString(DevId).toUpperCase();
    }

    public NeblinaDevice(String name, long id, BluetoothDevice dev) {
        Nebdev = dev;
        Name = name;
        DevId = id;
        mBleGatt = null;
        mCtrlChar = null;
        mCharWritCompleted = true;
        // IntentFilter filter;
        // filter = new IntentFilter("com.motsai.NebCtrlPanel");

        //MyReceiver receiver = new MyReceiver();
        //registerReceiver(mBroadcastReceiver, filter);
    }

    public void setCallback(NeblinaCallback callback) {
        mCallback = callback;
    }

    @Override
    public boolean equals(Object obj) {
        if (DevId == ((NeblinaDevice)obj).DevId)
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

    public boolean connect(Context ctext, boolean autoConnect) {
        mBleGatt = Nebdev.connectGatt(ctext, autoConnect, this);
        mCtx = ctext;
        return mBleGatt != null;
    }

    public void disconnect() {
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
            BluetoothGattService service = gatt.getService(Neblina.NEB_SERVICE_UUID);
            BluetoothGattCharacteristic data_characteristic = service.getCharacteristic(Neblina.NEB_DATACHAR_UUID);
            mCtrlChar = service.getCharacteristic(Neblina.NEB_CTRLCHAR_UUID);
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
            if (mCallback != null) {
                mCallback.deviceConnected(this);
            }
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
        if (mCallback == null)
            return;

        final byte[] pkt =  characteristic.getValue();
        int subsys = pkt[0] & 0x1f;
        final int pktype = pkt[0] >> 5;
        byte[] data = new byte[16];
        boolean errFlag = false;

        //Log.i("onCharacteristicChanged", "called");

        if (pktype == Neblina.NEBLINA_PACKET_TYPE_ACK)
            return;

        if ((subsys & 0x80) == 0x80)
        {
            subsys &= 0x7F;
            errFlag = true;
        }

        int datalen = pkt.length - 4;

        for (int i = 0; i < datalen; i++)
            data[i] = pkt[i+4];

        if (pktype == Neblina.NEBLINA_PACKET_TYPE_RESPONSE) {
            mCallback.didReceiveResponsePacket(this, subsys, pkt[3], data, datalen);
            return;
        }

        switch (subsys) {
            case Neblina.NEBLINA_SUBSYSTEM_GENERAL:
                mCallback.didReceiveGeneralData(this, pktype, pkt[3], data, datalen, errFlag);
                break;
            case Neblina.NEBLINA_SUBSYSTEM_FUSION:  // Motion Engine
                mCallback.didReceiveFusionData(this, pktype, pkt[3], data, datalen, errFlag);
                break;
            case Neblina.NEBLINA_SUBSYSTEM_POWER:	// Power management
                mCallback.didReceivePmgntData(this, pktype, pkt[3], data, datalen, errFlag);
                break;
            case Neblina.NEBLINA_SUBSYSTEM_LED:		// LED control
                mCallback.didReceiveLedData(this, pktype, pkt[3], data, datalen, errFlag);
                break;
            case Neblina.NEBLINA_SUBSYSTEM_DEBUG:		// Status & logging
                mCallback.didReceiveDebugData(this, pktype, pkt[3], data, datalen, errFlag);
                break;
            case Neblina.NEBLINA_SUBSYSTEM_RECORDER:	//NOR flash memory recorder
                mCallback.didReceiveRecorderData(this, pktype, pkt[3], data, datalen, errFlag);
                break;
            case Neblina.NEBLINA_SUBSYSTEM_EEPROM:	//small EEPROM storage
                mCallback.didReceiveEepromData(this, pktype, pkt[3], data, datalen, errFlag);
                break;
            case Neblina.NEBLINA_SUBSYSTEM_SENSOR:
                mCallback.didReceiveSensorData(this, pktype, pkt[3], data, datalen, errFlag);
                break;
        }
    }

    public void sendCommand(byte SubSystem, byte CmdId, int ParamLen, byte[] ParamData) {
        if (isDeviceReady() == false) {
            return;
        }

        byte[] pkbuf = new byte[4 + ParamLen];

        pkbuf[0] = (byte)((Neblina.NEBLINA_PACKET_TYPE_COMMAND << 5) | SubSystem);
        pkbuf[1] = (byte)ParamLen;	// Data len
        pkbuf[2] = (byte)0xFF;
        pkbuf[3] = CmdId;	// Cmd

        for (int i = 0; i < ParamLen; i++) {
            pkbuf[4 + i] = ParamData[i];
        }

        pkbuf[2] = NeblinaUtilities.crc8(pkbuf, 4 + ParamLen);

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
        //out.writeValue(mDelegate);
        out.writeValue(mCtrlChar);

    }

    public static final Parcelable.Creator<NeblinaDevice> CREATOR
            = new Parcelable.Creator<NeblinaDevice>() {
        public NeblinaDevice createFromParcel(Parcel in) {
            return new NeblinaDevice(in);
        }

        public NeblinaDevice[] newArray(int size) {
            return new NeblinaDevice[size];
        }
    };

    private NeblinaDevice(Parcel in) {
        Nebdev = (BluetoothDevice) in.readValue(null);
        Name = in.readString();
        DevId = in.readLong();
        mBleGatt = (BluetoothGatt) in.readValue(null);
        //mDelegate = (NeblinaDelegate) in.readValue(null);
        mCtrlChar = (BluetoothGattCharacteristic) in.readValue(null);
    }

}
