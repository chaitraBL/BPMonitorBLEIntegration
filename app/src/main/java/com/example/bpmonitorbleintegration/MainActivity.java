package com.example.bpmonitorbleintegration;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


@TargetApi(21)
public class MainActivity extends AppCompatActivity {

    //    private static final int ACCESS_COARSE_LOCATION_REQUEST = 0;
//    private BluetoothAdapter mBluetoothAdapter;
//    private static final int RQ_ENABLE_BLUETOOTH = 1;
//
//    private BluetoothLeScanner bluetoothLeScanner;
//    private boolean scanning;
//    private Handler handler = new Handler();
////    private LeDeviceListAdapter leDeviceListAdapter = new LeDeviceListAdapter();
//
//    // Stops scanning after 10 seconds.
//    private static final long SCAN_PERIOD = 10000;
//    ListView listViewLE;
//

//    private final int LOCATION_PERMISSION_REQUEST = 101;
//    private Context context;
//
//
//    private boolean mScanning;
    List<BluetoothDevice> listBluetoothDevice;
    ArrayAdapter<String> adapterBluetoothDevice;
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
    String BLE_PIN = "1234";

    private static final String TAG = "BluetoothLEService";
    private static final int STATE_DISCONNECT = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
//    IBinder mBinder = new LocalBinder();
    private int mConnectionState = STATE_DISCONNECT;
    private BluetoothGatt mBluetoothGatt;
    private String bluetoothAddress;
    private boolean mConnected = false;
    private Context context;
    TextView statusText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        hasPermissions();
//
//        // Check if BLE is supported on the device.
//        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
//            Toast.makeText(this,
//                    "BLUETOOTH_LE not supported in this device!",
//                    Toast.LENGTH_LONG).show();
////            finish();
//        }
//
//        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
////
////        bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
//
//        listViewLE = findViewById(R.id.lelist);
//
//        listBluetoothDevice = new ArrayList<>();
//        adapterLeScanResult = new ArrayAdapter<BluetoothDevice>(this, android.R.layout.simple_list_item_1, listBluetoothDevice);
//        listViewLE.setAdapter(adapterLeScanResult);
////        listViewLE.setOnItemClickListener(scanResultOnItemClickListener);
//
//        handler = new Handler();

        listView = findViewById(R.id.lelist);
        statusText = findViewById(R.id.actual_status);
        statusText.setText("Disconnected");
        listBluetoothDevice = new ArrayList<>();
//        adapterBluetoothDevice = new ArrayAdapter<String>(context, R.layout.list_item);
        adapterLeScanResult = new ArrayAdapter<BluetoothDevice>(this, android.R.layout.simple_list_item_1, listBluetoothDevice);
        listView.setAdapter(adapterLeScanResult);
        listView.setOnItemClickListener(scanResultOnItemClickListener);
        checkPermissions(MainActivity.this, this);
        mHandler = new Handler();
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "BLE Not Supported",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();


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
//                            connectToDevice(device);
                            IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
                            intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
                            registerReceiver(broadCastReceiver, intentFilter);

                             Boolean result = connect(device.getAddress());
                            Toast.makeText(getApplicationContext(), "request result" + result, Toast.LENGTH_SHORT).show();
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
            case R.id.bluetooth_button:
                enableBluetooth();
                return true;
//            case R.id.bluetooth_searching:
////                scanLeDevice();
//                scanLeDevice(true);
////                checkPermissions();
//                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
//        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                mLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
                filters = new ArrayList<ScanFilter>();
            }
            scanLeDevice(true);
//        }
    }

    public void enableBluetooth() {
        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            Toast.makeText(context, "Bluetooth Enabled", Toast.LENGTH_SHORT).show();
        }
        else {
            mBluetoothAdapter.disable();
            Toast.makeText(context, "Bluetooth Disabled", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            scanLeDevice(false);
        }
    }

    @Override
    protected void onDestroy() {
        if (mGatt == null) {
            return;
        }
        mGatt.close();
        mGatt = null;
        super.onDestroy();
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

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            Log.i("callbackType", String.valueOf(callbackType));
            Log.i("result", result.toString());
//            Toast.makeText(getApplicationContext(), "Scan Result" + result.toString(), Toast.LENGTH_SHORT).show();
            BluetoothDevice btDevice = result.getDevice();
            addBluetoothDevice(btDevice);
//            connectToDevice(btDevice);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
//                Log.i("ScanResult - Results", sr.toString());
//                Log.i("ScanDevice - Results", sr.getDevice().getAddress());
//                Toast.makeText(getApplicationContext(), "Scan Result " + sr.getDevice().getAddress() , Toast.LENGTH_SHORT).show();
                addBluetoothDevice(sr.getDevice());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
//            Log.e("Scan Failed", "Error Code: " + errorCode);
            Toast.makeText(getApplicationContext(), "error code" + errorCode, Toast.LENGTH_SHORT).show();
        }

        private void addBluetoothDevice(BluetoothDevice device) {
            if (!listBluetoothDevice.contains(device)) {
//                adapterBluetoothDevice.add(device.getAddress() + "\n" + device.getName());
                listBluetoothDevice.add(device);
                listView.invalidateViews();
            }
        }
    };

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.i("onLeScan", device.toString());
//                            connectToDevice(device);
                        }
                    });
                }
            };

    public boolean connect(String address) {
        if (mBluetoothAdapter == null || address == null) {
//            Log.w("TAG", "BluetoothAdapter not initialize or unspecified address");
            Toast.makeText(getApplicationContext(), "BluetoothAdapter not initialize or unspecified address", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (mBluetoothAdapter != null && address.equals(bluetoothAddress) && mBluetoothGatt != null) {
//            Log.d(TAG, "Try to use existing connection");
            Toast.makeText(getApplicationContext(), "Try to use existing connection", Toast.LENGTH_SHORT).show();
            if (mBluetoothGatt.connect()) {
                mConnectionState = STATE_CONNECTING;
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
        mBluetoothGatt = bluetoothDevice.connectGatt(this, false, gattCallback);
        bluetoothAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
//                    Log.i("gattCallback", "STATE_CONNECTED");
                    mConnectionState = STATE_CONNECTED;
                    gatt.discoverServices();
                    statusText.setText("Connected");
                    Toast.makeText(getApplicationContext(), "Connected...", Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
//                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    statusText.setText("Disconnected");
                    Toast.makeText(getApplicationContext(), "Disconnected...", Toast.LENGTH_SHORT).show();
                    mConnectionState = STATE_DISCONNECT;

                    break;
                default:
//                    Log.e("gattCallback", "STATE_OTHER");
                    Toast.makeText(getApplicationContext(), "Other state...", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
//            Log.i("onServicesDiscovered", services.toString());
            gatt.readCharacteristic(services.get(1).getCharacteristics().get
                    (0));
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic
                                                 characteristic, int status) {
            Log.i("onCharacteristicRead", characteristic.toString());
            gatt.disconnect();
        }
    };

    public void checkPermissions(Activity activity, Context context) {
        if (ContextCompat.checkSelfPermission(context,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        } else {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_LOCATION);
        }

    }

    private BroadcastReceiver broadCastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (BluetoothDevice.ACTION_PAIRING_REQUEST.equals(action)) {
                BluetoothDevice bluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                bluetoothDevice.setPin(BLE_PIN.getBytes());
//                Log.e("Pin TAG","Auto-entering pin: " + BLE_PIN);
//                Toast.makeText(getApplicationContext(), "Auto-entering pin:" + BLE_PIN, Toast.LENGTH_SHORT).show();
                bluetoothDevice.createBond();
//                Log.e("Pin TAG", "pin entered and request sent...");
                Toast.makeText(getApplicationContext(), "pin entered and request sent...", Toast.LENGTH_SHORT).show();
            }
        }
    };
}