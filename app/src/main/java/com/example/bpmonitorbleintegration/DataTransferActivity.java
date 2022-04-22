package com.example.bpmonitorbleintegration;

import static com.example.bpmonitorbleintegration.R.layout.activity_data_transfer;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class DataTransferActivity extends AppCompatActivity {

    ImageButton startBtn;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private BLEService mBluetoothLeService;
    private String deviceAddress;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    private boolean mConnected;
    BluetoothDevice bluetoothDevice;
    IntentFilter intentFilter;
    AlertDialog.Builder builder;
    String receivedData;
    TextView receivedMsg, statusText;
    private String TAG = "DataTransferActivity";
    MainActivity mainActivity;
    Intent intent;
    Handler mHandler;
    SharedPreferences pref;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_data_transfer);

        startBtn = findViewById(R.id.btn_read);
        statusText = findViewById(R.id.actual_status);
        receivedMsg = findViewById(R.id.receivedMsg);
        deviceAddress = getIntent().getStringExtra("Device");

        intentFilter = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
        intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver(broadCastReceiver, intentFilter);

        Intent getServiceIntent = new Intent(DataTransferActivity.this, BLEService.class);
        bindService(getServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        pref = PreferenceManager.getDefaultSharedPreferences(this);
        //Send request to START the readings.
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mNotifyCharacteristic != null) {

//                     byte[] startValue = {0x7B,0x04,0x16,0x00,0x01,0x01,0x09,0x01,0x00,0x39,0x7D};
                    mBluetoothLeService.writeCharacteristics(mNotifyCharacteristic, Constants.startValue);
//                    mBluetoothLeService.writeCharacteristics(mNotifyCharacteristic, startValue);
                }

//                SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(this);
//                int cuff = pref.getInt("Cuff",-1);
//                int pressure = pref.getInt("Pressure", -1);
                String cuff = pref.getString("Cuff","");
                String pressure = pref.getString("Pressure","");
                Log.i(TAG,"cuff " + cuff);
                Log.i(TAG, "Pressure " + pressure);

                //Alert controller.
                builder = new AlertDialog.Builder(DataTransferActivity.this);
                builder.setTitle("Raw Readings");
                LayoutInflater layoutInflater = getLayoutInflater();

                //this is custom dialog
                //custom_popup_dialog contains textview only
                View customView = layoutInflater.inflate(R.layout.custom_popup_dialog, null);
                // reference the textview of custom_popup_dialog
                TextView tv = customView.findViewById(R.id.tvpopup);

                TextView tv1 = customView.findViewById(R.id.tvpopup1);
                tv.setText(cuff);
                tv1.setText(pressure);
                //Send request to force STOP the readings.
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (mNotifyCharacteristic != null) {
                            mBluetoothLeService.writeCharacteristics(mNotifyCharacteristic, Constants.startValue);
                        }
                    }
                });
                //Send Ack - received readings.
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (mNotifyCharacteristic != null) {
                            mBluetoothLeService.writeCharacteristics(mNotifyCharacteristic, Constants.startValue);
                        }
                    }
                });

                builder.setView(customView);
                AlertDialog dialog = builder.create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
//                        if (mNotifyCharacteristic.getValue() == ) {
//
//                        }
                        ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    }
                });
                dialog.show();
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Connect to the device through BLE.
            registerReceiver(broadCastReceiver, GattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                boolean result = mBluetoothLeService.connect(deviceAddress);
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadCastReceiver);
    }

    private static IntentFilter GattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_GATT_CONNECTED);
        intentFilter.addAction(Constants.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(Constants.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(Constants.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    private final BroadcastReceiver broadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Constants.ACTION_GATT_CONNECTED.equals(action)) {

                mConnected = true;
                updateConnectionState("Connected");
            }
            else if (Constants.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState("Disconnected");
            }
            else if (Constants.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                List<BluetoothGattService> gattService = mBluetoothLeService.getSupportedGattServices();
                Log.i("TAG", "Size " + gattService.size());
                for (int i = 0; i < gattService.size(); i++) {
                    BluetoothGattService service = gattService.get(2);
                    Log.i("Tag", "Services found " + gattService.get(i).getUuid().toString());
                    if (BLEGattAttributes.lookup(service.getUuid().toString()).matches("Service")) {
                        for (BluetoothGattCharacteristic gattCharacteristic : mBluetoothLeService.getSupportedGattCharacteristics(service)) {
                            Log.i("Tag", "Character found " + gattCharacteristic.getUuid().toString());
                            if (((gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) || ((gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0)) {
                                mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, true);
                                try {
                                    Thread.sleep(350);
                                } catch (InterruptedException e) {
                                }
                            }
                            if (BLEGattAttributes.lookup(gattCharacteristic.getUuid().toString()).matches("Character Level")) {
                                mNotifyCharacteristic = gattCharacteristic;
                            }
                        }
                    }

                }

//                for (BluetoothGattService gattService : mBluetoothLeService.getSupportedGattServices()) {
//                    Log.i(TAG, "Service found " + gattService.getUuid().toString());
//                    if (BLEGattAttributes.lookup(gattService.getUuid().toString()).equals("0000ffe0-0000-1000-8000-00805f9b34fb")) {
//                        for (BluetoothGattCharacteristic gattCharacteristic : mBluetoothLeService.getSupportedGattCharacteristics(gattService)) {
//                            Log.i(TAG, "Character found " + gattCharacteristic.getUuid().toString());
//                            if (((gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) || ((gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0)) {
//                                mBluetoothLeService.setCharacteristicNotification(gattCharacteristic, true);
//                                try {
//                                    Thread.sleep(350);
//                                } catch (InterruptedException e) {
//                                }
//                            }
//                            if (BLEGattAttributes.lookup(gattCharacteristic.getUuid().toString()).matches("Character Level")) {
//                                mNotifyCharacteristic = gattCharacteristic;
//                            }
//                        }
//                    }
//                }

            }

            else if (Constants.ACTION_DATA_AVAILABLE.equals(action)) {
                receivedData = intent.getStringExtra(Constants.EXTRA_DATA);
                displayData(intent.getStringExtra(Constants.EXTRA_DATA));
            }
        }
    };

    private  void displayData(String data) {
        if (data != null) {
           receivedMsg.setText(data);
        }

    }

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BLEService.LocalBinder) service).getService();

            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
//                finish();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mBluetoothLeService.connect(deviceAddress);
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    private void updateConnectionState(final String status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusText.setText(status);
            }
        });
    }
}