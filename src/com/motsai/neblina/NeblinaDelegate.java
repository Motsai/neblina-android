package com.motsai.neblina;

/**
 * Created by hoanmotsai on 2016-06-20.
 */
public interface NeblinaDelegate {

    public final static String ACTION_NEB_CONNECTED = "com.motsai.neblina.ACTION_NEB_CONNECTED";
    public final static String ACTION_NEB_GENERAL_DATA = "com.motsai.neblina.ACTION_NEB_GENERAL_DATA";
    public final static String ACTION_NEB_FUSION_DATA = "com.motsai.neblina.ACTION_NEB_FUSION_DATA";
    public final static String ACTION_NEB_PMGNT_DATA = "com.motsai.neblina.ACTION_NEB_PMGNT_DATA";
    public final static String ACTION_NEB_LED_DATA = "com.motsai.neblina.ACTION_NEB_LED_DATA";
    public final static String ACTION_NEB_DEBUG_DATA = "com.motsai.neblina.ACTION_NEB_DEBUG_DATA";
    public final static String ACTION_NEB_RECORDER_DATA = "com.motsai.neblina.ACTION_NEB_RECORDER_DATA";
    public final static String ACTION_NEB_EEPROM_DATA = "com.motsai.neblina.ACTION_NEB_EEPROM_DATA";
    public final static String ACTION_NEB_SENSOR_DATA = "com.motsai.neblina.ACTION_NEB_SENSOR_DATA";

    void didConnectNeblina(NeblinaDevice sender);

    /**
     * Command response packet
     * @param sender
     * @param subsystem
     * @param cmdRspId
     * @param data
     * @param dataLen
     */
    void didReceiveResponsePacket(NeblinaDevice sender, int subsystem, int cmdRspId, byte[] data, int dataLen);
    void didReceiveRSSI(NeblinaDevice sender, int rssi);

    /**
     * Data streamming packet
     * @param sender
     * @param respType
     * @param cmdRspId
     * @param data
     * @param dataLen
     * @param errFlag
     */
    void didReceiveGeneralData(NeblinaDevice sender, int respType, int cmdRspId, byte[] data, int dataLen, boolean errFlag);
    void didReceiveFusionData(NeblinaDevice sender, int respType, int cmdRspId, byte[] data, int dataLen, boolean errFlag);
    void didReceivePmgntData(NeblinaDevice sender, int respType, int cmdRspId, byte[] data, int dataLen, boolean errFlag);
    void didReceiveLedData(NeblinaDevice sender, int respType, int cmdRspId, byte[] data, int dataLen, boolean errFlag);
    void didReceiveDebugData(NeblinaDevice sender, int respType, int cmdRspId, byte[] data, int dataLen, boolean errFlag);
    void didReceiveRecorderData(NeblinaDevice sender, int respType, int cmdRspId, byte[] data, int dataLen, boolean errFlag);
    void didReceiveEepromData(NeblinaDevice sender, int respType, int cmdRspId, byte[] data, int dataLen, boolean errFlag);
    void didReceiveSensorData(NeblinaDevice sender, int respType, int cmdRspId, byte[] data, int dataLen, boolean errFlag);
}
