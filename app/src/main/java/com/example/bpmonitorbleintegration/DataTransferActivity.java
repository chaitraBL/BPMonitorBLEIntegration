package com.example.bpmonitorbleintegration;

import static com.example.bpmonitorbleintegration.R.layout.activity_data_transfer;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
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
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.ParcelUuid;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
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
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class DataTransferActivity extends AppCompatActivity{

    ImageButton startBtn;
    private BLEService mBluetoothLeService;
    private String deviceAddress;
    public BluetoothGattCharacteristic mNotifyCharacteristic;
    private boolean mConnected;
    BluetoothDevice bluetoothDevice;
    IntentFilter intentFilter;
    AlertDialog.Builder builder;
    TextView statusText, systolicText, diastolicText, heartRateText,rangeText, tv, batteryLevel;
    private String TAG = "DataTransferActivity";
    SharedPreferences pref;

    CountDownTimer timer;
    Button readBtn;
    RecyclerView recyclerView;
    Decoder decoder;
    public AlertDialog dialog;

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
        systolicText = findViewById(R.id.systalic_val);
        diastolicText = findViewById(R.id.dystalic_val);
        heartRateText = findViewById(R.id.rate_val);
        rangeText = findViewById(R.id.range_val);
        deviceAddress = getIntent().getStringExtra("Device");
        readBtn = findViewById(R.id.final_val);
        recyclerView = findViewById(R.id.recyclerview_tasks);

        batteryLevel = findViewById(R.id.battery_level);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        intentFilter = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
        intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver(broadCastReceiver, intentFilter);

        //Initialising Decoder class.
        decoder = new Decoder();

        Intent getServiceIntent = new Intent(DataTransferActivity.this, BLEService.class);
        bindService(getServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        pref = PreferenceManager.getDefaultSharedPreferences(this);

        //Send request to START the readings.
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mNotifyCharacteristic != null) {
//                    Log.i(TAG, "Start value " + Arrays.toString(Constants.startValue) + " " + Constants.startValue);
                    Constants.startValue = decoder.computeCheckSum(Constants.startValue);
//                    Log.i(TAG, "Start value after checksum " + Arrays.toString(Constants.startValue) + " " + Constants.startValue);
                    mBluetoothLeService.writeCharacteristics(mNotifyCharacteristic, Constants.startValue);

//                    timer=  new CountDownTimer(0, 2000) {
//                        @Override
//                        public void onTick(long millisUntilFinished) {
////                            time.setText(String.valueOf(count));
////                            count++;
//                        }
//
//                        @Override
//                        public void onFinish() {
//                            LayoutInflater layoutInflater = getLayoutInflater();
//                            View customView = layoutInflater.inflate(R.layout.custom_popup_dialog, null);
//                            tv = customView.findViewById(R.id.tvpopup);
//                            tv.setText("Please Start again");
//                        }
//                    };
//                    timer.start();

                }

                //Alert controller.
                builder = new AlertDialog.Builder(DataTransferActivity.this);
                builder.setTitle("Readings");
                LayoutInflater layoutInflater = getLayoutInflater();

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
                            Constants.startValue = decoder.computeCheckSum(Constants.startValue);
//                            Log.i(TAG, "Force stop value after checksum " + Arrays.toString(Constants.startValue) + " " + Constants.startValue);
                            mBluetoothLeService.writeCharacteristics(mNotifyCharacteristic, Constants.startValue);
                        }
                    }
                });
                //Send Ack - received readings.
                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if (mNotifyCharacteristic != null) {
                            Constants.startValue = decoder.computeCheckSum(Constants.startValue);
//                            Log.i(TAG, "Stop value after checksum " + Arrays.toString(Constants.startValue) + " " + Constants.startValue);
                            mBluetoothLeService.writeCharacteristics(mNotifyCharacteristic, Constants.startValue);
                            Constants.is_resultReceived = false;
                        }
                    }
                });

                builder.setView(customView);
                dialog = builder.create();
                //Prevent dialog box from getting dismissed on back key pressed
                dialog.setCancelable(false);
                //Prevent dialog box from getting dismissed on outside touch
                dialog.setCanceledOnTouchOutside(false);
                //To hide Ok button until readings complete.
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                            ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                    }
                });
                dialog.show();
            }
        });

        // To read final values and store it to local DB.
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

    //Menu item.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.data_menu_file, menu);
        return super.onCreateOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.read_database:
                getTasks();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }

    }

// To add actions to intent filter.
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
            updateGUI(intent);
            final String action = intent.getAction();
            if (Constants.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState("Connected");
            }
            else if (Constants.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState("Disconnected");
                new AlertDialog.Builder(DataTransferActivity.this)
                    .setTitle("Message")
                        .setMessage("Connection terminated!, please connect again")
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(DataTransferActivity.this, MainActivity.class);
                                startActivity(intent);
                                }
                            })
                        .show();

            }
            else if (Constants.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                //Receive services and characteristics
                List<BluetoothGattService> gattService = mBluetoothLeService.getSupportedGattServices();
//                Log.i("TAG", "Size " + gattService.size());
                for (BluetoothGattService service : gattService)
                {
//                    Log.i(TAG, service.getUuid().toString());
//                    Log.i(TAG, BLEGattAttributes.CLIENT_SERVICE_CONFIG);
                    if(service.getUuid().toString().equalsIgnoreCase(BLEGattAttributes.CLIENT_SERVICE_CONFIG))
                    {
                        List<BluetoothGattCharacteristic> gattCharacteristics =
                                service.getCharacteristics();
//                        Log.i(TAG, "Count is:" + gattCharacteristics.size());
                        for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics)
                        {
                            if(gattCharacteristic.getUuid().toString().equalsIgnoreCase(BLEGattAttributes.CLIENT_CHARACTERISTIC_CONFIG))
                            {
//                                Log.i(TAG, gattCharacteristic.getUuid().toString());
//                                Log.i(TAG, BLEGattAttributes.CLIENT_CHARACTERISTIC_CONFIG);
                                mNotifyCharacteristic = gattCharacteristic;
                                mBluetoothLeService.setCharacteristicNotification(gattCharacteristic,true);
                                return;
                            }
                        }
                    }
                }
            }

            else if (Constants.ACTION_DATA_AVAILABLE.equals(action)) {
                displayData(intent.getStringExtra(Constants.EXTRA_DATA));

            }
        }
    };

    private void updateGUI(Intent intent) {
        if (intent.getExtras() != null) {
            long millisUntilFinished = intent.getLongExtra("countdown", 0);
            Log.i(TAG, "Countdown seconds remaining: " +  millisUntilFinished / 1000);
        }
    }

    private  void displayData(String data) {
        if (data != null) {
//           Log.i(TAG, "received data " + data);
           tv.setText(data);

//           Log.i(TAG,"flag " + Constants.is_resultReceived);
            //To enable/disable Ok button on basis of readings.
           if (Constants.is_resultReceived == true) {
               ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
           }
           else
           {
               ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
           }
        }
        //Showing battery level using color code.
        showBattery();
    }

    public void showBattery(){
//        Log.i(TAG, "Battery level " + mBluetoothLeService.batteryLevel);
        if (mBluetoothLeService.batteryLevel == Constants.HIGH_BATTERY) {
            batteryLevel.setBackgroundColor(Color.parseColor("#008000"));
        }
        else if (mBluetoothLeService.batteryLevel == Constants.MID_BATTERY){
            batteryLevel.setBackgroundColor(Color.parseColor("#FFA500"));
        }
        else if (mBluetoothLeService.batteryLevel == Constants.LOW_BATTERY) {
            batteryLevel.setBackgroundColor(Color.parseColor("#FF0000"));
        }
    }

    // Connect and disconnect to the services.
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

        // To get current time and date.
        DateFormat df = new SimpleDateFormat("HH:mm"); // Format time
        String time = df.format(Calendar.getInstance().getTime());
//        Log.i(TAG, "Time " + time);

//        DateFormat df1 = new SimpleDateFormat("yyyy/MM/dd"); // Format date
        DateFormat df1 = new SimpleDateFormat("dd,MM"); // Format date
        String date = df1.format(Calendar.getInstance().getTime());
//        Log.i(TAG, "date " + date);

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
                adapter.notifyDataSetChanged();
            }
        }
        GetTasks gt = new GetTasks();
        gt.execute();
    }
}

