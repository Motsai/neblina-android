package com.motsai.neblina;

import android.bluetooth.BluetoothManager;

import java.util.Map;

public class NeblinaAPI {
    private NeblinaCore mCore;
    private Map<String, NeblinaDevice> mSendingPool;

    public NeblinaAPI(BluetoothManager bluetoothManager) {
        mCore = new NeblinaCore(bluetoothManager);
    }

    public void addDeviceToSendingPool(String key) {
        NeblinaDevice device = mCore.getDevice(key);
        if (device != null) {
            mSendingPool.put(key, device);
        }
    }

    public void removeDeviceFromSendingPool(String key) {
        mSendingPool.remove(key);
    }

    public void setCallback(NeblinaCallback callback) {
        mCore.setCallback(callback);
    }

    public void startDiscovery() {
        mCore.startDiscovery();
    }

    public void stopDiscovery() {
        mCore.stopDiscovery();
    }

    // Communication API

    public void getSystemStatus() {
        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_GENERAL, Neblina.NEBLINA_COMMAND_GENERAL_SYSTEM_STATUS, 0, null);
        }
    }

    public void getFusionStatus() {
        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_GENERAL, Neblina.NEBLINA_COMMAND_GENERAL_FUSION_STATUS, 0, null);
        }
    }

    public void getRecorderStatus() {
        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_GENERAL, Neblina.NEBLINA_COMMAND_GENERAL_RECORDER_STATUS, 0, null);
        }
    }

    public void getFirmwareVersion() {
        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_GENERAL, Neblina.NEBLINA_COMMAND_GENERAL_FIRMWARE_VERSION, 0, null);
        }
    }

    public void getDataPortState() {
        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_GENERAL, Neblina.NEBLINA_COMMAND_GENERAL_INTERFACE_STATUS, 0, null);
        }
    }

    public void setDataPort(int PortIdx, int Ctrl) {
        byte[] param = new byte[] { (byte)PortIdx, (byte)Ctrl };

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_GENERAL, Neblina.NEBLINA_COMMAND_GENERAL_INTERFACE_STATE, param.length, param);
        }
    }

    public void getPowerStatus() {
        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_GENERAL, Neblina.NEBLINA_COMMAND_GENERAL_POWER_STATUS, 0, null);
        }
    }

    public void getSensorStatus() {
        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_GENERAL, Neblina.NEBLINA_COMMAND_GENERAL_SENSOR_STATUS, 0, null);
        }
    }

    public void disableStreaming() {
        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_GENERAL, Neblina.NEBLINA_COMMAND_GENERAL_DISABLE_STREAMING, 0, null);
        }
    }

    public void resetTimeStamp(byte type) {
        byte[] param = new byte[] { type};

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_GENERAL, Neblina.NEBLINA_COMMAND_GENERAL_RESET_TIMESTAMP, param.length, param);
        }
    }

    public void firmwareUpdate() {
        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_GENERAL, Neblina.NEBLINA_COMMAND_GENERAL_FIRMWARE_UPDATE, 0, null);
        }
    }

    public void getDeviceName() {
        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_GENERAL, Neblina.NEBLINA_COMMAND_GENERAL_DEVICE_NAME_GET, 0, null);
        }
    }

    public void setDeviceName(String name) {
        byte[] param = name.getBytes();

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_GENERAL, Neblina.NEBLINA_COMMAND_GENERAL_DEVICE_NAME_SET, param.length, param);
        }
    }

    // ***
    // *** EEPROM
    // ***
    public void eepromRead(short pageNo) {
        byte[] param = new byte[2];

        param[0] = (byte)(pageNo & 0xff);
        param[1] = (byte)((pageNo >> 8) & 0xff);

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_EEPROM, Neblina.NEBLINA_COMMAND_EEPROM_READ, param.length, param);
        }
    }

    public void eepromWrite(short pageNo, byte[] data) {
        byte[] param = new byte[2 + data.length];

        param[0] = (byte)(pageNo & 0xff);
        param[1] = (byte)((pageNo >> 8) & 0xff);

        for (int i = 0; i < 8; i++) {
            param[i + 2] = data[i];
        }

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_EEPROM, Neblina.NEBLINA_COMMAND_EEPROM_WRITE, param.length, param);
        }
    }

    // *** LED subsystem commands
    public void getLed() {
        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_LED, Neblina.NEBLINA_COMMAND_LED_STATUS, 0, null);
        }
    }

    public void setLed(byte LedNo, byte Value) {
        byte[] param = new byte[2];

        param[0] = LedNo;
        param[1] = Value;

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_LED, Neblina.NEBLINA_COMMAND_LED_STATE, param.length, param);
        }
    }

    // *** Power management sybsystem commands
    public void getTemperature() {
        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_POWER, Neblina.NEBLINA_COMMAND_POWER_TEMPERATURE, 0, null);
        }
    }

    public void setBatteryChargeCurrent(short Current) {
        byte[] param = new byte[2];

        param[0] = (byte)(Current & 0xFF);
        param[1] = (byte)((Current >> 8) & 0xFF);

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_POWER, Neblina.NEBLINA_COMMAND_POWER_CHARGE_CURRENT, param.length, param);
        }
    }

    // ***
    // *** Fusion subsystem commands
    // ***
    public void setFusionRate(short Rate) {
        byte[] param = new byte[2];

        param[0] = (byte)(Rate & 0xFF);
        param[1] = (byte)((Rate >> 8) & 0xFF);

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_RATE, param.length, param);
        }
    }

    public void setFusionDownSample(short Rate) {
        byte[] param = new byte[2];

        param[0] = (byte)(Rate & 0xFF);
        param[1] = (byte)((Rate >> 8) & 0xFF);

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_DOWNSAMPLE, param.length, param);
        }
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

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_MOTION_STATE_STREAM, param.length, param);
        }
    }

    public void streamQuaternion(boolean state)
    {
        byte[] param = new byte[] { NeblinaUtilities.convertBoolToByte(state) };

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_QUATERNION_STREAM, param.length, param);
        }
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

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_EULER_ANGLE_STREAM, param.length, param);
        }
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

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_EXTERNAL_FORCE_STREAM, param.length, param);
        }
    }

    public void setFusionType(byte Mode) {
        byte[] param = new byte[1];

        param[0] = Mode;

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_FUSION_TYPE, param.length, param);
        }
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

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_TRAJECTORY_RECORD, param.length, param);
        }
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

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_TRAJECTORY_INFO_STREAM, param.length, param);
        }
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

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_PEDOMETER_STREAM, param.length, param);
        }
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

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_SITTING_STANDING_STREAM, param.length, param);
        }
    }

    public void lockHeadingReference() {
        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_LOCK_HEADING_REFERENCE, 0, null);
        }
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

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_FINGER_GESTURE_STREAM, param.length, param);
        }
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

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_ROTATION_INFO_STREAM, param.length, param);
        }
    }

    public void externalHeadingCorrection(short yaw, short error) {
        byte[] param = new byte[4];

        param[0] = (byte)(yaw & 0xFF);
        param[1] = (byte)((yaw >> 8) & 0xFF);
        param[2] = (byte)(error & 0xFF);
        param[3] = (byte)((error >> 8) & 0xFF);

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_EXTERNAL_HEADING_CORRECTION, param.length, param);
        }
    }

    public void resetAnalysis() {
        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_ANALYSIS_RESET, 0, null);
        }
    }

    public void calibrateAnalysis() {
        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_ANALYSIS_CALIBRATE, 0, null);
        }
    }

    public void createPoseAnalysis(byte id, short[] qtf) {
        byte[] param = new byte[1 + qtf.length * 2];

        param[0] = id;

        for (int i = 0; i < qtf.length; i++) {
            param[1 + (i << 1)] = (byte)(qtf[i] & 0xFF);
            param[2 + (i << 1)] = (byte)((qtf[i] >> 8) & 0xFF);
        }

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_ANALYSIS_CREATE_POSE, param.length, param);
        }
    }

    public void setActivePoseAnalysis(byte id) {
        byte[] param = new byte[1];

        param[0] = id;

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_ANALYSIS_SET_ACTIVE_POSE, param.length, param);
        }
    }

    public void getActivePoseAnalysis() {
        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_ANALYSIS_GET_ACTIVE_POSE, 0, null);
        }
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

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_ANALYSIS_STREAM, param.length, param);
        }
    }

    public void getPoseAnalysisInfo(byte id) {
        byte[] param = new byte[1];

        param[0] = id;

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_ANALYSIS_POSE_INFO, param.length, param);
        }
    }

    public void calibrateForwardPosition() {
        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_CALIBRATE_FORWARD_POSITION, 0, null);
        }
    }

    public void calibrateDownPosition() {
        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_CALIBRATE_DOWN_POSITION, 0, null);
        }
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

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_MOTION_DIRECTION_STREAM, param.length, param);
        }
    }

    // ***
    // *** Storage subsystem commands
    // ***
    public void getSessionCount() {
        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_RECORDER, Neblina.NEBLINA_COMMAND_RECORDER_SESSION_COUNT, 0, null);
        }
    }

    public void getSessionInfo(short sessionId) {
        byte[] param = new byte[2];

        param[0] = (byte)(sessionId & 0xFF);
        param[1] = (byte)((sessionId >> 8) & 0xFF);

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_RECORDER, Neblina.NEBLINA_COMMAND_RECORDER_SESSION_INFO, param.length, param);
        }
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

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_RECORDER, Neblina.NEBLINA_COMMAND_RECORDER_ERASE_ALL, param.length, param);
        }

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

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_RECORDER, Neblina.NEBLINA_COMMAND_RECORDER_PLAYBACK, param.length, param);
        }
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

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_RECORDER, Neblina.NEBLINA_COMMAND_RECORDER_RECORD, param.length, param);
        }
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

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_RECORDER, Neblina.NEBLINA_COMMAND_RECORDER_SESSION_READ, param.length, param);
        }
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

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_RECORDER, Neblina.NEBLINA_COMMAND_RECORDER_SESSION_DOWNLOAD, param.length, param);
        }
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

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_SENSOR, Neblina.NEBLINA_COMMAND_SENSOR_SET_DOWNSAMPLE, param.length, param);
        }
    }

    public void sensorSetRange(short type, short range) {
        byte[] param = new byte[4];

        param[0] = (byte)(type & 0xFF);
        param[1] = (byte)(type >> 8);
        param[2] = (byte)(range & 0xFF);
        param[3] = (byte)(range >> 8);

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_SENSOR, Neblina.NEBLINA_COMMAND_SENSOR_SET_RANGE, param.length, param);
        }
    }

    public void sensorSetRate(short type, short rate) {
        byte[] param = new byte[4];

        param[0] = (byte)(type & 0xFF);
        param[1] = (byte)(type >> 8);
        param[2] = (byte)(rate & 0xFF);
        param[3] = (byte)(rate >> 8);

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_SENSOR, Neblina.NEBLINA_COMMAND_SENSOR_SET_RATE, param.length, param);
        }
    }

    public void sensorGetDownsample(byte streamId) {
        byte[] param = new byte[1];

        param[0] = streamId;

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_SENSOR, Neblina.NEBLINA_COMMAND_SENSOR_GET_DOWNSAMPLE, param.length, param);
        }
    }

    public void sensorGetRange(byte type) {
        byte[] param = new byte[1];

        param[0] = type;

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_SENSOR, Neblina.NEBLINA_COMMAND_SENSOR_GET_RANGE, param.length, param);
        }
    }

    public void sensorGetRate(byte type) {
        byte[] param = new byte[1];

        param[0] = type;

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_SENSOR, Neblina.NEBLINA_COMMAND_SENSOR_GET_RATE, param.length, param);
        }
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

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_SENSOR, Neblina.NEBLINA_COMMAND_SENSOR_ACCELEROMETER_STREAM, param.length, param);
        }
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

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_SENSOR, Neblina.NEBLINA_COMMAND_SENSOR_GYROSCOPE_STREAM, param.length, param);
        }
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

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_SENSOR, Neblina.NEBLINA_COMMAND_SENSOR_HUMIDITY_STREAM, param.length, param);
        }
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

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_SENSOR, Neblina.NEBLINA_COMMAND_SENSOR_MAGNETOMETER_STREAM, param.length, param);
        }
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

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_SENSOR, Neblina.NEBLINA_COMMAND_SENSOR_PRESSURE_STREAM, param.length, param);
        }
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

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_SENSOR, Neblina.NEBLINA_COMMAND_SENSOR_TEMPERATURE_STREAM, param.length, param);
        }
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

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_SENSOR, Neblina.NEBLINA_COMMAND_SENSOR_ACCELEROMETER_GYROSCOPE_STREAM, param.length, param);
        }
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

        for (NeblinaDevice device : mSendingPool.values() ) {
            device.sendCommand(Neblina.NEBLINA_SUBSYSTEM_SENSOR, Neblina.NEBLINA_COMMAND_SENSOR_ACCELEROMETER_MAGNETOMETER_STREAM, param.length, param);
        }
    }
}