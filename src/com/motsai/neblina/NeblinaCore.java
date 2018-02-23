package com.motsai.neblina;

import android.bluetooth.BluetoothManager;

public class NeblinaCore {
    NeblinaCallback mCallback;
    NeblinaDeviceList mDeviceList;
    NeblinaDiscovery mDiscovery;

    private NeblinaDiscoveryCallback mDiscoveryCallback = new NeblinaDiscoveryCallback() {
        @Override
        public void onDeviceFound(NeblinaDevice device) {
            device.setCallback(mCallback);
            mCallback.deviceDiscovered(device);
        }

        @Override
        public void onStart() {
            mCallback.discoveryStarted();
        }

        @Override
        public void onStop() {
            mCallback.discoveryFinished();
        }
    };

    public NeblinaCore(BluetoothManager bluetoothManager) {
        mDeviceList = new NeblinaDeviceList();
        mDiscovery = new NeblinaDiscovery(bluetoothManager, mDiscoveryCallback);
    }

    public NeblinaDevice getDevice(String key) {
        return mDeviceList.get(key);
    }

    public void setCallback(NeblinaCallback callback) {
        mCallback = callback;
    }

    public void startDiscovery() {
        mDiscovery.start();
    }

    public void stopDiscovery() {
        mDiscovery.stop();
    }
}