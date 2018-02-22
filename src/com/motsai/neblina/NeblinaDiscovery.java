package com.motsai.neblina;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class NeblinaDiscovery {
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLEScanner;

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            ScanRecord scanRecord = result.getScanRecord();

            String name = scanRecord.getDeviceName();
            byte[] manufacturer = scanRecord.getManufacturerSpecificData(0x0274);

            if (name == null) {
                name = device.getName();
            }

            if ( name == null || manufacturer == null || manufacturer.length < 8) {
                return;
            }

            ByteBuffer buffer = ByteBuffer.wrap(manufacturer);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            long deviceID = buffer.getLong();
        }
    }

    public NeblinaDiscovery() {
        final BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);

        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
    }

    public void start() {
        mBluetoothLEScanner.startScan();
    }

    public void stop() {

    }
}