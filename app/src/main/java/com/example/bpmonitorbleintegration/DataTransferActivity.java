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
import android.bluetooth.BluetoothGattServer;
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
import android.os.Looper;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
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

    Button readBtn;
//    RecyclerView recyclerView;
    Decoder decoder;
    public AlertDialog dialog;
    RoomDB localDB;
    ProgressBar progress;
    String resultData;

    public int counter = 0;
    private CountDownTimer mCountDownTimer;
    private boolean mTimerRunning;
    private long mTimeLeftInMillis = 2000;
    private long startTime = 100;
    View customView;

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
//        recyclerView = findViewById(R.id.recyclerview_tasks);
        batteryLevel = findViewById(R.id.battery_level);
        progress = findViewById(R.id.progress_start);

//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        intentFilter = new IntentFilter(BluetoothDevice.ACTION_PAIRING_REQUEST);
        intentFilter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver(broadCastReceiver, intentFilter);

        //Initialising Decoder class.
        decoder = new Decoder();
        localDB = new RoomDB();

        getSupportActionBar().setTitle("Readings");

        pref = PreferenceManager.getDefaultSharedPreferences(this);

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

        //Send request to START the readings.
        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mNotifyCharacteristic != null) {
//                    Log.i(TAG, "Start value " + Arrays.toString(Constants.startValue) + " " + Constants.startValue);
                    Constants.startValue = decoder.computeCheckSum(Constants.startValue);
//                    Log.i(TAG, "Start value after checksum " + Arrays.toString(Constants.startValue) + " " + Constants.startValue);
                    mBluetoothLeService.writeCharacteristics(mNotifyCharacteristic, Constants.startValue);
                }
//
                mCountDownTimer = new CountDownTimer(mTimeLeftInMillis, 1000) {
                    @Override
                    public void onTick(long l) {
                        counter++;
                        mCountDownTimer = new CountDownTimer(startTime,100) {
                            @Override
                            public void onTick(long l) {
                                counter++;
                            }

                            @Override
                            public void onFinish() {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (Constants.is_ackReceived == true) {
                                            mCountDownTimer.cancel();
//                                                Constants.is_ackReceived = false;

                                                //Alert controller to start readings.
                                                builder = new AlertDialog.Builder(DataTransferActivity.this);
                                                builder.setTitle("Readings");
                                                LayoutInflater layoutInflater = getLayoutInflater();

                                                //this is custom dialog
                                                //custom_popup_dialog contains textview only
                                                View customView = layoutInflater.inflate(R.layout.custom_popup_dialog, null);
//                                    LinearLayout attLayout = customView.findViewById(R.id.att_layout);
                                                // reference the textview of custom_popup_dialog
                                                tv = customView.findViewById(R.id.tvpopup);
                                                tv.setTextSize(15);
//                                    tv.setText(resultData);

                                                //Send request to force STOP the readings.
                                                builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        // Nothing done here.
                                                    }
                                                });

                                                //Send Ack - received readings.
                                                builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        if (mNotifyCharacteristic != null) {
                                                            Constants.cancelValue = decoder.computeCheckSum(Constants.cancelValue);
//                            Log.i(TAG, "Stop value after checksum " + Arrays.toString(Constants.startValue) + " " + Constants.startValue);
                                                            mBluetoothLeService.writeCharacteristics(mNotifyCharacteristic, Constants.cancelValue);

                                                            mCountDownTimer = new CountDownTimer(startTime, 50) {
                                                                @Override
                                                                public void onTick(long l) {
                                                                    counter++;
//                                                Log.i(TAG, "counter Started " + startTime);
                                                                    runOnUiThread(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            if (Constants.is_ackReceived == true) {
                                                                                mCountDownTimer.cancel();
                                                                                dialog.dismiss();
                                                                                Constants.is_readingStarted = false;
                                                                                Constants.is_resultReceived = false;
//                                                                                Constants.is_ackReceived = false;

                                                                                Log.i(TAG, "run: cuff replaced " + Constants.is_cuffReplaced);
                                                                                if (Constants.is_cuffReplaced == true) {
                                                                                    alertDialogForReset();
                                                                                }
                                                                            }

                                                                        }
                                                                    });
                                                                }

                                                                @Override
                                                                public void onFinish() {
//                                                Log.i(TAG, "Stopped");
                                                                    runOnUiThread(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            if (Constants.is_ackReceived == false){
//                                                Log.i(TAG, "Start again");
                                                                                dialog.show();
                                                                                Constants.cancelValue = decoder.computeCheckSum(Constants.cancelValue);
//                            Log.i(TAG, "Stop value after checksum " + Arrays.toString(Constants.startValue) + " " + Constants.startValue);
                                                                                mBluetoothLeService.writeCharacteristics(mNotifyCharacteristic, Constants.cancelValue);
                                                                                start();
                                                                            }
//
                                                                        }
                                                                    });
                                                                }
                                                            }.start();
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
                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO) {
                                                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                                    @Override
                                                    public void onShow(DialogInterface dialogInterface) {
                                                        ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(false);
                                                    }
                                                });
                                            }
                                            dialog.show();

                                                //Send request to force STOP the readings WRT: timer.
                                                dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
                                                    @Override
                                                    public void onClick(View view) {
                                                        if (mNotifyCharacteristic != null) {
                                                            Constants.cancelValue = decoder.computeCheckSum(Constants.cancelValue);
//                            Log.i(TAG, "Force stop value after checksum " + Arrays.toString(Constants.startValue) + " " + Constants.startValue);
                                                            mBluetoothLeService.writeCharacteristics(mNotifyCharacteristic, Constants.cancelValue);

                                                            mCountDownTimer = new CountDownTimer(startTime, 100) {
                                                                @Override
                                                                public void onTick(long l) {
                                                                    counter++;
//                                            Log.i(TAG, "counter Started " + startTime);
                                                                    runOnUiThread(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            if (Constants.is_ackReceived == true) {
                                                                                mCountDownTimer.cancel();
                                                                                dialog.dismiss();
                                                                            Constants.is_readingStarted = false;
//                                                                            Constants.is_ackReceived = false;
                                                                            }
                                                                        }
                                                                    });
                                                                }

                                                                @Override
                                                                public void onFinish() {
//                                            Log.i(TAG, "Stopped");
                                                                    runOnUiThread(new Runnable() {
                                                                        @Override
                                                                        public void run() {
                                                                            if (Constants.is_ackReceived == false){
//                                                Log.i(TAG, "Start again");
                                                                                dialog.show();
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
                                                });
//                                                mCountDownTimer.cancel();
//                                                Constants.is_ackReceived = false;
                                        }
                                    }
                                });
                            }
                        }.start();
//                        Log.i(TAG, "counter Started " + mTimeLeftInMillis);
                    }

                    @Override
                    public void onFinish() {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (Constants.is_ackReceived == false){
                                    mCountDownTimer.cancel();
                                    Toast.makeText(getApplicationContext(), "Please start again", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                }.start();
            }
        });

        // To read final values and store it to local DB.
        readBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progress.setVisibility(View.VISIBLE);
                systolicText.setText(String.valueOf(mBluetoothLeService.systalic));
                diastolicText.setText(String.valueOf(mBluetoothLeService.dystolic));
                heartRateText.setText(String.valueOf(mBluetoothLeService.rate));
                rangeText.setText(String.valueOf(mBluetoothLeService.range));

                if (systolicText.getText().toString().equals(" ")) {
                    Toast.makeText(getApplicationContext(),"Please enter systolic value",Toast.LENGTH_SHORT).show();
                }
                else if (diastolicText.getText().toString().equals(" ")) {
                    Toast.makeText(getApplicationContext(),"Please enter diastolic value",Toast.LENGTH_SHORT).show();
                }
                else if (heartRateText.getText().toString().equals(" ")) {
                    Toast.makeText(getApplicationContext(),"Please enter heart rate value",Toast.LENGTH_SHORT).show();
                }
                else if (rangeText.getText().toString().equals(" ")) {
                    Toast.makeText(getApplicationContext(),"Please enter MAP value",Toast.LENGTH_SHORT).show();
                }
                else {
                    localDB.saveTask(deviceAddress, mBluetoothLeService.systalic, mBluetoothLeService.dystolic, mBluetoothLeService.rate, mBluetoothLeService.range, DataTransferActivity.this);
                }
                progress.setVisibility(View.GONE);
            }
        });
    }

    private void alertDialogForReset() {
        dialog = new AlertDialog.Builder(DataTransferActivity.this)
                .setTitle("Message")
                .setMessage("Have you replaced the cuff")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                            mCountDownTimer.cancel();
                            dialog.dismiss();
                            Constants.resetValue = decoder.computeCheckSum(Constants.resetValue);
//          Log.i(TAG, "Reset value after checksum " + Arrays.toString(Constants.resetValue) + " " + Constants.resetValue);
                            mBluetoothLeService.writeCharacteristics(mNotifyCharacteristic, Constants.resetValue);


                            mCountDownTimer = new CountDownTimer(startTime, 50) {
                                @Override
                                public void onTick(long l) {
                                    counter++;
//                                                Log.i(TAG, "counter Started " + startTime);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (Constants.is_ackReceived == true) {
                                                mCountDownTimer.cancel();
                                                dialog.dismiss();
                                                Constants.is_cuffReplaced = false;
                                            }

                                        }
                                    });
                                }

                                @Override
                                public void onFinish() {
//                                                Log.i(TAG, "Stopped");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (Constants.is_ackReceived == false){
//                                                Log.i(TAG, "Start again");
//                                                dialog.show();
                                                Constants.resetValue = decoder.computeCheckSum(Constants.resetValue);
////          Log.i(TAG, "Reset value after checksum " + Arrays.toString(Constants.resetValue) + " " + Constants.resetValue);
                                                mBluetoothLeService.writeCharacteristics(mNotifyCharacteristic, Constants.resetValue);
                                                start();
                                            }
                                        }
                                    });
                                }
                            }.start();

                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                            mCountDownTimer.cancel();
                            dialog.dismiss();
                            Constants.resetValue = decoder.computeCheckSum(Constants.resetValue);
//          Log.i(TAG, "Reset value after checksum " + Arrays.toString(Constants.resetValue) + " " + Constants.resetValue);
                            mBluetoothLeService.writeCharacteristics(mNotifyCharacteristic, Constants.resetValue);


                            mCountDownTimer = new CountDownTimer(startTime, 50) {
                                @Override
                                public void onTick(long l) {
                                    counter++;
//                                                Log.i(TAG, "counter Started " + startTime);
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (Constants.is_ackReceived == true) {
                                                mCountDownTimer.cancel();
                                                dialog.dismiss();
                                                Constants.is_cuffReplaced = false;
                                            }

                                        }
                                    });
                                }

                                @Override
                                public void onFinish() {
//                                                Log.i(TAG, "Stopped");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (Constants.is_ackReceived == false){
//                                                Log.i(TAG, "Start again");
                                                dialog.show();
                                                Constants.noResetValue = decoder.computeCheckSum(Constants.noResetValue);
////          Log.i(TAG, "Reset value after checksum " + Arrays.toString(Constants.resetValue) + " " + Constants.resetValue);
                                                mBluetoothLeService.writeCharacteristics(mNotifyCharacteristic, Constants.noResetValue);
                                                start();
                                            }
                                        }
                                    });
                                }
                            }.start();

                    }
                }).show();
    }

    //To delete the memory cache.
//    public void deleteCache(Context context) {
//        try {
//            File dir = context.getCacheDir();
//            if (dir.list() != null) {
//                deleteDir2(dir);
//            }
//        } catch (Exception e) { e.printStackTrace();}
//    }
//
//    public boolean deleteDir2(File dir) {
//        if (dir.isDirectory()) {
//            for (File child : dir.listFiles()) {
//                boolean success = deleteDir2(child);
//                if (!success) {
//                    return false;
//                }
//            }
//        }
//        return dir.delete();
//    }

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
    }

    @Override
    protected void onStart() {
        super.onStart();
        //To bind the service connection.
        Intent getServiceIntent = new Intent(DataTransferActivity.this, BLEService.class);
        bindService(getServiceIntent, mServiceConnection, BIND_AUTO_CREATE);

        progress.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Unbind service connection.
        unbindService(mServiceConnection);
    }

//    @Override
//    protected void onDestroy() {
//        super.onDestroy();
//        deleteCache(getApplicationContext());
//    }

    //Menu item.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.data_menu_file, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
//        if (item.getItemId() == R.id.reset) {
//            Constants.resetValue = decoder.computeCheckSum(Constants.resetValue);
////          Log.i(TAG, "Reset value after checksum " + Arrays.toString(Constants.resetValue) + " " + Constants.resetValue);
//            mBluetoothLeService.writeCharacteristics(mNotifyCharacteristic, Constants.resetValue);
//
//            return true;
//        }

        if (item.getItemId() == R.id.disable_bluetooth) {
            dialog = new AlertDialog.Builder(DataTransferActivity.this)
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
                                Intent intent = new Intent(DataTransferActivity.this, HomePage.class);
                                startActivity(intent);
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
                    counter = counter++;
//                    Log.i(TAG, "timer in readings " + mTimeLeftInMillis);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            progress.setVisibility(View.GONE);
                            if (Constants.is_readingStarted == true)
                            {
                                mCountDownTimer.cancel();
                                tv.setText(data);

//                                Constants.is_readingStarted = false;
                            }

                            // Method 1: To enable/disable Ok button on basis of readings.
                            if (Constants.is_resultReceived == true) {
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
                                Toast.makeText(DataTransferActivity.this,"Please start again",Toast.LENGTH_SHORT).show();
                                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE).setEnabled(true);
                                ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(false);
//                                dialog.dismiss();
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
                            Toast.makeText(DataTransferActivity.this,"Something went wrong please connect again!!!",Toast.LENGTH_SHORT).show();
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
            batteryLevel.setBackgroundColor(Color.parseColor("#008000"));
        }
        else if (mBluetoothLeService.batteryLevel == Constants.MID_BATTERY){
            batteryLevel.setBackgroundColor(Color.parseColor("#FFA500"));
        }
        else if (mBluetoothLeService.batteryLevel == Constants.LOW_BATTERY) {
            batteryLevel.setBackgroundColor(Color.parseColor("#FF0000"));
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
                mBluetoothLeService.setHandler(myHandler);
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
                    new AlertDialog.Builder(DataTransferActivity.this)
                            .setTitle("Message")
                            .setMessage("Connection terminated!, please connect again")
                            .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    //Navigating to next activity on tap of ok button.
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                        Intent intent = new Intent(DataTransferActivity.this, MainActivity.class);
                                        startActivity(intent);
                                    }
                                }
                            }).show();
                }
            }
        });
    }
}

