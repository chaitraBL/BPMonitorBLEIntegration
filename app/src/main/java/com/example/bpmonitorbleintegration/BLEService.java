package com.example.bpmonitorbleintegration;

import android.accessibilityservice.AccessibilityService;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.icu.text.UFormat;
import android.os.Binder;
import android.os.Build;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.SparseArray;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.WeakHashMap;

public class BLEService extends Service implements DecodeListener{

    private final static String TAG = BLEService.class.getSimpleName();
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private static int mConnectionState = Constants.STATE_DISCONNECTED;

    private final IBinder mBinder = new LocalBinder();
    private String bluetoothAddress;
    public final static UUID UUID_CHAR_LEVEL = UUID.fromString(BLEGattAttributes.CLIENT_CHARACTERISTIC_CONFIG);
    public final static UUID UUI_SERVICE_LEVEL = UUID.fromString(BLEGattAttributes.CLIENT_SERVICE_CONFIG);
    private Decoder decoder;
    public int systalic;
    public int dystolic;
    public int rate;
    public int range;
    public long pressure;
    public int batteryLevel;

//    Decoder mDecoder;
    Handler mHandler;

    // To connect to bluetooth device and gatt services.
    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean connect(String address) {
        if (mBluetoothAdapter == null || address == null) {
            Toast.makeText(getApplicationContext(), "BluetoothAdapter not initialize or unspecified address", Toast.LENGTH_SHORT).show();
            return false;
        }
        bluetoothGattCallback.onConnectionStateChange(mBluetoothGatt, BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_CONNECTING);
        if (mBluetoothAdapter != null && address.equals(bluetoothAddress) && mBluetoothGatt != null) {
            Toast.makeText(getApplicationContext(), "Try to use existing connection", Toast.LENGTH_SHORT).show();
            if (mBluetoothGatt.connect()) {
                mConnectionState = Constants.STATE_CONNECTING;
                Toast.makeText(getApplicationContext(), "Connecting...", Toast.LENGTH_SHORT).show();
                return true;
            } else {
                Toast.makeText(getApplicationContext(), "Cannot connect...", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        final BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(address);
        if (bluetoothDevice == null) {
            Toast.makeText(getApplicationContext(), "Device not found ", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (Build.VERSION.SDK_INT >= 23) {
            mBluetoothGatt = bluetoothDevice.connectGatt(this, false, bluetoothGattCallback, BluetoothDevice.TRANSPORT_LE,BluetoothDevice.PHY_LE_1M);
        } else {
            mBluetoothGatt = bluetoothDevice.connectGatt(this, false, bluetoothGattCallback);
        }
        bluetoothGattCallback.onConnectionStateChange(mBluetoothGatt, BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_CONNECTED);

        bluetoothAddress = address;
        mConnectionState = Constants.STATE_CONNECTING;
        return true;
    }

    // BluetoothGatt callback to connect, discover services, read and write the characteristics and data etc...
    private final BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            String intentAction;
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    if (status == BluetoothGatt.GATT_SUCCESS) {
                        mConnectionState = Constants.STATE_CONNECTED;
                        intentAction = Constants.ACTION_GATT_CONNECTED;
                        broadcastUpdate(intentAction);
                        gatt.requestConnectionPriority(BluetoothGatt.CONNECTION_PRIORITY_HIGH);
                        gatt.requestMtu(247);
                    }
                    break;

                case BluetoothProfile.STATE_DISCONNECTED:
                    intentAction = Constants.ACTION_GATT_DISCONNECTED;
                    broadcastUpdate(intentAction);
                    mConnectionState = Constants.STATE_DISCONNECTED;
                    disconnect();
//                    new Handler(Looper.getMainLooper()).postDelayed({
////                            initialize()
//                    }, 500);
                    break;

                default:
//                    Toast.makeText(getApplicationContext(), "Connecting...", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            if (status == BluetoothGatt.GATT_SUCCESS){
                try {
                    Thread.sleep(600);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                gatt.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(Constants.ACTION_GATT_SERVICES_DISCOVERED);
                new ConnectionThread().start();

                try {
                    // BluetoothGatt gatt
                    final Method refresh = gatt.getClass().getMethod("refresh");
                    if (refresh != null) {
                        refresh.invoke(gatt);
                    }
                } catch (Exception e) {
                    // Log it
                }
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(Constants.ACTION_DATA_AVAILABLE, characteristic);
                readCharacteristic(characteristic);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
//            byte[] messageBytes = characteristic.getValue();
////            Log.i(TAG, "onCharacteristicChange " + messageBytes);
//
//            String messageString = null;
//            try {
//                messageString = new String(messageBytes, "UTF-8");
//            } catch (UnsupportedEncodingException e) {
//                Log.e(TAG, "Unable to convert message bytes to string");
//            }
            broadcastUpdate(Constants.ACTION_DATA_AVAILABLE, characteristic);
        }
    };

    // Constructor
    public BLEService()
    {

    }

    public void setHandler(Handler mHandler){
        this.mHandler = mHandler;
    }

    private void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    // To retrieve the packets from device.
    private void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        if (UUID_CHAR_LEVEL.equals(characteristic.getUuid())) {
            final byte[] data = characteristic.getValue();

            String msg;

//            To convert data to hex value.
//            if (data != null && data.length > 0) {
//                final StringBuilder stringBuilder = new StringBuilder(data.length);
//                for (byte byteChar : data) {
//                    Log.i(TAG, "hex value " + String.format("%02X ", byteChar));
////                    stringBuilder.append(String.format("%02X ", byteChar));
//                }
////                intent.putExtra(Constants.EXTRA_DATA, new String(data) + "\n" +stringBuilder.toString());
//            }

//            Log.i(TAG, "length before switch " + data[6]);
            int length = data[6];
            int[] value = new int[30];
            for (int i = 0; i <= length; ++i)
            {
//                Log.i("Decoder", "values " + i + " " + data[i]);
                value[i] = (int) (data[i] & 0xff);
//                Log.i("Decoder", "new values " + value[i]);
            }

//            Log.i("Decoder", "Command id " + (value[5]));

            // Check for checksum
            boolean checkSumVal = decoder.checkSumValidation(value,characteristic);
//            Log.i("Decoder", "checksum " + checkSumVal);
            decoder.add1(value,action);
            if (checkSumVal == true)
            {
                //Command Id wise receiving data.
                switch (value[5]) {
                    case Constants.DEVICE_COMMANDID:
//                        Log.i(TAG, "Device id " + value);
                        Constants.deviceId = new byte[]{(byte) value[1], (byte) value[2], (byte) value[3], (byte) value[4]};
                        Constants.startValue = decoder.replaceArrayVal(Constants.startValue,Constants.deviceId);
                        Constants.ack = decoder.replaceArrayVal(Constants.ack,Constants.deviceId);
                        Constants.noAck = decoder.replaceArrayVal(Constants.noAck,Constants.deviceId);
                        Constants.checkSumError = decoder.replaceArrayVal(Constants.checkSumError,Constants.deviceId);
//                        Log.i(TAG, "new start value " + Constants.startValue);
                        break;
                    case Constants.RAW_COMMANDID:
                        Constants.is_readingStarted = true;
                        int cuffValue = value[8] * 256 + value[9];
                        int pulseValue = value[10] * 256 + value[11];
                        intent.putExtra(Constants.EXTRA_DATA, cuffValue + " / " + pulseValue);
                        Constants.is_resultReceived = false;
                        break;

                    case Constants.RESULT_COMMANDID:
                        Constants.is_resultReceived = true;
                        int systolic = value[8] * 256 + value[9];
                        int dystolic = value[10] * 256 + value[11];
                        int heartRateValue = value[12];
                        intent.putExtra(Constants.EXTRA_DATA, systolic + " / " + dystolic + " / " + heartRateValue);

//                        int rangeValue = value[13];

                        Constants.ack = decoder.computeCheckSum(Constants.ack);
//                        Log.i(TAG, "ack " + Arrays.toString(Constants.ack));
//                        Log.i(TAG, "ack sent " + Constants.ack);
                        writeCharacteristics(characteristic,Constants.checkSumError);
                        Constants.is_readingStarted = false;
                        break;

                    case Constants.ERROR_COMMANDID:
                        int error = value[8];
                        switch (error) {
                            case 1:
                                msg = "Indicates cuff placement/fitment incorrect!!!";
                                intent.putExtra(Constants.EXTRA_DATA, msg + "\n" + "Try again");
                                break;
                            case 2:
                                msg = "Indicates hand movement detected!!!";
                                intent.putExtra(Constants.EXTRA_DATA, msg + "\n" + "Try again");
                                break;
                            case 3:
                                msg = "Indicates irregular heartbeat during measurement!!!";
                                intent.putExtra(Constants.EXTRA_DATA, msg + "\n" + "Try again");
                                break;
                            case 4:
                                msg = "Indicates cuff over pressurised!!!";
                                intent.putExtra(Constants.EXTRA_DATA, msg + "\n" + "Try again");
                                break;
                            case 5:
                                msg = "Indicates low battery!!!";
                                intent.putExtra(Constants.EXTRA_DATA, msg + "\n" + "Try again");
                                break;
                            default:
                                msg = " ";
                                intent.putExtra(Constants.EXTRA_DATA, msg);
//                                intent.putExtra(Constants.EXTRA_DATA, msg + "\n" + "Try again");
                                break;
                        }
                        Constants.ack = decoder.computeCheckSum(Constants.ack);
//                        Log.i(TAG, "error" + Arrays.toString(Constants.ack));
//                        Log.i(TAG, "ack sent " + Constants.ack);
                        writeCharacteristics(characteristic,Constants.ack);
                        Constants.is_resultReceived = true;
                        break;

                    case Constants.ACK_COMMANDID:
                        Constants.is_ackReceived = true;
                        int ack = value[8];
//                        Log.i(TAG, "ack in bleservice " + ack);
//
                        break;

                    case Constants.BATTERY_COMMANDID:
                        Constants.is_batterValueReceived = true;
                        int batteryLevel = value[8];
//                        Log.i(TAG, "Battery level " + batteryLevel);
//                        intent.putExtra(Constants.EXTRA_DATA, batteryLevel);
                        Constants.ack = decoder.computeCheckSum(Constants.ack);
//                        Log.i(TAG, "error" + Arrays.toString(Constants.ack));
//                        Log.i(TAG, "ack sent " + Constants.ack);
                        writeCharacteristics(characteristic,Constants.ack);
                        break;
                }

            }
            else {
                Constants.checkSumError = decoder.computeCheckSum(Constants.checkSumError);
//                Log.i(TAG, "check sum error " + Arrays.toString(Constants.checkSumError));
//                Log.i(TAG, "ack sent " + Constants.ack);
                writeCharacteristics(characteristic,Constants.checkSumError);
            }
            Arrays.fill(value,(byte) 0);
        }
        sendBroadcast(intent);
    }

    // To read the data.
    public void readCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Toast.makeText(getApplicationContext(), "BluetoothAdapter not initialised", Toast.LENGTH_SHORT).show();
            return;
        }
        mBluetoothGatt.readCharacteristic(bluetoothGattCharacteristic);
    }

    //To write data to the device.
    public void writeCharacteristics(BluetoothGattCharacteristic characteristics, byte[] value){
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Toast.makeText(getApplicationContext(), "BluetoothAdapter not initialised", Toast.LENGTH_SHORT).show();
            return;
        }
        characteristics.setValue(value);
        mBluetoothGatt.writeCharacteristic(characteristics);
    }

    public void setCharacteristicNotification( BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Toast.makeText(getApplicationContext(), "BluetoothAdapter not initialised", Toast.LENGTH_SHORT).show();
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);
    }

    public void setCharacteristickIndication(BluetoothGattCharacteristic characteristic, Boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Toast.makeText(getApplicationContext(), "BluetoothAdapter not initialised", Toast.LENGTH_SHORT).show();
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

//        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(charUuid);
//        if (enabled) {
//            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//        }
//        else {
//            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
//        }
//        mBluetoothGatt.writeDescriptor(descriptor);
    }

    // To get list of gatt services from bluetooth device.
    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

    // To get list of gatt characteristics from bluetooth device.
    public List<BluetoothGattCharacteristic> getSupportedGattCharacteristics(BluetoothGattService service) {
        if (mBluetoothGatt == null){
            return null;
        }
        return service.getCharacteristics();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        close();
        return super.onUnbind(intent);
    }

    public void close() {
        if (mBluetoothGatt == null) {
            return;
        }
        mBluetoothGatt.close();
        mBluetoothGatt = null;
    }

    public void disconnect() {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Log.d(TAG, "Bluetooth adapter not initialize");
            return;
        }
        mBluetoothGatt.disconnect();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        stopSelf();
        clearServicesCache();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

    }

    //remove device authorization/ bond/ pairing
//    public static void removeBond(BluetoothDevice device){
//        try {
//            if (device == null){
//                throw new Exception();
//            }
//            Method method = device.getClass().getMethod("removeBond", (Class[]) null);
//            method.invoke(device, (Object[]) null);
//            Log.d(LOG_TAG, "removeBond() called");
//            Thread.sleep(600);
//            Log.d(LOG_TAG, "removeBond() - finished method");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    // To clear gatt services cache.
    private boolean clearServicesCache()
    {
        boolean result = false;
        try {
            Method refreshMethod = mBluetoothGatt.getClass().getMethod("refresh");
            if(refreshMethod != null) {
                result = (boolean) refreshMethod.invoke(mBluetoothGatt);
            }
        } catch (Exception e) {
            Log.e(TAG, "ERROR: Could not invoke refresh method");
        }
        return result;
    }
    public static BluetoothAdapter getBluetoothAdapter(Context context) {
        BluetoothManager mBluetoothManager = (BluetoothManager) context.getSystemService(BLUETOOTH_SERVICE);
        return mBluetoothManager.getAdapter();
    }

    public boolean initialize() {
        mBluetoothAdapter = getBluetoothAdapter(this);
        return true;
    }

    @Override
    public void pressureValue(int value1, int value2) {
        pressure = value1;

        if (mHandler != null) {
//            Log.i(TAG, " pressure value " + value1 + " " + value2);
            mHandler.obtainMessage(Constants.RAW_COMMANDID,value1,value2);
        }
    }

    @Override
    public void deviceId(int deviceId) {
//        Log.i(TAG, "device Id" + deviceId);
        if (mHandler != null) {
            mHandler.obtainMessage(deviceId).sendToTarget();
        }
    }

    @Override
    public void systolic(int value) {
//        Log.i(TAG, "Systa " + value);
        systalic = value;
    }

    @Override
    public void diastolic(int value) {
//        Log.i(TAG, "Diasta " + value);
        dystolic = value;
    }

    @Override
    public void heartRate(int value) {
//        Log.i(TAG, "heart " + value);
        rate = value;
    }

    @Override
    public void range(int value) {
//        Log.i(TAG, "range " + value);
        range = value;
    }

    @Override
    public void errorMsg(int errNo) {
//        Log.i(TAG, "Error " + errNo);
        if (mHandler != null) {
            mHandler.obtainMessage(errNo).sendToTarget();
        }
    }

    @Override
    public void ackMsg(int ackNo) {
//        Log.i(TAG, "Ack " + ackNo);
        if (mHandler != null) {
            mHandler.obtainMessage(ackNo).sendToTarget();
        }
    }

    @Override
    public void batteryMsg(int value) {
        batteryLevel = value;
//        Log.i(TAG, "Battery level " + batteryLevel);
    }

    public class LocalBinder extends Binder {
        BLEService getService() {
            return BLEService.this;
        }
    }

    private class ConnectionThread extends Thread {
        @Override
        public void run() {
            super.run();
            decoder = new Decoder(BLEService.this);
            decoder.start();
        }
    }
}
