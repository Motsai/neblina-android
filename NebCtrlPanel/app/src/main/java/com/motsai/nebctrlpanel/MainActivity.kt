package com.motsai.nebctrlpanel

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.renderscript.*
import com.motsai.neblina.*
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.HashMap

import fr.arnaudguyon.smartgl.math.Vector3D
import fr.arnaudguyon.smartgl.opengl.LightParallel
//import fr.arnaudguyon.smartgl.opengl.Object3D;
import fr.arnaudguyon.smartgl.opengl.RenderObject
//import fr.arnaudguyon.smartgl.opengl.RenderPassObject3D;
import fr.arnaudguyon.smartgl.opengl.RenderPassSprite
import fr.arnaudguyon.smartgl.opengl.SmartColor
import fr.arnaudguyon.smartgl.opengl.SmartGLRenderer
import fr.arnaudguyon.smartgl.opengl.SmartGLView
import fr.arnaudguyon.smartgl.opengl.SmartGLViewController
import fr.arnaudguyon.smartgl.opengl.Sprite
import fr.arnaudguyon.smartgl.opengl.Texture
//import fr.arnaudguyon.smartgl.tools.WavefrontModel;
import fr.arnaudguyon.smartgl.touch.TouchHelperEvent
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.experimental.or

class MainActivity : AppCompatActivity(), NeblinaDelegate, SmartGLViewController {
    private var mBluetoothAdapter: BluetoothAdapter? = null
    private val mHandler: Handler? = null
    private var mLEScanner: BluetoothLeScanner? = null
    private var mAdapter: DeviceListAdapter? = null
    private var mDev: Neblina? = null
    private var mCmdListView: ListView? = null
    private var mTextLine1: TextView? = null
    private var mTextLine2: TextView? = null
    private var mTextLine3: TextView? = null
    private var mQuatRate: Int = 0
    private var mQuatPeriod: Int = 0
    private var mQuatTimeStamp: Long = 0
    private var mQuatDropCnt = 0
    private var mQuatCnt = 0
    private var mQuatBdCnt = 0
    private var mFlashEraseProgress = false
    private var mRenderer : SmartGLRenderer? = null
    private var modelview = 0
    private var mShip: Object3D? = null
    private var mCube: Object3D?= null
    private var mCur3DObj : Object3D? = null
    private var mSpaceFrigateTexture: Texture? = null
    private var mRenderPassShip: RenderPassObject3D? = null
    private var mRenderPassCube: RenderPassObject3D? = null

    val cmdList = arrayOf(
            NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_GENERAL, Neblina.NEBLINA_COMMAND_GENERAL_INTERFACE_STATE,
                Neblina.NEBLINA_INTERFACE_STATUS_BLE.toInt(), "BLE Data Port", NebCmdItem.ACTUATOR_TYPE_SWITCH.toInt(), ""),
            NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_GENERAL, Neblina.NEBLINA_COMMAND_GENERAL_INTERFACE_STATE,
                Neblina.NEBLINA_INTERFACE_STATUS_UART.toInt(), "UART Data Port", NebCmdItem.ACTUATOR_TYPE_SWITCH.toInt(), ""),
            NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_GENERAL, Neblina.NEBLINA_COMMAND_GENERAL_DEVICE_NAME_SET,
                0, "Change Device Name", NebCmdItem.ACTUATOR_TYPE_TEXT_FIELD_BUTTON.toInt(), "Change"),
            NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_CALIBRATE_FORWARD_POSITION,
                0, "Calibrate Forward Pos", NebCmdItem.ACTUATOR_TYPE_BUTTON.toInt(), "Calib Fwrd"),
            NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_CALIBRATE_DOWN_POSITION,
                0, "Calibrate Down Pos", NebCmdItem.ACTUATOR_TYPE_BUTTON.toInt(), "Calib Dwn"),
            NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_GENERAL, Neblina.NEBLINA_COMMAND_GENERAL_RESET_TIMESTAMP,
                0, "Reset timestamp", NebCmdItem.ACTUATOR_TYPE_BUTTON.toInt(), "Reset"),
            NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_FUSION_TYPE,
                0, "Fusion 9 axis", NebCmdItem.ACTUATOR_TYPE_SWITCH.toInt(), ""),
            NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_QUATERNION_STREAM,
                Neblina.NEBLINA_FUSION_STATUS_QUATERNION.toInt(), "Quaternion Stream", NebCmdItem.ACTUATOR_TYPE_SWITCH.toInt(), ""),
            NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_PEDOMETER_STREAM,
                Neblina.NEBLINA_FUSION_STATUS_PEDOMETER.toInt(), "Pedometer Stream", NebCmdItem.ACTUATOR_TYPE_SWITCH.toInt(), ""),
            NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_ROTATION_INFO_STREAM,
                Neblina.NEBLINA_FUSION_STATUS_ROTATION_INFO.toInt(), "Rotation info Stream", NebCmdItem.ACTUATOR_TYPE_TEXT_FIELD_SWITCH.toInt(), ""),
            NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_SENSOR, Neblina.NEBLINA_COMMAND_SENSOR_ACCELEROMETER_STREAM,
                Neblina.NEBLINA_SENSOR_STATUS_ACCELEROMETER.toInt(), "Accelerometer Sensor Stream", NebCmdItem.ACTUATOR_TYPE_SWITCH.toInt(), ""),
            NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_SENSOR, Neblina.NEBLINA_COMMAND_SENSOR_GYROSCOPE_STREAM,
                Neblina.NEBLINA_SENSOR_STATUS_GYROSCOPE.toInt(), "Gyro Sensor Stream", NebCmdItem.ACTUATOR_TYPE_SWITCH.toInt(), ""),
            NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_SENSOR, Neblina.NEBLINA_COMMAND_SENSOR_MAGNETOMETER_STREAM,
                Neblina.NEBLINA_SENSOR_STATUS_MAGNETOMETER.toInt(), "Mag Sensor Stream", NebCmdItem.ACTUATOR_TYPE_SWITCH.toInt(), ""),
            NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_SENSOR, Neblina.NEBLINA_COMMAND_SENSOR_ACCELEROMETER_GYROSCOPE_STREAM,
                Neblina.NEBLINA_SENSOR_STATUS_ACCELEROMETER_GYROSCOPE.toInt(), "Accel & Gyro Stream", NebCmdItem.ACTUATOR_TYPE_SWITCH.toInt(), ""),
            NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_SENSOR, Neblina.NEBLINA_COMMAND_SENSOR_HUMIDITY_STREAM,
                Neblina.NEBLINA_SENSOR_STATUS_HUMIDITY.toInt(), "Humidity Sensor Stream", NebCmdItem.ACTUATOR_TYPE_SWITCH.toInt(), ""),
            NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_FUSION, Neblina.NEBLINA_COMMAND_FUSION_LOCK_HEADING_REFERENCE,
                0, "Lock Heading Ref.", NebCmdItem.ACTUATOR_TYPE_BUTTON.toInt(), "Lock"),
            NebCmdItem(0xf.toByte(), 2.toByte(), 0, "Luggage data logging", NebCmdItem.ACTUATOR_TYPE_SWITCH.toInt(), ""),
            NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_RECORDER, Neblina.NEBLINA_COMMAND_RECORDER_RECORD,
                Neblina.NEBLINA_RECORDER_STATUS_RECORD.toInt(), "Stream/Record", NebCmdItem.ACTUATOR_TYPE_BUTTON.toInt(), "Start"),
            NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_RECORDER, Neblina.NEBLINA_COMMAND_RECORDER_RECORD,
                0, "Stream/Record", NebCmdItem.ACTUATOR_TYPE_BUTTON.toInt(), "Stop"),
            NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_RECORDER, Neblina.NEBLINA_COMMAND_RECORDER_RECORD,
                0, "Flash Record", NebCmdItem.ACTUATOR_TYPE_SWITCH.toInt(), ""),
            NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_RECORDER, Neblina.NEBLINA_COMMAND_RECORDER_PLAYBACK,
                0, "Flash Playback", NebCmdItem.ACTUATOR_TYPE_SWITCH.toInt(), ""),
            NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_LED, Neblina.NEBLINA_COMMAND_LED_STATE,
                0, "Set LED0 level", NebCmdItem.ACTUATOR_TYPE_TEXT_FIELD.toInt(), ""),
            NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_LED, Neblina.NEBLINA_COMMAND_LED_STATE,
                0, "Set LED1 level", NebCmdItem.ACTUATOR_TYPE_TEXT_FIELD.toInt(), ""),
            NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_LED, Neblina.NEBLINA_COMMAND_LED_STATE,
                0, "Set LED2", NebCmdItem.ACTUATOR_TYPE_SWITCH.toInt(), ""),
            NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_EEPROM, Neblina.NEBLINA_COMMAND_EEPROM_READ,
                0, "EEPROM Read", NebCmdItem.ACTUATOR_TYPE_BUTTON.toInt(), "Read"),
            NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_POWER, Neblina.NEBLINA_COMMAND_POWER_CHARGE_CURRENT,
                0, "Charge Current in mA", NebCmdItem.ACTUATOR_TYPE_TEXT_FIELD.toInt(), ""),
            NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_RECORDER, Neblina.NEBLINA_COMMAND_RECORDER_ERASE_ALL,
                0, "Flash Erase All", NebCmdItem.ACTUATOR_TYPE_BUTTON.toInt(), "Erase"),
            NebCmdItem(Neblina.NEBLINA_SUBSYSTEM_GENERAL, Neblina.NEBLINA_COMMAND_GENERAL_FIRMWARE_UPDATE,
                0, "Firmware Update", NebCmdItem.ACTUATOR_TYPE_BUTTON.toInt(), "Enter DFU"),
            NebCmdItem(0xf.toByte(), 0.toByte(), 0, "Motion data stream", NebCmdItem.ACTUATOR_TYPE_SWITCH.toInt(), ""),
            NebCmdItem(0xf.toByte(), 1.toByte(), 0, "Heading", NebCmdItem.ACTUATOR_TYPE_SWITCH.toInt(), ""))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION), 1)

        val glview = findViewById(R.id.surfaceView) as SmartGLView
        glview.setDefaultRenderer(this)
        glview.setController(this)

        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

        mBluetoothAdapter = bluetoothManager.adapter
        mLEScanner = mBluetoothAdapter!!.getBluetoothLeScanner()
        mAdapter = DeviceListAdapter(this, R.layout.nebdevice_list_content)//android.R.layout.simple_list_item_1);
        mTextLine1 = findViewById<View>(R.id.textView1) as TextView
        mTextLine2 = findViewById<View>(R.id.textView2) as TextView
        mTextLine3 = findViewById<View>(R.id.textView3) as TextView

        val sw3dv = findViewById<Switch>(R.id.switch_3dview) as Switch
        sw3dv.isChecked = false
        sw3dv.setOnCheckedChangeListener(object : CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(vi: CompoundButton, isChecked: Boolean) {

                if (isChecked) {
                    modelview = 1
                    mCur3DObj = mCube
                    mRenderer?.addRenderPass(mRenderPassCube)
                    mRenderer?.removeRenderPass(mRenderPassShip)
                }
                else {
                    modelview = 0
                    mCur3DObj = mShip
                    mRenderer?.addRenderPass(mRenderPassShip)
                    mRenderer?.removeRenderPass(mRenderPassCube)
                }
            }
        })

        val listView = findViewById<View>(R.id.founddevice_listView) as ListView
        listView.adapter = mAdapter
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val adapter = parent.adapter as DeviceListAdapter
            mLEScanner!!.stopScan(mScanCallback)
            if (mDev != null) {
                mDev!!.Disconnect()
            }

            mDev = adapter.getItem(position) as Neblina
            mDev!!.SetDelegate(this@MainActivity)
            mDev!!.Connect(baseContext)
        }


        mCmdListView = findViewById<View>(R.id.cmd_listView) as ListView

        val adapter = CmdListAdapter(this,
                R.layout.nebcmd_item, cmdList)

        mCmdListView!!.setAdapter(adapter)
        mCmdListView!!.setTag(this)
        mCmdListView!!.setOnScrollListener(object : AbsListView.OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    if (mDev != null) {
                        mDev!!.getSystemStatus()
                    }
                }
            }

            override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {

            }
        }
        )

        mLEScanner!!.startScan(mScanCallback)
    }

    private val mScanCallback = object : ScanCallback() {
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val scanRecord = result.scanRecord
            val scanData = scanRecord!!.bytes
            var name = scanRecord.deviceName
            var deviceID: Long = 0
            val manuf = scanRecord.getManufacturerSpecificData(0x0274)


            if (name == null)
                name = device.name

            if (name == null || manuf == null || manuf.size < 8)
                return


            val x = ByteBuffer.wrap(manuf)
            x.order(ByteOrder.LITTLE_ENDIAN)
            deviceID = x.long


            val listView = findViewById<View>(R.id.founddevice_listView) as ListView
            val r = listView.adapter as DeviceListAdapter
            mAdapter!!.addItem(Neblina(name, deviceID, device))
            mAdapter!!.notifyDataSetChanged()

        }

        override fun onBatchScanResults(results: List<ScanResult>) {
            for (sr in results) {
                Log.i("ScanResult - Results", sr.toString())
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Log.e("Scan Failed", "Error Code: " + errorCode)
        }
    }

    inner class DeviceListAdapter(private val mContext: Context, textViewResourceId: Int)//}, ArrayList<Neblina> devices) {
    //super(context, textViewResourceId);
        : BaseAdapter() {
        private val mNebDevices = HashMap<String, Neblina>()

        fun addItem(dev: Neblina) {
            if (mNebDevices.containsKey(dev.toString()) == false) {
                mNebDevices.put(dev.toString(), dev)
                Log.w("BLUETOOTH DEBUG", "Item added " + dev.toString())
            }
        }

        override fun getCount(): Int {
            return mNebDevices.size
        }

        override fun getItem(position: Int): Any {
            return mNebDevices.values.toTypedArray()[position]
        }

        override fun getItemId(position: Int): Long {
            return 0
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            var convertView = convertView

            if (convertView == null) {
                val inflater = LayoutInflater.from(mContext)
                convertView = inflater.inflate(R.layout.nebdevice_list_content, parent, false)
            }

            val textView = convertView!!.findViewById<View>(R.id.id) as TextView
            //ImageView imageView = (ImageView) rowView.findViewById(R.id.icon);
            if (mNebDevices.size > position) {
                textView.text = mNebDevices.values.toTypedArray()[position].toString()
            }

            return convertView
        }

    }

    fun onSwitchButtonChanged(button: CompoundButton, isChecked: Boolean) {
        val idx = button.tag as Int
        if (idx < 0 || idx > cmdList.size)
            return

        Log.d("onSwitchButtonChanged", "idx " + idx.toString())
        when (cmdList[idx].mSubSysId) {
            Neblina.NEBLINA_SUBSYSTEM_GENERAL -> when (cmdList[idx].mCmdId) {
                Neblina.NEBLINA_COMMAND_GENERAL_INTERFACE_STATE -> if (isChecked)
                    mDev!!.setDataPort(idx, 1.toByte().toInt())
                else
                    mDev!!.setDataPort(idx, 0.toByte().toInt())
                else -> {
                }
            }

            Neblina.NEBLINA_SUBSYSTEM_FUSION -> when (cmdList[idx].mCmdId) {
                Neblina.NEBLINA_COMMAND_FUSION_RATE -> {
                }
                Neblina.NEBLINA_COMMAND_FUSION_DOWNSAMPLE -> {
                }
                Neblina.NEBLINA_COMMAND_FUSION_MOTION_STATE_STREAM -> mDev!!.streamMotionState(isChecked)
                Neblina.NEBLINA_COMMAND_FUSION_QUATERNION_STREAM -> {
                    mDev!!.streamEulerAngle(false)
                    mDev!!.streamQuaternion(isChecked)
                }
                Neblina.NEBLINA_COMMAND_FUSION_EULER_ANGLE_STREAM -> {
                    mDev!!.streamQuaternion(false)
                    mDev!!.streamEulerAngle(isChecked)
                }
                Neblina.NEBLINA_COMMAND_FUSION_EXTERNAL_FORCE_STREAM -> mDev!!.streamExternalForce(isChecked)
                Neblina.NEBLINA_COMMAND_FUSION_FUSION_TYPE -> if (isChecked)
                    mDev!!.setFusionType(1.toByte())
                else
                    mDev!!.setFusionType(0.toByte())
                Neblina.NEBLINA_COMMAND_FUSION_TRAJECTORY_RECORD -> {
                }
                Neblina.NEBLINA_COMMAND_FUSION_TRAJECTORY_INFO_STREAM -> {
                }
                Neblina.NEBLINA_COMMAND_FUSION_PEDOMETER_STREAM -> mDev!!.streamPedometer(isChecked)
                Neblina.NEBLINA_COMMAND_FUSION_SITTING_STANDING_STREAM -> {
                }
                Neblina.NEBLINA_COMMAND_FUSION_LOCK_HEADING_REFERENCE -> {
                }
                Neblina.NEBLINA_COMMAND_FUSION_FINGER_GESTURE_STREAM -> {
                }
                Neblina.NEBLINA_COMMAND_FUSION_ROTATION_INFO_STREAM -> mDev!!.streamRotationInfo(isChecked)
                Neblina.NEBLINA_COMMAND_FUSION_EXTERNAL_HEADING_CORRECTION -> {
                }
            }
            Neblina.NEBLINA_SUBSYSTEM_SENSOR -> when (cmdList[idx].mCmdId) {
                Neblina.NEBLINA_COMMAND_SENSOR_ACCELEROMETER_STREAM -> mDev!!.sensorStreamAccelData(isChecked)
                Neblina.NEBLINA_COMMAND_SENSOR_GYROSCOPE_STREAM -> mDev!!.sensorStreamGyroData(isChecked)
                Neblina.NEBLINA_COMMAND_SENSOR_HUMIDITY_STREAM -> mDev!!.sensorStreamHumidityData(isChecked)
                Neblina.NEBLINA_COMMAND_SENSOR_MAGNETOMETER_STREAM -> mDev!!.sensorStreamMagData(isChecked)
                Neblina.NEBLINA_COMMAND_SENSOR_PRESSURE_STREAM -> mDev!!.sensorStreamPressureData(isChecked)
                Neblina.NEBLINA_COMMAND_SENSOR_TEMPERATURE_STREAM -> mDev!!.sensorStreamTemperatureData(isChecked)
                Neblina.NEBLINA_COMMAND_SENSOR_ACCELEROMETER_GYROSCOPE_STREAM -> mDev!!.sensorStreamAccelGyroData(isChecked)
                Neblina.NEBLINA_COMMAND_SENSOR_ACCELEROMETER_MAGNETOMETER_STREAM -> mDev!!.sensorStreamAccelMagData(isChecked)
                else -> {
                }
            }

            Neblina.NEBLINA_SUBSYSTEM_RECORDER -> when (cmdList[idx].mCmdId) {
                Neblina.NEBLINA_COMMAND_RECORDER_PLAYBACK -> mDev!!.sessionPlayback(isChecked, 0.toShort())
            }

            0xf.toByte() -> when (cmdList[idx].mCmdId) {
                2.toByte() -> {
                    //                        if (isChecked) {
                    mDev!!.sessionRecord(isChecked)
                    mDev!!.sensorStreamAccelGyroData(isChecked)
                    mDev!!.sensorStreamMagData(isChecked)
                    mDev!!.sensorStreamPressureData(isChecked)
                    mDev!!.sensorStreamTemperatureData(isChecked)
                }
            }//                      }
        //                      else {
        //                         mDev.sessionRecord(isChecked);
        //                     }
        }
    }

    fun onButtonClick(button: View) {
        val idx = button.tag as Int
        if (idx < 0 || idx > cmdList.size)
            return

        when (cmdList[idx].mSubSysId) {
            Neblina.NEBLINA_SUBSYSTEM_EEPROM -> when (cmdList[idx].mCmdId) {
                Neblina.NEBLINA_COMMAND_EEPROM_READ -> mDev!!.eepromRead(0.toShort())
                Neblina.NEBLINA_COMMAND_EEPROM_WRITE -> {
                }
            }
            Neblina.NEBLINA_SUBSYSTEM_FUSION -> when (cmdList[idx].mCmdId) {
                Neblina.NEBLINA_COMMAND_FUSION_CALIBRATE_FORWARD_POSITION -> mDev!!.calibrateForwardPosition()
                Neblina.NEBLINA_COMMAND_FUSION_CALIBRATE_DOWN_POSITION -> mDev!!.calibrateDownPosition()
            }
            Neblina.NEBLINA_SUBSYSTEM_RECORDER -> when (cmdList[idx].mCmdId) {
                Neblina.NEBLINA_COMMAND_RECORDER_ERASE_ALL -> if (mFlashEraseProgress == false) {
                    mFlashEraseProgress = true

                    mTextLine3!!.setText("Erasing...")
                    mTextLine3!!.getRootView().postInvalidate()

                    mDev!!.eraseStorage(false)
                }
                Neblina.NEBLINA_COMMAND_RECORDER_RECORD -> if (cmdList[idx].mActiveStatus == 0) {
                    mDev!!.sessionRecord(false)
                } else {
                    mDev!!.sessionRecord(true)
                }
                Neblina.NEBLINA_COMMAND_RECORDER_SESSION_DOWNLOAD -> {
                }
                Neblina.NEBLINA_COMMAND_RECORDER_PLAYBACK -> {
                }
            }
            0xFF.toByte() -> if (cmdList[idx].mCmdId.toInt() == 1)
            //start stream/record
            {
                mDev!!.streamQuaternion(true)
                //mNedDev.streamIMU(true);
                mDev!!.sessionRecord(true)
            } else
            //stop stream/record
            {
                mDev!!.disableStreaming()
                mDev!!.sessionRecord(false)
            }
        }
    }

    fun updateUI(data: ByteArray?) {
        for (i in cmdList.indices) {
            var status = 0
            when (cmdList[i].mSubSysId) {
                Neblina.NEBLINA_SUBSYSTEM_GENERAL -> status = data!![8].toInt() and 0xFF
                Neblina.NEBLINA_SUBSYSTEM_FUSION -> {
                    status = (data!![0].toInt() and 0xFF) or ((data[1]?.toInt() and 0xFF) shl 8) or ((data[2]?.toInt() and 0xFF) shl 16) or ((data[3]?.toInt() and 0xFF) shl 24)
                    Log.w("BLUETOOTH DEBUG", "NEBLINA_SUBSYSTEM_FUSION STATUS " + status.toString() )
                }
                Neblina.NEBLINA_SUBSYSTEM_SENSOR -> status = (data!![4].toInt() and 0xFF) or ((data[5]?.toInt() and 0xFF) shl 8)
                Neblina.NEBLINA_SUBSYSTEM_RECORDER -> status = data!![7].toInt() and 0xFF
            }
           // val rowView = mCmdListView?.getChildAt(i)
            //if (rowView != null) {
                Log.d("***** updateUI", "status *****" + cmdList[i].mActuator.toString() + "i=" + i.toString())
                when (cmdList[i].mActuator) {
                    NebCmdItem.ACTUATOR_TYPE_TEXT_FIELD_SWITCH.toInt(),
                    NebCmdItem.ACTUATOR_TYPE_SWITCH.toInt() -> {
                        Log.d("***** updateUI", "ACTUATOR_TYPE_SWITCH *****" + i.toString() + " "+ cmdList[i].mActiveStatus.toString())
                        //val x = cmdAdapter.findViewWithTag<View>(cmdList[i].mActuator)
                       // val v = rowView!!.findViewById(R.id.switch1) as Switch? //<View>(i) as Switch
                        val v = mCmdListView?.findViewWithTag<View>(i) as Switch?
                        if (v != null) {
                            val visi = v.visibility
                            v.visibility = View.INVISIBLE;
                            if ((cmdList[i].mActiveStatus and status) == 0) {
                                v.isChecked = false
                            } else {
                                Log.d("***** updateUI", "v.visibility *****" + visi.toString())
                                v.isChecked = true
                            }
                            v.visibility = visi
                            v.rootView.postInvalidate()
                        }
                    }
                    NebCmdItem.ACTUATOR_TYPE_BUTTON.toInt() -> {
                    }

                }
                //rowView.postInvalidate()
            //}
        }
    }


    override fun didConnectNeblina(sender: Neblina?) {
        Log.w("BLUETOOTH DEBUG", "Connected " + sender.toString())

        sender?.getSystemStatus()
        sender?.getFirmwareVersion()
    }

    override fun didReceiveResponsePacket(sender: Neblina?, subsystem: Int, cmdRspId: Int, data: ByteArray?, dataLen: Int) {
        when (subsystem) {
            Neblina.NEBLINA_SUBSYSTEM_GENERAL.toInt() -> {
                when (cmdRspId) {
                    Neblina.NEBLINA_COMMAND_GENERAL_SYSTEM_STATUS.toInt() -> {
                        runOnUiThread { updateUI(data) }
                        //updateUI(data);
                    }
                    Neblina.NEBLINA_COMMAND_GENERAL_FIRMWARE_VERSION.toInt() -> {
                        runOnUiThread {
                            val b = (data!![4].toInt() and 0xFF) or ((data!![5].toInt() and 0xFF) shl 8) or ((data!![6].toInt() and 0xFF) shl 16) as Int
                            val s = String.format("API:%d, FW:%d.%d.%d-%d", data!![0], data[1], data[2], data[3], b)
                            val tv = findViewById<View>(R.id.version_TextView) as TextView
                            tv.text = s
                            tv.rootView.postInvalidate()
                        }
                    }
                }
                when (cmdRspId) {
                    Neblina.NEBLINA_COMMAND_FUSION_QUATERNION_STREAM.toInt() -> {
                        mQuatRate = data!![2].toInt() or (data[3].toInt() shl 8)
                        mQuatPeriod = 1000000 / mQuatRate
                    }
                }
            }
            Neblina.NEBLINA_SUBSYSTEM_FUSION.toInt() -> when (cmdRspId) {
                Neblina.NEBLINA_COMMAND_FUSION_QUATERNION_STREAM.toInt() -> {
                    mQuatRate = data!![2].toInt() or (data[3].toInt() shl 8)
                    mQuatPeriod = 1000000 / mQuatRate
                }
            }
            Neblina.NEBLINA_SUBSYSTEM_SENSOR.toInt() -> {
            }
            Neblina.NEBLINA_SUBSYSTEM_RECORDER.toInt() -> when (cmdRspId) {
                Neblina.NEBLINA_COMMAND_RECORDER_ERASE_ALL.toInt() -> {
                    runOnUiThread {
                        mTextLine3!!.setText("Flash erased")
                        mTextLine3!!.getRootView().postInvalidate()
                    }
                    mFlashEraseProgress = false
                }
            }
        }
    }

    override fun didReceiveRSSI(sender: Neblina?, rssi: Int) {
   //     TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun didReceiveGeneralData(sender: Neblina?, respType: Int, cmdRspId: Int, data: ByteArray?, dataLen: Int, errFlag: Boolean) {
        when (cmdRspId) {
            Neblina.NEBLINA_COMMAND_GENERAL_SYSTEM_STATUS.toInt() -> {
                runOnUiThread { updateUI(data) }
                //updateUI(data);
            }
        }
    }

    override fun didReceiveFusionData(sender: Neblina?, respType: Int, cmdRspId: Int, data: ByteArray?, dataLen: Int, errFlag: Boolean) {
        val timeStamp = data!![0].toLong() and 0xFF or (data[1].toLong() and 0xFF shl 8) or (data[2].toLong() and 0xFF shl 16) or (data[3].toLong() and 0xFF shl 24)
        when (cmdRspId) {
            Neblina.NEBLINA_COMMAND_FUSION_EULER_ANGLE_STREAM.toInt() -> {
                val rotx = ((data[4].toInt() and 0xFF) or ((data[5].toInt() and 0xFF) shl 8)).toDouble() / 10.0
                val roty = ((data[6].toInt() and 0xFF) or ((data[7].toInt() and 0xFF) shl 8)).toDouble() / 10.0
                val rotz = ((data[8].toInt() and 0xFF) or ((data[9].toInt() and 0xFF) shl 8)).toDouble() / 10.0

                runOnUiThread {
                    val s = String.format("Euler : T : %d - (Yaw : %f, Ptich : %f, Roll : %f)", timeStamp, rotx, roty, rotz)
                    //Log.w("BLUETOOTH DEBUG", s);
                    mTextLine1!!.setText(s)
                    mTextLine1!!.getRootView().postInvalidate()
                }
                //val controler = mActivityGLView!!.controller as GLViewController
                //controler.setQuaternion(0.0.toFloat(), rotx.toFloat(), roty.toFloat(), rotz.toFloat())
            }
            Neblina.NEBLINA_COMMAND_FUSION_QUATERNION_STREAM.toInt() -> {
                val q1 = (((data[4].toInt() and 0xFF) or ((data[5].toInt() and 0xFF) shl 8)).toShort()).toDouble() / 32768.0
                val q2 = (((data[6].toInt() and 0x00FF) or ((data[7].toInt() and 0x00FF) shl 8)).toShort()).toDouble() / 32768.0
                val q3 = (((data[8].toInt() and 0x00FF) or ((data[9].toInt() and 0x00FF) shl 8)).toShort()).toDouble() / 32768.0
                val q4 = (((data[10].toInt() and 0x00FF) or ((data[11].toInt() and 0x00FF) shl 8)).toShort()).toDouble() / 32768.0
                //val q1 = (data[4] as Short and 0xFF as Short) or (data[5] as Short and 0xFF shl 8)) / 32768.0
                runOnUiThread {

                    val s = String.format("Quat: T : %d - (%.2f, %.2f, %.2f, %.2f)", timeStamp, q1, q2, q3, q4)
                    mTextLine1!!.setText(s)
                    mTextLine1!!.getRootView().postInvalidate()

                    mCur3DObj?.setQuaternion(q1.toFloat(), q2.toFloat(), q3.toFloat(), q4.toFloat())
                    //mShip?.setQuaternion(q1.toFloat(), q2.toFloat(), q3.toFloat(), q4.toFloat())
                    //mCube?.setQuaternion(q1.toFloat(), q2.toFloat(), q3.toFloat(), q4.toFloat())
                }

                //ByteBuffer ar = ByteBuffer.wrap(data);

                //int ts = (data[0] & 0xFF) | ((data[1] & 0xFF) << 8) | ((data[2] & 0xFF) << 16) | ((data[3] & 0xFF) << 24);
                var dt: Long = 0
                if (timeStamp == mQuatTimeStamp) {
                    mQuatBdCnt++
                }
                if (timeStamp > mQuatTimeStamp) {
                    dt = timeStamp - mQuatTimeStamp
                } else {
                    dt = -0x1.toLong() - mQuatTimeStamp + timeStamp
                }

                if (dt < 0) {
                    dt = -dt
                }
                if (dt > mQuatPeriod + (mQuatPeriod shr 1) || dt < mQuatPeriod - (mQuatPeriod shr 1)) {
                    mQuatDropCnt++
                }
                mQuatCnt++


                val finalDt = dt
                runOnUiThread {
                    val s = String.format("%d, %d %d %d", finalDt, mQuatCnt, mQuatDropCnt, mQuatBdCnt)
                    mTextLine2!!.setText(s)
                    mTextLine2!!.getRootView().postInvalidate()

                }
                mQuatTimeStamp = timeStamp
            }
        }
    }

    override fun didReceivePmgntData(sender: Neblina?, respType: Int, cmdRspId: Int, data: ByteArray?, dataLen: Int, errFlag: Boolean) {
       // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun didReceiveLedData(sender: Neblina?, respType: Int, cmdRspId: Int, data: ByteArray?, dataLen: Int, errFlag: Boolean) {
       // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun didReceiveDebugData(sender: Neblina?, respType: Int, cmdRspId: Int, data: ByteArray?, dataLen: Int, errFlag: Boolean) {
       /// TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun didReceiveRecorderData(sender: Neblina?, respType: Int, cmdRspId: Int, data: ByteArray?, dataLen: Int, errFlag: Boolean) {
       // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun didReceiveEepromData(sender: Neblina?, respType: Int, cmdRspId: Int, data: ByteArray?, dataLen: Int, errFlag: Boolean) {
       // TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun didReceiveSensorData(sender: Neblina?, respType: Int, cmdRspId: Int, data: ByteArray?, dataLen: Int, errFlag: Boolean) {
        val timeStamp = data!![0].toLong() and 0xFF or (data[1].toLong() and 0xFF shl 8) or (data[2].toLong() and 0xFF shl 16) or (data[3].toLong() and 0xFF shl 24)
        when (cmdRspId) {
            Neblina.NEBLINA_COMMAND_SENSOR_ACCELEROMETER_STREAM.toInt() -> {
                val x = data[4].toInt() and 0xff or (data[5].toInt() shl 8)
                val y = data[6].toInt() and 0xff or (data[7].toInt() shl 8)
                val z = data[8].toInt() and 0xff or (data[9].toInt() shl 8)
                runOnUiThread {
                    val s = String.format("Accel : %d - (%d, %d, %d)", timeStamp, x, y, z)
                    //Log.w("BLUETOOTH DEBUG", s);
                    mTextLine1!!.setText(s)
                    mTextLine1!!.getRootView().postInvalidate()
                }
            }
            Neblina.NEBLINA_COMMAND_SENSOR_GYROSCOPE_STREAM.toInt() -> {
                val x = data[4].toInt() and 0xff or (data[5].toInt() shl 8)
                val y = data[6].toInt() and 0xff or (data[7].toInt() shl 8)
                val z = data[8].toInt() and 0xff or (data[9].toInt() shl 8)
                runOnUiThread {
                    val s = String.format("Gyro : %d - (%d, %d, %d)", timeStamp, x, y, z)
                    //Log.w("BLUETOOTH DEBUG", s);
                    mTextLine1!!.setText(s)
                    mTextLine1!!.getRootView().postInvalidate()
                }
            }
            Neblina.NEBLINA_COMMAND_SENSOR_HUMIDITY_STREAM.toInt() -> {
                val x = data[4].toInt() and 0xff or (data[5].toInt() shl 8) or (data[6].toInt() shl 16) or (data[7].toInt() shl 24)
                val xf = x.toFloat() / 100.0.toFloat()
                runOnUiThread {
                    val s = String.format("Humidity : %f %", xf)
                    //Log.w("BLUETOOTH DEBUG", s);
                    mTextLine1!!.setText(s)
                    mTextLine1!!.getRootView().postInvalidate()
                }
            }
            Neblina.NEBLINA_COMMAND_SENSOR_MAGNETOMETER_STREAM.toInt() -> {
                val x = (data[4].toInt() and 0xff) or ((data[5].toInt() and 0xFF) shl 8)
                val y = (data[6].toInt() and 0xff) or ((data[7].toInt() and 0xFF) shl 8)
                val z = (data[8].toInt() and 0xff) or ((data[9].toInt() and 0xFF) shl 8)
                runOnUiThread {
                    val s = String.format("Mag : %d - (%d, %d, %d)", timeStamp, x, y, z)
                    //Log.w("BLUETOOTH DEBUG", s);
                    mTextLine1!!.setText(s)
                    mTextLine1!!.getRootView().postInvalidate()
                }
            }
            Neblina.NEBLINA_COMMAND_SENSOR_PRESSURE_STREAM.toInt() -> {
            }
            Neblina.NEBLINA_COMMAND_SENSOR_TEMPERATURE_STREAM.toInt() -> {
                val x = data[4].toInt() and 0xff or (data[5].toInt() shl 8) or (data[6].toInt() shl 16) or (data[7].toInt() shl 24)
                val xf = x.toFloat() / 100.0.toFloat()
                runOnUiThread {
                    val s = String.format("Temp : %f %", xf)
                    //Log.w("BLUETOOTH DEBUG", s);
                    mTextLine2!!.setText(s)
                    mTextLine2!!.getRootView().postInvalidate()
                }
            }
            Neblina.NEBLINA_COMMAND_SENSOR_ACCELEROMETER_GYROSCOPE_STREAM.toInt() -> {
            }
            Neblina.NEBLINA_COMMAND_SENSOR_ACCELEROMETER_MAGNETOMETER_STREAM.toInt() -> {
            }
        }
    }

    // 3D View
    public override fun onPrepareView(smartGLView: SmartGLView) {

        val context = smartGLView.context

        // Add RenderPass for Sprites & Object3D
        mRenderer = smartGLView.smartGLRenderer
        mRenderPassShip = RenderPassObject3D(RenderPassObject3D.ShaderType.SHADER_TEXTURE_LIGHTS, true, true)
        mRenderPassCube = RenderPassObject3D(RenderPassObject3D.ShaderType.SHADER_COLOR, true, false)
        mRenderer?.addRenderPass(mRenderPassShip)

        mRenderer?.setDoubleSided(false)

        val lightColor = SmartColor(1f, 1f, 1f)
        val lightDirection = Vector3D(0f, 1f, -1f)
        lightDirection.normalize()
        val lightParallel = LightParallel(lightColor, lightDirection)
        mRenderer?.setLightParallel(lightParallel)

        mSpaceFrigateTexture = Texture(context, R.drawable.space_frigate_6_color)

        var model = WavefrontModel.Builder(context, R.raw.space_frigate_obj)
                .addTexture("", mSpaceFrigateTexture)
                .create();
        mShip = model.toObject3D()
        mShip?.setPos(0f, 0f, -10f);
        mRenderPassShip?.addObject(mShip);

        model = WavefrontModel.Builder(context, R.raw.calibration_cube_obj)
                .create();
        mCube = model.toObject3D()
        mCube?.setPos(0f, 0f, -10f)
        mRenderPassCube?.addObject(mCube)

        mCur3DObj = mShip;

    }

    override fun onReleaseView(smartGLView: SmartGLView) {
        if (mSpaceFrigateTexture != null) {
            mSpaceFrigateTexture?.release()
        }
    }

    override fun onResizeView(smartGLView: SmartGLView) {
        //        onReleaseView(smartGLView);
        //        onPrepareView(smartGLView);
    }

    override fun onTick(smartGLView: SmartGLView) {
     //   val renderer = smartGLView.smartGLRenderer
     //   val frameDuration = renderer.frameDuration

    }

    public override fun onTouchEvent(smartGLView:SmartGLView, event:TouchHelperEvent) {}
}
