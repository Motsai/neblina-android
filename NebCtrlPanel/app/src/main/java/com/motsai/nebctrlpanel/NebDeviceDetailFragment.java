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
        new NebCmdItem(Neblina.NEB_CTRL_SUBSYS_DEBUG, Neblina.DEBUG_CMD_SET_DATAPORT, "BLE Data Port", 1, ""),
        new NebCmdItem(Neblina.NEB_CTRL_SUBSYS_DEBUG, Neblina.DEBUG_CMD_SET_DATAPORT, "UART Data Port", 1, ""),
        new NebCmdItem(Neblina.NEB_CTRL_SUBSYS_MOTION_ENG, Neblina.MOTION_CMD_SET_FUSION_TYPE, "Fusion 9 axis", 1, ""),
        new NebCmdItem(Neblina.NEB_CTRL_SUBSYS_MOTION_ENG, Neblina.MOTION_CMD_QUATERNION, "Quaternion Stream", 1, ""),
        new NebCmdItem(Neblina.NEB_CTRL_SUBSYS_MOTION_ENG, Neblina.MOTION_CMD_MAG_DATA, "Mag Stream", 1, ""),
        new NebCmdItem(Neblina.NEB_CTRL_SUBSYS_MOTION_ENG, Neblina.MOTION_CMD_LOCK_HEADING_REF, "Lock Heading Ref.", 1, ""),
        new NebCmdItem(Neblina.NEB_CTRL_SUBSYS_STORAGE, Neblina.STORAGE_CMD_ERASE, "Flash Erase All", 1, ""),
        new NebCmdItem(Neblina.NEB_CTRL_SUBSYS_STORAGE, Neblina.STORAGE_CMD_RECORD, "Flash Record", 1, ""),
        new NebCmdItem(Neblina.NEB_CTRL_SUBSYS_STORAGE, Neblina.STORAGE_CMD_PLAY, "Flash Playback", 1, ""),
        new NebCmdItem(Neblina.NEB_CTRL_SUBSYS_LED, Neblina.LED_CMD_SET_VALUE, "Set LED0 level", 3, ""),
        new NebCmdItem(Neblina.NEB_CTRL_SUBSYS_LED, Neblina.LED_CMD_SET_VALUE, "Set LED1 level", 3, ""),
        new NebCmdItem(Neblina.NEB_CTRL_SUBSYS_LED, Neblina.LED_CMD_SET_VALUE, "Set LED2", 1, ""),
        new NebCmdItem(Neblina.NEB_CTRL_SUBSYS_EEPROM, Neblina.EEPROM_CMD_READ, "EEPROM Read", 2, "Read"),
        new NebCmdItem(Neblina.NEB_CTRL_SUBSYS_POWERMGMT, Neblina.POWERMGMT_CMD_SET_CHARGE_CURRENT, "Charge Current in mA", 3, ""),
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
                        mNedDev.getMotionStatus();
                        mNedDev.getDataPortState();
                        mNedDev.getLed();
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
        int idx = (int) button.getTag();
        if (idx < 0 && idx > cmdList.length)
            return;

        switch (cmdList[idx].mSubSysId) {
            case NEB_CTRL_SUBSYS_DEBUG:
                switch (cmdList[idx].mCmdId)
                {
                    case DEBUG_CMD_SET_INTERFACE:
                        //mNedDev.setInterface(isChecked == true ? 1);
                        break;
                    case DEBUG_CMD_DUMP_DATA:
                        break;
                    case DEBUG_CMD_SET_DATAPORT:
                        if (isChecked)
                            mNedDev.setDataPort(idx, (byte) 1);
                        else
                            mNedDev.setDataPort(idx, (byte) 0);
                        break;
                    default:
                        break;
                }
                break;

            case NEB_CTRL_SUBSYS_MOTION_ENG:
                switch (cmdList[idx].mCmdId) {
                    case MOTION_CMD_QUATERNION:
                        mNedDev.streamQuaternion(isChecked);
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
                        mNedDev.eepromRead(0);
                        break;
                    case EEPROM_CMD_WRITE:
                        break;
                }
                break;
        }
    }

    // MARK : NeblinaDelegate
    public void didConnectNeblina() {
        mNedDev.getMotionStatus();
        mNedDev.getDataPortState();
        mNedDev.getLed();
        mNedDev.getFirmwareVersion();
    }
    public void didReceiveRSSI(int rssi) {

    }
    public void didReceiveFusionData(int type , byte[] data, boolean errFlag) {
        switch (type) {
            case MOTION_CMD_QUATERNION:


                String s = String.format("%d, %d, %d", data[4], data[6], data[8]);
                Log.w("BLUETOOTH DEBUG", s);
                mTextLabel1.setText(s);
                mTextLabel1.getRootView().postInvalidate();
                break;
        }
    }
    public void didReceiveDebugData(int type, byte[] data, int dataLen, boolean errFlag) {
        NebListAdapter adapter = (NebListAdapter) mCmdListView.getAdapter();

        switch (type) {
            case DEBUG_CMD_MOTENGINE_RECORDER_STATUS:
                {
                    switch (data[8]) {
                        case 1:    // Playback
                        {
                            int i = getCmdIdx(NEB_CTRL_SUBSYS_STORAGE, STORAGE_CMD_RECORD);
                            Switch v = (Switch) mCmdListView.findViewWithTag(i);
                            if (v != null) {
                                v.setChecked(false);
                                v.getRootView().postInvalidate();
                            }
                            i = getCmdIdx(NEB_CTRL_SUBSYS_STORAGE, STORAGE_CMD_PLAY);
                            v = (Switch) mCmdListView.findViewWithTag(i);
                            if (v != null) {
                                v.setChecked(true);
                                v.getRootView().postInvalidate();
                            }
                        }
                        break;
                        case 2:    // Recording
                        {
                            int i = getCmdIdx(NEB_CTRL_SUBSYS_STORAGE, STORAGE_CMD_PLAY);
                            Switch v = (Switch) mCmdListView.findViewWithTag(i);
                            if (v != null) {
                                v.setChecked(false);
                                v.getRootView().postInvalidate();
                            }
                            i = getCmdIdx(NEB_CTRL_SUBSYS_STORAGE, STORAGE_CMD_RECORD);
                            v = (Switch) mCmdListView.findViewWithTag(i);
                            if (v != null) {
                                v.setChecked(true);
                                v.getRootView().postInvalidate();
                            }
                        }
                        break;
                        default: {
                            int i = getCmdIdx(NEB_CTRL_SUBSYS_STORAGE, STORAGE_CMD_RECORD);
                            Switch v = (Switch) mCmdListView.findViewWithTag(i);
                            if (v != null) {
                                v.setChecked(false);
                                v.getRootView().postInvalidate();
                            }
                            i = getCmdIdx(NEB_CTRL_SUBSYS_STORAGE, STORAGE_CMD_PLAY);
                            v = (Switch) mCmdListView.findViewWithTag(i);
                            if (v != null) {
                                v.setChecked(false);
                                v.getRootView().postInvalidate();
                            }
                        }
                        break;
                    }
                    int i = getCmdIdx(NEB_CTRL_SUBSYS_MOTION_ENG, MOTION_CMD_QUATERNION);
                    Switch v = (Switch) mCmdListView.findViewWithTag(i);

                    if (v != null) {
                        v.setChecked(((data[4] & 8) >> 3) != 0);
                        v.getRootView().postInvalidate();
                    }
                    i = getCmdIdx(NEB_CTRL_SUBSYS_MOTION_ENG, MOTION_CMD_MAG_DATA);
                    v = (Switch) mCmdListView.findViewWithTag(i);
                    if (v != null) {
                        v.setChecked(((data[4] & 0x80) >> 7) != 0);
                        v.getRootView().postInvalidate();
                    }
                }
                break;
            case DEBUG_CMD_GET_FW_VERSION:
                {
                    String s = String.format("API:%d, FEN:%d.%d.%d, BLE:%d.%d.%d", data[0], data[1], data[2], data[3], data[4], data[5], data[6]);

                    mTextLabel2.setText(s);
                    mTextLabel2.getRootView().postInvalidate();
                }
                break;
            case DEBUG_CMD_DUMP_DATA:
                {
                    String s = String.format("%02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x %02x",
                            data[0], data[1], data[2], data[3], data[4], data[5], data[6], data[7], data[8], data[9],
                            data[10], data[11], data[12], data[13], data[14], data[15]);
                    mTextLabel1.setText(s);
                    mTextLabel1.getRootView().postInvalidate();
                }
                break;
            case DEBUG_CMD_GET_DATAPORT:
                int i = getCmdIdx(NEB_CTRL_SUBSYS_DEBUG, DEBUG_CMD_SET_DATAPORT);
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
    public void didReceivePmgntData(int type, byte[] data, int dataLen, boolean errFlag) {

    }
    public void didReceiveStorageData(int type, byte[] data, int dataLen, boolean errFlag) {

    }
    public void didReceiveEepromData(int type, byte[] data, int dataLen, boolean errFlag) {

    }
    public void didReceiveLedData(int type, byte[] data, int dataLen, boolean errFlag) {
        
    }
}
