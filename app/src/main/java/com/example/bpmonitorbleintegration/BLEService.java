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
import android.icu.text.UFormat;
import android.os.Binder;
import android.os.Build;

import android.os.IBinder;
import android.util.Log;
import android.util.SparseArray;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class BLEService extends Service {

    private final static String TAG = BLEService.class.getSimpleName();
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;

    private static int mConnectionState = Constants.STATE_DISCONNECTED;

    private final IBinder mBinder = new LocalBinder();
    private String bluetoothAddress;
    public final static UUID UUID_CHAR_LEVEL = UUID.fromString(BLEGattAttributes.CLIENT_CHARACTERISTIC_CONFIG);
    public final static UUID UUI_SERVICE_LEVEL = UUID.fromString(BLEGattAttributes.CLIENT_SERVICE_CONFIG);

    @RequiresApi(api = Build.VERSION_CODES.O)
    public boolean connect(String address) {
        if (mBluetoothAdapter == null || address == null) {
//            Log.w("TAG", "BluetoothAdapter not initialize or unspecified address");
            Toast.makeText(getApplicationContext(), "BluetoothAdapter not initialize or unspecified address", Toast.LENGTH_SHORT).show();
            return false;
        }
        bluetoothGattCallback.onConnectionStateChange(mBluetoothGatt, BluetoothGatt.GATT_SUCCESS, BluetoothProfile.STATE_CONNECTING);
        if (mBluetoothAdapter != null && address.equals(bluetoothAddress) && mBluetoothGatt != null) {
//            Log.d(TAG, "Try to use existing connection");
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
//            Log.w(TAG, "Device not found");
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
                    gatt.close();
                    break;

                default:
//                    Toast.makeText(getApplicationContext(), "Connecting...", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            super.onMtuChanged(gatt, mtu, status);
            if (status == BluetoothGatt.GATT_SUCCESS){
                        gatt.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                broadcastUpdate(Constants.ACTION_GATT_SERVICES_DISCOVERED);
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
            byte[] messageBytes = characteristic.getValue();
            String messageString = null;
            try {
                messageString = new String(messageBytes, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Unable to convert message bytes to string");
            }
            broadcastUpdate(Constants.ACTION_DATA_AVAILABLE, characteristic);
        }
    };


    public void broadcastUpdate(final String action) {
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }
    public void broadcastUpdate(final String action, final BluetoothGattCharacteristic characteristic) {
        final Intent intent = new Intent(action);

        if (UUID_CHAR_LEVEL.equals(characteristic.getUuid())) {
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data) {
                    Log.i(TAG, "byte Array " + byteChar);
                    stringBuilder.append(String.format("%02X ", byteChar));
                }
                intent.putExtra(Constants.EXTRA_DATA, new String(data) + "\n" +stringBuilder.toString());
            }
        }

//        if (UUID_CHAR_LEVEL.equals(characteristic.getUuid())) {
//            final byte[] data = characteristic.getValue();
//            if (data != null && data.length > 0) {
//                final SparseArray<byte[]> parsed = parseAdvertisingData(data);
//                for (int i = 0; i < parsed.size(); i++) {
//                    final int type = parsed.keyAt(i);
//                    final byte[] data1 = parsed.valueAt(i);
//                    final StringBuilder stringBuilder = new StringBuilder(data1.length);
//                    for (byte byteChar : data1) {
//                        stringBuilder.append(String.format("%02X ", byteChar));
//                    }
//                    intent.putExtra(Constants.EXTRA_DATA, new String(data) + "\n" + stringBuilder.toString());
//                }
//            }
//        }
        sendBroadcast(intent);
    }

    public SparseArray<byte[]> parseAdvertisingData(byte[] rawData) {
        final SparseArray<byte[]> parsedData = new SparseArray<>();
        for (int index = 0; index < rawData.length; ) {
            final byte dataLength = rawData[index++];
            if (dataLength == 0) {
                break;
            }

            final int dataType = rawData[index];
            if (dataType == 0) {
                break;
            }

//            byte[] data1 = Arrays.copyOf(rawData, dataLength);
            byte[] data = Arrays.copyOfRange(rawData, index + 1, index + dataLength);

            parsedData.put(dataType, data);

            index += dataLength;
        }
        return parsedData;
    }

    public BLEService()
    {

    }

    public void readCharacteristic(BluetoothGattCharacteristic bluetoothGattCharacteristic) {
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
        BluetoothManager mBluetoothManager = (BluetoothManager) context.getSystemService(BLUETOOTH_SERVICE);
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
