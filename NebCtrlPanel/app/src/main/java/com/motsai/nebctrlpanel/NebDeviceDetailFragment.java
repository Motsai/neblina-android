package com.motsai.nebctrlpanel;

import android.app.Activity;
import android.content.ClipData;
import android.support.design.widget.CollapsingToolbarLayout;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.motsai.neblina.Neblina;
import com.motsai.neblina.NeblinaDelegate;

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

    /**
     * The dummy content this fragment is presenting.
     */
    private Neblina mItem;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public NebDeviceDetailFragment() {
    }

    public void SetItem(Neblina item) {

        mItem = item;
        mItem.SetDelegate(this);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments().containsKey(ARG_ITEM_ID)) {
            // Load the dummy content specified by the fragment
            // arguments. In a real-world scenario, use a Loader
            // to load content from a content provider.
           // mItem = DummyContent.ITEM_MAP.get(getArguments().getString(ARG_ITEM_ID));
            mItem = (Neblina) getArguments().getParcelable(ARG_ITEM_ID);

            mItem.SetDelegate(this);
           // mItem.Connect(null);
            Activity activity = this.getActivity();
            CollapsingToolbarLayout appBarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
           // if (appBarLayout != null) {
             //   appBarLayout.setTitle(mItem.content);
           // }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.nebdevice_detail, container, false);

        // Show the dummy content as text in a TextView.
        if (mItem != null) {
           // ((TextView) rootView.findViewById(R.id.nebdevice_detail)).setText(mItem.details);
        }

        return rootView;
    }

    // MARK : NeblinaDelegate
    public void didConnectNeblina() {
        mItem.streamQuaternion(true);
    }
    public void didReceiveRSSI(int rssi) {

    }
    public void didReceiveFusionData(int type , byte[] data, boolean errFlag) {
        switch (type) {
            case Neblina.MOTION_CMD_QUATERNION:
                Log.w("BLUETOOTH DEBUG", "Item added " + data.toString());
                break;
        }
    }
    public void didReceiveDebugData(int type, byte[] data, boolean errFlag) {

    }
    public void didReceivePmgntData(int type, byte[] data, boolean errFlag) {

    }
    public void didReceiveStorageData(int type, byte[] data, boolean errFlag) {

    }
    public void didReceiveEepromData(int type, byte[] data, boolean errFlag) {

    }
    public void didReceiveLedData(int type, byte[] data, boolean errFlag) {
        
    }
}
