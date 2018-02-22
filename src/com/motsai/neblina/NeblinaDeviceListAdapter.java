package com.motsai.neblina;

import android.content.Context;
import android.util.Log;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.motsai.neblina.NeblinaDeviceList;
import java.util.HashMap;
import java.util.Map;

public abstract class NeblinaDeviceListAdapter extends BaseAdapter {

    private final NeblinaDeviceList mDevices = new NeblinaDeviceList();

    public NeblinaDeviceListAdapter() {
    }

    public void addItem(NeblinaDevice dev) {
        if (mDevices.containsKey(dev.toString()) == false) {
            mDevices.put(dev.toString(), dev);
            Log.w("BLUETOOTH DEBUG", "Item added " + dev.toString());
        }
    }

    @Override
    public int getCount() {
        return mDevices.size();
    }

    @Override
    public Object getItem(int position) {
        return mDevices.values().toArray()[position];
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }
}