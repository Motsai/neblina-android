package com.motsai.neblina;

public abstract class NeblinaCallback {

    public final static String ACTION_NEB_CONNECTED = "com.motsai.neblina.ACTION_NEB_CONNECTED";
    public final static String ACTION_NEB_GENERAL_DATA = "com.motsai.neblina.ACTION_NEB_GENERAL_DATA";
    public final static String ACTION_NEB_FUSION_DATA = "com.motsai.neblina.ACTION_NEB_FUSION_DATA";
    public final static String ACTION_NEB_PMGNT_DATA = "com.motsai.neblina.ACTION_NEB_PMGNT_DATA";
    public final static String ACTION_NEB_LED_DATA = "com.motsai.neblina.ACTION_NEB_LED_DATA";
    public final static String ACTION_NEB_DEBUG_DATA = "com.motsai.neblina.ACTION_NEB_DEBUG_DATA";
    public final static String ACTION_NEB_RECORDER_DATA = "com.motsai.neblina.ACTION_NEB_RECORDER_DATA";
    public final static String ACTION_NEB_EEPROM_DATA = "com.motsai.neblina.ACTION_NEB_EEPROM_DATA";
    public final static String ACTION_NEB_SENSOR_DATA = "com.motsai.neblina.ACTION_NEB_SENSOR_DATA";

    public void deviceConnected(NeblinaDevice sender) {}
    public void deviceDisconnected(NeblinaDevice sender) {}
    public void deviceDiscovered(NeblinaDevice sender) {}
    public void discoveryStarted() {}
    public void discoveryFinished() {}

    public void didReceiveResponsePacket(NeblinaDevice sender, int subsystem, int cmdRspId, byte[] data, int dataLen) {}
    public void didReceiveGeneralData(NeblinaDevice sender, int respType, int cmdRspId, byte[] data, int dataLen, boolean errFlag) {}
    public void didReceiveFusionData(NeblinaDevice sender, int respType, int cmdRspId, byte[] data, int dataLen, boolean errFlag) {}
    public void didReceivePmgntData(NeblinaDevice sender, int respType, int cmdRspId, byte[] data, int dataLen, boolean errFlag) {}
    public void didReceiveLedData(NeblinaDevice sender, int respType, int cmdRspId, byte[] data, int dataLen, boolean errFlag) {}
    public void didReceiveDebugData(NeblinaDevice sender, int respType, int cmdRspId, byte[] data, int dataLen, boolean errFlag) {}
    public void didReceiveRecorderData(NeblinaDevice sender, int respType, int cmdRspId, byte[] data, int dataLen, boolean errFlag) {}
    public void didReceiveEepromData(NeblinaDevice sender, int respType, int cmdRspId, byte[] data, int dataLen, boolean errFlag) {}
    public void didReceiveSensorData(NeblinaDevice sender, int respType, int cmdRspId, byte[] data, int dataLen, boolean errFlag) {}
}
