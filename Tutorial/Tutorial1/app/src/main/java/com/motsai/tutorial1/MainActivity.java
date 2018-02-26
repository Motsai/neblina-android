package com.motsai.tutorial1;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Switch;

import com.motsai.neblina.Neblina;
import com.motsai.neblina.NeblinaAPI;
import com.motsai.neblina.NeblinaCallback;
import com.motsai.neblina.NeblinaDevice;
import com.motsai.neblina.NeblinaDeviceListAdapter;
import com.motsai.neblina.NeblinaUtilities;

public class MainActivity extends AppCompatActivity {

    private NeblinaAPI mNeblinaAPI;
    private BluetoothAdapter mBluetoothAdapter;
    private Handler mHandler;
    private BluetoothLeScanner mLEScanner;
    private DeviceListAdapter mDeviceListAdapter;
    private NeblinaDevice mDev;
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

    private class DeviceListAdapter extends NeblinaDeviceListAdapter {

        private final Context mContext;

        public DeviceListAdapter(Context context) {
            mContext = context;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                convertView = inflater.inflate(R.layout.nebdevice_list_content, parent, false);
            }

            TextView textView = (TextView) convertView.findViewById(R.id.id);
            if (getCount() > position) {
                textView.setText(getItem(position).toString());
            }

            return convertView;
        }
    }

    //@RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, 1);

        mNeblinaAPI = new NeblinaAPI((BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE));
        mNeblinaAPI.setCallback(mNeblinaCallback);
        mNeblinaAPI.startDiscovery();

        mTextView = (TextView) findViewById(R.id.text_view);

        mDeviceListAdapter = new DeviceListAdapter(this);//android.R.layout.simple_list_item_1);
        ListView listView = (ListView) findViewById(R.id.founddevice_list);
        listView.setAdapter(mDeviceListAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NeblinaDeviceListAdapter adapter = (NeblinaDeviceListAdapter) parent.getAdapter();
                mNeblinaAPI.stopDiscovery();
                if (mDev != null) {
                    mDev.disconnect();
                }

                mDev = (NeblinaDevice)adapter.getItem(position);
                //mDev.SetDelegate(MainActivity.this);
                mDev.connect(getBaseContext(), true);

            }
        });

        mQuatSwitch = (Switch) findViewById(R.id.switch1);

        mOnCheck = new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (mDev == null)
                    return;

                mNeblinaAPI.streamQuaternion(isChecked);
            }
        };
        mQuatSwitch.setOnCheckedChangeListener(mOnCheck);
    }

    private NeblinaCallback mNeblinaCallback = new NeblinaCallback() {
        @Override
        public void deviceDiscovered(NeblinaDevice sender) {
            mDeviceListAdapter.addItem(sender);
            mDeviceListAdapter.notifyDataSetChanged();
        }

        public void deviceConnected(NeblinaDevice sender) {
            Log.w("BLUETOOTH DEBUG", "Connected " + sender.toString());

            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    mQuatSwitch.setChecked(true);
                }
            });
            mNeblinaAPI.addDeviceToSendingPool(sender);
            mNeblinaAPI.streamQuaternion(true);
        }

        public void deviceDisconnected(NeblinaDevice sender) {
            Log.w("BLUETOOTH", "Disconnected: " + sender.toString());
            mNeblinaAPI.removeDeviceFromSendingPool(sender);
        }

        public void didReceiveFusionData(NeblinaDevice sender, int respType, int cmdRspId, byte[] data, int dataLen, boolean errFlag) {
            switch (cmdRspId) {
                case Neblina.NEBLINA_COMMAND_FUSION_QUATERNION_STREAM:
                    //int timeStamp = (int)data[0] | ((int)data[1] << 8) | ((int)data[2] << 16) | ((int)data[3] << 24);
                    long timeStamp = NeblinaUtilities.convertByteToUnsignedInt(data[0], data[1], data[2], data[3]);
                    double q1 = NeblinaUtilities.convertByteToUnsignedShort(data[4], data[5]) / 32768.0;
                    double q2 = NeblinaUtilities.convertByteToUnsignedShort(data[6], data[7]) / 32768.0;
                    double q3 = NeblinaUtilities.convertByteToUnsignedShort(data[8], data[9]) / 32768.0;
                    double q4 = NeblinaUtilities.convertByteToUnsignedShort(data[10], data[11]) / 32768.0;

                    final String s = String.format("T : %d - (%.3f, %.3f, %.3f, %.3f)", timeStamp, q1, q2, q3, q4);
                    Log.w("BLUETOOTH DEBUG", s);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView txtView = (TextView) findViewById(R.id.text_view);
                            txtView.setText(s);
                            //mTextView.getRootView().invalidate();
                            txtView.getRootView().postInvalidate();
                        }
                    });

                    break;
            }
        }
    };
}
