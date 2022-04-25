package com.example.bpmonitorbleintegration;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;

public class Decoder
{
    String buffer = "";
    private boolean isData;
    DecodeListener decodeListener;

    public Decoder(DecodeListener decodeListener) {
        this.decodeListener = decodeListener;
    }

    public void start() {
//        Arrays.fill(byteStream,(byte) 0);
        isData = false;
    }

    public void add1(byte[] bytes){
        Log.i("Decoder", "Command id " + bytes[5]);

        boolean checkSumVal = checkSumValidation(bytes);
        Log.i("Decoder", "checksum " + checkSumVal);

        // As per the command id data will be retrieved.
        switch (bytes[5]) {
            case Constants.RAW_COMMANDID:
                //Method 1: conversion of cuff and pulse pressure value.
                int cuffValue = bytes[8] * 256 + bytes[9];
//              Log.i("Decoder", "pressure value " + cuffValue);
                int pulseValue = bytes[10] * 256 + bytes[11];
//              Log.i("Decoder", "pulse value " + pulseValue);
                decodeListener.pressureValue(cuffValue, pulseValue);
//              decodeListener.pulseValue(pulseValue);

                // Accessing device id.
                int dev_id1 = Integer.parseInt(Integer.toHexString(ByteBuffer.wrap(new byte[]{0x00,0x00,bytes[1],bytes[2]}).getInt()));
                int dev_id2 = Integer.parseInt(Integer.toHexString(ByteBuffer.wrap(new byte[]{0x00,0x00,bytes[3],bytes[4]}).getInt()));
                int final_devid = Integer.valueOf(String.valueOf(dev_id1) + String.valueOf(dev_id2));

                buffer += String.valueOf(final_devid);
                decodeListener.deviceId(final_devid);
                break;

            case Constants.RESULT_COMMANDID:
                //Method 2: conversion of systolic and dystiolic value.
                decodeListener.systolic(ByteBuffer.wrap(new byte[]{0x00,0x00,bytes[8],bytes[9]}).getInt());
                decodeListener.diastolic(ByteBuffer.wrap(new byte[]{0x00,0x00,bytes[10],bytes[11]}).getInt());
                int heartRateValue = bytes[12];
                Log.i("Decoder", "Heart Rate " + heartRateValue);
                decodeListener.heartRate(heartRateValue);
                int rangeValue = bytes[13];
                Log.i("Decoder", "range  " + rangeValue);
                decodeListener.range(rangeValue);
                break;

            case Constants.ERROR_COMMANDID:
                int error = bytes[8];
                decodeListener.errorMsg(error);
                break;

            case Constants.ACK_COMMANDID:
                int ack = bytes[8];
                decodeListener.ackMsg(ack);
                break;
        }
    }

        // Checking checksum conversion is true/false to retrieve data.
    private boolean checkSumValidation(byte[] data) {
        int checkSum = data[12] * 256 + data[13];
        Log.i("Decoder", "checkSum " + checkSum);
        int checkSumVerified = 0;
//        for (byte value : data) {
//            checkSumVerified += value;
//        }

        for (int i = 1; i < data.length; ++i) {
            Log.i("Decoder", "checkSum index " + i + " " + data[i]);
            checkSumVerified += data[i];
            Log.i("Decoder", "checkSumVerified inside loop " + checkSumVerified);
        }
            Log.i("Decoder", "checkSumVerified " + checkSumVerified);

        if (checkSum == checkSumVerified)
        {
            return true;
        }
        else{
            return false;
        }
    }
}
