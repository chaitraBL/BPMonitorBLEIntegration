package com.example.bpmonitorbleintegration;

import android.util.Log;

import java.nio.ByteBuffer;
import java.util.concurrent.LinkedBlockingQueue;

public class Decoder
{
    private  byte[] byteStream;
    private int dataIndex;
    String buffer = "";
    private boolean isData;
    DecodeListener decodeListener;
    private int cuffValue;
    private int pressureValue;
    //Buffer queue
    private LinkedBlockingQueue<Integer> bufferQueue = new LinkedBlockingQueue<Integer>(256);

    public Decoder(DecodeListener decodeListener) {
        this.decodeListener = decodeListener;
    }

    public void start() {
        byteStream = new byte[255];
        isData = false;
    }

    public void add(byte[] bytes) {
        for (int i = 0; i < bytes.length; i++) {
            Log.i("Decoder", "byte value " + bytes[i]);
            byteStream[dataIndex++] = bytes[i];
            decodeListener.pressureValue(ByteBuffer.wrap(new byte[]{0x00,0x00, (byte) (byteStream[7]),byteStream[8]}).getInt());
            decodeListener.pulseValue(ByteBuffer.wrap(new byte[]{0x00,0x00, (byte) (byteStream[9]),byteStream[10]}).getInt());

            int dev_id1 = Integer.parseInt(Integer.toHexString(ByteBuffer.wrap(new byte[]{0x00,0x00,byteStream[1],byteStream[2]}).getInt()));
            int dev_id2 = Integer.parseInt(Integer.toHexString(ByteBuffer.wrap(new byte[]{0x00,0x00,byteStream[3],byteStream[4]}).getInt()));
            Log.i("Decoder", "dev id1 " + String.valueOf(dev_id1)+String.valueOf(dev_id2));
            Log.i("Decoder", "dev id2 " + Integer.valueOf(String.valueOf(dev_id1)+String.valueOf(dev_id2)));
            int final_devid = Integer.valueOf(String.valueOf(dev_id1)+String.valueOf(dev_id2));
            buffer += String.valueOf(dev_id1);
            Log.i("Decoder","device id " + final_devid);
            decodeListener.deviceId(final_devid);
        }
    }
}
