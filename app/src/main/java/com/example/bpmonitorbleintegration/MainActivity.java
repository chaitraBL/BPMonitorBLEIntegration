package com.example.bpmonitorbleintegration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
//import org.apache.commons.codec.binary.Hex;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


@TargetApi(21)
public class MainActivity extends AppCompatActivity {

    List<BluetoothDevice> listBluetoothDevice;
    ArrayAdapter<BluetoothDevice> adapterBluetoothDevice;
    ArrayList<BluetoothDevice> foundDevices;
    ListAdapter adapterLeScanResult;
    private BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 100000;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    private static final int PERMISSIONS_REQUEST_LOCATION = 101;
    ListView listView;
    TextView receivedMsg;
//    EditText sentMsg;
//    Button sendBtn;

    int[] pressure;
    int[] pulse;

    String receivedData;
    private boolean mConnected;
    private static final String TAG = "BluetoothLEService";
    TextView statusText;
    private BLEService mBLEService;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    BluetoothDevice bluetoothDevice;
    IntentFilter intentFilter;
    BluetoothManager bluetoothManager;
    Spinner spin;
    String item;
    String[] formatName = {"Text", "ByteArray"};

    AlertDialog.Builder builder;

    private static final char[] HEX_ARRAY = {'0', '1', '2', '3', '4', '5', '6',
            '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

    private static final String HEXES = "0123456789ABCDEF";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Check if BLE is supported on the device.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this,
                    "BLUETOOTH_LE not supported in this device!",
                    Toast.LENGTH_LONG).show();
//            finish();
        }

        listView = findViewById(R.id.lelist);
        statusText = findViewById(R.id.actual_status);
        statusText.setText("Disconnected");

//        spin = findViewById(R.id.spinner);
//        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, formatName);
//        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//        spin.setAdapter(dataAdapter);

//        sendBtn = findViewById(R.id.btn_send);
        receivedMsg = findViewById(R.id.txt_receivedMsg);
//        sentMsg = findViewById(R.id.ed_message);
//        // Set the length to 8 characters
//        sentMsg.setFilters(new InputFilter[] { new InputFilter.LengthFilter(8) });
//
//        // Set the keypad to numeric
//        sentMsg.setInputType(InputType.TYPE_CLASS_NUMBER);
//
//        // Only allow the user to enter 0 and 1 numbers
//        sentMsg.setKeyListener(DigitsKeyListener.getInstance("01"));
        listBluetoothDevice = new ArrayList<BluetoothDevice>();
        adapterLeScanResult = new ArrayAdapter<BluetoothDevice>(this, android.R.layout.simple_list_item_1, listBluetoothDevice);
        listView.setAdapter(adapterLeScanResult);

//        adapterBluetoothDevice = new ArrayAdapter<BluetoothDevice>(this, android.R.layout.activity_list_item, R.id.textView, foundDevices);
//        listView.setAdapter(adapterBluetoothDevice);

        listView.setOnItemClickListener(scanResultOnItemClickListener);
        checkPermissions(MainActivity.this, this);
        mHandler = new Handler();

        bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        intentFilter = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
        intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver(broadCastReceiver, intentFilter);

//        sendBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                String message = sentMsg.getText().toString();
////                if (item.equals("Text")) {
////                    mNotifyCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
////                    if (mNotifyCharacteristic != null) {
////                        Log.i("TAG", "While choosing text ");
////                        mBLEService.writeCharacteristics(mNotifyCharacteristic, message);
////                    }
////                }
////                else if (item.equals("ByteArray")) {
//                    mNotifyCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
//                    if (mNotifyCharacteristic != null) {
//                    byte[] value1 = {0x7B,0x00,0x00,0x01};
////                        byte[] value = StringUtils.toBytes(sentMsg.getText());
////                        Log.i("TAG", "raw data " + new BigInteger(1, value1).toString(16));
////                        String convertedResult = new BigInteger(1, value1).toString(16);
////                        Log.i("TAG", "While choosing byte ");
////                        Log.i("TAG", "res " + convertedResult);
//                        mBLEService.writeCharacteristics(mNotifyCharacteristic, value1);
////                    }
//                }
//
//            }
//        });
    }

    AdapterView.OnItemClickListener scanResultOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            final BluetoothDevice device = (BluetoothDevice) adapterView.getItemAtPosition(i);


            String msg = device.getAddress() + "\n"
                    + device.getBluetoothClass().toString() + "\n"
                    + getBTDevieType(device);

            new AlertDialog.Builder(MainActivity.this)
                    .setTitle(device.getName())
                    .setMessage(msg)
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                                Boolean result = mBLEService.connect(device.getAddress());

//                                Intent intent = new Intent(MainActivity.this, DataTransferActivity.class);
//                                intent.putExtra("Characteristics", mNotifyCharacteristic);
//                                startActivity(intent);
                            }
                        }
                    })
                    .show();
        }
    };

    private String getBTDevieType(BluetoothDevice d) {
        String type = "";

        switch (d.getType()) {
            case BluetoothDevice.DEVICE_TYPE_CLASSIC:
                type = "DEVICE_TYPE_CLASSIC";
                break;
            case BluetoothDevice.DEVICE_TYPE_DUAL:
                type = "DEVICE_TYPE_DUAL";
                break;
            case BluetoothDevice.DEVICE_TYPE_LE:
                type = "DEVICE_TYPE_LE";
                break;
            case BluetoothDevice.DEVICE_TYPE_UNKNOWN:
                type = "DEVICE_TYPE_UNKNOWN";
                break;
            default:
                type = "unknown...";
        }
        return type;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu_file, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bluetooth_searching:
                scanLeDevice(true);
                return true;

            case R.id.start_icon:
                byte[] value = {0x7B,0x00,0x00,0x00,0x01,0x10,0x0A,0x00,0x01,0x00,0x1C,0x7D};
                if (mNotifyCharacteristic != null) {
                    mBLEService.writeCharacteristics(mNotifyCharacteristic, value);
                }
                Log.i("TAG", "received in alert " + receivedData);
                builder = new AlertDialog.Builder(this);
                LayoutInflater layoutInflater = getLayoutInflater();

                //this is custom dialog
                //custom_popup_dialog contains textview only
                View customView = layoutInflater.inflate(R.layout.custom_popup_dialog, null);
                // reference the textview of custom_popup_dialog
                TextView tv = customView.findViewById(R.id.tvpopup);

                tv.setText(receivedData);
                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (mNotifyCharacteristic != null) {
                            mBLEService.writeCharacteristics(mNotifyCharacteristic, Constants.startValue);
                        }
                    }
                });
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (mNotifyCharacteristic != null) {
                            mBLEService.writeCharacteristics(mNotifyCharacteristic, Constants.startValue);
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
                return true;

                default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
                filters = new ArrayList<ScanFilter>();
            }
            registerReceiver(broadCastReceiver, GattUpdateIntentFilter());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            scanLeDevice(false);
            unregisterReceiver(broadCastReceiver);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mGatt == null) {
            return;
        }
        mGatt.close();
        mGatt = null;

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_CANCELED) {
                //Bluetooth not enabled.
                Toast.makeText(getApplicationContext(), "Bluetooth not enabled", Toast.LENGTH_SHORT).show();
//                finish();
                return;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void scanLeDevice(final boolean enable) {
        if (enable) {
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if (Build.VERSION.SDK_INT < 21) {
                        mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    } else {
                        mLEScanner.stopScan(mScanCallback);
                    }
                }
            }, SCAN_PERIOD);
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.startLeScan(mLeScanCallback);
            } else {
                mLEScanner.startScan(filters, settings, mScanCallback);

            }
        } else {
            if (Build.VERSION.SDK_INT < 21) {
                mBluetoothAdapter.stopLeScan(mLeScanCallback);
            } else {
                mLEScanner.stopScan(mScanCallback);
            }
        }
    }

    private final ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            bluetoothDevice = result.getDevice();
            Intent getServiceIntent = new Intent(MainActivity.this, BLEService.class);
            bindService(getServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
            addBluetoothDevice(bluetoothDevice);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
//                Log.i("TAG", "scan data" + sr.getDevice());
                addBluetoothDevice(sr.getDevice());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Toast.makeText(getApplicationContext(), "error code" + errorCode, Toast.LENGTH_SHORT).show();
        }

        private void addBluetoothDevice(BluetoothDevice device) {
            if (!listBluetoothDevice.contains(device)) {
                listBluetoothDevice.add(device);
                listView.invalidateViews();
            }
        }
    };

    private final BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                            Log.i("onLeScan", device.toString());
                        }
                    });
                }
            };

    private static IntentFilter GattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Constants.ACTION_GATT_CONNECTED);
        intentFilter.addAction(Constants.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(Constants.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(Constants.ACTION_DATA_AVAILABLE);
        return intentFilter;
    }

    public void checkPermissions(Activity activity, Context context) {
        if (ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_LOCATION);
        }

    }

    private final BroadcastReceiver broadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (Constants.ACTION_GATT_CONNECTED.equals(action)) {

                mConnected = true;
                updateConnectionState("Connected");
                Toast.makeText(getApplicationContext(), "Connected in broadcast receiver", Toast.LENGTH_SHORT).show();
            }
            else if (Constants.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState("Disconnected");
            }
            else if (Constants.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                List<BluetoothGattService> gattService = mBLEService.getSupportedGattServices();
                Log.i("TAG", "Size " + gattService.size());
                for (int i = 0; i < gattService.size(); i++) {
                    Log.i("TAG", "looped values " + gattService.get(i));
                    BluetoothGattService service = gattService.get(4);
                    Log.i("Tag", "Services found " + gattService.get(i).getUuid().toString());
                    if (BLEGattAttributes.lookup(service.getUuid().toString()).matches("Service")) {
                        for (BluetoothGattCharacteristic gattCharacteristic : mBLEService.getSupportedGattCharacteristics(service)) {
                            Log.i("Tag", "Character found " + gattCharacteristic.getUuid().toString());
                            if (((gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) || ((gattCharacteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0)) {
                                Log.i("TAG", "Characteristics in broadcast " + gattCharacteristic);
                                mBLEService.setCharacteristicNotification(gattCharacteristic, true);
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

            }
            else if (Constants.ACTION_DATA_AVAILABLE.equals(action)) {
                receivedData = intent.getStringExtra(Constants.EXTRA_DATA);
                displayData(intent.getStringExtra(Constants.EXTRA_DATA));
//                final String rawData = intent.getStringExtra(Constants.EXTRA_DATA);
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
            mBLEService = ((BLEService.LocalBinder) service).getService();

            if (!mBLEService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
//                finish();
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                mBLEService.connect(bluetoothDevice.getAddress());
            }
        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBLEService = null;
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