package com.motsai.neblina;

/**
 * Created by hoanmotsai on 2016-08-01.
 */
public class NebCmdItem {
    public byte mSubSysId;
    public byte mCmdId;
    public String mName;
    public int mActuator;
    public String mText;
    public NebCmdItem(byte SubSys, byte CmdId, String Name, int Actuator, String Text) {
        mSubSysId = SubSys;
        mCmdId = CmdId;
        mName = Name;
        mActuator = Actuator;
        mText = Text;
    }
}
