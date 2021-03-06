package com.mygdx.game.android.NeblinaClasses;


/**
 * Created by scott on 2016-06-30.
 */

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.cognito.DefaultSyncCallback;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBMapper;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.DynamoDBScanExpression;
import com.amazonaws.mobileconnectors.dynamodbv2.dynamodbmapper.PaginatedScanList;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClient;
import com.mygdx.game.android.Adapters.NebCmdItem;
import com.mygdx.game.android.Adapters.NebListAdapter;
import com.mygdx.game.android.ControlPanel.BLEDeviceScanActivity;
import com.mygdx.game.android.ControlPanel.DynamicData;
import com.mygdx.game.android.R;
import com.mygdx.game.simulation.Simulation;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.mygdx.game.android.NeblinaClasses.Neblina.DEBUG_CMD_DUMP_DATA;
import static com.mygdx.game.android.NeblinaClasses.Neblina.DEBUG_CMD_GET_DATAPORT;
import static com.mygdx.game.android.NeblinaClasses.Neblina.DEBUG_CMD_GET_FW_VERSION;
import static com.mygdx.game.android.NeblinaClasses.Neblina.DEBUG_CMD_MOTENGINE_RECORDER_STATUS;
import static com.mygdx.game.android.NeblinaClasses.Neblina.DEBUG_CMD_SET_DATAPORT;
import static com.mygdx.game.android.NeblinaClasses.Neblina.DEBUG_CMD_SET_INTERFACE;
import static com.mygdx.game.android.NeblinaClasses.Neblina.EEPROM_CMD_READ;
import static com.mygdx.game.android.NeblinaClasses.Neblina.EEPROM_CMD_WRITE;
import static com.mygdx.game.android.NeblinaClasses.Neblina.MOTION_CMD_DOWN_SAMPLE;
import static com.mygdx.game.android.NeblinaClasses.Neblina.MOTION_CMD_EULER_ANGLE;
import static com.mygdx.game.android.NeblinaClasses.Neblina.MOTION_CMD_EXTFORCE;
import static com.mygdx.game.android.NeblinaClasses.Neblina.MOTION_CMD_IMU_DATA;
import static com.mygdx.game.android.NeblinaClasses.Neblina.MOTION_CMD_MAG_DATA;
import static com.mygdx.game.android.NeblinaClasses.Neblina.MOTION_CMD_MOTION_STATE;
import static com.mygdx.game.android.NeblinaClasses.Neblina.MOTION_CMD_QUATERNION;
import static com.mygdx.game.android.NeblinaClasses.Neblina.MOTION_CMD_SET_FUSION_TYPE;
import static com.mygdx.game.android.NeblinaClasses.Neblina.MOTION_CMD_TRAJECTORY_RECORD;
import static com.mygdx.game.android.NeblinaClasses.Neblina.NEB_CTRL_SUBSYS_DEBUG;
import static com.mygdx.game.android.NeblinaClasses.Neblina.NEB_CTRL_SUBSYS_EEPROM;
import static com.mygdx.game.android.NeblinaClasses.Neblina.NEB_CTRL_SUBSYS_MOTION_ENG;
import static com.mygdx.game.android.NeblinaClasses.Neblina.NEB_CTRL_SUBSYS_STORAGE;
import static com.mygdx.game.android.NeblinaClasses.Neblina.STORAGE_CMD_ERASE;
import static com.mygdx.game.android.NeblinaClasses.Neblina.STORAGE_CMD_PLAY;
import static com.mygdx.game.android.NeblinaClasses.Neblina.STORAGE_CMD_RECORD;

//This Class implements a Detail List for one specific selected Neblina device
public class NebDeviceDetailFragment extends Fragment implements NeblinaDelegate {

    private Neblina mNebDev;

     // The fragment argument representing the item ID that this fragment represents.
    public static final String ARG_ITEM_ID = "item_id";

    // Adding one line will create a new button that relates to the different Neblina Commands
    public static final NebCmdItem[] cmdList = new NebCmdItem[] {
            new NebCmdItem(NEB_CTRL_SUBSYS_DEBUG, DEBUG_CMD_SET_DATAPORT, "BLE Data Port", 1, ""),
            new NebCmdItem(NEB_CTRL_SUBSYS_DEBUG, DEBUG_CMD_SET_DATAPORT, "UART Data Port", 1, ""),
            new NebCmdItem(NEB_CTRL_SUBSYS_MOTION_ENG, MOTION_CMD_QUATERNION, "Quaternion Stream", 1, ""),
            new NebCmdItem(NEB_CTRL_SUBSYS_MOTION_ENG, MOTION_CMD_IMU_DATA, "IMU Stream", 1, ""),
            new NebCmdItem(NEB_CTRL_SUBSYS_STORAGE, STORAGE_CMD_RECORD, "Start Record", 2, "START"),
            new NebCmdItem(NEB_CTRL_SUBSYS_STORAGE, STORAGE_CMD_RECORD, "Stop Record", 2, "STOP"),
            new NebCmdItem(NEB_CTRL_SUBSYS_MOTION_ENG, MOTION_CMD_SET_FUSION_TYPE, "Fusion 9 axis", 1, ""),
            new NebCmdItem(NEB_CTRL_SUBSYS_MOTION_ENG, MOTION_CMD_MOTION_STATE, "Notify MOTION STATE", 1, ""),
            new NebCmdItem(NEB_CTRL_SUBSYS_MOTION_ENG, MOTION_CMD_MAG_DATA, "Mag Stream", 1, ""),
            new NebCmdItem(NEB_CTRL_SUBSYS_MOTION_ENG, Neblina.MOTION_CMD_LOCK_HEADING_REF, "Lock Heading Ref.", 1, ""),
            new NebCmdItem(NEB_CTRL_SUBSYS_STORAGE, Neblina.STORAGE_CMD_ERASE, "Flash Erase All", 1, ""),
            new NebCmdItem(NEB_CTRL_SUBSYS_STORAGE, STORAGE_CMD_RECORD, "Flash Record", 1, ""),
//            new NebCmdItem(NEB_CTRL_SUBSYS_STORAGE, STORAGE_CMD_PLAY, "Flash Playback", 1, ""),
            new NebCmdItem(Neblina.NEB_CTRL_SUBSYS_LED, Neblina.LED_CMD_SET_VALUE, "Set LED0 level", 3, ""),
            new NebCmdItem(Neblina.NEB_CTRL_SUBSYS_LED, Neblina.LED_CMD_SET_VALUE, "Set LED1 level", 3, ""),
            new NebCmdItem(Neblina.NEB_CTRL_SUBSYS_LED, Neblina.LED_CMD_SET_VALUE, "Set LED2", 1, ""),
            new NebCmdItem(NEB_CTRL_SUBSYS_EEPROM, EEPROM_CMD_READ, "EEPROM Read", 2, "Read"),
            new NebCmdItem(Neblina.NEB_CTRL_SUBSYS_POWERMGMT, Neblina.POWERMGMT_CMD_SET_CHARGE_CURRENT, "Charge Current in mA", 3, ""),
            new NebCmdItem((byte)0xf, (byte)0, "Motion data stream", 1, ""),
            new NebCmdItem((byte)0xf, (byte)1, "Heading", 1, ""),
            new NebCmdItem(NEB_CTRL_SUBSYS_MOTION_ENG, Neblina.MOTION_CMD_PEDOMETER, "Pedometer", 1, ""),
            new NebCmdItem(NEB_CTRL_SUBSYS_MOTION_ENG, Neblina.MOTION_CMD_SIT_STAND, "Sit Stand", 1, "")
    };

    //The dummy content this fragment is presenting.
    private TextView mTextLabel1;
    private TextView mTextLabel2;
    private TextView mTextLabel3;
    private TextView mTextLabel4;
    private TextView mVersionText;
    private ListView mCmdListView;

    //Static Store for all the Spaceship quaternion values
    public static float[] latest_Q0s = new float[Simulation.MAX_SHIPS];
    public static float[] latest_Q1s = new float[Simulation.MAX_SHIPS];
    public static float[] latest_Q2s = new float[Simulation.MAX_SHIPS];
    public static float[] latest_Q3s = new float[Simulation.MAX_SHIPS];
    public static String Q0_string = "";
    public static String Q1_string = "";
    public static String Q2_string = "";
    public static String Q3_string = "";
    public static long timestamp_N =0;

    private Quaternions quaternionAWS = null;

    //Dynamic Graph Activity
    public static DynamicData dynamicDataActivity;
    public static boolean upAndRunning = false;

    //AWS Variables
    public String identityID = "";
    private boolean runDeleteOnce = true;


    //Mandatory empty constructor for the fragment manager to instantiate the fragment (e.g. upon screen orientation changes)
    public NebDeviceDetailFragment() {
    }

    //Maps a specific Neblina device to the Detail List Fragment
    public void SetItem(Neblina item) {
        mNebDev = item;
        mNebDev.SetDelegate(this);
    }


    //Default onCreate() function
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }


    //Here we setup the list of Buttons
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.nebdevice_detail, container, false);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTextLabel1 = (TextView) rootView.findViewById(R.id.textView1);
                mTextLabel2 = (TextView) rootView.findViewById(R.id.textView2);
                mTextLabel3 = (TextView) rootView.findViewById(R.id.textView3);
                mTextLabel4 = (TextView) rootView.findViewById(R.id.textView4);
                mVersionText = (TextView) rootView.findViewById(R.id.versionText);
                mCmdListView = (ListView) rootView.findViewById(R.id.listView);
            }
        });

        NebListAdapter adapter = new NebListAdapter(getActivity().getApplicationContext(),
                R.layout.nebcmd_item, cmdList);

        mCmdListView.setAdapter(adapter);
        mCmdListView.setTag(this);
        mCmdListView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {

                    if (mNebDev != null) {
                        mNebDev.initializeState=2;
                        mNebDev.getFirmwareVersion();
                    }
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i1, int i2) {

            }
        });
        return rootView;
    }

    //Here we process button presses
    public void onSwitchButtonChanged(CompoundButton button, boolean isChecked) {
        int idx = (int) button.getTag();
        if (idx < 0 && idx > cmdList.length){
            Log.w("DEBUG", "Using a strange Tag");
            return;
        }

        //Here is where we process the button presses
        switch (cmdList[idx].mSubSysId) {
            case NEB_CTRL_SUBSYS_DEBUG:
                switch (cmdList[idx].mCmdId)
                {
                    case DEBUG_CMD_SET_INTERFACE:
//                        mNebDev.setInterface(isChecked == true ? 1);
                        break;
                    case DEBUG_CMD_DUMP_DATA:
                        break;
                    case DEBUG_CMD_SET_DATAPORT:
                        if (isChecked)
                            mNebDev.setDataPort(idx, (byte) 1);
                        else
                            mNebDev.setDataPort(idx, (byte) 0);
                        break;
                    default:
                        break;
                }
                break;

            //Respond to Flash Record, and Flash Erase Buttons
            case NEB_CTRL_SUBSYS_STORAGE:
                switch (cmdList[idx].mCmdId){
                    case STORAGE_CMD_RECORD:
                        if (isChecked)
                            mNebDev.sessionRecord(true);
                        else
                            mNebDev.sessionRecord(false);
                        break;
                    case STORAGE_CMD_ERASE:
                        if (isChecked)
                            mNebDev.eraseStorage(true);
                        else
                            mNebDev.eraseStorage(false);
                        break;
                }


            case NEB_CTRL_SUBSYS_MOTION_ENG:
                switch (cmdList[idx].mCmdId) {
                    case MOTION_CMD_QUATERNION:
                        mNebDev.streamQuaternion(isChecked);
                        break;
                    case MOTION_CMD_IMU_DATA:
                        mNebDev.streamIMU(isChecked);
                        break;
                    case MOTION_CMD_MOTION_STATE:
                        mNebDev.streamMotionState(isChecked);
                        break;
                    default:
                        Log.w("BLUETOOTH_DEBUG","COMMAND NOT IMPLEMENTED!");
                }
                break;
        }
    }

    public void onButtonClick(View button) {
        int idx = (int) button.getTag();
        if (idx < 0 && idx > cmdList.length)
            return;
        switch (cmdList[idx].mSubSysId) {
            case NEB_CTRL_SUBSYS_EEPROM:
                switch (cmdList[idx].mCmdId) {
                    case EEPROM_CMD_READ:
                        mNebDev.eepromRead(0);
                        break;
                    case EEPROM_CMD_WRITE:
                        break;
                }
                break;
            case NEB_CTRL_SUBSYS_STORAGE:
                int i = getCmdIdx(NEB_CTRL_SUBSYS_STORAGE, STORAGE_CMD_RECORD);
                if (idx == i ){

                    mNebDev.streamQuaternion(true);
                    try{
                        TimeUnit.MILLISECONDS.sleep(250);
                    }
                    catch (InterruptedException e){
                        Log.w("BLUETOOTH_DEBUG","Interrupted");
                    }


                    mNebDev.streamIMU(true);

                    try{
                        TimeUnit.MILLISECONDS.sleep(250);
                    }
                    catch (InterruptedException e){
                        Log.w("BLUETOOTH_DEBUG","Interrupted");
                    }
                    mNebDev.sessionRecord(true);

                }else{
                    mNebDev.streamDisableAll();

                    try{
                        TimeUnit.MILLISECONDS.sleep(250);
                    }
                    catch (InterruptedException e){
                        Log.w("BLUETOOTH_DEBUG","Interrupted");
                    }

                    mNebDev.sessionRecord(false);
                }

        }
    }

    public void didReceiveRSSI(int rssi) {
    }

    public void didReceiveFusionData(int type , byte[] data, boolean errFlag,int deviceNum) {
        switch (type) {
            case MOTION_CMD_QUATERNION:

                //TODO: CLEAN UP THIS MESSY CODE... SIMPLE CALLS CAN BE USED TO DECODE THESE PACKETS
                //Merge Note B. Original Code
                //Puts the characteristic values into the intent
                if (data != null && data.length > 0) {
                    final StringBuilder stringBuilder = new StringBuilder(data.length);
                    for (byte byteChar : data)
                        stringBuilder.append(String.format("%02X ", byteChar));
                }

                //TODO: Fix timestamping
//            timestamp_N = (timestamp[3]&0xff)<<24 | (timestamp[2]&0xff)<<16 | (timestamp[1]&0xff)<<8 | (timestamp[0]&0xff)<<0;

                //Unwrap Data Based on Motsai's Neblina Protocol
                if (data.length == 16) {
                    //Plus 1 is to remind me that the end of the range is non-inclusive
                    //Minus 4 since the header and timestamp are chopped off
//                    final byte[] header = Arrays.copyOfRange(data, 0, 3 + 1); //Bytes 0-3 are the header
//                    final byte[] timestamp = Arrays.copyOfRange(data, 4, 7 + 1); //Bytes 4-7 are the timestamp
                    final byte[] q0 = Arrays.copyOfRange(data, 8-4, 9-4 + 1); // Bytes 8-9 are Q0 value
                    final byte[] q1 = Arrays.copyOfRange(data, 10-4, 11-4 + 1); // Bytes 10-11 are Q1 value
                    final byte[] q2 = Arrays.copyOfRange(data, 12-4, 13-4 + 1); // Bytes 12-13 are Q2 value
                    final byte[] q3 = Arrays.copyOfRange(data, 14-4, 15-4 + 1); // Bytes 14-15 are Q3 value
                    final byte[] reserved = Arrays.copyOfRange(data, 16-4, 19-4 + 1); // Bytes 16-19 are reserved

                    latest_Q0s[deviceNum] = normalizedQ(q0);
                    latest_Q1s[deviceNum] = normalizedQ(q1);
                    latest_Q2s[deviceNum] = normalizedQ(q2);
                    latest_Q3s[deviceNum] = normalizedQ(q3);

                    //Create a string version
                    Q0_string = String.valueOf(latest_Q0s[deviceNum]);
                    Q1_string = String.valueOf(latest_Q1s[deviceNum]);
                    Q2_string = String.valueOf(latest_Q2s[deviceNum]);
                    Q3_string = String.valueOf(latest_Q3s[deviceNum]);

                    //Merge Note A. Neblina Code
                    final String s1 = String.format(Q0_string+ ", ");
                    final String s2 = String.format(Q1_string + ", " );
                    final String s3 = String.format(Q2_string + ", " );
                    final String s4 = String.format(Q3_string );

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            mTextLabel1.setText(s1);
                            mTextLabel2.setText(s2);
                            mTextLabel3.setText(s3);
                            mTextLabel4.setText(s4);
                            mTextLabel1.getRootView().postInvalidate();
                            mTextLabel2.getRootView().postInvalidate();
                            mTextLabel3.getRootView().postInvalidate();
                            mTextLabel4.getRootView().postInvalidate();
                        }
                    });

                    if(BLEDeviceScanActivity.isStreaming) {
                        quaternionAWS = new Quaternions(String.valueOf(System.currentTimeMillis()), latest_Q0s[deviceNum], latest_Q1s[deviceNum], latest_Q2s[deviceNum], latest_Q3s[deviceNum]);
                        new runAWS().execute();
                    }

                break;
        }
                break;

            case MOTION_CMD_DOWN_SAMPLE:
                Log.w("BLUETOOTH_DEBUG", "COMMAND: MOTION_CMD_DOWN_SAMPLE");
                break;
            case MOTION_CMD_MOTION_STATE:
                short motion = (short) data[8-4];
                //Byte 8 is motion start(1) or stop (0)
                Log.w("BLUETOOTH_DEBUG", "COMMAND: MOTION_CMD_MOTION_STATE:" + motion);
                break;
            case MOTION_CMD_IMU_DATA:
                short valAX = (short)(((data[9-4]&0xff)<<8)|(data[8-4]&0xff)); //extract the value
                float normalizedAX = (float) valAX / 32768; //normalize by dividing by 2^15
                if (normalizedAX > 1.0) normalizedAX = normalizedAX-2;

                short valAY = (short)(((data[11-4]&0xff)<<8)|(data[10-4]&0xff));
                float normalizedAY = (float) valAY / 32768; //normalize by dividing by 2^15
                if (normalizedAY > 1.0) normalizedAY = normalizedAY-2;


                short valAZ = (short)(((data[13-4]&0xff)<<8)|(data[12-4]&0xff));
                float normalizedAZ = (float) valAZ / 32768; //normalize by dividing by 2^15
                if (normalizedAZ > 1.0) normalizedAZ = normalizedAZ-2;

                double magnitude = Math.sqrt(((double) valAX)*((double) valAX) + ((double) valAY)*((double) valAY) + ((double) valAZ)*((double) valAZ));

                if(upAndRunning) {
                    dynamicDataActivity.onSensorChanged(normalizedAX * 100, normalizedAY * 100, normalizedAZ * 100);
                }
//                Log.w("BLUETOOTH_DEBUG", "Accelerometer: " + normalizedAX + "," + normalizedAY + "," + normalizedAZ);
                break;
            case MOTION_CMD_EULER_ANGLE:
                Log.w("BLUETOOTH_DEBUG", "COMMAND: MOTION_CMD_EULER_ANGLE");
                break;
            case MOTION_CMD_EXTFORCE:
                Log.w("BLUETOOTH_DEBUG", "COMMAND: MOTION_CMD_EXTFORCE");
                break;
            case MOTION_CMD_SET_FUSION_TYPE:
                Log.w("BLUETOOTH_DEBUG", "COMMAND: MOTION_CMD_SET_FUSION_TYPE");
                break;
            case MOTION_CMD_TRAJECTORY_RECORD:
                Log.w("BLUETOOTH_DEBUG", "COMMAND: MOTION_CMD_TRAJECTORY_RECORD");
                break;
            default:
                Log.w("BLUETOOTH_DEBUG", "COMMAND CODE NOT RECOGNIZED");

        }
    }

    public void didReceiveDebugData(int type, final byte[] data, int dataLen, boolean errFlag) {

        Log.w("BLUETOOTH DEBUG", "RECEIVING DEBUG INFORMATION! type: " + type);
        NebListAdapter adapter = (NebListAdapter) mCmdListView.getAdapter();

        switch (type) {
            case DEBUG_CMD_MOTENGINE_RECORDER_STATUS:
            {
                switch (data[8]) {
                    case 1:    // Playback
                    {
                        int i = getCmdIdx(NEB_CTRL_SUBSYS_STORAGE, STORAGE_CMD_RECORD);
                        final Switch v = (Switch) mCmdListView.findViewWithTag(i);
                        if (v != null) {

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    v.setChecked(false);
                                    v.getRootView().postInvalidate();
                                }
                            });

                        }
                        i = getCmdIdx(NEB_CTRL_SUBSYS_STORAGE, STORAGE_CMD_PLAY);
                        final Switch v2 = (Switch) mCmdListView.findViewWithTag(i);
                        if (v2 != null) {

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    v2.setChecked(true);
                                    v2.getRootView().postInvalidate();
                                }
                            });
                        }
                    break;
                    }
                    case 2:    // Recording
                    {
                        int i = getCmdIdx(NEB_CTRL_SUBSYS_STORAGE, STORAGE_CMD_PLAY);
                        final Switch v = (Switch) mCmdListView.findViewWithTag(i);
                        if (v != null) {

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    v.setChecked(false);
                                    v.getRootView().postInvalidate();
                                }
                            });


                        }
                        i = getCmdIdx(NEB_CTRL_SUBSYS_STORAGE, STORAGE_CMD_RECORD);
                        final Switch v2 = (Switch) mCmdListView.findViewWithTag(i);
                        if (v2 != null) {

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    v2.setChecked(true);
                                    v2.getRootView().postInvalidate();
                                }
                            });
                        }
                    break;
                    }

                    case 3: { //Storage
                        int i = getCmdIdx(NEB_CTRL_SUBSYS_STORAGE, STORAGE_CMD_RECORD);
                        final Switch v = (Switch) mCmdListView.findViewWithTag(i);
                        if (v != null) {

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    v.setChecked(false);
                                    v.getRootView().postInvalidate();
                                }
                            });


                        }
                        i = getCmdIdx(NEB_CTRL_SUBSYS_STORAGE, STORAGE_CMD_PLAY);
                        final Switch v2 = (Switch) mCmdListView.findViewWithTag(i);
                        if (v2 != null) {

                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    v2.setChecked(false);
                                    v2.getRootView().postInvalidate();
                                }
                            });
                        }
                        break;
                    }
                    default: {
                        Log.w("BLUETOOTH_DEBUG", "Unhandled Button Case" + data[8]);
                        break;
                    }
                }

                int i = getCmdIdx(NEB_CTRL_SUBSYS_MOTION_ENG, MOTION_CMD_QUATERNION);
                final Switch v = (Switch) mCmdListView.findViewWithTag(i);

                if (v != null) {

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            v.setChecked(((data[4] & 8) >> 3) != 0);
                            v.getRootView().postInvalidate();
                        }
                    });

                }
                i = getCmdIdx(NEB_CTRL_SUBSYS_MOTION_ENG, MOTION_CMD_MAG_DATA);
                final Switch v2 = (Switch) mCmdListView.findViewWithTag(i);
                if (v2 != null) {

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            v2.setChecked(((data[4] & 0x80) >> 7) != 0);
                            v2.getRootView().postInvalidate();
                        }
                    });

                }
                break;
            }
//TODO: FIX THESE CASES
            case DEBUG_CMD_GET_FW_VERSION:
            {
                final String s = String.format("API:%d, FEN:%d.%d.%d, BLE:%d.%d.%d", data[0], data[1], data[2], data[3], data[4], data[5], data[6]);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mVersionText.setText(s);
                        mVersionText.getRootView().postInvalidate();
                    }
                });

            }
            break;
            case DEBUG_CMD_DUMP_DATA:
            {
                final String s = String.format("%02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x",
                        data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9],
                        data[10], data[11], data[12], data[13], data[14], data[15]);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTextLabel1.setText(s);
                        mTextLabel1.getRootView().postInvalidate();
                    }
                });

            }
            break;
            case DEBUG_CMD_SET_DATAPORT:
                //TODO: Set the switch button to ON for the BLE PORT
                break;

            case DEBUG_CMD_GET_DATAPORT:
                int i = getCmdIdx(NEB_CTRL_SUBSYS_DEBUG, DEBUG_CMD_SET_DATAPORT);
                final Switch v = (Switch) mCmdListView.findViewWithTag(i);
                if (v != null) {

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            v.setChecked(data[0] != 0);
                            v.getRootView().postInvalidate();
                        }
                    });
                }
                final Switch v2 = (Switch) mCmdListView.findViewWithTag(i + 1);
                if (v2 != null) {

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            v2.setChecked(data[1] != 0);
                            v2.getRootView().postInvalidate();
                        }
                    });
                }
                break;
        }
    }
    public void didReceivePmgntData(int type, byte[] data, int dataLen, boolean errFlag) {
        //Implement what you want to do with the data here
    }
    public void didReceiveStorageData(int type, byte[] data, int dataLen, boolean errFlag) {
        //Implement what you want to do with the data here
    }
    public void didReceiveEepromData(int type, byte[] data, int dataLen, boolean errFlag) {
        //Implement what you want to do with the data here
    }
    public void didReceiveLedData(int type, byte[] data, int dataLen, boolean errFlag) {
        //Implement what you want to do with the data here
    }

    /********************************** AWS Networking Functions *********************************/

    public class runAWS extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                    getContext(),
                    "us-east-1:6e702b0c-80ab-4461-9ec3-239f1d163cd5", // Identity Pool ID
                    Regions.US_EAST_1 // Region
            );


            identityID = credentialsProvider.getIdentityId();
            Log.w("AWS_DEBUG", "Doing Stuff in background. ID = " + identityID);

            // Initialize the Cognito Sync client
            CognitoSyncManager syncClient = new CognitoSyncManager(
                    getContext(),
                    Regions.US_EAST_1, // Region
                    credentialsProvider);

            // Create a record in a dataset and synchronize with the server
//            com.amazonaws.mobileconnectors.cognito.Dataset dataset = syncClient.openOrCreateDataset("myDataset");
//            dataset.put("myKey", "myValue");
//            dataset.synchronize(new DefaultSyncCallback() {
//                @Override
//                public void onSuccess(com.amazonaws.mobileconnectors.cognito.Dataset dataset, List newRecords) {
//                            Log.w("AWS_DEBUG", "Creating a Record was successful!" + identityID);
//                }
//            });

            AmazonDynamoDBClient ddbClient = new AmazonDynamoDBClient(credentialsProvider);
            DynamoDBMapper mapper = new DynamoDBMapper(ddbClient);

//        clearDatabase(mapper);
            mapper.save(quaternionAWS); //seems to only create one item at a time...

            return null;
        }
    }

    //BE CAREFUL!!! This function will delete everything in the database... USE AT YOUR OWN RISK!!!!
    private void clearDatabase(DynamoDBMapper mapper) {
        DynamoDBScanExpression scanExpression = new DynamoDBScanExpression();
        PaginatedScanList<Quaternions> result = mapper.scan(Quaternions.class, scanExpression);
        for (Quaternions data : result) {
            mapper.delete(data);
        }
    }

    /*************************************** Helper function *************************************/
    public int getCmdIdx(int subsysId, int cmdId) {
        for (int i = 0; i < cmdList.length; i++) {
            if (cmdList[i].mSubSysId == subsysId && cmdList[i].mCmdId == cmdId) {
                return i;
            }
        }
        return -1;
    }

    private float normalizedQ(byte[] q) {
        if(q.length==2){
            int val = ((q[1]&0xff)<<8)|(q[0]&0xff); //concatenate the byte array into an int
            float normalized = (float) val / 32768; //normalize by dividing by 2^15
            if (normalized > 1.0) normalized = normalized-2;
            return normalized;
        }else return -1;
    }
}
