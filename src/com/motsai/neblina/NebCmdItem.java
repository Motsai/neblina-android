package com.motsai.neblina;

/**
 * Created by hoanmotsai on 2016-08-01.
 */
public class NebCmdItem {
    public static final byte ACTUATOR_TYPE_SWITCH			    = 1;
    public static final byte ACTUATOR_TYPE_BUTTON			    = 2;
    public static final byte ACTUATOR_TYPE_TEXT_FIELD		    = 3;
    public static final byte ACTUATOR_TYPE_TEXT_FILED_BUTTON	= 4;

    public byte mSubSysId;
    public byte mCmdId;
    public String mName;
    public int mActuator;
    public int mActiveStatus;
    public String mText;
    public NebCmdItem(byte SubSys, byte CmdId, int ActiveStatus, String Name, int Actuator, String Text) {
        mSubSysId = SubSys;
        mCmdId = CmdId;
        mActiveStatus = ActiveStatus;
        mName = Name;
        mActuator = Actuator;
        mText = Text;
    }
}
