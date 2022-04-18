package com.example.bpmonitorbleintegration;

public interface DecodeListener {
    void pressureValue(int value);
    void pulseValue(int value);
    void deviceId(int deviceId);
    void data1(int value);
    void data2(int value);
    void data3(int value);
    void data4(int value);
}
