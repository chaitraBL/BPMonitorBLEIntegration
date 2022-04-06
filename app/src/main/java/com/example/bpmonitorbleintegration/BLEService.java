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
import android.content.Intent;
import android.hardware.camera2.params.OisSample;
import android.os.Binder;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.renderscript.Sampler;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.List;
import java.util.UUID;

public class BLEService extends Service {

    private final static String TAG = BLEService.class.getSimpleName();
//    private static final UUID UUID_BATTERY_LEVEL = ;UUID_BATTERY_LEVEL

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    private BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;

    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

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
    public static UUID serviceUuid = convertFromInteger(0xFFE0);
    public static UUID charUuid = convertFromInteger(0xFFE1);

    public static String String_GENUINO101_ledService =
            "19B10000-E8F2-537E-4F6C-D104768A1214";
    public final static ParcelUuid ParcelUuid_GENUINO101_ledService =
            ParcelUuid.fromString(String_GENUINO101_ledService);
    public final static UUID UUID_GENUINO101_ledService =
            UUID.fromString(String_GENUINO101_ledService);

    public static String String_GENUINO101_switchChar =
            "19B10001-E8F2-537E-4F6C-D104768A1214";
    public final static UUID UUID_GENUINO101_switchChare =
            UUID.fromString(String_GENUINO101_switchChar);
    private final IBinder mBinder = new LocalBinder();
    private String bluetoothAddress;
    public final static UUID UUID_CHAR_LEVEL = UUID.fromString(BLEGattAttributes.CLIENT_CHARACTERISTIC_CONFIG);

    BluetoothGattCharacteristic mBluetoothGattChar;

    public static UUID convertFromInteger(int i) {
        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        long value = i & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }

//    public BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
//        @Override
//        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
//            super.onConnectionStateChange(gatt, status, newState);
//            Log.d(TAG, "onConnectionStateChange " + newState);
//            Toast.makeText(getApplicationContext(), "onConnectionStateChange " + newState, Toast.LENGTH_SHORT).show();
//            String intentAction;
//            switch (newState) {
//                case BluetoothProfile.STATE_CONNECTED:
////                    Log.i("gattCallback", "STATE_CONNECTED");
//                    mConnectionState = STATE_CONNECTED;
//                    intentAction = ACTION_GATT_CONNECTED;
//                    broadcastUpdate(intentAction);
//                    gatt.discoverServices();
//
//                    Toast.makeText(getApplicationContext(), "Connected...", Toast.LENGTH_SHORT).show();
//                    break;
//                case BluetoothProfile.STATE_DISCONNECTED:
////                    Log.e("gattCallback", "STATE_DISCONNECTED");
//                    intentAction = ACTION_GATT_DISCONNECTED;
//                    broadcastUpdate(intentAction);
//                    Toast.makeText(getApplicationContext(), "Disconnected...", Toast.LENGTH_SHORT).show();
//                    mConnectionState = STATE_DISCONNECTED;
//
//                    break;
//                default:
////                    Log.e("gattCallback", "STATE_OTHER");
//                    Toast.makeText(getApplicationContext(), "Other state...", Toast.LENGTH_SHORT).show();
//            }
////            if (newState == BluetoothProfile.STATE_CONNECTED) {
////                intentAction = ACTION_GATT_CONNECTED;
////                mConnectionState = STATE_CONNECTED;
////                broadcastUpdate(intentAction);
////                Log.i(TAG, "Connected to GATT server.");
////                // Attempts to discover services after successful connection.
////                Log.i(TAG, "Attempting to start service discovery:" +
////                        mBluetoothGatt.discoverServices());
////                Toast.makeText(getApplicationContext(), "Attempting to start service discovery: " + mBluetoothGatt.discoverServices(), Toast.LENGTH_SHORT).show();
////            } else if (newState == STATE_DISCONNECTED) {
////                intentAction = ACTION_GATT_DISCONNECTED;
////                mConnectionState = STATE_DISCONNECTED;
////                Log.i(TAG, "Disconnected from GATT server.");
////                broadcastUpdate(intentAction);
////            }
//        }
//
//        @Override
//        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
//            super.onServicesDiscovered(gatt, status);
//            Log.d(TAG, "onServicesDiscovered " + status);
//            Toast.makeText(getApplicationContext(), "onServicesDiscovered: " + status, Toast.LENGTH_SHORT).show();
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                broadcastUpdate(ACTION_GATT_SERVICES_DISCOVERED);
//            } else {
//                Log.w(TAG, "onServicesDiscovered received: " + status);
//                Toast.makeText(getApplicationContext(), "onServicesDiscovered received: " + status, Toast.LENGTH_SHORT).show();
//
//            }
//        }
//
//        @Override
//        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//            super.onCharacteristicRead(gatt, characteristic, status);
//            Log.d(TAG, "onCharacteristicRead " + status);
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
//            }
//        }
//
//        @Override
//        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
//            super.onCharacteristicWrite(gatt, characteristic, status);
//            Log.d(TAG, "onCharacteristicWrite " + status);
//        }
//
//        @Override
//        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
//            super.onCharacteristicChanged(gatt, characteristic);
//            Log.d(TAG, "onCharacteristicChanged");
//            broadcastUpdate(ACTION_DATA_AVAILABLE, characteristic);
//        }
//    };

//    public boolean connect(String address) {
//        if (mBluetoothAdapter == null || address == null) {
////            Log.w("TAG", "BluetoothAdapter not initialize or unspecified address");
//            Toast.makeText(getApplicationContext(), "BluetoothAdapter not initialize or unspecified address", Toast.LENGTH_SHORT).show();
//            return false;
//        }
//        if (mBluetoothAdapter != null && address.equals(bluetoothAddress) && mBluetoothGatt != null) {
////            Log.d(TAG, "Try to use existing connection");
//            Toast.makeText(getApplicationContext(), "Try to use existing connection", Toast.LENGTH_SHORT).show();
//            if (mBluetoothGatt.connect()) {
//                mConnectionState = STATE_CONNECTING;
//                Toast.makeText(getApplicationContext(), "Connecting...", Toast.LENGTH_SHORT).show();
//                return true;
//            } else {
//                Toast.makeText(getApplicationContext(), "Cannot connect...", Toast.LENGTH_SHORT).show();
//                return false;
//            }
//        }
//        final BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(address);
//        if (bluetoothDevice == null) {
////            Log.w(TAG, "Device not found");
//            Toast.makeText(getApplicationContext(), "Device not found ", Toast.LENGTH_SHORT).show();
//            return false;
//        }
//        mBluetoothGatt = bluetoothDevice.connectGatt(this, false, bluetoothGattCallback);
//        bluetoothAddress = address;
//        mConnectionState = STATE_CONNECTING;
//        return true;
//
////        if (mBluetoothAdapter == null || address == null) {
////            Log.w(TAG, "BluetoothAdapter not initialize or unspecified address");
////            return false;
////        }
////        if (mBluetoothAdapter != null && address.equals(bluetoothAddress) && mBluetoothGatt != null) {
////            Log.d(TAG, "Try to use existing connection");
////            if (mBluetoothGatt.connect()) {
////                mConnectionState = STATE_CONNECTING;
////                return true;
////            } else {
////                return false;
////            }
////        }
////        final BluetoothDevice bluetoothDevice = mBluetoothAdapter.getRemoteDevice(address);
////        if (bluetoothDevice == null) {
////            Log.w(TAG, "Device not found");
////            return false;
////        }
////        mBluetoothGatt = bluetoothDevice.connectGatt(this, false, bluetoothGattCallback);
////        bluetoothAddress = address;
////        mConnectionState = STATE_CONNECTING;
////        return true;
//    }

    public void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }
    public void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);
        Log.e("action", action);
        Toast.makeText(getApplicationContext(), "action " + action, Toast.LENGTH_SHORT).show();
        if (UUID_CHAR_LEVEL.equals(characteristic.getUuid())) {
            int format = BluetoothGattCharacteristic.FORMAT_UINT8;
            final int battery_level = characteristic.getIntValue(format, 0);
            intent.putExtra(EXTRA_DATA, battery_level+"%");
        }
        sendBroadcast(intent);
    }

    public void readCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
//        mBluetoothGatt.readCharacteristic(bluetoothGattCharacteristic);
//        byte[] messageBytes = bluetoothGattCharacteristic.getValue();
//        Log.i("Tag","Read: " + StringUtils.byteArrayInHexFormat(messageBytes));
//        String message = StringUtils.stringFromBytes(messageBytes);
//        if (message == null) {
//            Log.e("TAG","Unable to convert bytes to string");
//            return;
//        }
//
//        Log.i("Tag","Received message: " + message);
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Toast.makeText(getApplicationContext(), "BluetoothAdapter not initialised", Toast.LENGTH_SHORT).show();
            return;
        }
        mBluetoothGatt.readCharacteristic(bluetoothGattCharacteristic);
    }

    public void writeCharacteristics(BluetoothGattCharacteristic characteristics, byte[] value){
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Toast.makeText(getApplicationContext(), "BluetoothAdapter not initialised", Toast.LENGTH_SHORT).show();
            return;
        }

//        String value1 = StringUtils.stringFromBytes(value);
////                intToByteArray(Data);
//
//        BluetoothGattService mCustomService = mBluetoothGatt.getService(serviceUuid);
//        if(mCustomService == null){
//            Log.w(TAG, "Custom BLE Service not found");
//            return;
//        }
//        /*get the read characteristic from the service*/
//        BluetoothGattCharacteristic characteristic = mCustomService.getCharacteristic(charUuid);
        characteristics.setValue(value);
        mBluetoothGatt.writeCharacteristic(characteristics);
    }
    public void setCharacteristicNotification( BluetoothGattCharacteristic characteristic, boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Toast.makeText(getApplicationContext(), "BluetoothAdapter not initialised", Toast.LENGTH_SHORT).show();
            return;
        }

        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(BLEGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
        if (enabled) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        }
        else {
            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        mBluetoothGatt.writeDescriptor(descriptor);
    }

    public void setCharacteristickIndication(BluetoothGattCharacteristic characteristic, Boolean enabled) {
        if (mBluetoothAdapter == null || mBluetoothGatt == null) {
            Toast.makeText(getApplicationContext(), "BluetoothAdapter not initialised", Toast.LENGTH_SHORT).show();
            return;
        }
        mBluetoothGatt.setCharacteristicNotification(characteristic, enabled);

        BluetoothGattDescriptor descriptor = characteristic.getDescriptor(UUID.fromString(BLEGattAttributes.CLIENT_CHARACTERISTIC_CONFIG));
        if (enabled) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        }
        else {
            descriptor.setValue(BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        }
        mBluetoothGatt.writeDescriptor(descriptor);

    }

    public List<BluetoothGattService> getSupportedGattServices() {
        if (mBluetoothGatt == null) return null;

        return mBluetoothGatt.getServices();
    }

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

    public static BluetoothAdapter getBluetoothAdapter(Context context) {
        BluetoothManager mBluetoothManager = (BluetoothManager) context.getSystemService(context.BLUETOOTH_SERVICE);
        return mBluetoothManager.getAdapter();
    }

    public boolean initialize() {
        mBluetoothAdapter = getBluetoothAdapter(this);
        return true;
    }

    public class LocalBinder extends Binder {
        BLEService getService() {
            return BLEService.this;
        }
    }
}
