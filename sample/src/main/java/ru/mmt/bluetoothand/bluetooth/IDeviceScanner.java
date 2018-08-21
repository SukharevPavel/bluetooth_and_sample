package ru.mmt.bluetoothand.bluetooth;

import android.bluetooth.BluetoothDevice;

/**
 * Created by pasha on 11.07.17.
 */

interface IDeviceScanner {

    int SCAN_PERIOD = 5000;

    void getHeartRateDevice(Callback callback);

    interface Callback {
        void onBluetoothNotSupported();

        void onDeviceFound(BluetoothDevice device);

        void onNoDeviceFound();

        void onBluetoothDisabled();

        void onBusy();
    }
}
