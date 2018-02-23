package com.motsai.neblina;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.util.Log;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

public class NeblinaDiscovery {
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothLeScanner mBluetoothLEScanner;
    private NeblinaDiscoveryCallback mCallback;

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice bluetoothDevice = result.getDevice();
            ScanRecord scanRecord = result.getScanRecord();

            String name = scanRecord.getDeviceName();
            byte[] manufacturer = scanRecord.getManufacturerSpecificData(0x0274);

            if (name == null) {
                name = bluetoothDevice.getName();
            }

            if ( name == null || manufacturer == null || manufacturer.length < 8) {
                return;
            }

            ByteBuffer buffer = ByteBuffer.wrap(manufacturer);
            buffer.order(ByteOrder.LITTLE_ENDIAN);
            long deviceID = buffer.getLong();

            NeblinaDevice device = new NeblinaDevice(name, deviceID, bluetoothDevice);
            mCallback.onDeviceFound(device);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.d("<ScanCallback> - ScanResult", sr.toString());
            }
        }
    };

    public NeblinaDiscovery(BluetoothManager bluetoothManager, NeblinaDiscoveryCallback callback) {
        mBluetoothAdapter = bluetoothManager.getAdapter();
        mBluetoothLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mCallback = callback;
    }

    public void start() {
        mCallback.onStart();
        mBluetoothLEScanner.startScan(mScanCallback);
    }

    public void stop() {
        mBluetoothLEScanner.stopScan(mScanCallback);
        mCallback.onStop();
    }
}