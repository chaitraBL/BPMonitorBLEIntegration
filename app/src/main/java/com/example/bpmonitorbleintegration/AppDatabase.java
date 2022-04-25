package com.example.bpmonitorbleintegration;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {BloodPressureDB.class}, version = 1)
public abstract class AppDatabase extends RoomDatabase
{
    public abstract BpReadingsDao bpReadingsDao();
}
