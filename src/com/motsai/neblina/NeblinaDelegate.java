package com.motsai.neblina;

import java.util.Objects;

/**
 * Created by hoanmotsai on 2016-06-20.
 */
public interface NeblinaDelegate {

    public final static String ACTION_NEB_CONNECTED = "com.motsai.neblina.ACTION_NEB_CONNECTED";
    public final static String ACTION_NEB_DEBUG_DATA = "com.motsai.neblina.ACTION_NEB_DEBUG_DATA";
    public final static String ACTION_NEB_FUSION_DATA = "com.motsai.neblina.ACTION_NEB_FUSION_DATA";
    public final static String ACTION_NEB_PMGNT_DATA = "com.motsai.neblina.ACTION_NEB_PMGNT_DATA";
    public final static String ACTION_NEB_LED_DATA = "com.motsai.neblina.ACTION_NEB_LED_DATA";
    public final static String ACTION_NEB_STORAGE_DATA = "com.motsai.neblina.ACTION_NEB_STORAGE_DATA";
    public final static String ACTION_NEB_EEPROM_DATA = "com.motsai.neblina.ACTION_NEB_EEPROM_DATA";


/*    void didConnectNeblina();
    void didReceiveRSSI(int rssi);
    void didReceiveFusionData(int type , byte[] data, boolean errFlag);
    void didReceiveDebugData(int type, byte[] data, int datalen, boolean errFlag);
    void didReceivePmgntData(int type, byte[] data, int datalen, boolean errFlag);
    void didReceiveStorageData(int type, byte[] data, int datalen, boolean errFlag);
    void didReceiveEepromData(int type, byte[] data, int datalen, boolean errFlag);
    void didReceiveLedData(int type, byte[] data, int datalen, boolean errFlag);*/

    void didConnectNeblina(Neblina sender);
    void didReceiveRSSI(Neblina sender, int rssi);
    void didReceiveGeneralData(Neblina sender, int cmdRspId, byte[] data, int dataLen, boolean errFlag);
    void didReceiveFusionData(Neblina sender, int cmdRspId, byte[] data, int dataLen, boolean errFlag);
    void didReceivePmgntData(Neblina sender, int cmdRspId, byte[] data, int dataLen, boolean errFlag);
    void didReceiveLedData(Neblina sender, int cmdRspId, byte[] data, int dataLen, boolean errFlag);
    void didReceiveDebugData(Neblina sender, int cmdRspId, byte[] data, int dataLen, boolean errFlag);
    void didReceiveRecorderData(Neblina sender, int cmdRspId, byte[] data, int dataLen, boolean errFlag);
    void didReceiveEepromData(Neblina sender, int cmdRspId, byte[] data, int dataLen, boolean errFlag);
    void didReceiveSensorData(Neblina sender, int cmdRspId, byte[] data, int dataLen, boolean errFlag);
}
