package com.motsai.nebctrlpanel;

import android.content.Intent;
import android.content.Context;
import android.app.Activity;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.SimpleCursorAdapter;

import com.motsai.neblina.Neblina;
import com.motsai.neblina.NeblinaDelegate;
import com.motsai.neblina.NebCmdItem;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import java.nio.channels.NotYetBoundException;
import java.util.List;

import static com.motsai.neblina.Neblina.*;

/**
 * A fragment representing a single NebDevice detail screen.
 * This fragment is either contained in a {@link NebDeviceListActivity}
 * in two-pane mode (on tablets) or a {@link NebDeviceDetailActivity}
 * on handsets.
 */
public class NebDeviceDetailFragment extends Fragment implements NeblinaDelegate {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */
    public static final String ARG_ITEM_ID = "item_id";
    public static final NebCmdItem[] cmdList = new NebCmdItem[] {
        new NebCmdItem((byte)0xff, (byte)0x01, "Stream/Record", 2, "Start"),
        new NebCmdItem((byte)0xff, (byte)0x0, "Stream/Record", 2, "Stop"),
        new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_GENERAL, Neblina.NEBLINA_COMMAND_GENERAL_INTERFACE_STATE, "BLE Data Port", 1, ""),
        new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_GENERAL, Neblina.NEBLINA_COMMAND_GENERAL_INTERFACE_STATE, "UART Data Port", 1, ""),
        new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_FUSION_TYPE, "Fusion 9 axis", 1, ""),
        new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_QUATERNION_STREAM, "Quaternion Stream", 1, ""),
        new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_SENSOR, Neblina.NEBLINA_COMMAND_SENSOR_MAGNETOMETER_STREAM, "Mag Stream", 1, ""),
        new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_LOCK_HEADING_REFERENCE, "Lock Heading Ref.", 1, ""),
        new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_RECORDER, Neblina.NEBLINA_COMMAND_RECORDER_ERASE_ALL, "Flash Erase All", 1, ""),
        new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_RECORDER, Neblina.NEBLINA_COMMAND_RECORDER_RECORD, "Flash Record", 1, ""),
        new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_RECORDER, Neblina.NEBLINA_COMMAND_RECORDER_PLAYBACK, "Flash Playback", 1, ""),
        new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_LED, Neblina.NEBLINA_COMMAND_LED_STATE, "Set LED0 level", 3, ""),
        new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_LED, Neblina.NEBLINA_COMMAND_LED_STATE, "Set LED1 level", 3, ""),
        new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_LED, Neblina.NEBLINA_COMMAND_LED_STATE, "Set LED2", 1, ""),
        new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_EEPROM, Neblina.NEBLINA_COMMAND_EEPROM_READ, "EEPROM Read", 2, "Read"),
        new NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_POWER, Neblina.NEBLINA_COMMAND_POWER_CHARGE_CURRENT, "Charge Current in mA", 3, ""),
        new NebCmdItem((byte)0xf, (byte)0, "Motion data stream", 1, ""),
        new NebCmdItem((byte)0xf, (byte)1, "Heading", 1, "")
    };

    /**
     * The dummy content this fragment is presenting.
     */
    public Neblina mNedDev;
    private TextView mTextLabel1;
    private TextView mTextLabel2;
    private ListView mCmdListView;

    public int getCmdIdx(int subsysId, int cmdId) {
        for (int i = 0; i < cmdList.length; i++) {
            if (cmdList[i].mSubSysId == subsysId && cmdList[i].mCmdId == cmdId) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NebDeviceDetailFragment() {
    }

    public void SetItem(Neblina item) {

        mNedDev = item;
        mNedDev.SetDelegate(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            mNedDev = (Neblina) getArguments().getParcelable(ARG_ITEM_ID);

            mNedDev.SetDelegate(this);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.nebdevice_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mNedDev != null) {
            mTextLabel1 = (TextView) rootView.findViewById(R.id.textView1);
            mTextLabel2 = (TextView) rootView.findViewById(R.id.textView2);
            mCmdListView = (ListView) rootView.findViewById(R.id.listView);
            NebListAdapter adapter = new NebListAdapter(this.getContext(),
                    R.layout.nebcmd_item, cmdList);

            mCmdListView.setAdapter(adapter);
            mCmdListView.setTag(this);
            mCmdListView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                    if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                        mNedDev.getSystemStatus();
                        mNedDev.getFirmwareVersion();
                    }
                }

                @Override
                public void onScroll(AbsListView absListView, int i, int i1, int i2) {

                }
            });
          }

        return rootView;
    }

    //
    public void onSwitchButtonChanged(CompoundButton button, boolean isChecked) {
        int idx = (Integer) button.getTag();
        if (idx < 0 && idx > cmdList.length)
            return;

        switch (cmdList[idx].mSubSysId) {
            case NEBLINA_SUBSYSTEM_GENERAL:
                switch (cmdList[idx].mCmdId)
                {
                    case NEBLINA_COMMAND_GENERAL_INTERFACE_STATE:
                        if (isChecked)
                            mNedDev.setDataPort(idx, (byte) 1);
                        else
                            mNedDev.setDataPort(idx, (byte) 0);
                        break;
                    default:
                        break;
                }
                break;

            case NEBLINA_SUBSYSTEM_FUSION:
                switch (cmdList[idx].mCmdId) {
                    case NEBLINA_COMMAND_FUSION_RATE:
                        break;
                    case NEBLINA_COMMAND_FUSION_DOWNSAMPLE:
                        break;
                    case NEBLINA_COMMAND_FUSION_MOTION_STATE_STREAM:
                        mNedDev.streamMotionState(isChecked);
                        break;
                    case NEBLINA_COMMAND_FUSION_QUATERNION_STREAM:
                        mNedDev.streamQuaternion(isChecked);
                        break;
                    case NEBLINA_COMMAND_FUSION_EULER_ANGLE_STREAM:
                        mNedDev.streamEulerAngle(isChecked);
                        break;
                    case NEBLINA_COMMAND_FUSION_EXTERNAL_FORCE_STREAM:
                        mNedDev.streamExternalForce(isChecked);
                        break;
                    case NEBLINA_COMMAND_FUSION_FUSION_TYPE:
                        break;
                    case NEBLINA_COMMAND_FUSION_TRAJECTORY_RECORD:
                        break;
                    case NEBLINA_COMMAND_FUSION_TRAJECTORY_INFO_STREAM:
                        break;
                    case NEBLINA_COMMAND_FUSION_PEDOMETER_STREAM:
                        break;
                    case NEBLINA_COMMAND_FUSION_SITTING_STANDING_STREAM:
                        break;
                    case NEBLINA_COMMAND_FUSION_LOCK_HEADING_REFERENCE:
                        break;
                    case NEBLINA_COMMAND_FUSION_FINGER_GESTURE_STREAM:
                        break;
                    case NEBLINA_COMMAND_FUSION_ROTATION_INFO_STREAM:
                        break;
                    case NEBLINA_COMMAND_FUSION_EXTERNAL_HEADING_CORRECTION:
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
            case NEBLINA_SUBSYSTEM_EEPROM:
                switch (cmdList[idx].mCmdId) {
                    case NEBLINA_COMMAND_EEPROM_READ:
                        mNedDev.eepromRead((short)0);
                        break;
                    case NEBLINA_COMMAND_EEPROM_WRITE:
                        break;
                }
                break;
            case (byte)0xFF:
                if (cmdList[idx].mCmdId==1) //start stream/record
                {
                    mNedDev.streamQuaternion(true);
                    //mNedDev.streamIMU(true);
                    mNedDev.sessionRecord(true);
                }
                else //stop stream/record
                {
                    mNedDev.disableStreaming();
                    mNedDev.sessionRecord(false);
                }
                break;
        }
    }

    // MARK : NeblinaDelegate
    public void didConnectNeblina(Neblina sender) {
        mNedDev.getSystemStatus();
        mNedDev.getLed();
        mNedDev.getFirmwareVersion();
    }
    public void didReceiveRSSI(Neblina sender, int rssi) {

    }

    public void didReceiveFusionData(Neblina sender, int cmdRspId , byte[] data, int datalen, boolean errFlag) {
        switch (cmdRspId) {
            case NEBLINA_COMMAND_FUSION_QUATERNION_STREAM:


                String s = String.format("%d, %d, %d", data[4], data[6], data[8]);
                Log.w("BLUETOOTH DEBUG", s);
                mTextLabel1.setText(s);
                mTextLabel1.getRootView().postInvalidate();
                break;
        }
    }
    public void didReceiveGeneralData(Neblina sender, int cmdRspId, byte[] data, int dataLen, boolean errFlag) {
        NebListAdapter adapter = (NebListAdapter) mCmdListView.getAdapter();

        switch (cmdRspId) {
            case NEBLINA_COMMAND_GENERAL_SYSTEM_STATUS:
                {
                    switch (data[8]) {
                        case 1:    // Playback
                        {
                            int i = getCmdIdx(NEBLINA_SUBSYSTEM_RECORDER, NEBLINA_COMMAND_RECORDER_RECORD);
                            Switch v = (Switch) mCmdListView.findViewWithTag(i);
                            if (v != null) {
                                v.setChecked(false);
                                v.getRootView().postInvalidate();
                            }
                            i = getCmdIdx(NEBLINA_SUBSYSTEM_RECORDER, NEBLINA_COMMAND_RECORDER_PLAYBACK);
                            v = (Switch) mCmdListView.findViewWithTag(i);
                            if (v != null) {
                                v.setChecked(true);
                                v.getRootView().postInvalidate();
                            }
                        }
                        break;
                        case 2:    // Recording
                        {
                            int i = getCmdIdx(NEBLINA_SUBSYSTEM_RECORDER, NEBLINA_COMMAND_RECORDER_PLAYBACK);
                            Switch v = (Switch) mCmdListView.findViewWithTag(i);
                            if (v != null) {
                                v.setChecked(false);
                                v.getRootView().postInvalidate();
                            }
                            i = getCmdIdx(NEBLINA_SUBSYSTEM_RECORDER, NEBLINA_COMMAND_RECORDER_RECORD);
                            v = (Switch) mCmdListView.findViewWithTag(i);
                            if (v != null) {
                                v.setChecked(true);
                                v.getRootView().postInvalidate();
                            }
                        }
                        break;
                        default: {
                            int i = getCmdIdx(NEBLINA_SUBSYSTEM_RECORDER, NEBLINA_COMMAND_RECORDER_RECORD);
                            Switch v = (Switch) mCmdListView.findViewWithTag(i);
                            if (v != null) {
                                v.setChecked(false);
                                v.getRootView().postInvalidate();
                            }
                            i = getCmdIdx(NEBLINA_SUBSYSTEM_RECORDER, NEBLINA_COMMAND_RECORDER_PLAYBACK);
                            v = (Switch) mCmdListView.findViewWithTag(i);
                            if (v != null) {
                                v.setChecked(false);
                                v.getRootView().postInvalidate();
                            }
                        }
                        break;
                    }
                    int i = getCmdIdx(NEBLINA_SUBSYSTEM_FUSION, NEBLINA_COMMAND_FUSION_QUATERNION_STREAM);
                    Switch v = (Switch) mCmdListView.findViewWithTag(i);

                    if (v != null) {
                        v.setChecked(((data[4] & 8) >> 3) != 0);
                        v.getRootView().postInvalidate();
                    }
                    i = getCmdIdx(NEBLINA_SUBSYSTEM_SENSOR, NEBLINA_COMMAND_SENSOR_MAGNETOMETER_STREAM);
                    v = (Switch) mCmdListView.findViewWithTag(i);
                    if (v != null) {
                        v.setChecked(((data[4] & 0x80) >> 7) != 0);
                        v.getRootView().postInvalidate();
                    }
                }
                break;
            case NEBLINA_COMMAND_GENERAL_FIRMWARE_VERSION:
                {
                    String s = String.format("API:%d, FEN:%d.%d.%d, BLE:%d.%d.%d", data[0], data[1], data[2], data[3], data[4], data[5], data[6]);

                    mTextLabel2.setText(s);
                    mTextLabel2.getRootView().postInvalidate();
                }
                break;
            case NEBLINA_COMMAND_GENERAL_INTERFACE_STATUS:
                int i = getCmdIdx(NEBLINA_SUBSYSTEM_GENERAL, NEBLINA_COMMAND_GENERAL_INTERFACE_STATE);
                Switch v = (Switch) mCmdListView.findViewWithTag(i);
                if (v != null) {
                    v.setChecked(data[0] != 0);
                    v.getRootView().postInvalidate();
                }
                v = (Switch) mCmdListView.findViewWithTag(i + 1);
                if (v != null) {
                    v.setChecked(data[1] != 0);
                    v.getRootView().postInvalidate();
                }
                break;
        }

    }
    public void didReceivePmgntData(Neblina sender, int type, byte[] data, int dataLen, boolean errFlag) {

    }
    public void didReceiveRecorderData(Neblina sender, int type, byte[] data, int dataLen, boolean errFlag) {

    }
    public void didReceiveEepromData(Neblina sender, int type, byte[] data, int dataLen, boolean errFlag) {

    }
    public void didReceiveLedData(Neblina sender, int type, byte[] data, int dataLen, boolean errFlag) {
        
    }
    public void didReceiveDebugData(Neblina sender, int cmdRspId, byte[] data, int dataLen, boolean errFlag) {
        switch (cmdRspId) {
            case NEBLINA_COMMAND_DEBUG_DUMP_DATA: {
                String s = String.format("%02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x",
                        data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9],
                        data[10], data[11], data[12], data[13], data[14], data[15]);
                mTextLabel1.setText(s);
                mTextLabel1.getRootView().postInvalidate();
            }
            break;
        }
    }
    public void didReceiveSensorData(Neblina sender, int type, byte[] data, int dataLen, boolean errFlag) {

    }
}
