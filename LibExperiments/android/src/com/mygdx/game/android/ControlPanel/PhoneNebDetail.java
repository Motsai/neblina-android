package com.mygdx.game.android.ControlPanel;

import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.mygdx.game.android.R;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

import static com.mygdx.game.android.ControlPanel.BLEDeviceScanActivity.isStreaming;

public class PhoneNebDetail extends FragmentActivity {

    //Butterknife get views

    @InjectView(R.id.cloudStreamToggle)
    Button toggleButton;

    @InjectView(R.id.dataVisualisation)
    Button dataButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ButterKnife.inject(this);
        setContentView(R.layout.activity_phone_neb_detail);
    }






    /*************************************** Buttons *************************************/

    @OnClick(R.id.cloudStreamToggle)void streamToCloud(View view) {

        if(view.isActivated()){
            isStreaming = false;
            view.setActivated(false);
        }else {
            isStreaming = true;
            view.setActivated(true);
        }
    }

    //This starts the data visualization tools
    @OnClick(R.id.dataVisualisation) void dataVisualization(){
        Log.w("DEBUG", "Starting Visualization");
        Intent intent = new Intent(this, DynamicData.class);
        startActivity(intent);
    }

}
