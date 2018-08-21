package ru.mmt.bluetoothand.bluetooth;

import java.util.UUID;

/**
 * Created by pasha on 07.07.17.
 */

public final class BluetoothConstants {

    private BluetoothConstants() {
    }

    public final static class Service {
        public final static UUID BLOOD_PRESSURE_SERVICE_UUID = UUID.fromString("00001810-0000-1000-8000-00805f9b34fb");
        public final static UUID CUSTOM_SERVICE = UUID.fromString("233bf000-5a34-1b6d-975c-000d5690abe4");

        private Service() {
        }
    }

    public final static class Characteristic {

        public final static UUID DATE_CHARACTERISTIC_UUID = UUID.fromString("00002a08-0000-1000-8000-00805f9b34fb");
        public final static UUID CUSTOM_CHARACTERISTIC = UUID.fromString("233bf001-5a34-1b6d-975c-000d5690abe4");
        public final static UUID BLOOD_PRESSURE_CHARACTERISTIC = UUID.fromString("00002a35-0000-1000-8000-00805f9b34fb");

        private Characteristic() {
        }

    }

    public final static class Descriptor {

        public final static UUID CLIENT_CHARACTERISTIC_CONFIGURATION_UUID = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");

        private Descriptor() {
        }

    }

    public final static class Commands {
        public final static byte[] DISCONNECT = new byte[]{0x02, 0x01, 0x03};
    }


}
