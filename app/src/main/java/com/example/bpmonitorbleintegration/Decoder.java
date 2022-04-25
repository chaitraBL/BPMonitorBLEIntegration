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
    BLEService mBLEservice;

    public Decoder(DecodeListener decodeListener) {
        this.decodeListener = decodeListener;
    }

    public void start() {
//        Arrays.fill(byteStream,(byte) 0);
        isData = false;
    }

    public void add1(byte[] bytes){
        int[] value = new int[20];
        for (int i = 0; i <= bytes[6]; i++)
        {
//            Log.i("Decoder", "values " + bytes[i]);
            value[i] = (int) (bytes[i] & 0xff);
//            Log.i("Decoder", "new values " + value[i]);
        }

        Log.i("Decoder", "Command id " + (bytes[5]&0xff));

        boolean checkSumVal = checkSumValidation(value);
        Log.i("Decoder", "checksum " + checkSumVal);

        if (checkSumVal == true)
        {
            // As per the command id data will be retrieved.
            switch (value[5]) {
                case Constants.RAW_COMMANDID:
                    //Method 1: conversion of cuff and pulse pressure value.
                    int cuffValue = value[8] * 256 + value[9];
                    int pulseValue = value[10] * 256 + value[11];
                    decodeListener.pressureValue(cuffValue, pulseValue);

                    // Accessing device id.
//                    int dev_id1 = Integer.parseInt(Integer.toHexString(ByteBuffer.wrap(new byte[]{0x00,0x00,bytes[1],bytes[2]}).getInt()));
//                    int dev_id2 = Integer.parseInt(Integer.toHexString(ByteBuffer.wrap(new byte[]{0x00,0x00,bytes[3],bytes[4]}).getInt()));
                    int final_devid = Integer.valueOf(String.valueOf(value[1]) + String.valueOf(value[2]) + String.valueOf(value[3]) + String.valueOf(value[4]));

                    buffer += String.valueOf(final_devid);
                    decodeListener.deviceId(final_devid);
                    break;

                case Constants.RESULT_COMMANDID:
                    //Method 2: conversion of systolic and dystiolic value for byte[].
//                    decodeListener.systolic(ByteBuffer.wrap(new byte[]{0x00,0x00,bytes[8],bytes[9]}).getInt());
//                    decodeListener.diastolic(ByteBuffer.wrap(new byte[]{0x00,0x00,bytes[10],bytes[11]}).getInt());
                    int systolic = value[8] * 256 + value[9];
//
                    decodeListener.systolic(systolic);
                    int dystolic = value[10] * 256 + value[11];
                    decodeListener.diastolic(dystolic);
                    int heartRateValue = value[12];
                    Log.i("Decoder", "Heart Rate " + heartRateValue);
                    decodeListener.heartRate(heartRateValue);
                    int rangeValue = value[13];
                    Log.i("Decoder", "range  " + rangeValue);
                    decodeListener.range(rangeValue);
                    break;

                case Constants.ERROR_COMMANDID:
                    int error = value[8];
                    decodeListener.errorMsg(error);
                    break;

                case Constants.ACK_COMMANDID:
                    int ack = value[8];
                    decodeListener.ackMsg(ack);
                    break;
            }
        }

    }

        // Checking checksum conversion is true/false to retrieve data.
    private boolean checkSumValidation(int[] data) {
        int checkSum = data[12] * 256 + data[13];

        long final_checksum = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            final_checksum = Integer.toUnsignedLong(checkSum);
        }
        Log.i("Decoder", "checkSum " + checkSum);
        int checkSumVerified = 0;

        Log.i("Decoder", "length " + data.length);
        int length = data[6];
        for (int i = 1; i <= length - 2; ++i) {

            checkSumVerified += data[i];
        }
            Log.i("Decoder", "checkSumVerified " + checkSumVerified);

        if (checkSum == checkSumVerified)
        {
//            mBLEservice.writeCharacteristics();
            return true;
        }
        else{

            return false;
        }
    }
}
