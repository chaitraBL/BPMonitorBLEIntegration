package com.example.bpmonitorbleintegration.reading;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

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
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.bpmonitorbleintegration.R;
import com.example.bpmonitorbleintegration.bleconnect.BLEService;
import com.example.bpmonitorbleintegration.bleconnect.Decoder;
import com.example.bpmonitorbleintegration.constants.BLEGattAttributes;
import com.example.bpmonitorbleintegration.constants.Constants;
import com.example.bpmonitorbleintegration.database.RoomDB;
import com.example.bpmonitorbleintegration.home.HomePage;

import java.util.List;
import java.util.Objects;

//https://stackoverflow.com/questions/69529502/countdown-timer-which-runs-in-the-background-in-android - for background timer

public class ReadingData extends AppCompatActivity {

    private BLEService mBluetoothLeService;
    private String deviceAddress;
    public BluetoothGattCharacteristic mNotifyCharacteristic;
    BluetoothDevice bluetoothDevice;
    IntentFilter intentFilter;
    AlertDialog.Builder builder1;
    Decoder decoder;
    public AlertDialog dialog, dialog1;
    RoomDB localDB;

    public int counter = 0;
    private CountDownTimer mCountDownTimer;
    private long mTimeLeftInMillis = 2000;
    private final long startTime = 50;
    private final String TAG = ReadingData.class.getName();

    private TextView statusText, batteryText, systolicText, diastolicText, heartRateText, mapText, progressText;
    private Button startBtn;
    private Button stopBtn;
    Button saveReadingBtn;
    private ProgressBar progressBar, progress;
    int i = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        setContentView(R.layout.activity_reading_data1);

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
        stopBtn = findViewById(R.id.stop_reading);
        saveReadingBtn = (Button) findViewById(R.id.save_result1);

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
                if (result == false) {
                    finish();
                }
            }
        }

        stopBtn.setEnabled(false);
        stopBtn.setVisibility(View.INVISIBLE);

        stopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mNotifyCharacteristic != null) {
//                    Constants.is_ackReceived = false;
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
//                                                    Log.i(TAG, "run: cuff replaced before condition " + Constants.is_cuffReplaced);
                                                    if (Constants.is_ackReceived == true) {
                                                        mCountDownTimer.cancel();
                                                        progressText.setText("---");

                                                        startBtn.setEnabled(true);
                                                        startBtn.setVisibility(View.VISIBLE);
                                                        stopBtn.setVisibility(View.INVISIBLE);
                                                        stopBtn.setEnabled(false);
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
                                    if ((Constants.is_ackReceived == false)){
//                                                Log.i(TAG, "Start again");
                                        Constants.cancelValue = decoder.computeCheckSum(Constants.cancelValue);
                                        mBluetoothLeService.writeCharacteristics(mNotifyCharacteristic, Constants.cancelValue);
                                        start();
                                    }

//                                    Constants.is_ackReceived = false;

                                }
                            });
                        }
                    }.start();
                    if (Constants.is_finalResult == true) {
                        systolicText.setText(String.valueOf(mBluetoothLeService.systalic));
                        diastolicText.setText(String.valueOf(mBluetoothLeService.dystolic));
                        heartRateText.setText(String.valueOf(mBluetoothLeService.rate));
                        mapText.setText(String.valueOf(mBluetoothLeService.range));
//                        localDB.saveTask(deviceAddress, mBluetoothLeService.systalic, mBluetoothLeService.dystolic, mBluetoothLeService.rate, mBluetoothLeService.range, ReadingData.this);
                        Constants.is_finalResult = false;
                    }
                }
            }
        });

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Constants.is_buttonStarted = true;
                if (mNotifyCharacteristic != null) {
//                    Log.i(TAG, "Start value " + Arrays.toString(Constants.startValue) + " " + Constants.startValue);
                    Constants.startValue = decoder.computeCheckSum(Constants.startValue);
//                    Log.i(TAG, "Start value after checksum " + Arrays.toString(Constants.startValue) + " " + Constants.startValue);
                    mBluetoothLeService.writeCharacteristics(mNotifyCharacteristic, Constants.startValue);
                }

                systolicText.setText("");
                diastolicText.setText("");
                heartRateText.setText("");
                mapText.setText("");
//                startBtn.setText("Cancel");
            }
        });

        saveReadingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progress.setVisibility(View.VISIBLE);
                if (systolicText.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.enter_systolic_value), Toast.LENGTH_SHORT).show();
                    progress.setVisibility(View.GONE);
                }
                else if (diastolicText.getText().toString().equals(""))
                {
                    Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.enter_diastolic_value),Toast.LENGTH_SHORT).show();
                    progress.setVisibility(View.GONE);
                }
                else if (heartRateText.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.enter_heart_rate),Toast.LENGTH_SHORT).show();
                    progress.setVisibility(View.GONE);
                }
                else if (mapText.getText().toString().equals("")) {
                    Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.enter_map),Toast.LENGTH_SHORT).show();
                    progress.setVisibility(View.GONE);
                }
                else {
                    localDB.saveTask(deviceAddress, Integer.parseInt(systolicText.getText().toString()), Integer.parseInt(diastolicText.getText().toString()), Integer.parseInt(heartRateText.getText().toString()), Integer.parseInt(mapText.getText().toString()), ReadingData.this);
                    systolicText.setText("");
                    diastolicText.setText("");
                    heartRateText.setText("");
                    mapText.setText("");
                    progress.setVisibility(View.GONE);
                }
            }
        });
    }

    // To check cuff replacement is reset or not.
    private void alertDialogForReset() {
        builder1 = new AlertDialog.Builder(ReadingData.this);
        builder1.setTitle(getApplicationContext().getResources().getString(R.string.message));
        builder1.setMessage(getApplicationContext().getResources().getString(R.string.cuff_replaced));
        // On click ok button reset command will be sent
        builder1.setPositiveButton(getApplicationContext().getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog1, int which) {
//                Log.i(TAG, "onClick: cuff replaced after alert " + Constants.is_cuffReplaced);
//                 byte[] resetValue = {0x7B,0x00,0x00,0x00,0x01,0x02,0x0A,0x00,0x01,0x00,0x0E,0x7D};
                Constants.resetValue = decoder.computeCheckSum(Constants.resetValue);
//          Log.i(TAG, "Reset value after checksum " + Arrays.toString(Constants.resetValue) + " " + Constants.resetValue);
                mBluetoothLeService.writeCharacteristics(mNotifyCharacteristic, Constants.resetValue);

//                Log.i(TAG, "run: ack in ok " + Constants.is_ackReceived);
                setTimerForResetVal();

            }
        });
        // On click ok button noreset command will be sent
        builder1.setNegativeButton(getApplicationContext().getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                Constants.noResetValue = decoder.computeCheckSum(Constants.noResetValue);
//          Log.i(TAG, "Reset value after checksum " + Arrays.toString(Constants.resetValue) + " " + Constants.resetValue);
                mBluetoothLeService.writeCharacteristics(mNotifyCharacteristic, Constants.noResetValue);
//                Log.i(TAG, "run: ack in ok before timer " + Constants.is_ackReceived);

                // To check the ack after the reset command sent
                mCountDownTimer = new CountDownTimer(100, 50) {
                    @Override
                    public void onTick(long l) {
                        counter++;
//                                                Log.i(TAG, "counter Started " + startTime);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {

                                mCountDownTimer = new CountDownTimer(20, 10)
                                {
                                    public void onTick(long millisUntilFinished)
                                    {
                                    }
                                    public void onFinish()
                                    {
//                                        Log.i(TAG, "run: ack in cancel alert " + Constants.is_ackReceived);
                                        if (Constants.is_ackReceived == true) {
                                            mCountDownTimer.cancel();
//                                    Log.i(TAG, "run: ack in cancel condition " + Constants.is_ackReceived);
                                            dialog1.dismiss();
//
                                        }
//
                                    }
                                };
                                mCountDownTimer.start();
                            }
                        });
                    }

                    @Override
                    public void onFinish() {
//                                                Log.i(TAG, "Stopped");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // If ack not received
                                if (Constants.is_ackReceived == false){
//                                                Log.i(TAG, "Start again");
                                    dialog1.show();
                                    Constants.noResetValue = decoder.computeCheckSum(Constants.noResetValue);
////          Log.i(TAG, "Reset value after checksum " + Arrays.toString(Constants.resetValue) + " " + Constants.resetValue);
                                    mBluetoothLeService.writeCharacteristics(mNotifyCharacteristic, Constants.noResetValue);
                                    start();
                                }
                                Constants.is_ackReceived = false;
                            }
                        });
                    }
                }.start();

            }
        });
        //To create alert dialog
        dialog1 = builder1.create();
        //Prevent dialog box from getting dismissed on back key pressed
        dialog1.setCancelable(false);
        //Prevent dialog box from getting dismissed on outside touch
        dialog1.setCanceledOnTouchOutside(false);
        //To show alert dialog
        dialog1.show();
    }

    // To check the ack after the reset command sent
    public void setTimerForResetVal() {
        mCountDownTimer = new CountDownTimer(100, 50) {
            @Override
            public void onTick(long l) {
                counter++;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                                if (Constants.is_ackReceived == true) {
                                    mCountDownTimer.cancel();
//                            Log.i(TAG, "run: ack in condition " + Constants.is_ackReceived);
//
                                    dialog1.dismiss();
                                }
//
                    }
                });
            }

            @Override
            public void onFinish() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // If ack not received
                        if (Constants.is_ackReceived == false){
                            dialog1.show();
                            Constants.resetValue = decoder.computeCheckSum(Constants.resetValue);
////          Log.i(TAG, "Reset value after checksum " + Arrays.toString(Constants.resetValue) + " " + Constants.resetValue);
                            mBluetoothLeService.writeCharacteristics(mNotifyCharacteristic, Constants.resetValue);
                            start();
                        }
//
                    }
                });
            }
        }.start();
//
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
//        dialog1.dismiss();
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
//        dialog1.dismiss();
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
                    .setTitle(getApplicationContext().getResources().getString(R.string.message))
                    .setMessage(getApplicationContext().getResources().getString(R.string.are_you_sure_disable_bluetooth))
                    .setPositiveButton(getApplicationContext().getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
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
                    .setNegativeButton(getApplicationContext().getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
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
            boolean mConnected;
            if (Constants.ACTION_GATT_CONNECTED.equals(action)) {
                mConnected = true;
                updateConnectionState(getApplicationContext().getResources().getString(R.string.connected));
            }
//            else if (Constants.ACTION_GATT_CONNECTING.equals(action)) {
//
//            }
            else if (Constants.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                updateConnectionState(getApplicationContext().getResources().getString(R.string.disconnected));
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
//                Log.i(TAG, "onReceive: intent value in broadcast" + intent.getStringExtra(Constants.EXTRA_DATA));
                displayData(intent.getStringExtra(Constants.EXTRA_DATA));
            }
        }
    };

    private  void displayData(String data) {

//        Toast.makeText(getApplicationContext(), "received data before" + data, Toast.LENGTH_SHORT).show();
        if (data != null) {
//            Log.i(TAG, "received data before " + data);
//            Toast.makeText(getApplicationContext(), "received data after" + data, Toast.LENGTH_SHORT).show();
//           Log.i(TAG, "reading started and result received " + Constants.is_readingStarted + " " + Constants.is_resultReceived);
            mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                    mTimeLeftInMillis = millisUntilFinished;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progress.setVisibility(View.GONE);

                            if (Constants.is_ackReceived == true)
                            {
                                mCountDownTimer.cancel();
                                startBtn.setEnabled(false);
                                startBtn.setVisibility(View.INVISIBLE);
                                stopBtn.setVisibility(View.VISIBLE);
                                stopBtn.setEnabled(true);
                                progressText.setText(data);

                                if (Constants.is_finalResult == true) {
                                    if ((mBluetoothLeService.systalic < 30) || (mBluetoothLeService.systalic > 200)){
                                    Toast.makeText(getApplicationContext(), getApplication().getResources().getString(R.string.systolic_error), Toast.LENGTH_SHORT).show();
                                }
                                else if ((mBluetoothLeService.dystolic < 40) || (mBluetoothLeService.dystolic > 120)) {
                                    Toast.makeText(getApplicationContext(), getApplication().getResources().getString(R.string.diastolic_error), Toast.LENGTH_SHORT).show();
                                }
                                else {
                                        progressText.setText(data);
                                }
                                }
//                                startBtn.setText("Cancel");
//                                if (counter < mTimeLeftInMillis) {
//                                    counter = counter++;
//                                    Handler handler = new Handler();
//                                    handler.post(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            progressBar.setProgress(Integer.parseInt(data));
//                                        }
//                                    });
//                                    try {
//                                        Thread.sleep(8);
//                                    }catch (InterruptedException e) {
//                                        e.printStackTrace();
//                                    }
////
//////                                    Log.i(TAG, "timer in readings " + (int) mTimeLeftInMillis);
//                                }
//                                Constants.is_readingStarted = false;
                            }

                            if ((Constants.is_resultReceived == true) || (Constants.is_readingStarted == true)) {
                                mCountDownTimer = new CountDownTimer(startTime, 10) {
                                    @Override
                                    public void onTick(long l) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
//                                                Log.i(TAG, "run: cuff replaced before condion in displayDate " + Constants.is_cuffReplaced);
                                                if (Constants.is_cuffReplaced == true) {
                                                    mCountDownTimer.cancel();
                                                    progressText.setText("");
//                                                    Log.i(TAG, "run: cuff replaced before alert start " + Constants.is_cuffReplaced);
                                                    alertDialogForReset();
                                                    Constants.is_cuffReplaced = false;

                                                }
                                            }
                                        });
                                    }

                                    @Override
                                    public void onFinish() {
                                        if ((Constants.is_resultReceived == false) || (Constants.is_readingStarted == false)) {
//                                            Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.please_start_again), Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }.start();
                            }
                        }
                    });
                }

                @Override
                public void onFinish() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (Constants.is_ackReceived == false){
                                mCountDownTimer.cancel();
                                Toast.makeText(ReadingData.this,getApplicationContext().getResources().getString(R.string.please_start_again),Toast.LENGTH_SHORT).show();
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
                            Toast.makeText(ReadingData.this, getApplicationContext().getResources().getString(R.string.please_connect_again),Toast.LENGTH_SHORT).show();
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
            Toast.makeText(getApplicationContext(), getApplicationContext().getResources().getString(R.string.battery_low),Toast.LENGTH_SHORT).show();
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
                boolean result = mBluetoothLeService.connect(deviceAddress);
                if (result == false) {
                    finish();
                }
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
                            .setTitle(getApplicationContext().getResources().getString(R.string.message))
                            .setMessage(getApplicationContext().getResources().getString(R.string.connection_terminated))
                            .setPositiveButton(getApplicationContext().getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {
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