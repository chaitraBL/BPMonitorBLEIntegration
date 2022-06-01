package com.example.bpmonitorbleintegration;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.util.List;
import java.util.Objects;

public class ManualReadings extends AppCompatActivity {
    EditText systolic, diastolic, heartRate;
    Button save;
    Decoder decoder;
    RoomDB database;
    int map = 0;
//    RecyclerView manualList;
    String TAG = ManualReadings.class.getName();
    ProgressBar progressBar;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_readings);

        systolic = findViewById(R.id.manual_systa);
        diastolic = findViewById(R.id.manual_diasta);
        heartRate = findViewById(R.id.manual_heartRate);
        save = findViewById(R.id.save_manual);
//        manualList = findViewById(R.id.recyclerview_list);
        progressBar = findViewById(R.id.progress_manual);
//        manualList.setLayoutManager(new LinearLayoutManager(this));
        decoder = new Decoder();
        database = new RoomDB();

        ActionBar actioBar = getSupportActionBar();
        Objects.requireNonNull(actioBar).setTitle(R.string.manual_reading);
        actioBar.setHomeAsUpIndicator(R.drawable.ic_baseline_keyboard_arrow_left_24);
        actioBar.setDisplayHomeAsUpEnabled(true);

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                //Method 1: Validating the edit text fields.
//                if(TextUtils.isEmpty(strUserName)) {
//                    etUserName.setError("Your message");
//                    return;
//                }

                //Method 2: Validating the edit text fields.
                if (systolic.getText().toString().equals(""))
                {
                    Toast.makeText(getApplicationContext(),"Please enter systolic value",Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
                else if (diastolic.getText().toString().equals(""))
                {
                    Toast.makeText(getApplicationContext(),"Please enter diastolic value",Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
                else if (heartRate.getText().toString().equals(""))
                {
                    Toast.makeText(getApplicationContext(),"Please enter heart rate value",Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
                else {
                    //Calculates mean arterial pressure(MAP) value.
                    map = decoder.calculateMAP(Integer.parseInt(systolic.getText().toString()),Integer.parseInt(diastolic.getText().toString()));
                    //Saves to local database.
                    database.saveTask("No device",Integer.parseInt(systolic.getText().toString()),Integer.parseInt(diastolic.getText().toString()),Integer.parseInt(heartRate.getText().toString()),map,ManualReadings.this);
                    //After saving data make textfield empty.
                    systolic.setText("");
                    diastolic.setText("");
                    heartRate.setText("");
                    progressBar.setVisibility(View.GONE);
                }

            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            this.finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}