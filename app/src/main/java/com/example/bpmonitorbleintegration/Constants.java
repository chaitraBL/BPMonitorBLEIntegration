package com.example.bpmonitorbleintegration;

public class Constants {
    public final static String ACTION_GATT_CONNECTED =
            "android-er.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "android-er.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "android-er.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "android-er.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "android-er.EXTRA_DATA";

    public final static String CUFF_DATA = "android-er.EXTRA_DATA";

    public final static String PRESSURE_DATA = "android-er.EXTRA_DATA";
    public static final int STATE_DISCONNECTED = 0;
    public static final int STATE_CONNECTING = 1;
    public static final int STATE_CONNECTED = 2;

    public static final byte[] startValue = {0x7B,0x00,0x00,0x00,0x01,0x10,0x0A,0x00,0x01,0x00,0x1C,0x7D};
    public static final byte rawCommandID = 17;
    public static final byte resultCommandID = 18;
    public static final byte errorCommandID = 19;
    public static final byte ackCommandID = 20;
}
