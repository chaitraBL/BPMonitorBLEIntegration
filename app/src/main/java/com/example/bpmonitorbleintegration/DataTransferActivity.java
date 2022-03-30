package com.example.bpmonitorbleintegration;

import static com.example.bpmonitorbleintegration.R.layout.activity_data_transfer;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.UUID;

public class DataTransferActivity extends AppCompatActivity {

    Button readBtn, writeBtn;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGatt mBluetoothGatt;
    private BLEService mBluetoothLeService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(activity_data_transfer);

        readBtn = findViewById(R.id.btn_read);
        writeBtn = findViewById(R.id.btn_write);

        readBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mBluetoothLeService != null) {
                    mBluetoothLeService.readCustomCharacteristic();
                }
            }
        });

        writeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mBluetoothLeService != null) {
                    mBluetoothLeService.writeCustomCharacteristic(0xAA);
                }
            }
        });
    }


}