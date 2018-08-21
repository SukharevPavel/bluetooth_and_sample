package ru.mmt.bluetoothand.bluetooth;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.Context;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import static ru.mmt.bluetoothand.bluetooth.BluetoothConstants.Service.CUSTOM_SERVICE;

/**
 * Api for controlling bluetooth connection
 */
@RequiresApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public class BluetoothController {

    private static BluetoothController instance;
    private final Context context;
    private IDeviceScanner deviceScanner;
    private BluetoothPairingCallback pairingCallback;
    private BluetoothMeasurementCallback measurementCallback;
    private BluetoothGatt bluetoothGatt;
    private HeartRateResults currentResults;
    private boolean isLoading = false;
    private BluetoothGattCallback bluetoothGattReadCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i(BluetoothController.class.getSimpleName(), "onConnectionStateChange " + newState);
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                if (measurementCallback != null) {
                    if (currentResults == null) {
                        measurementCallback.onNoResults();
                    } else {
                        measurementCallback.onSuccess(currentResults);
                    }
                    switchLoading(false);
                }
            }
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                bluetoothGatt.discoverServices();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i(BluetoothController.class.getSimpleName(), "onServicesDiscovered");
            setDateTime();
            super.onServicesDiscovered(gatt, status);

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.i(BluetoothController.class.getSimpleName(), "onCharacteristicWrite");
            readFromDevice();
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.i(BluetoothController.class.getSimpleName(), "onCharacteristicChanged");

            currentResults = new HeartRateResults(characteristic.getValue());
        }
    };
    private BluetoothGattCallback bluetoothGattPairCallback = new BluetoothGattCallback() {


        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.i(BluetoothController.class.getSimpleName(), "onConnectionStateChange " + newState);
            if (newState == BluetoothGatt.STATE_CONNECTED) {
                bluetoothGatt.discoverServices();
            }
            if (newState == BluetoothGatt.STATE_DISCONNECTED) {
                switchLoading(false);
                if (bluetoothGatt.getDevice().getBondState() == BluetoothDevice.BOND_BONDED &&
                        pairingCallback != null) {
                    pairingCallback.onBounded();
                }
            }

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            Log.i(BluetoothController.class.getSimpleName(), "onCharacteristicWrite");
            sendDisconnectCommand();
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            Log.i(BluetoothController.class.getSimpleName(), "onCharacteristicChanged");
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);
            Log.i(BluetoothController.class.getSimpleName(), "onDescriptorRead");
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);
            Log.i(BluetoothController.class.getSimpleName(), "onDescriptorWrite");

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i(BluetoothController.class.getSimpleName(), "onServicesDiscovered");
            super.onServicesDiscovered(gatt, status);
            setDateTime();
           // sendDisconnectCommand();
        }

    };

    private BluetoothController(Context context) {
        this.context = context.getApplicationContext();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
        deviceScanner = new LegacyDeviceScanner(context.getApplicationContext());
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                deviceScanner = new LollipopDeviceScanner(context.getApplicationContext());
            }
        }
    }

    public static BluetoothController getInstance(Context context) {
        if (instance == null) {
            instance = new BluetoothController(context.getApplicationContext());
        }
        return instance;
    }

    private void setDateTime(){
        Log.i(BluetoothController.class.getSimpleName(), "setDateTime");
        BluetoothGattCharacteristic characteristic = bluetoothGatt
                .getService(BluetoothConstants.Service.BLOOD_PRESSURE_SERVICE_UUID)
                .getCharacteristic(BluetoothConstants.Characteristic.DATE_CHARACTERISTIC_UUID);
        characteristic.setValue(generateCurrentTimeByteArray());
        bluetoothGatt.writeCharacteristic(characteristic);
    }

    private byte[] generateCurrentTimeByteArray(){
        Calendar calendar = new GregorianCalendar();
        calendar.setTime(new Date());
        short shortYear = Short.reverseBytes((short) calendar.get(Calendar.YEAR));
        byte[] byteArray = new byte[7];
        byteArray[0] = (byte) ((shortYear >> 8) & 0xff);
        byteArray[1] = (byte) (shortYear & 0xff);

        byteArray[2] = (byte) (calendar.get(Calendar.MONTH));

        byteArray[3] = (byte) calendar.get(Calendar.DAY_OF_MONTH);

        byteArray[4] = (byte) calendar.get(Calendar.HOUR_OF_DAY);

        byteArray[5] = (byte) calendar.get(Calendar.MINUTE);

        byteArray[6] = (byte) calendar.get(Calendar.SECOND);
        return byteArray;
    }

    private void sendDisconnectCommand() {
        Log.i(BluetoothController.class.getSimpleName(), "sendDisconnectCommand");
        BluetoothGattCharacteristic characteristic = bluetoothGatt
                .getService(CUSTOM_SERVICE)
                .getCharacteristic(BluetoothConstants.Characteristic.CUSTOM_CHARACTERISTIC);
        characteristic.setValue(BluetoothConstants.Commands.DISCONNECT);
        bluetoothGatt.writeCharacteristic(characteristic);
    }

    private void readFromDevice() {
        Log.i(BluetoothController.class.getSimpleName(), "readFromDevice");

        BluetoothGattCharacteristic heartRateCharacteristic = bluetoothGatt
                .getService(BluetoothConstants.Service.BLOOD_PRESSURE_SERVICE_UUID)
                .getCharacteristic(BluetoothConstants.Characteristic.BLOOD_PRESSURE_CHARACTERISTIC);
        bluetoothGatt.setCharacteristicNotification(heartRateCharacteristic, true);

        BluetoothGattDescriptor descriptor = heartRateCharacteristic.
                getDescriptor(BluetoothConstants.Descriptor.CLIENT_CHARACTERISTIC_CONFIGURATION_UUID);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_INDICATION_VALUE);
        bluetoothGatt.writeDescriptor(descriptor);
    }

    public void setPairingCallback(BluetoothPairingCallback pairingCallback) {
        this.pairingCallback = pairingCallback;
    }

    public void setMeasurementCallback(BluetoothMeasurementCallback measurementCallback) {
        this.measurementCallback = measurementCallback;
    }

    private void switchLoading(boolean newValue) {
        if (isLoading != newValue) {
            isLoading = newValue;
            if (pairingCallback != null) {
                pairingCallback.onChangeStatus(isLoading);
            }
        }
    }

    public void getMeasurements() {
        Log.i(BluetoothController.class.getSimpleName(), "getMeasurements");
        if (deviceScanner == null) {
            if (measurementCallback != null) {
                measurementCallback.onBluetoothNotSupported();
            }
            return;
        }
        currentResults = null;
        deviceScanner.getHeartRateDevice(new IDeviceScanner.Callback() {
            @Override
            public void onBluetoothNotSupported() {
                if (measurementCallback != null) {
                    measurementCallback.onBluetoothNotSupported();
                }
            }

            @Override
            public void onDeviceFound(BluetoothDevice device) {
                readDevice(device);
            }

            @Override
            public void onNoDeviceFound() {
                if (measurementCallback != null) {
                    measurementCallback.onConnectionFailed();
                }
            }

            @Override
            public void onBluetoothDisabled() {
                if (measurementCallback != null) {
                    measurementCallback.onBluetoothDisabled();
                }
            }

            @Override
            public void onBusy() {
                if (measurementCallback != null) {
                    measurementCallback.onBusy();
                }
            }
        });
    }

    public void connectDevice() {
        Log.i(BluetoothController.class.getSimpleName(), "connectDevice");
        if (deviceScanner == null) {
            if (pairingCallback != null) {
                pairingCallback.onBluetoothNotSupported();
            }
            return;
        }
        switchLoading(true);
        deviceScanner.getHeartRateDevice(new IDeviceScanner.Callback() {
            @Override
            public void onBluetoothNotSupported() {
                if (pairingCallback != null) {
                    switchLoading(false);
                    pairingCallback.onBluetoothNotSupported();
                }
            }

            @Override
            public void onDeviceFound(BluetoothDevice device) {
                pairDevice(device);
            }

            @Override
            public void onNoDeviceFound() {
                if (pairingCallback != null) {
                    switchLoading(false);
                    pairingCallback.onPairingDeviceFailed();
                }
            }

            @Override
            public void onBluetoothDisabled() {
                if (pairingCallback != null) {
                    switchLoading(false);
                    pairingCallback.onBluetoothDisabled();
                }
            }

            @Override
            public void onBusy() {
                if (pairingCallback != null) {
                    switchLoading(false);
                    pairingCallback.onBusy();
                }
            }
        });
    }

    private void readDevice(BluetoothDevice device) {
        Log.i(BluetoothController.class.getSimpleName(), "readDevice");
        bluetoothGatt = device.connectGatt(context, false, bluetoothGattReadCallback);
    }

    private void pairDevice(BluetoothDevice device) {
        Log.i(BluetoothController.class.getSimpleName(), "pairDevice");
        bluetoothGatt = device.connectGatt(context, false, bluetoothGattPairCallback);
    }

    public boolean isLoading() {
        return isLoading;
    }

    public interface BluetoothPairingCallback {

        void onBluetoothDisabled();
        
        void onBluetoothNotSupported();

        void onBounded();

        void onBusy();

        void onPairingDeviceFailed();

        void onChangeStatus(boolean isLoading);
    }
    
    public interface BluetoothMeasurementCallback {
        void onSuccess(HeartRateResults results);

        void onConnectionFailed();

        void onNoResults();

        void onBluetoothDisabled();

        void onBluetoothNotSupported();

        void onBusy();
    }

    public class HeartRateResults {

        private int systolicPressure;

        private int diasystolicPressure;

        private int meanPressure;

        private int pulsePressure;

        HeartRateResults(byte[] message) {
            StringBuilder builder= new StringBuilder("{");
            for (int i=0;i<message.length;i++) {
                builder.append(message[i]+",");
            }
            builder.append("}");
            Log.i(BluetoothController.class.getName(), builder.toString());
            systolicPressure = Math.abs(message[1]);
            diasystolicPressure =  Math.abs(message[3]);
            meanPressure = Math.abs(message[5]);
            pulsePressure = Math.abs(message[14]);
        }

        public int getSystolicPressure() {
            return systolicPressure;
        }

        public int getDiasystolicPressure() {
            return diasystolicPressure;
        }

        public int getMeanPressure() {
            return meanPressure;
        }

        public int getPulse() {
            return pulsePressure;
        }
    }
}
