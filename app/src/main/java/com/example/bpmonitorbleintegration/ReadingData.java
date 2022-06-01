package com.example.bpmonitorbleintegration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.List;
import java.util.Objects;

public class ReadingData extends AppCompatActivity {

    private BLEService mBluetoothLeService;
    private String deviceAddress;
    public BluetoothGattCharacteristic mNotifyCharacteristic;
    private boolean mConnected;
    BluetoothDevice bluetoothDevice;
    IntentFilter intentFilter;
    AlertDialog.Builder builder, builder1;
    Decoder decoder;
    public AlertDialog dialog, dialog1;
    RoomDB localDB;
    String resultData;

    public int counter = 0;
    private CountDownTimer mCountDownTimer;
    private boolean mTimerRunning;
    private long mTimeLeftInMillis = 2000;
    private long startTime = 50;
    View customView;
    private String TAG = ReadingData.class.getName();

    private TextView statusText, batteryText, systolicText, diastolicText, heartRateText, mapText, progressText;
    private Button startBtn;
    private ProgressBar progressBar, progress;
    int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reading_data);

        statusText = findViewById(R.id.actual_status);
        batteryText = findViewById(R.id.battery_level);
        systolicText = findViewById(R.id.systalic_val);
        diastolicText = findViewById(R.id.dystalic_val);
        heartRateText = findViewById(R.id.rate_val);
        mapText = findViewById(R.id.range_val);
        progressText = findViewById(R.id.progress_text);
        progress = findViewById(R.id.progress_start);
        progressBar = findViewById(R.id.progress_bar);
        startBtn = findViewById(R.id.start_reading);

        deviceAddress = getIntent().getStringExtra("Device");
        intentFilter = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
        intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver(broadCastReceiver, intentFilter);

        //Initialising Decoder class.
        decoder = new Decoder();
        localDB = new RoomDB();

        Objects.requireNonNull(getSupportActionBar()).setTitle(R.string.readings);

//        pref = PreferenceManager.getDefaultSharedPreferences(this);

        // Connect to the device through BLE.
        registerReceiver(broadCastReceiver, GattUpdateIntentFilter());
        if (mBluetoothLeService != null) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                mBluetoothLeService.close();
                mBluetoothLeService.disconnect();
                mBluetoothLeService.stopSelf();
                boolean result = mBluetoothLeService.connect(deviceAddress);
            }
        }

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mNotifyCharacteristic != null) {
//                    Log.i(TAG, "Start value " + Arrays.toString(Constants.startValue) + " " + Constants.startValue);
                    Constants.startValue = decoder.computeCheckSum(Constants.startValue);
//                    Log.i(TAG, "Start value after checksum " + Arrays.toString(Constants.startValue) + " " + Constants.startValue);
                    mBluetoothLeService.writeCharacteristics(mNotifyCharacteristic, Constants.startValue);
                }

                mCountDownTimer = new CountDownTimer(50,10) {
                    @Override
                    public void onTick(long l) {

                    }

                    @Override
                    public void onFinish() {
                        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis,1000) {
                            @SuppressLint("SetTextI18n")
                            @Override
                            public void onTick(long l) {
                                if (Constants.is_readingStarted == true) {
                                    startBtn.setText("Cancel");
                                    if (mNotifyCharacteristic != null) {
                                        Constants.cancelValue = decoder.computeCheckSum(Constants.cancelValue);
//                            Log.i(TAG, "Force stop value after checksum " + Arrays.toString(Constants.startValue) + " " + Constants.startValue);
                                        mBluetoothLeService.writeCharacteristics(mNotifyCharacteristic, Constants.cancelValue);

                                        mCountDownTimer = new CountDownTimer(startTime, 10) {
                                            @Override
                                            public void onTick(long l) {
                                                counter++;
//                                            Log.i(TAG, "counter Started " + startTime);
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
//                                                                            Log.i(TAG, "run ontick: ack " + Constants.is_ackReceived);
//                                                                            Toast.makeText(getApplicationContext(), "ontick ack " + Constants.is_ackReceived, Toast.LENGTH_SHORT).show();
                                                        mCountDownTimer = new CountDownTimer(30, 10) {
                                                            @Override
                                                            public void onTick(long l) {
                                                                counter++;
//                                            Log.i(TAG, "counter Started " + startTime);
                                                            }

                                                            @Override
                                                            public void onFinish() {
//                                            Log.i(TAG, "Stopped");
                                                                runOnUiThread(new Runnable() {
                                                                    @Override
                                                                    public void run() {
//                                                                            Log.i(TAG, "run onfinish: ack " + Constants.is_ackReceived);
//                                                                            Toast.makeText(getApplicationContext(), "onfinish ack " + Constants.is_ackReceived, Toast.LENGTH_SHORT).show();
                                                                        if (Constants.is_ackReceived == true) {
                                                                            mCountDownTimer.cancel();
//                                                                                dialog.setCancelable(true);
                                                                            Constants.is_readingStarted = false;
//                                                                            Constants.is_ackReceived = false;
                                                                        }
                                                                    }
                                                                });
                                                            }
                                                        }.start();
                                                    }
                                                });
                                            }

                                            @Override
                                            public void onFinish() {
//                                            Log.i(TAG, "Stopped");
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
//                                                                            Log.i(TAG, "run onfinish: ack " + Constants.is_ackReceived);
//                                                                            Toast.makeText(getApplicationContext(), "onfinish ack " + Constants.is_ackReceived, Toast.LENGTH_SHORT).show();
                                                        if (Constants.is_ackReceived == false){
//                                                Log.i(TAG, "Start again");
                                                            Constants.cancelValue = decoder.computeCheckSum(Constants.cancelValue);
                                                            mBluetoothLeService.writeCharacteristics(mNotifyCharacteristic, Constants.cancelValue);
                                                            start();
                                                        }
                                                    }
                                                });
                                            }
                                        }.start();
                                    }
                                }

                                else if (Constants.is_resultReceived == true) {
                                    startBtn.setText("Stop");


                                }
                            }

                            @Override
                            public void onFinish() {

                            }
                        }.start();
                    }
                }.start();
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
                mBluetoothLeService.close();
                mBluetoothLeService.disconnect();
                mBluetoothLeService.stopSelf();
                boolean result = mBluetoothLeService.connect(deviceAddress);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        //Disconnect through services.
        unregisterReceiver(broadCastReceiver);
        dialog.dismiss();
        dialog1.dismiss();
    }

    @Override
    protected void onStart() {
        super.onStart();
        //To bind the service connection.
        Intent getServiceIntent = new Intent(ReadingData.this, BLEService.class);
        bindService(getServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        progress.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind service connection.
        unbindService(mServiceConnection);
    }

    //Menu item.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.data_menu_file, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.disable_bluetooth) {
            dialog = new AlertDialog.Builder(ReadingData.this)
                    .setTitle("Message")
                    .setMessage("Are you sure, disable bluetooth")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                            BluetoothAdapter bAdapter = BluetoothAdapter.getDefaultAdapter();
                            bAdapter.disable();
                            mBluetoothLeService.disconnect();
                            mBluetoothLeService.close();
                            //Navigating to next activity on tap of ok button.
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                Intent intent = new Intent(ReadingData.this, HomePage.class);
                                startActivity(intent);
//                                finish();
                            }
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialog.dismiss();
                        }
                    }).show();
        }
        return super.onOptionsItemSelected(item);
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
//            updateGUI(intent);
            final String action = intent.getAction();
            if (Constants.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState("Connected");
            }
//            else if (Constants.ACTION_GATT_CONNECTING.equals(action)) {
//
//            }
            else if (Constants.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState("Disconnected");
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

    private  void displayData(String data) {

        Log.i(TAG, "received data before " + data);
        if (data != null) {
//           Log.i(TAG, "reading started and result received " + Constants.is_readingStarted + " " + Constants.is_resultReceived);
            mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
//                    mTimeLeftInMillis = millisUntilFinished;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progress.setVisibility(View.GONE);
                            if (Constants.is_readingStarted == true)
                            {
                                mCountDownTimer.cancel();
                                if (counter <= mTimeLeftInMillis) {
                                    progressBar.setProgress(counter);
                                    progressText.setText(data);
                                    counter = counter++;
                                    Log.i(TAG, "timer in readings " + counter);
                                }
//                                Constants.is_readingStarted = false;
                            }

                            // Method 1: To enable/disable Ok/cancel button on basis of readings.
                            if (Constants.is_resultReceived == true) {
                                progressText.setText(data);
                                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
                            }
                            else
                            {
                                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(true);
                            }

//          Method 2:  ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(Constants.is_resultReceived == true);
                        }
                    });
                }

                @Override
                public void onFinish() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (Constants.is_readingStarted == false){
                                mCountDownTimer.cancel();
                                Toast.makeText(ReadingData.this,"Please start again",Toast.LENGTH_SHORT).show();
                                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
//
                            }
                        }
                    });
                }
            }.start();
        }
        else {
            startTimer();
        }
    }

    private void startTimer() {
        mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                mTimeLeftInMillis = millisUntilFinished;
                counter = counter++;
//                Log.i(TAG, "timer in battery " + mTimeLeftInMillis);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (Constants.is_batterValueReceived == true)
                        {
                            mCountDownTimer.cancel();
                            //Showing battery level using color code.
                            showBattery();
//                    Constants.is_batterValueReceived = false;
                        }
                    }
                });
            }

            @Override
            public void onFinish() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (Constants.is_batterValueReceived == false){
                            mCountDownTimer.cancel();
                            Toast.makeText(ReadingData.this,"Something went wrong please connect again!!!",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        }.start();
    }

    // Change color on basis of battery level.
    public void showBattery(){
//        Log.i(TAG, "Battery level " + mBluetoothLeService.batteryLevel);
        progress.setVisibility(View.GONE);
        if (mBluetoothLeService.batteryLevel == Constants.HIGH_BATTERY) {
            batteryText.setBackgroundColor(Color.parseColor("#008000"));
        }
        else if (mBluetoothLeService.batteryLevel == Constants.MID_BATTERY){
            batteryText.setBackgroundColor(Color.parseColor("#FFA500"));
        }
        else if (mBluetoothLeService.batteryLevel == Constants.LOW_BATTERY) {
            batteryText.setBackgroundColor(Color.parseColor("#FF0000"));
            Toast.makeText(getApplicationContext(), "Battery is low, Please Change battery",Toast.LENGTH_SHORT).show();
        }
//        Constants.is_batterValueReceived = false;
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
                mBluetoothLeService.close();
                mBluetoothLeService.disconnect();
                mBluetoothLeService.stopSelf();
                mBluetoothLeService.connect(deviceAddress);
//                mBluetoothLeService.setHandler(myHandler);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };

    //Updating connection status through text field, if disconnected status navigating to mainActivity through alert dialog.
    private void updateConnectionState(final String status) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                statusText.setText(status);

                if (status.equals("Disconnected")){
                    new AlertDialog.Builder(ReadingData.this)
                            .setTitle("Message")
                            .setMessage("Connection terminated!, please connect again")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    //Navigating to next activity on tap of ok button.
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        Intent intent = new Intent(ReadingData.this, MainActivity.class);
                                        startActivity(intent);
                                    }
                                }
                            }).show();
                }
            }
        });
    }

}