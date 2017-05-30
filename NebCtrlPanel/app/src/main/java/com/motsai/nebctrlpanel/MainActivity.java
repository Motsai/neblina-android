package com.motsai.nebctrlpanel;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.AbsListView;
import android.widget.Switch;
import android.widget.TextView;

import com.motsai.neblina.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.lang.Math.*;

public class MainActivity extends AppCompatActivity implements NeblinaDelegate {
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private BluetoothLeScanner mLEScanner;
    private DeviceListAdapter mAdapter;
    private Neblina mDev;
    private ListView mCmdListView;
    private TextView mTextLine1;
    private TextView mTextLine2;
    private TextView mTextLine3;
    private int mQuatRate;
    private int mQuatPeriod;
    private long mQuatTimeStamp = 0;
    private int mQuatDropCnt = 0;
    private int mQuatCnt = 0;
    private int mQuatBdCnt = 0;
    private boolean mFlashEraseProgress = false;

    public static final NebCmdItem[] cmdList = new NebCmdItem[] {
            new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_GENERAL, Neblina.NEBLINA_COMMAND_GENERAL_INTERFACE_STATE,
                    Neblina.NEBLINA_INTERFACE_STATUS_BLE, "BLE Data Port", NebCmdItem.ACTUATOR_TYPE_SWITCH, ""),
            new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_GENERAL, Neblina.NEBLINA_COMMAND_GENERAL_INTERFACE_STATE,
                    Neblina.NEBLINA_INTERFACE_STATUS_UART, "UART Data Port", NebCmdItem.ACTUATOR_TYPE_SWITCH, ""),
            new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_GENERAL, Neblina.NEBLINA_COMMAND_GENERAL_DEVICE_NAME_SET,
                    0, "Change Device Name", NebCmdItem.ACTUATOR_TYPE_TEXT_FILED_BUTTON, "Change"),
            new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_CALIBRATE_FORWARD_POSITION,
                    0, "Calibrate Forward Pos", NebCmdItem.ACTUATOR_TYPE_BUTTON, "Calib Fwrd"),
            new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_CALIBRATE_DOWN_POSITION,
                    0, "Calibrate Down Pos", NebCmdItem.ACTUATOR_TYPE_BUTTON, "Calib Dwn"),
            new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_GENERAL, Neblina.NEBLINA_COMMAND_GENERAL_RESET_TIMESTAMP,
                    0, "Reset timestamp", NebCmdItem.ACTUATOR_TYPE_BUTTON, "Reset"),
            new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_FUSION_TYPE,
                    0, "Fusion 9 axis", NebCmdItem.ACTUATOR_TYPE_SWITCH, ""),
            new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_QUATERNION_STREAM,
                    Neblina.NEBLINA_FUSION_STATUS_QUATERNION, "Quaternion Stream", NebCmdItem.ACTUATOR_TYPE_SWITCH, ""),
            new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_SENSOR, Neblina.NEBLINA_COMMAND_SENSOR_ACCELEROMETER_STREAM,
                    Neblina.NEBLINA_SENSOR_STATUS_ACCELEROMETER, "Accelerometer Sensor Stream", NebCmdItem.ACTUATOR_TYPE_SWITCH, ""),
            new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_SENSOR, Neblina.NEBLINA_COMMAND_SENSOR_GYROSCOPE_STREAM,
                    Neblina.NEBLINA_SENSOR_STATUS_GYROSCOPE, "Gyro Sensor Stream", NebCmdItem.ACTUATOR_TYPE_SWITCH, ""),
            new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_SENSOR, Neblina.NEBLINA_COMMAND_SENSOR_MAGNETOMETER_STREAM,
                    Neblina.NEBLINA_SENSOR_STATUS_MAGNETOMETER, "Mag Sensor Stream", NebCmdItem.ACTUATOR_TYPE_SWITCH, ""),
            new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_SENSOR, Neblina.NEBLINA_COMMAND_SENSOR_ACCELEROMETER_GYROSCOPE_STREAM,
                    Neblina.NEBLINA_SENSOR_STATUS_ACCELEROMETER_GYROSCOPE, "Accel & Gyro Stream", NebCmdItem.ACTUATOR_TYPE_SWITCH, ""),
            new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_SENSOR, Neblina.NEBLINA_COMMAND_SENSOR_HUMIDITY_STREAM,
                    Neblina.NEBLINA_SENSOR_STATUS_HUMIDITY, "Humidity Sensor Stream", NebCmdItem.ACTUATOR_TYPE_SWITCH, ""),
            new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_LOCK_HEADING_REFERENCE,
                    0, "Lock Heading Ref.", NebCmdItem.ACTUATOR_TYPE_BUTTON, "Lock"),
            new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_RECORDER, Neblina.NEBLINA_COMMAND_RECORDER_RECORD,
                    Neblina.NEBLINA_RECORDER_STATUS_RECORD, "Stream/Record", NebCmdItem.ACTUATOR_TYPE_BUTTON, "Start"),
            new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_RECORDER, Neblina.NEBLINA_COMMAND_RECORDER_RECORD,
                    0, "Stream/Record", NebCmdItem.ACTUATOR_TYPE_BUTTON, "Stop"),
            new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_RECORDER, Neblina.NEBLINA_COMMAND_RECORDER_RECORD,
                    0, "Flash Record", NebCmdItem.ACTUATOR_TYPE_SWITCH, ""),
            new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_RECORDER, Neblina.NEBLINA_COMMAND_RECORDER_PLAYBACK,
                    0, "Flash Playback", NebCmdItem.ACTUATOR_TYPE_SWITCH, ""),
            new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_LED, Neblina.NEBLINA_COMMAND_LED_STATE,
                    0, "Set LED0 level", NebCmdItem.ACTUATOR_TYPE_TEXT_FIELD, ""),
            new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_LED, Neblina.NEBLINA_COMMAND_LED_STATE,
                    0, "Set LED1 level", NebCmdItem.ACTUATOR_TYPE_TEXT_FIELD, ""),
            new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_LED, Neblina.NEBLINA_COMMAND_LED_STATE,
                    0, "Set LED2", NebCmdItem.ACTUATOR_TYPE_SWITCH, ""),
            new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_EEPROM, Neblina.NEBLINA_COMMAND_EEPROM_READ,
                    0, "EEPROM Read", NebCmdItem.ACTUATOR_TYPE_BUTTON, "Read"),
            new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_POWER, Neblina.NEBLINA_COMMAND_POWER_CHARGE_CURRENT,
                    0, "Charge Current in mA", NebCmdItem.ACTUATOR_TYPE_TEXT_FIELD, ""),
            new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_RECORDER, Neblina.NEBLINA_COMMAND_RECORDER_ERASE_ALL,
                    0, "Flash Erase All", NebCmdItem.ACTUATOR_TYPE_BUTTON, "Erase"),
            new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_GENERAL, Neblina.NEBLINA_COMMAND_GENERAL_FIRMWARE_UPDATE,
                    0, "Firmware Update", NebCmdItem.ACTUATOR_TYPE_BUTTON, "Enter DFU"),
            new NebCmdItem((byte)0xf, (byte)0, 0, "Motion data stream", NebCmdItem.ACTUATOR_TYPE_SWITCH, ""),
            new NebCmdItem((byte)0xf, (byte)1, 0, "Heading", NebCmdItem.ACTUATOR_TYPE_SWITCH, "")
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        //mTextView = (TextView) findViewById(R.id.text_view);

        mBluetoothAdapter = bluetoothManager.getAdapter();
        mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mAdapter  = new DeviceListAdapter(this, R.layout.nebdevice_list_content);//android.R.layout.simple_list_item_1);
        mTextLine1 = (TextView)findViewById(R.id.textView1);
        mTextLine2 = (TextView)findViewById(R.id.textView2);
        mTextLine3 = (TextView)findViewById(R.id.textView3);
        ListView listView = (ListView) findViewById(R.id.founddevice_listView);
        listView.setAdapter(mAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                DeviceListAdapter adapter = (DeviceListAdapter) parent.getAdapter();
                mLEScanner.stopScan(mScanCallback);
                if (mDev != null) {
                    mDev.Disconnect();
                }

                mDev = (Neblina)adapter.getItem(position);
                mDev.SetDelegate(MainActivity.this);
                mDev.Connect(getBaseContext());

            }
        });

        mCmdListView = (ListView) findViewById(R.id.cmd_listView);

        CmdListAdapter adapter = new CmdListAdapter(this,
                R.layout.nebcmd_item, cmdList);

        mCmdListView.setAdapter(adapter);
        mCmdListView.setTag(this);
        mCmdListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    if (mDev != null) {
                        mDev.getSystemStatus();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {

            }
            }
        );

        mLEScanner.startScan(mScanCallback);

 /*       mQuatSwitch = (Switch) findViewById(R.id.switch1);
        //mQuatSwitch.setChecked(true);

        mOnCheck = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mDev == null)
                    return;

                mDev.streamQuaternion(isChecked);
            }
        };
        mQuatSwitch.setOnCheckedChangeListener(mOnCheck);*/
    }

    public void onSwitchButtonChanged(CompoundButton button, boolean isChecked) {
        int idx = (Integer) button.getTag();
        if (idx < 0 && idx > cmdList.length)
            return;

        switch (cmdList[idx].mSubSysId) {
            case Neblina.NEBLINA_SUBSYSTEM_GENERAL:
                switch (cmdList[idx].mCmdId)
                {
                    case Neblina.NEBLINA_COMMAND_GENERAL_INTERFACE_STATE:
                        if (isChecked)
                            mDev.setDataPort(idx, (byte) 1);
                        else
                            mDev.setDataPort(idx, (byte) 0);
                        break;
                    default:
                        break;
                }
                break;

            case Neblina.NEBLINA_SUBSYSTEM_FUSION:
                switch (cmdList[idx].mCmdId) {
                    case Neblina.NEBLINA_COMMAND_FUSION_RATE:
                        break;
                    case Neblina.NEBLINA_COMMAND_FUSION_DOWNSAMPLE:
                        break;
                    case Neblina.NEBLINA_COMMAND_FUSION_MOTION_STATE_STREAM:
                        mDev.streamMotionState(isChecked);
                        break;
                    case Neblina.NEBLINA_COMMAND_FUSION_QUATERNION_STREAM:
                        mDev.streamEulerAngle(false);
                        mDev.streamQuaternion(isChecked);
                        break;
                    case Neblina.NEBLINA_COMMAND_FUSION_EULER_ANGLE_STREAM:
                        mDev.streamQuaternion(false);
                        mDev.streamEulerAngle(isChecked);
                        break;
                    case Neblina.NEBLINA_COMMAND_FUSION_EXTERNAL_FORCE_STREAM:
                        mDev.streamExternalForce(isChecked);
                        break;
                    case Neblina.NEBLINA_COMMAND_FUSION_FUSION_TYPE:
                        if (isChecked)
                            mDev.setFusionType((byte) 1);
                        else
                            mDev.setFusionType((byte) 0);
                        break;
                    case Neblina.NEBLINA_COMMAND_FUSION_TRAJECTORY_RECORD:
                        break;
                    case Neblina.NEBLINA_COMMAND_FUSION_TRAJECTORY_INFO_STREAM:
                        break;
                    case Neblina.NEBLINA_COMMAND_FUSION_PEDOMETER_STREAM:
                        break;
                    case Neblina.NEBLINA_COMMAND_FUSION_SITTING_STANDING_STREAM:
                        break;
                    case Neblina.NEBLINA_COMMAND_FUSION_LOCK_HEADING_REFERENCE:
                        break;
                    case Neblina.NEBLINA_COMMAND_FUSION_FINGER_GESTURE_STREAM:
                        break;
                    case Neblina.NEBLINA_COMMAND_FUSION_ROTATION_INFO_STREAM:
                        break;
                    case Neblina.NEBLINA_COMMAND_FUSION_EXTERNAL_HEADING_CORRECTION:
                        break;
                }
                break;
            case Neblina.NEBLINA_SUBSYSTEM_SENSOR:
                switch (cmdList[idx].mCmdId)
                {
                    case Neblina.NEBLINA_COMMAND_SENSOR_ACCELEROMETER_STREAM:
                        mDev.sensorStreamAccelData(isChecked);
                        break;
                    case Neblina.NEBLINA_COMMAND_SENSOR_GYROSCOPE_STREAM:
                        mDev.sensorStreamGyroData(isChecked);
                        break;
                    case Neblina.NEBLINA_COMMAND_SENSOR_HUMIDITY_STREAM:
                        mDev.sensorStreamHumidityData(isChecked);
                        break;
                    case Neblina.NEBLINA_COMMAND_SENSOR_MAGNETOMETER_STREAM:
                        mDev.sensorStreamMagData(isChecked);
                        break;
                    case Neblina.NEBLINA_COMMAND_SENSOR_PRESSURE_STREAM:
                        mDev.sensorStreamPressureData(isChecked);
                        break;
                    case Neblina.NEBLINA_COMMAND_SENSOR_TEMPERATURE_STREAM:
                        mDev.sensorStreamTemperatureData(isChecked);
                        break;
                    case Neblina.NEBLINA_COMMAND_SENSOR_ACCELEROMETER_GYROSCOPE_STREAM:
                        mDev.sensorStreamAccelGyroData(isChecked);
                        break;
                    case Neblina.NEBLINA_COMMAND_SENSOR_ACCELEROMETER_MAGNETOMETER_STREAM:
                        mDev.sensorStreamAccelMagData(isChecked);
                        break;
                    default:
                        break;
                }
                break;
        }
    }

    public void onButtonClick(View button) {
        int idx = (Integer) button.getTag();
        if (idx < 0 && idx > cmdList.length)
            return;
        switch (cmdList[idx].mSubSysId) {
            case Neblina.NEBLINA_SUBSYSTEM_EEPROM:
                switch (cmdList[idx].mCmdId) {
                    case Neblina.NEBLINA_COMMAND_EEPROM_READ:
                        mDev.eepromRead((short)0);
                        break;
                    case Neblina.NEBLINA_COMMAND_EEPROM_WRITE:
                        break;
                }
                break;
            case Neblina.NEBLINA_SUBSYSTEM_FUSION:
                switch (cmdList[idx].mCmdId) {
                    case Neblina.NEBLINA_COMMAND_FUSION_CALIBRATE_FORWARD_POSITION:
                        mDev.calibrateForwardPosition();
                        break;
                    case Neblina.NEBLINA_COMMAND_FUSION_CALIBRATE_DOWN_POSITION:
                        mDev.calibrateDownPosition();
                        break;
                }
                break;
            case Neblina.NEBLINA_SUBSYSTEM_RECORDER:
                switch (cmdList[idx].mCmdId) {
                    case Neblina.NEBLINA_COMMAND_RECORDER_ERASE_ALL:
                        if (mFlashEraseProgress == false) {
                            mFlashEraseProgress = true;

                            mTextLine3.setText("Erasing...");
                            mTextLine3.getRootView().postInvalidate();

                            mDev.eraseStorage(false);
                        }
                        break;
                    case Neblina.NEBLINA_COMMAND_RECORDER_RECORD:
                        if (cmdList[idx].mActiveStatus == 0) {
                            mDev.sessionRecord(false);
                        }
                        else {
                            mDev.sessionRecord(true);
                        }
                        break;
                    case Neblina.NEBLINA_COMMAND_RECORDER_SESSION_DOWNLOAD:
                        break;
                    case Neblina.NEBLINA_COMMAND_RECORDER_PLAYBACK:
                        break;
                }
                break;
            case (byte)0xFF:
                if (cmdList[idx].mCmdId==1) //start stream/record
                {
                    mDev.streamQuaternion(true);
                    //mNedDev.streamIMU(true);
                    mDev.sessionRecord(true);
                }
                else //stop stream/record
                {
                    mDev.disableStreaming();
                    mDev.sessionRecord(false);
                }
                break;
        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("callbackType", String.valueOf(callbackType));
            Log.i("result", result.toString());
            BluetoothDevice device = result.getDevice();
            ScanRecord scanRecord = result.getScanRecord();
            byte[] scanData = scanRecord.getBytes();
            String name = scanRecord.getDeviceName();
            long deviceID = 0;
            byte[] manuf = scanRecord.getManufacturerSpecificData(0x0274);


            if (name == null)
                name = device.getName();

            if (name == null || manuf == null || manuf.length < 8)
                return;


            ByteBuffer x = ByteBuffer.wrap(manuf);
            x.order(ByteOrder.LITTLE_ENDIAN);
            deviceID = x.getLong();


            ListView listView = (ListView) findViewById(R.id.founddevice_listView);
            DeviceListAdapter r = (DeviceListAdapter) listView.getAdapter();
            mAdapter.addItem(new Neblina(name, deviceID, device));
            mAdapter.notifyDataSetChanged();

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("ScanResult - Results", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };

    public class DeviceListAdapter extends BaseAdapter {

        private final Context mContext;
        private final Map<String, Neblina> mNebDevices = new HashMap<String, Neblina>();

        public DeviceListAdapter(Context context, int textViewResourceId) {//}, ArrayList<Neblina> devices) {
            //super(context, textViewResourceId);
            mContext = context;
        }

        public void addItem(Neblina dev) {
            if (mNebDevices.containsKey(dev.toString()) == false) {
                mNebDevices.put(dev.toString(), dev);
                Log.w("BLUETOOTH DEBUG", "Item added " + dev.toString());
            }
        }

        @Override
        public int getCount() {
            return mNebDevices.size();
        }

        @Override
        public Object getItem(int position) {
            return mNebDevices.values().toArray()[position];
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(R.layout.nebdevice_list_content, parent, false);
            }

            TextView textView = (TextView) convertView.findViewById(R.id.id);
            //ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
            if (mNebDevices.size() > position) {
                textView.setText(mNebDevices.values().toArray()[position].toString());
            }

            return convertView;
        }

    }

    public void updateUI(byte[] data) {
        Log.d("***** updateUI", "updateUI *****");
        for (int i = 0; i < cmdList.length; i++) {
            int status = 0;
            switch (cmdList[i].mSubSysId) {
                case Neblina.NEBLINA_SUBSYSTEM_GENERAL:
                    status = data[8];
                    break;
                case Neblina.NEBLINA_SUBSYSTEM_FUSION:
                    status = (int)data[0] | ((int)data[1] << 8) | ((int)data[2] << 16) | ((int)data[3] << 24);
                    break;
                case Neblina.NEBLINA_SUBSYSTEM_SENSOR:
                    status = (int)data[4] | ((int)data[5] << 8);
                    break;
                case Neblina.NEBLINA_SUBSYSTEM_RECORDER:
                    status = data[7];
                    break;
            }

            switch (cmdList[i].mActuator) {
                case NebCmdItem.ACTUATOR_TYPE_SWITCH:
                    Switch v = (Switch) mCmdListView.findViewWithTag(i);

                    if (v != null) {
                        if ((cmdList[i].mActiveStatus & status) == 0) {
                            v.setChecked(false);
                        }
                        else {
                            v.setChecked(true);
                        }

                        v.getRootView().postInvalidate();
                    }
                    break;
                case NebCmdItem.ACTUATOR_TYPE_BUTTON:
                    break;
            }

        }
    }

    public void didConnectNeblina(Neblina sender) {
        Log.w("BLUETOOTH DEBUG", "Connected " + sender.toString());

        sender.getSystemStatus();
        sender.getFirmwareVersion();
    }
    public void didReceiveResponsePacket(Neblina sender, int subsystem, int cmdRspId, final byte[] data, int dataLen) {
        switch (subsystem) {
            case Neblina.NEBLINA_SUBSYSTEM_GENERAL:
                switch (cmdRspId) {
                    case Neblina.NEBLINA_COMMAND_GENERAL_SYSTEM_STATUS: {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateUI(data);
                            }
                        });
                        //updateUI(data);
                    }
                    break;
                    case Neblina.NEBLINA_COMMAND_GENERAL_FIRMWARE_VERSION:
                    {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                String s = String.format("API:%d, FEN:%d.%d.%d, BLE:%d.%d.%d", data[0], data[1], data[2], data[3], data[4], data[5], data[6]);
                                TextView tv = (TextView)findViewById(R.id.version_TextView);
                                tv.setText(s);
                                tv.getRootView().postInvalidate();
                            }
                        });
                    }
                    break;
                }
            case Neblina.NEBLINA_SUBSYSTEM_FUSION:
                switch (cmdRspId) {
                    case Neblina.NEBLINA_COMMAND_FUSION_QUATERNION_STREAM:
                        mQuatRate = data[2] | (data[3] << 8);
                        mQuatPeriod = 1000000 / mQuatRate;
                        break;
                }
                break;
            case Neblina.NEBLINA_SUBSYSTEM_SENSOR:

                break;
            case Neblina.NEBLINA_SUBSYSTEM_RECORDER:
                switch (cmdRspId) {
                    case Neblina.NEBLINA_COMMAND_RECORDER_ERASE_ALL:
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mTextLine3.setText("Flash erased");
                                mTextLine3.getRootView().postInvalidate();
                            }
                        });
                        mFlashEraseProgress = false;
                        break;
                }
                break;
        }
    }

    public void didReceiveRSSI(Neblina sender, int rssi) {

    }

    public void didReceiveGeneralData(Neblina sender, int respType, int cmdRspId, final byte[] data, int dataLen, boolean errFlag) {
        switch (cmdRspId) {
            case Neblina.NEBLINA_COMMAND_GENERAL_SYSTEM_STATUS: {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        updateUI(data);
                    }
                });
                //updateUI(data);
            }
            break;
            case Neblina.NEBLINA_COMMAND_GENERAL_FIRMWARE_VERSION:
            {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String s = String.format("API:%d, FEN:%d.%d.%d, BLE:%d.%d.%d", data[0], data[1], data[2], data[3], data[4], data[5], data[6]);
                        TextView tv = (TextView)findViewById(R.id.version_TextView);
                        tv.setText(s);
                        tv.getRootView().postInvalidate();
                    }
                });
            }
            break;
        }
    }

    public void didReceiveFusionData(Neblina sender, int respType, int cmdRspId, byte[] data, int dataLen, boolean errFlag) {
        final long timeStamp = ((long)data[0] & 0xFF) | (((long)data[1] & 0xFF) << 8) | (((long)data[2] & 0xFF) << 16) | (((long)data[3] & 0xFF) << 24);
        switch (cmdRspId) {
            case Neblina.NEBLINA_COMMAND_FUSION_EULER_ANGLE_STREAM:
                final double rotx = ((double)((int)data[4] | ((int)data[5] << 8))) / 10.0;
                final double roty = ((double)((int)data[6] | ((int)data[7] << 8))) / 10.0;
                final double rotz = ((double)((int)data[8] | ((int)data[9] << 8))) / 10.0;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        String s = String.format("Euler : T : %d - (Yaw : %f, Ptich : %f, Roll : %f)", timeStamp, rotx, roty, rotz);
                        //Log.w("BLUETOOTH DEBUG", s);
                        mTextLine1.setText(s);
                        mTextLine1.getRootView().postInvalidate();
                    }
                });

                break;
            case Neblina.NEBLINA_COMMAND_FUSION_QUATERNION_STREAM:
                final double q1 = ((double)((int)data[4] | ((int)data[5] << 8))) / 32768.0;
                final double q2 = ((double)((int)data[6] | ((int)data[7] << 8))) / 32768.0;
                final double q3 = ((double)((int)data[8] | ((int)data[9] << 8))) / 32768.0;
                final double q4 = ((double)((int)data[10] | ((int)data[11] << 8))) / 32768.0;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                    String s = String.format("T : %d - (%f, %f, %f, %f)", timeStamp, q1, q2, q3, q4);
                    //Log.w("BLUETOOTH DEBUG", s);
                    mTextLine1.setText(s);
                    mTextLine1.getRootView().postInvalidate();
                    }
                });

                //ByteBuffer ar = ByteBuffer.wrap(data);

                //int ts = (data[0] & 0xFF) | ((data[1] & 0xFF) << 8) | ((data[2] & 0xFF) << 16) | ((data[3] & 0xFF) << 24);
                long dt = 0;
                if (timeStamp == mQuatTimeStamp) {
                    mQuatBdCnt++;
                }
                if (timeStamp > mQuatTimeStamp) {
                    dt = timeStamp - mQuatTimeStamp;
                }
                else {
                    dt = (long)0xFFFFFFFF - mQuatTimeStamp + timeStamp;
                }

                if (dt < 0) {
                    dt = -dt;
                }
                if (dt > (mQuatPeriod + (mQuatPeriod >> 1)) || dt < (mQuatPeriod - (mQuatPeriod >> 1))) {
                    mQuatDropCnt++;
                }
                mQuatCnt++;


                final long finalDt = dt;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String s = String.format("%d, %d %d %d", finalDt, mQuatCnt, mQuatDropCnt, mQuatBdCnt);
                        mTextLine2.setText(s);
                        mTextLine2.getRootView().postInvalidate();
                    }
                });
                mQuatTimeStamp = timeStamp;
                break;
        }
    }

    public void didReceivePmgntData(Neblina sender, int respType, int cmdRspId, byte[] data, int dataLen, boolean errFlag) {

    }
    public void didReceiveLedData(Neblina sender, int respType, int cmdRspId, byte[] data, int dataLen, boolean errFlag) {

    }
    public void didReceiveDebugData(Neblina sender, int respType, int cmdRspId, byte[] data, int dataLen, boolean errFlag) {

    }
    public void didReceiveRecorderData(Neblina sender, int respType, int cmdRspId, byte[] data, int dataLen, boolean errFlag) {
    }
    public void didReceiveEepromData(Neblina sender, int respType, int cmdRspId, byte[] data, int dataLen, boolean errFlag) {

    }
    public void didReceiveSensorData(Neblina sender, int respType, int cmdRspId, byte[] data, int dataLen, boolean errFlag) {
        final long timeStamp = ((long)data[0] & 0xFF) | (((long)data[1] & 0xFF) << 8) | (((long)data[2] & 0xFF) << 16) | (((long)data[3] & 0xFF) << 24);
        switch (cmdRspId) {
            case Neblina.NEBLINA_COMMAND_SENSOR_ACCELEROMETER_STREAM: {
                    final int x = ((int) data[4] & 0xff) | ((int) data[5] << 8);
                    final int y = ((int) data[6] & 0xff) | ((int) data[7] << 8);
                    final int z = ((int) data[8] & 0xff) | ((int) data[9] << 8);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            String s = String.format("Accel : %d - (%d, %d, %d)", timeStamp, x, y, z);
                            //Log.w("BLUETOOTH DEBUG", s);
                            mTextLine1.setText(s);
                            mTextLine1.getRootView().postInvalidate();
                        }
                    });
                }
                break;
            case Neblina.NEBLINA_COMMAND_SENSOR_GYROSCOPE_STREAM: {
                    final int x = ((int) data[4] & 0xff) | ((int) data[5] << 8);
                    final int y = ((int) data[6] & 0xff) | ((int) data[7] << 8);
                    final int z = ((int) data[8] & 0xff) | ((int) data[9] << 8);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            String s = String.format("Gyro : %d - (%d, %d, %d)", timeStamp, x, y, z);
                            //Log.w("BLUETOOTH DEBUG", s);
                            mTextLine1.setText(s);
                            mTextLine1.getRootView().postInvalidate();
                            }
                    });
                }
                break;
            case Neblina.NEBLINA_COMMAND_SENSOR_HUMIDITY_STREAM: {
                    int x = ((int) data[4] & 0xff) | ((int) data[5] << 8) | ((int) data[6] << 16) | ((int) data[7] << 24);
                    final float xf = (float) x / (float)100.0;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            String s = String.format("Humidity : %f %", xf);
                            //Log.w("BLUETOOTH DEBUG", s);
                            mTextLine1.setText(s);
                            mTextLine1.getRootView().postInvalidate();
                        }
                    });
                }
                break;
            case Neblina.NEBLINA_COMMAND_SENSOR_MAGNETOMETER_STREAM: {
                    final int x = ((int) data[4] & 0xff) | ((int) data[5] << 8);
                    final int y = ((int) data[6] & 0xff) | ((int) data[7] << 8);
                    final int z = ((int) data[8] & 0xff) | ((int) data[9] << 8);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {

                            String s = String.format("Mag : %d - (%d, %d, %d)", timeStamp, x, y, z);
                            //Log.w("BLUETOOTH DEBUG", s);
                            mTextLine1.setText(s);
                            mTextLine1.getRootView().postInvalidate();
                        }
                    });
                }
                break;
            case Neblina.NEBLINA_COMMAND_SENSOR_PRESSURE_STREAM:
                break;
            case Neblina.NEBLINA_COMMAND_SENSOR_TEMPERATURE_STREAM:
                break;
            case Neblina.NEBLINA_COMMAND_SENSOR_ACCELEROMETER_GYROSCOPE_STREAM:
                break;
            case Neblina.NEBLINA_COMMAND_SENSOR_ACCELEROMETER_MAGNETOMETER_STREAM:
                break;
        }
    }
}
