package com.example.bpmonitorbleintegration;

import android.bluetooth.BluetoothGattCharacteristic;
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
    DataTransferActivity dataTransferActivity;

    public Decoder(DecodeListener decodeListener) {
        this.decodeListener = decodeListener;
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
//                    intent.putExtra(Constants.EXTRA_DATA, cuffValue + " / " + pulseValue);

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
//                    Log.i("Decoder", "Heart Rate " + heartRateValue);
                    decodeListener.heartRate(heartRateValue);
                    int rangeValue = value[13];
//                    Log.i("Decoder", "range  " + rangeValue);
                    decodeListener.range(rangeValue);
//                    intent.putExtra(Constants.EXTRA_DATA, systolic + " / " + dystolic + " / " + heartRateValue);
                    break;

                case Constants.ERROR_COMMANDID:
                    int error = value[8];
                    decodeListener.errorMsg(error);
//                    intent.putExtra(Constants.EXTRA_DATA, error);
                    break;

                case Constants.ACK_COMMANDID:
                    int ack = value[8];
                    decodeListener.ackMsg(ack);
//                    intent.putExtra(Constants.EXTRA_DATA, ack);
                    break;
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

            case Constants.ERROR_COMMANDID:
                checkSum = data[9] * 256 + data[10];
                break;

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

    public void computeCheckSum(byte[] data){
        int[] value = new int[20];
        int length = data[6];
        int newLength = value[6];
        for (int i = 0; i <= length; i++)
        {
                Log.i("Decoder", "data in computeCheckSum " + i + " " + data[i]);
            value[i] = (int) (data[i] & 0xff);
                Log.i("Decoder", "new data in computeCheckSum " + value[i]);
        }



    }
}
