package com.motsai.neblina;

public abstract class NeblinaDiscoveryCallback {
    public void onDeviceFound(NeblinaDevice device) {
        throw new RuntimeException("Stub!");
    }

    public void onStart() {
        throw new RuntimeException("Stub!");
    }

    public void onStop() {
        throw new RuntimeException("Stub!");
    }
}