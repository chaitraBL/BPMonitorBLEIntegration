package com.example.bpmonitorbleintegration;

import android.hardware.lights.LightsManager;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface AverageReadingsDoa {
    @Query("SELECT * FROM AverageBPDB")
    List<AverageBPDB> getAll();

    @Insert
    void insert(AverageBPDB readingsDB);
}
