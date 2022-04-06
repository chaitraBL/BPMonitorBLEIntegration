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
import android.os.ParcelUuid;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


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
    TextView receivedMsg;
    EditText sentMsg;
    Button sendBtn;

    private BluetoothGattService mGattServer;
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
    private BLEService mBLEService;
    private BluetoothGattCharacteristic mNotifyCharacteristic;
    public static UUID serviceUuid = convertFromInteger(0xFFE0);
    public static UUID charUuid = convertFromInteger(0xFFE1);
    BluetoothDevice bluetoothDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

//        hasPermissions();
//
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

        sendBtn = findViewById(R.id.btn_send);
        receivedMsg = findViewById(R.id.txt_receivedMsg);
        sentMsg = findViewById(R.id.ed_message);
        listBluetoothDevice = new ArrayList<>();
//        adapterBluetoothDevice = new ArrayAdapter<String>(context, R.layout.list_item);
        adapterLeScanResult = new ArrayAdapter<BluetoothDevice>(this, android.R.layout.simple_list_item_1, listBluetoothDevice);
        listView.setAdapter(adapterLeScanResult);
        listView.setOnItemClickListener(scanResultOnItemClickListener);
        checkPermissions(MainActivity.this, this);
        mHandler = new Handler();

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String message =  sentMsg.getText().toString();
                Log.i("Message TAG", "sent characteristics: " + mNotifyCharacteristic);
                Toast.makeText(getApplicationContext(),"Sent Characteristics" + mNotifyCharacteristic, Toast.LENGTH_SHORT).show();
                if (mNotifyCharacteristic != null) {
                    mBLEService.writeCharacteristics(mNotifyCharacteristic,message.getBytes());
                }

            }
        });


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
                            IntentFilter intentFilter = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
                            intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
                            registerReceiver(broadCastReceiver, intentFilter);

//                            if (device != null) {
//                                Intent getServiceIntent = new Intent(MainActivity.this, BLEService.class);
//                                bindService(getServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
//                            }
//                            Log.i(TAG, "Device address " + device.getAddress());
                             Boolean result = connect(device.getAddress());
                            Toast.makeText(getApplicationContext(), "request result" + result, Toast.LENGTH_SHORT).show();
                        }
                    })
                    .show();

        }
    };

    public static UUID convertFromInteger(int i) {
        final long MSB = 0x0000000000001000L;
        final long LSB = 0x800000805f9b34fbL;
        long value = i & 0xFFFFFFFF;
        return new UUID(MSB | (value << 32), LSB);
    }

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
//                enableBluetooth();
                return true;
            case R.id.bluetooth_searching:
//                scanLeDevice();
                scanLeDevice(true);
                Intent getServiceIntent = new Intent(MainActivity.this, BLEService.class);
                bindService(getServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
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

//    public void enableBluetooth() {
//        if (mBluetoothAdapter.isEnabled()) {
//            mBluetoothAdapter.disable();
//            Toast.makeText(context, "Bluetooth Disabled", Toast.LENGTH_SHORT).show();
//        }
//        else {
//            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
//            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
//            Toast.makeText(context, "Bluetooth Enabled", Toast.LENGTH_SHORT).show();
//        }
//    }

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
        mBluetoothGatt = bluetoothDevice.connectGatt(this, false, bluetoothGattCallback);
        bluetoothAddress = address;
        mConnectionState = STATE_CONNECTING;
        return true;
    }

    private boolean mInitialised;
    private BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.d(TAG, "onConnectionStateChange " + newState);
            Toast.makeText(getApplicationContext(), "onConnectionStateChange " + newState, Toast.LENGTH_SHORT).show();
            String intentAction;
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED:
//                    Log.i("gattCallback", "STATE_CONNECTED");
                    mConnectionState = STATE_CONNECTED;
                    intentAction = BLEService.ACTION_GATT_CONNECTED;
                    mBLEService.broadcastUpdate(intentAction);
                    gatt.discoverServices();

                    Toast.makeText(getApplicationContext(), "Connected...", Toast.LENGTH_SHORT).show();
                    break;
                case BluetoothProfile.STATE_DISCONNECTED:
//                    Log.e("gattCallback", "STATE_DISCONNECTED");
                    intentAction = BLEService.ACTION_GATT_DISCONNECTED;
                    mBLEService.broadcastUpdate(intentAction);
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
            super.onServicesDiscovered(gatt, status);
            Log.d(TAG, "onServicesDiscovered " + status);
            Toast.makeText(getApplicationContext(), "onServicesDiscovered: " + status, Toast.LENGTH_SHORT).show();
//            if (status == BluetoothGatt.GATT_SUCCESS) {
//                mBLEService.broadcastUpdate(BLEService.ACTION_GATT_SERVICES_DISCOVERED);
//            } else {
//                Log.w(TAG, "onServicesDiscovered received: " + status);
//                Toast.makeText(getApplicationContext(), "onServicesDiscovered received: " + status, Toast.LENGTH_SHORT).show();
//
//            }
            if (status != BluetoothGatt.GATT_SUCCESS)
            {
                return;
            }
            mGattServer = gatt.getService(serviceUuid);
            mNotifyCharacteristic = mGattServer.getCharacteristic(charUuid);
            mNotifyCharacteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
//            Toast.makeText(getApplicationContext(),"onServiceDiscovered characteristics" + mNotifyCharacteristic, Toast.LENGTH_SHORT).show();
            mInitialised = gatt.setCharacteristicNotification(mNotifyCharacteristic, true);

        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.d(TAG, "onCharacteristicRead " + status);
            Toast.makeText(getApplicationContext(), "onCharacteristicRead" + status, Toast.LENGTH_SHORT).show();
            if (status == BluetoothGatt.GATT_SUCCESS) {
                mBLEService.broadcastUpdate(BLEService.ACTION_DATA_AVAILABLE, characteristic);
               mBLEService.readCharacteristic(characteristic);
                Toast.makeText(getApplicationContext() , "character " + characteristic, Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.d(TAG, "onCharacteristicWrite " + status);
            Toast.makeText(getApplicationContext(), "onCharacteristicWrite" + status, Toast.LENGTH_SHORT).show();
            if (status == BluetoothGatt.GATT_SUCCESS){
                String message = sentMsg.getText().toString();
//                message.getBytes()
                mBLEService.writeCharacteristics(characteristic, message.getBytes());
                mNotifyCharacteristic = characteristic;
                Toast.makeText(getApplicationContext(), "Sent Message", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.d(TAG, "onCharacteristicChanged");
            Toast.makeText(getApplicationContext(), "onCharacteristicChanged", Toast.LENGTH_SHORT).show();
            mBLEService.broadcastUpdate(BLEService.ACTION_DATA_AVAILABLE, characteristic);
            mBLEService.readCharacteristic(characteristic);
        }
    };

//    private void setupServer() {
//        BluetoothGattService service = new BluetoothGattService(serviceUuid,
//                BluetoothGattService.SERVICE_TYPE_PRIMARY);
//        BluetoothGattCharacteristic writeCharacteristic = new BluetoothGattCharacteristic(
//                charUuid,
//                BluetoothGattCharacteristic.PROPERTY_WRITE,
//                BluetoothGattCharacteristic.PERMISSION_WRITE);
//        service.addCharacteristic(writeCharacteristic);
//        mGattServer.addService(service);
//    }


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
            final String action = intent.getAction();
            if (BLEService.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState("connected");
            } else if (BLEService.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState("disconnected");
            } else if (BLEService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                for (BluetoothGattService gattService : mBLEService.getSupportedGattServices()) {
                    if (BLEGattAttributes.lookup(gattService.getUuid().toString()).matches("Service")) {
                        for (BluetoothGattCharacteristic gattCharacteristics : mBLEService.getSupportedGattCharacteristics(gattService)) {
                            if (((gattCharacteristics.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) || ((gattCharacteristics.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0)) {
                                mBLEService.setCharacteristicNotification(gattCharacteristics, true);
//                                Toast.makeText(getApplicationContext(),"characteristics" + gattCharacteristics, Toast.LENGTH_SHORT).show();
//                                mNotifyCharacteristic = gattCharacteristics;
//                                Toast.makeText(getApplicationContext(),"After appended characteristics" + mNotifyCharacteristic, Toast.LENGTH_SHORT).show();
                                try {
                                    Thread.sleep(350);
                                } catch (InterruptedException e) {
                                }
                            }
                            if (BLEGattAttributes.lookup(gattCharacteristics.getUuid().toString()).matches("Character Level")) {
                                Toast.makeText(getApplicationContext(),"characteristics" + gattCharacteristics, Toast.LENGTH_SHORT).show();
                                mNotifyCharacteristic = gattCharacteristics;
                                Toast.makeText(getApplicationContext(),"After appended characteristics" + mNotifyCharacteristic, Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
//                for (BluetoothGattService gattService:mBLEService.getSupportedGattServices()) {
//                    for (BluetoothGattCharacteristic characteristic:mBLEService.getSupportedGattCharacteristics(gattService)) {
//                        if (((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_NOTIFY) > 0) || ((characteristic.getProperties() & BluetoothGattCharacteristic.PROPERTY_INDICATE) > 0)) {
//                            mBLEService.setCharacteristicNotification(characteristic, true);
//
//                        }
//                    }
                }

                } else if (BLEService.ACTION_DATA_AVAILABLE.equals(action)) {
//               displayData(intent.getStringExtra(BLEService.EXTRA_DATA));
                    Log.i(TAG, intent.getStringExtra(BLEService.EXTRA_DATA));
                    String[] rxData = (intent.getStringArrayExtra(BLEService.EXTRA_DATA));
                    if (BLEGattAttributes.lookup(rxData[0]).matches("Service")) {
                        StringBuilder output = new StringBuilder();
                        for (int i = 0; i < rxData[1].length(); i += 2) {
                            output.append(Integer.parseInt(rxData[1].substring(i, i + 2), 16));
                        }
                        receivedMsg.append(output);
                    }


                }

            }
        };


    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBLEService = ((BLEService.LocalBinder) service).getService();

            if (!mBLEService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
//                finish();
            }
            connect(bluetoothDevice.getAddress());
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

//    private void displayData(String data) {
//        if (data != null) {
////            batteryLevel.setText(data);
//            Log.i(TAG, "batteryLevel" + data);
//            Toast.makeText(context, "batteryLevel " + data, Toast.LENGTH_SHORT).show();
//        }
//    }
//
//    private void displayGattServices(List<BluetoothGattService> gattServices) {
//        if (gattServices == null)
//            return;
//        String uuid = null;
//        String serviceString = "unknown service";
//        String charaString = "unknown characteristic";
//        for (BluetoothGattService gattService : gattServices) {
//            uuid = gattService.getUuid().toString();
//            serviceString = uuid;
////                    SampleGattAttributes.lookup(uuid);
//            if (serviceString != null) {
//                List<BluetoothGattCharacteristic> gattCharacteristics =
//                        gattService.getCharacteristics();
//                for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
//                    HashMap<String, String> currentCharaData = new HashMap<String, String>();
//                    uuid = gattCharacteristic.getUuid().toString();
//                    charaString = uuid;
////                            SampleGattAttributes.lookup(uuid);
//                    if (charaString != null) {
////                        serviceName.setText(charaString);
//                        Log.i(TAG, "Name" + charaString);
//                        Toast.makeText(context, "Name " + charaString, Toast.LENGTH_SHORT).show();
//                    }
//                    mNotifyCharacteristic = gattCharacteristic;
//                    return;
//                }
//            }
//        }
//    }
//
//
}