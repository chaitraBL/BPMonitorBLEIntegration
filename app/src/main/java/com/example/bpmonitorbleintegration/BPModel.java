package com.example.bpmonitorbleintegration;

public class BPModel {
    String name;
    String date;
    String time;
    int systa;
    int diasta;
    int heartRate;
    int map;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public int getSysta() {
        return systa;
    }

    public void setSysta(int systa) {
        this.systa = systa;
    }

    public int getDiasta() {
        return diasta;
    }

    public void setDiasta(int diasta) {
        this.diasta = diasta;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }

    public int getMap() {
        return map;
    }

    public void setMap(int map) {
        this.map = map;
    }
}
