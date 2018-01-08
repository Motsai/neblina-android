package com.motsai.nebctrlpanel;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import com.motsai.neblina.NebCmdItem;

/**
 * Created by hoanmotsai on 2017-05-17.
 */

public class CmdListAdapter extends ArrayAdapter<NebCmdItem> {
    public CmdListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public CmdListAdapter(Context context, int resource, NebCmdItem[] items) {
        super(context, resource, items);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(R.layout.nebcmd_item, null);
        }

        NebCmdItem p = getItem(position);

        if (p != null) {
            Log.d("***** CmdListAdapter", "getView *****" + position);
            TextView label = (TextView) v.findViewById(R.id.textView);
            label.setText(p.mName);

            Switch c = (Switch)v.findViewById(R.id.switch1);
            c.setVisibility(View.INVISIBLE);
            c.setTag(-1);
            Button b = (Button) v.findViewById(R.id.button);
            b.setVisibility(View.INVISIBLE);
            b.setTag(-1);
            TextView t = (TextView) v.findViewById(R.id.textField);
            t.setVisibility(View.INVISIBLE);
            t.setTag(-1);
            TextView t2 = (TextView) v.findViewById(R.id.textField1);
            t2.setVisibility(View.INVISIBLE);
            t2.setTag(-1);
//            b.setId(300 + i);

            switch (p.mActuator) {
                case NebCmdItem.ACTUATOR_TYPE_TEXT_FIELD_SWITCH:
                    c.setVisibility(View.VISIBLE);
                    c.setTag(position);

                case NebCmdItem.ACTUATOR_TYPE_SWITCH: // Switch
                    c.setVisibility(View.VISIBLE);
                    c.setTag(position);
                    c.setOnCheckedChangeListener(new Switch.OnCheckedChangeListener() {
                        @Override
                        public void onCheckedChanged(CompoundButton vi, boolean isChecked)
                        {
                            Log.d("***** CmdListAdapter", "setOnCheckedChangeListener *****" + isChecked);
                            // int idx = (int)vi.getTag();

                            ViewParent vp = vi.getParent().getParent().getParent();
                            if (vp instanceof ListView) {
                                ListView lv = (ListView) vp;
                                MainActivity act = (MainActivity)lv.getTag();
                                if (act != null) {
                                    act.onSwitchButtonChanged(vi, isChecked);
                                }
                            }
                        }
                    });


                    break;
                case NebCmdItem.ACTUATOR_TYPE_TEXT_FIELD_BUTTON:
                    t2.setVisibility(View.VISIBLE);
                    t2.setTag(position);

                case NebCmdItem.ACTUATOR_TYPE_BUTTON : // Button
                    b.setVisibility(View.VISIBLE);
                    b.setTag(position);
                    b.setText(p.mText);
                    b.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View vi)
                        {
                            // int idx = (int)vi.getTag();
                            ViewParent vp = vi.getParent().getParent().getParent();
                            if (vp instanceof ListView) {
                                ListView lv = (ListView) vp;
                                MainActivity act = (MainActivity)lv.getTag();
                                if (act != null) {
                                    act.onButtonClick(vi);
                                }
                            }
                        }
                    });
                    break;
                case NebCmdItem.ACTUATOR_TYPE_TEXT_FIELD: // Text field
                    t.setVisibility(View.VISIBLE);
                    t.setTag(position);

                    break;

            }
        }

        return v;
    }

}
