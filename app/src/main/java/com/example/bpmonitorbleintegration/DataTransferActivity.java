package com.example.bpmonitorbleintegration;

import static com.example.bpmonitorbleintegration.R.layout.activity_data_transfer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

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
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelUuid;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
    TextView statusText, systolicText, diastolicText, heartRateText,rangeText, tv;
    private String TAG = "DataTransferActivity";
    MainActivity mainActivity;
    Intent intent;
    Handler mHandler;
    SharedPreferences pref;
    RawDataModel dataModel;
    int cuffValue;
    int pressureVal;
    Button readBtn;
    RecyclerView recyclerView;

    private Handler myHandler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(@NonNull Message message) {
            switch (message.what) {
                case Constants.RAW_COMMANDID:
                    Log.i(TAG, "obj " + String.valueOf(message.obj));
                    Log.i(TAG, "arg1 & arg 2" + message.arg1 + " " + message.arg2);
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_data_transfer);

        startBtn = findViewById(R.id.btn_read);
        statusText = findViewById(R.id.actual_status);
//        receivedMsg = findViewById(R.id.receivedMsg);
        systolicText = findViewById(R.id.systalic_val);
        diastolicText = findViewById(R.id.dystalic_val);
        heartRateText = findViewById(R.id.rate_val);
        rangeText = findViewById(R.id.range_val);
        deviceAddress = getIntent().getStringExtra("Device");
        readBtn = findViewById(R.id.final_val);
        recyclerView = findViewById(R.id.recyclerview_tasks);
//        mHandler = new MyHandler(this);

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

                //Alert controller.
                builder = new AlertDialog.Builder(DataTransferActivity.this);
                builder.setTitle("Raw Readings");
                LayoutInflater layoutInflater = getLayoutInflater();

                //To retrieve integer value
//                SharedPreferences settings = getSharedPreferences("SharedPref", 0);
//                int snowDensity = settings.getInt("Cuff", 0); //0 is the default value
//                Log.i(TAG, "Cuff pressure " + snowDensity);

                //this is custom dialog
                //custom_popup_dialog contains textview only
                View customView = layoutInflater.inflate(R.layout.custom_popup_dialog, null);
                // reference the textview of custom_popup_dialog
                tv = customView.findViewById(R.id.tvpopup);
                tv.setTextSize(15);
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
                //To hide Ok button until readings complete.
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

        readBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                systolicText.setText(String.valueOf(mBluetoothLeService.systalic));
                diastolicText.setText(String.valueOf(mBluetoothLeService.dystolic));
                heartRateText.setText(String.valueOf(mBluetoothLeService.rate));
                rangeText.setText(String.valueOf(mBluetoothLeService.range));
                saveTask(deviceAddress, mBluetoothLeService.systalic, mBluetoothLeService.dystolic, mBluetoothLeService.rate, mBluetoothLeService.range);
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
        //Disconnect through services.
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

    //Broadcast receiver to receive services and data.
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
            }

            else if (Constants.ACTION_DATA_AVAILABLE.equals(action)) {
//                receivedData = intent.getStringExtra(Constants.EXTRA_DATA);
                displayData(intent.getStringExtra(Constants.EXTRA_DATA));
            }
        }
    };

    private  void displayData(String data) {
        if (data != null) {
//           receivedMsg.setText(data);
//           receivedData = data;
//           Log.i(TAG, "received data " + receivedData);
           tv.setText(data);
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
                mBluetoothLeService.setHandler(myHandler);
            }

        }
        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    //Updating connection status through text field.
    private void updateConnectionState(final String status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusText.setText(status);
            }
        });
    }

    // Save to the Local Room DB.
    private void saveTask(String address, int systolic, int dystolic, int heartRate, int range) {
//        final String sMessage = message.trim();
        final String sAddress = address.trim();

        DateFormat df = new SimpleDateFormat("HH:mm"); // Format time
        String time = df.format(Calendar.getInstance().getTime());
        Log.i(TAG, "Time " + time);

        DateFormat df1 = new SimpleDateFormat("yyyy/MM/dd"); // Format date
        String date = df1.format(Calendar.getInstance().getTime());
        Log.i(TAG, "date " + date);
//
//        if (sMessage.isEmpty())
//        {
//            edCreateMessage.setError("Task required");
//            edCreateMessage.requestFocus();
//            return;
//        }

        class SaveTask extends AsyncTask<Void, Void, Void> {

            @Override
            protected Void doInBackground(Void... voids) {

                BloodPressureDB reading = new BloodPressureDB();
                reading.setName(sAddress);
                reading.setDate(date);
                reading.setTime(time);
                reading.setDystolic(dystolic);
                reading.setSystolic(systolic);
                reading.setHeartRate(heartRate);
                reading.setRange(range);

                DatabaseClient.getInstance(getApplicationContext()).getAppDatabase().bpReadingsDao().insert(reading);
                return null;
            }

            @Override
            protected void onPostExecute(Void unused) {
                super.onPostExecute(unused);
//                finish();
//                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                Toast.makeText(getApplicationContext(),"Saved",Toast.LENGTH_SHORT).show();
            }
        }

        SaveTask st = new SaveTask();
        st.execute();
    }

    //To retrieve data from Room DB.
    private void getTasks() {
        class GetTasks extends AsyncTask<Void, Void, List<BloodPressureDB>> {

            @Override
            protected List<BloodPressureDB> doInBackground(Void... voids) {
                List<BloodPressureDB> taskList = DatabaseClient
                        .getInstance(getApplicationContext())
                        .getAppDatabase()
                        .bpReadingsDao()
                        .getAll();
                return taskList;
            }

            @Override
            protected void onPostExecute(List<BloodPressureDB> tasks) {
                super.onPostExecute(tasks);
                ReadingsAdapter adapter = new ReadingsAdapter(DataTransferActivity.this, tasks);
                recyclerView.setAdapter(adapter);
            }
        }

        GetTasks gt = new GetTasks();
        gt.execute();
    }
}

