package com.example.bpmonitorbleintegration;

import java.util.HashMap;

public class BLEGattAttributes {

    public static final String CLIENT_SERVICE_CONFIG = "0000ffe0-0000-1000-8000-00805f9b34fb";
    public static final String CLIENT_CHARACTERISTIC_CONFIG = "0000ffe1-0000-1000-8000-00805f9b34fb";

    private static HashMap<String, String> attributes = new HashMap();

    static {
        attributes.put(CLIENT_CHARACTERISTIC_CONFIG, "Character Level");
        attributes.put(CLIENT_SERVICE_CONFIG, "Service");
    }

    public static String lookup(String uuid) {
        String name = attributes.get(uuid);
        return name;
    }
//    private static HashMap<String,String> attributes = new HashMap();
//    public static String CLIENT_CHARACTERISTIC_CONFIG = "0000ffe1-0000-1000-8000-00805f9b34fb";
//
////    static {
////        attributes.put()
////    }
//
//    public static String lookup(String rxDatum) {
//        String name = attributes.get(uuid);
//        return name;
//    }
}
