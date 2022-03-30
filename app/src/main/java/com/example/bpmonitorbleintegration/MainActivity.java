package com.example.bpmonitorbleintegration;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.ListAdapter;
import android.widget.ListView;
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
//    List<BluetoothDevice> listBluetoothDevice;
//    ListAdapter adapterLeScanResult;
//    private final int LOCATION_PERMISSION_REQUEST = 101;
//    private Context context;
//
//
//    private boolean mScanning;

    private BluetoothAdapter mBluetoothAdapter;
    private int REQUEST_ENABLE_BT = 1;
    private Handler mHandler;
    private static final long SCAN_PERIOD = 100000;
    private BluetoothLeScanner mLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private BluetoothGatt mGatt;
    private final int PERMISSIONS_REQUEST_LOCATION = 101;

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu_file, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bluetooth_button:
//                enableBluetooth();
                return true;
            case R.id.bluetooth_searching:
//                scanLeDevice();
                scanLeDevice(true);
//                checkPermissions();
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
            scanLeDevice(true);
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
            connectToDevice(btDevice);
        }
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult sr : results) {
                Log.i("ScanResult - Results", sr.toString());
                Log.i("ScanDevice - Results", sr.getDevice().getAddress());
                Toast.makeText(getApplicationContext(), "Scan Result" + sr.toString() , Toast.LENGTH_SHORT).show();
            }
        }
        @Override
        public void onScanFailed(int errorCode) {
            Log.e("Scan Failed", "Error Code: " + errorCode);
            Toast.makeText(getApplicationContext(), "error code" + errorCode, Toast.LENGTH_SHORT).show();
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
                            connectToDevice(device);
                        }
                    });
                }
            };
    public void connectToDevice(BluetoothDevice device) {
        if (mGatt == null) {
            mGatt = device.connectGatt(this, false, gattCallback);
            scanLeDevice(false);// will stop after first device detection
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
                    Log.i("gattCallback", "STATE_CONNECTED");
                    gatt.discoverServices();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    break;
                default:
                    Log.e("gattCallback", "STATE_OTHER");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            List<BluetoothGattService> services = gatt.getServices();
            Log.i("onServicesDiscovered", services.toString());
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

    public static void checkPermissions(Activity activity, Context context){
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.INTERNET,
        };

        if(!hasPermissions(context, PERMISSIONS)){
            ActivityCompat.requestPermissions( activity, PERMISSIONS, PERMISSION_ALL);
        }
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

//    public void checkPermission()
//    {
//        if (ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION)
//                != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(
//                    this,
//                    new String[]{ACCESS_COARSE_LOCATION},
//                    PERMISSIONS_REQUEST_LOCATION);
//        }
//    }

//    public void enableBluetooth()
//    {
//        // Checks if Bluetooth is supported on the device.
//        if (mBluetoothAdapter == null) {
//            Toast.makeText(this,
//                    "Bluetooth is not supported in this device",
//                    Toast.LENGTH_LONG).show();
////            finish();
////            return;
//        }
//        else {
//            if (!mBluetoothAdapter.isEnabled()) {
//                Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//                startActivityForResult(enableIntent, RQ_ENABLE_BLUETOOTH);
//                Toast.makeText(getApplicationContext(), "Bluetooth Enabled", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(getApplicationContext(), "Bluetooth already enabled", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }

//    private void scanLeDevice() {
//        if (!scanning) {
//            // Stops scanning after a predefined scan period.
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    scanning = false;
//                    bluetoothLeScanner.stopScan(leScanCallback);
//                    Toast.makeText(getApplicationContext(), "Not Scanning...", Toast.LENGTH_SHORT).show();
//                }
//            }, SCAN_PERIOD);
//
//            bluetoothLeScanner.startScan(leScanCallback);
//
//            //scan specified devices only with ScanFilter
//            ScanFilter scanFilter =
//                    new ScanFilter.Builder()
//                            .setServiceUuid(BLEService.ParcelUuid_GENUINO101_ledService)
//                            .build();
//            List<ScanFilter> scanFilters = new ArrayList<ScanFilter>();
//            scanFilters.add(scanFilter);
//
//            ScanSettings scanSettings =
//                    new ScanSettings.Builder().build();
//
//            scanning = true;
//            bluetoothLeScanner.startScan(scanFilters,scanSettings,leScanCallback);
//            Toast.makeText(getApplicationContext(), "Scanning...", Toast.LENGTH_SHORT).show();
//        } else {
//            scanning = false;
//            bluetoothLeScanner.stopScan(leScanCallback);
//            Toast.makeText(getApplicationContext(), "Stop Scanning...", Toast.LENGTH_SHORT).show();
//        }
//    }

//    // Device scan callback.
//    private ScanCallback leScanCallback = new ScanCallback() {
//        @Override
//        public void onScanResult(int callbackType, ScanResult result) {
//            super.onScanResult(callbackType, result);
//
//            addBluetoothDevice(result.getDevice());
//        }
//
//        @Override
//        public void onBatchScanResults(List<ScanResult> results) {
//            super.onBatchScanResults(results);
//            for(ScanResult result : results){
//                addBluetoothDevice(result.getDevice());
//            }
//        }
//
//        @Override
//        public void onScanFailed(int errorCode) {
//            super.onScanFailed(errorCode);
//            Toast.makeText(MainActivity.this,
//                    "onScanFailed: " + String.valueOf(errorCode),
//                    Toast.LENGTH_LONG).show();
//        }
//
//        private void addBluetoothDevice(BluetoothDevice device){
//            if(!listBluetoothDevice.contains(device)){
//                listBluetoothDevice.add(device);
//                listViewLE.invalidateViews();
//            }
//        }
//    };

//    private void scanLeDevice() {
//        if (!scanning){
//            handler.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    mScanning = false;
//                    mBluetoothAdapter.getBluetoothLeScanner().stopScan(mLeScanCallback);
//                    Toast.makeText(getApplicationContext(), "Cannot scan...", Toast.LENGTH_SHORT).show();
//                }
//            }, SCAN_PERIOD);
//
//            mScanning = true;
//            mBluetoothAdapter.getBluetoothLeScanner().startScan(mLeScanCallback);
//            Toast.makeText(getApplicationContext(), "Scanning...", Toast.LENGTH_SHORT).show();
//        }
//        else {
//            mScanning = false;
//            mBluetoothAdapter.getBluetoothLeScanner().stopScan(mLeScanCallback);
//            Toast.makeText(getApplicationContext(), "Stop Scanning...", Toast.LENGTH_SHORT).show();
//        }
//
//    }
//
//    private ScanCallback mLeScanCallback = new ScanCallback() {
//        @Override
//        public void onScanResult(int callbackType, ScanResult result) {
//            super.onScanResult(callbackType, result);
//        }
//
//        @Override
//        public void onBatchScanResults(List<ScanResult> results) {
//            super.onBatchScanResults(results);
//            for(ScanResult result : results){
//                Toast.makeText(getApplicationContext(), "Device" + result.getDevice().getName(), Toast.LENGTH_SHORT).show();
//            }
//        }
//
//        @Override
//        public void onScanFailed(int errorCode) {
//            super.onScanFailed(errorCode);
//            Toast.makeText(MainActivity.this,
//                    "onScanFailed: " + String.valueOf(errorCode),
//                    Toast.LENGTH_LONG).show();
//        }
//    };
//
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
//
//        if (requestCode == RQ_ENABLE_BLUETOOTH && resultCode == Activity.RESULT_CANCELED) {
//            finish();
//            return;
//        }
//
//        getBluetoothAdapterAndLeScanner();
//
//        // Checks if Bluetooth is supported on the device.
//        if (mBluetoothAdapter == null) {
//            Toast.makeText(this,
//                    "bluetoothManager.getAdapter()==null",
//                    Toast.LENGTH_SHORT).show();
////            finish();
//            return;
//        }
//
//        super.onActivityResult(requestCode, resultCode, data);
//    }
//
//    private void getBluetoothAdapterAndLeScanner() {
//        // Get BluetoothAdapter and BluetoothLeScanner.
//        final BluetoothManager bluetoothManager =
//                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
//        mBluetoothAdapter = bluetoothManager.getAdapter();
//        bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
//
//        mScanning = false;
//    }
//
//    private boolean hasPermissions() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (getApplicationContext().checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                requestPermissions(new String[] { Manifest.permission.ACCESS_COARSE_LOCATION }, ACCESS_COARSE_LOCATION_REQUEST);
//                return false;
//            }
//        }
//        return true;
//    }
//
//    private void checkPermissions() {
//        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST);
//        } else {
////            Intent intent = new Intent(context, MainActivity2.class);
////            startActivityForResult(intent, SELECT_DEVICE);
//            scanLeDevice();
//        }
//    }

}