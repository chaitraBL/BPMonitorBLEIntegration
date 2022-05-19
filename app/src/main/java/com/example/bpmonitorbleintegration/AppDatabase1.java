package com.example.bpmonitorbleintegration;

import androidx.room.Database;
import androidx.room.RoomDatabase;

@Database(entities = {AverageBPDB.class}, version = 1)
abstract class AppDatabase1 extends RoomDatabase {
    public abstract AverageReadingsDoa averageReadingsDoa();
}
