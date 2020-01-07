package com.motsai.tutorial1;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Switch;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.motsai.neblina.Neblina;
import com.motsai.neblina.NeblinaDelegate;

import static com.motsai.tutorial1.MainActivity.DeviceListAdapter.*;

public class MainActivity extends AppCompatActivity implements NeblinaDelegate {

    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private BluetoothLeScanner mLEScanner;
    private DeviceListAdapter mAdapter;
    private Neblina mDev;
    private TextView mTextView;
    private Switch mQuatSwitch;
    private CompoundButton.OnCheckedChangeListener mOnCheck;/* = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (mDev == null)
                return;

            mDev.streamQuaternion(isChecked);
        }
    };*/

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            //Log.i("callbackType", String.valueOf(callbackType));
            //Log.i("result", result.toString());
            BluetoothDevice device = result.getDevice();
            ScanRecord scanRecord = result.getScanRecord();
            byte[] scanData = scanRecord.getBytes();
            String name = device.getName();//scanRecord.getDeviceName();
            long deviceID = 0;
            byte[] manuf = scanRecord.getManufacturerSpecificData(0x0274);


            //if (name == null)
            //    name = device.getName();
            if (manuf == null)
                return;

            if (name == null || manuf == null || manuf.length < 8)
                return;


            Log.i("Name", name);
            ByteBuffer x = ByteBuffer.wrap(manuf);
            x.order(ByteOrder.LITTLE_ENDIAN);
            deviceID = x.getLong();


            ListView listView = (ListView) findViewById(R.id.founddevice_list);
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

    //@RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        mTextView = (TextView) findViewById(R.id.text_view);

        mBluetoothAdapter = bluetoothManager.getAdapter();
        mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
        mAdapter  = new DeviceListAdapter(this, R.layout.nebdevice_list_content);//android.R.layout.simple_list_item_1);
        ListView listView = (ListView) findViewById(R.id.founddevice_list);
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


        mLEScanner.startScan(mScanCallback);

        mQuatSwitch = (Switch) findViewById(R.id.switch1);
        //mQuatSwitch.setChecked(true);

        mOnCheck = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mDev == null)
                    return;

                mDev.streamQuaternion(isChecked);
            }
        };
        mQuatSwitch.setOnCheckedChangeListener(mOnCheck);
    }

    public class DevListAdapter extends BaseAdapter {
        // private final Context mContext;
        private final Map<String, Neblina> mNebDevices = new HashMap<String, Neblina>();

        @Override
        public int getCount() {
            return mNebDevices.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return null;
        }
    }

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

    public void didConnectNeblina(Neblina sender) {
        Log.w("BLUETOOTH DEBUG", "Connected " + sender.toString());
        // Enable Quaternion Stream

        //mQuatSwitch.setEnabled(true);

        this.runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                mQuatSwitch.setChecked(true);
            }
        });
         sender.streamQuaternion(true);

    }
    public void didReceiveResponsePacket(Neblina sender, int subsystem, int cmdRspId, byte[] data, int dataLen) {

    }
    public void didReceiveRSSI(Neblina sender, int rssi) {

    }
    public void didReceiveGeneralData(Neblina sender, int respType, int cmdRspId, byte[] data, int dataLen, boolean errFlag) {

    }
    public void didReceiveFusionData(Neblina sender, int respType, int cmdRspId, byte[] data, int dataLen, boolean errFlag) {
        switch (cmdRspId) {
            case Neblina.NEBLINA_COMMAND_FUSION_QUATERNION_STREAM:
                int timeStamp = ((int)data[0] & 0xFF) | (((int)data[1] & 0xFF) << 8) | (((int)data[2] & 0xFF) << 16) | (((int)data[3] & 0xFF) << 24);
                double q1 = ((double)(((int)data[4] & 0xFF) | ((int)data[5] << 8))) / 32768.0;
                double q2 = ((double)(((int)data[6] & 0xFF) | ((int)data[7] << 8))) / 32768.0;
                double q3 = ((double)(((int)data[8] & 0xFF) | ((int)data[9] << 8))) / 32768.0;
                double q4 = ((double)(((int)data[10] & 0xFF) | ((int)data[11] << 8))) / 32768.0;

                String s = String.format("T : %d - (%.4f, %.4f, %.4f, %.4f)", timeStamp, q1, q2, q3, q4);
                Log.w("BLUETOOTH DEBUG", s);
                TextView txtView = (TextView) findViewById(R.id.text_view);
                txtView.setText(s);
                //mTextView.getRootView().invalidate();
                txtView.getRootView().postInvalidate();

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

    }
}
