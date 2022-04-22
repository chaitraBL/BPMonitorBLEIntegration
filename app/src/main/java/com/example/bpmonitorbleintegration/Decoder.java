package com.example.bpmonitorbleintegration;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

public class Decoder
{
    private  byte[] byteStream;
    private int dataIndex;
    String buffer = "";
    private boolean isData;
    DecodeListener decodeListener;

    public Decoder(DecodeListener decodeListener) {
        this.decodeListener = decodeListener;
    }

    public void start() {
        byteStream = new byte[15];
//        Arrays.fill(byteStream,(byte) 0);
        isData = false;
    }

    public void add1(byte[] bytes){
//            Log.i("Decoder", "length of data " + bytes.length);
//            for (int i = 0; i < bytes.length; i++) {
//                byteStream[dataIndex++] = bytes[i];
                switch (bytes[5]) {
                    case Constants.rawCommandID:
//                        Log.i("Decoder", " value1 " + bytes[10]);
//                        Log.i("Decoder", " value2 " + bytes[11]);

                        int cuffValue = bytes[8] * 256 + bytes[9];
//                        Log.i("Decoder", "pressure value " + cuffValue);
                        int pulseValue = bytes[10] * 256 + bytes[11];
//                        Log.i("Decoder", "pulse value " + pulseValue);
                        decodeListener.pressureValue(cuffValue);
                        decodeListener.pulseValue(pulseValue);

                        int final_devid = Integer.valueOf(String.valueOf(bytes[1]) + String.valueOf(bytes[2]) + String.valueOf(bytes[3]) + String.valueOf(bytes[4]));
                        buffer += String.valueOf(final_devid);
                        decodeListener.deviceId(final_devid);
                        break;

                    case Constants.resultCommandID:

                        int systolic = bytes[8];
//                        int dystolic =
                        int heartRate = bytes[12];
                        int range = bytes[13];

                        break;

                    case Constants.errorCommandID:
                        break;

                    case Constants.ackCommandID:
                        break;
                }
        }


//    public void add(byte[] bytes) {
//        if (byteStream != null) {
//
//            for (int i = 0; i < bytes.length; i++) {
//
//                Log.i("Decoder", "byte value " + bytes[i]);
//                byteStream[dataIndex++] = bytes[i];
//
//                decodeListener.pressureValue(ByteBuffer.wrap(new byte[]{0x00,0x00, (byte) (byteStream[8]),byteStream[9]}).getInt());
//                decodeListener.pulseValue(ByteBuffer.wrap(new byte[]{0x00,0x00, (byte) (byteStream[10]),byteStream[11]}).getInt());
//
//                int dev_id1 = Integer.parseInt(Integer.toHexString(ByteBuffer.wrap(new byte[]{0x00,0x00,byteStream[1],byteStream[2]}).getInt()));
//                int dev_id2 = Integer.parseInt(Integer.toHexString(ByteBuffer.wrap(new byte[]{0x00,0x00,byteStream[3],byteStream[4]}).getInt()));
//
//                int final_devid = Integer.valueOf(String.valueOf(dev_id1)+String.valueOf(dev_id2));
//                buffer += String.valueOf(final_devid);
////                Log.i("Decoder","device id " + final_devid);
//                decodeListener.deviceId(final_devid);
//            }
//        }
//    }
}
