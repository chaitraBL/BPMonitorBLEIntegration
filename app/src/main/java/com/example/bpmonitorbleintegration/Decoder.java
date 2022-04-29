package com.example.bpmonitorbleintegration;

import android.bluetooth.BluetoothGattCharacteristic;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
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
    CountDownTimer timer;
    public Decoder(DecodeListener decodeListener) {
        this.decodeListener = decodeListener;
    }
    public Decoder() {

    }

    public void start() {
//        Arrays.fill(byteStream,(byte) 0);
        isData = false;
    }

    public void add1(int[] value, final String action){
//        final Intent intent = new Intent(action);
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
                    Constants.is_resultReceived = true;
                    int systolic = value[8] * 256 + value[9];
//
                    decodeListener.systolic(systolic);
                    int dystolic = value[10] * 256 + value[11];
                    decodeListener.diastolic(dystolic);
                    int heartRateValue = value[12];
//                    Log.i("Decoder", "Heart Rate " + heartRateValue);
                    decodeListener.heartRate(heartRateValue);
                    int rangeValue = value[13];
//                    Log.i("Decoder", "range  " + rangeValue);
                    decodeListener.range(rangeValue);
                    break;

                case Constants.ERROR_COMMANDID:
                    Constants.is_resultReceived = true;
                    int error = value[8];
                    decodeListener.errorMsg(error);
                    break;

                case Constants.ACK_COMMANDID:
                    int ack = value[8];
//                    Log.i("Decoder", "ack " + ack);
                    decodeListener.ackMsg(ack);
                    break;

                case Constants.BATTERY_COMMANDID:
                    int batteryVal = value[8];
//                    Log.i("Decoder", "Battery level " + batteryVal);
                    decodeListener.batteryMsg(batteryVal);
            }
    }

        // Checking checksum conversion is true/false to retrieve data.
    public boolean checkSumValidation(int[] data, BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        int checkSum = 0;
        switch (data[5]) {
            case Constants.DEVICE_COMMANDID:
                checkSum = data[8] * 256 + data[9];
                break;

            case Constants.RAW_COMMANDID:
                checkSum = data[12] * 256 + data[13];
                break;

            case Constants.RESULT_COMMANDID:
                checkSum = data[14] * 256 + data[15];
                break;

                //Merging same cases.
            case Constants.ERROR_COMMANDID:

            case Constants.BATTERY_COMMANDID:

            case Constants.ACK_COMMANDID:
                checkSum = data[9] * 256 + data[10];
                break;

            default:
                Log.i("Decoder", "Command ID not match");
                checkSum = data[9] * 256 + data[10];
        }
//        Log.i("Decoder", "checkSum val " + checkSum);
        int checkSumVerified = 0;

        int length = data[6];
        for (int i = 1; i <= length - 2; ++i) {
            checkSumVerified += data[i];
        }
//            Log.i("Decoder", "checkSumVerified " + checkSumVerified);

        if (checkSum == checkSumVerified)
        {
            return true;
        }
        else {
            return false;
        }
    }

    public byte[] computeCheckSum(byte[] data){
        int length = data[6];
        int final_checkSum = 0;

//        Log.i("decoder", "new length " + length);
        for (int j = 1; j <= length - 2; j++)
        {
//            Log.i("Decoder","final_checSum in loop " + data[j]);
            final_checkSum += (data[j] & 0xff);
//            Log.i("Decoder","final_checSum in loop " + final_checkSum);
        }
//        Log.i("Decoder","final_checSum " + final_checkSum);
        data[9] = (byte) (final_checkSum >> 8 & 0xff);
        data[10] = (byte) (final_checkSum & 0xff);
//        Log.i("Decoder", "Check sum " + data[9] + " " + data[10]);
        return data;
    }

    public byte[] replaceArrayVal(byte[] value, byte[] value1) {
        value[1] = value1[0];
        value[2] = value[1];
        value[3] = value1[2];
        value[4] = value1[3];
        return value;
    }
}
