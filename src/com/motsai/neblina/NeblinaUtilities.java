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

    static public int convertByteToShort(byte p1, byte p2) {
        return ((p1 & 0xFF) | ((p2 & 0xFF) << 8));
    }

    static public short convertByteToUnsignedShot(byte p1, byte p2) {
        return (short)convertByteToShort(p1, p2);
    }

    static public long convertByteToUnsignedInt(byte p1, byte p2, byte p3, byte p4) {
        return ((p1 & 0xFF) | ((p2 & 0xFF) << 8) | ((p3 & 0xFF) << 16) | ((p4 & 0xFF) << 24));
    }

    static public int getCommandFromPacket(byte[] packet) {
        assert packet.length >= 4;
        return packet[3];
    }

    static public int getSubSystemFromPacket(byte[] packet) {
        assert packet.length >= 4;
        return packet[0] & 0x1F;
    }

    static public int getPacketTypeFromPacket(byte[] packet) {
        assert packet.length >= 4;
        return packet[0] >> 5;
    }

} // NeblinaUtilities class

/**********************************************************************************/