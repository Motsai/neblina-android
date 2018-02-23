package com.motsai.neblina;

/**********************************************************************************/

import android.util.Log;

/**********************************************************************************/

public class NeblinaUtilities {

    static public byte crc8(byte data[], int Len) {
        int i = 0;
        int e = 0;
        int f = 0;
        int crc = 0;

        for (i = 0; i < Len; i++) {
            //while (i < Len) {
            e = (crc ^ ((int)data[i] & 0xFF));
            f = (e ^ (e >> 4) ^ (e >> 7));
            crc = (((f << 1) ^ (f << 4)) & 0xff);
            //  i += 1;
        }

        Log.d("CRC8 : ", String.valueOf(crc));
        return (byte)crc;
    }

    static public byte convertBoolToByte(boolean p1) {
        return (byte)(p1 ? 1 : 0);
    }

    static public short convertByteToUnsignedShort(byte p1, byte p2) {
        return (short)((p1 & 0xFF) | ((p2 & 0xFF) << 8));
    }

    static public int convertByteToUnsignedInt(byte p1, byte p2, byte p3, byte p4) {
        return ((p1 & 0xFF) | ((p2 & 0xFF) << 8) | ((p3 & 0xFF) << 16) | ((p4 & 0xFF) << 24));
    }

} // NeblinaUtilities class

/**********************************************************************************/