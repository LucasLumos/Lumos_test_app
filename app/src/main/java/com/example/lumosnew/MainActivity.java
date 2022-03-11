package com.example.lumosnew;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private static final long SCAN_PERIOD = 2000000000;

    // Three ints indicating different connection status
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;
    private int LConnectionState = STATE_DISCONNECTED;
    private int RConnectionState = STATE_DISCONNECTED;

    public static BluetoothGattDescriptor descriptor;

    List<BluetoothGattCharacteristic> chars = new ArrayList<>();
    List<BluetoothGattCharacteristic> charsR = new ArrayList<>();

    public boolean leftPCBConnected = false;
    public boolean rightPCBConnected = false;

    // Indicating whether this is the first time reading every characteristic
    private boolean LFirstTimeRead = true;
    private boolean RFirstTimeRead = true;

    private byte[] lightValue;

    // The list of characteristics contained by the device
    public static List<BluetoothGattCharacteristic> characteristics;

    // All 6 characteristics in the left device
    private static BluetoothGattCharacteristic buttonChar;
    private static BluetoothGattCharacteristic capChar;
    private static BluetoothGattCharacteristic ledChar;
    private static BluetoothGattCharacteristic alsChar;
    private static BluetoothGattCharacteristic lightChar;
    private static BluetoothGattCharacteristic batteryChar;

    //7 characteristics in the right device
    private static BluetoothGattCharacteristic RbuttonChar;
    private static BluetoothGattCharacteristic RcapChar;
    private static BluetoothGattCharacteristic RledChar;
    private static BluetoothGattCharacteristic RalsChar;
    private static BluetoothGattCharacteristic RlightChar;
    private static BluetoothGattCharacteristic RbatteryChar;
    //private static BluetoothGattCharacteristic RlconnectedChar;

    // Tag used for displaying Toast message
    private final static String TAG = MainActivity.class.getSimpleName();

    // Used for requesting fine location
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;

    // Necessary objects for BLE operations
    private static BluetoothAdapter mBluetoothAdapter;
    private static BluetoothLeScanner mBluetoothLeScanner;
        // Whether the application is currently scanning or not
    private boolean mScanning;

    // The list of devices
    private static List<BluetoothDevice> listBluetoothDevice;

    // For creating a new temporary thread
    private static Handler handler = new Handler();

    // Result of the "connect" method
    private boolean LConnectStatus = false;
    private boolean RConnectStatus = false;

    //private boolean RLConnectStatus = false;

    // Address of the connected device
    private static String LBluetoothDeviceAddress;
    private static String RBluetoothDeviceAddress;

    private static BluetoothGatt LBluetoothGatt;
    private static BluetoothGatt RBluetoothGatt;


    private TextView lals;
    private TextView lbat;
    private TextView rals;
    private TextView rbat;
    private TextView lcon;
    private TextView lcap;
    private TextView rcap;
    private Switch light_switch;
    private SeekBar seekbar_intensity, seekbar_frequency;
    private Button reset_btn;
    private int on_off=0;//1 means on 0means off
    private ImageButton intensity_right,intensity_left,frequency_right,frequency_left;
    private TextView intensity_seek_text,frequency_seek_text;




    private byte[] LEDLValue;
    private byte[] buttonLValue;
    private byte[] capLValue;
    private byte[] alsLValue;
    private byte[] batteryLValue;

    private byte[] LEDRValue;
    private byte[] buttonRValue;
    private byte[] capRValue;
    private byte[] alsRValue;
    private byte[] batteryRValue;
    //private byte[] RLCValue;

    private Button btnOn;
    private Button btnGamma;
    private Button btnOff;
    private Button btnReset;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lcap = (TextView) findViewById(R.id.Cap_left_value);
        rcap = (TextView) findViewById(R.id.Cap_right_value);

        lcon = (TextView) findViewById(R.id.lcon);

        lals = (TextView) findViewById(R.id.ALS_left_value);
        lbat = (TextView) findViewById(R.id.left_battery_text_power_percentage);

        rals = (TextView) findViewById(R.id.ALS_right_value);
        rbat = (TextView) findViewById(R.id.right_battery_text_power_percentage);
        seekbar_intensity = (SeekBar) findViewById(R.id.light_control_light_intensity_seekbar);
        seekbar_frequency = (SeekBar) findViewById(R.id.light_control_light_frequency_seekbar);
        light_switch = (Switch) findViewById(R.id.power);
        reset_btn =(Button) findViewById(R.id.reset_btn);
        seekbar_intensity.setOnSeekBarChangeListener(this);
        seekbar_frequency.setOnSeekBarChangeListener(this);
        reset_btn.setOnClickListener(this);
        light_switch.setOnCheckedChangeListener(this);
        intensity_seek_text = findViewById(R.id.intensity_text);
        frequency_seek_text = findViewById(R.id.frequency_text);

        intensity_right = findViewById(R.id.right_arrow_intensity);
        intensity_left= findViewById(R.id.left_arrow_intensity);
        frequency_right= findViewById(R.id.right_arrow_frequency);
        frequency_left= findViewById(R.id.left_arrow_frequency);
        intensity_right.setOnClickListener(this);
        intensity_left.setOnClickListener(this);
        frequency_right.setOnClickListener(this);
        frequency_left.setOnClickListener(this);


        disconnected();

        // Check if BLE is supported on the device.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this,
                    "BLUETOOTH_LE not supported in this device",
                    Toast.LENGTH_SHORT).show();
            finish();
        }

                /*
        Request for fine location permission. Fine location permission is a must for ble to work on
        Android 10 and upper system.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
        }

        // Obtain bluetooth adapter and scanner.
        try {
            getBluetoothAdapterAndLeScanner();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // Checks if Bluetooth is supported on the device.
        if (mBluetoothAdapter == null) {
            Toast.makeText(this,
                    "bluetoothManager.getAdapter()==null",
                    Toast.LENGTH_SHORT).show();
            finish();
            return;
        }


        scanLeDevice(true);

    }



    // Start to scan devices nearby and then call the "connect" method
    @RequiresApi(api = Build.VERSION_CODES.M)
    private void scanLeDevice(final boolean enable) {
        if (enable) {

            // Stops scanning after a pre-defined scan period.
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBluetoothLeScanner.stopScan(scanCallback);

                    Toast.makeText(MainActivity.this,
                            "Scan timeout",
                            Toast.LENGTH_LONG).show();

                    mScanning = false;
                }
            }, SCAN_PERIOD);

            // Scan only specified device (device with lumosServiceUUID in this case)
            ScanFilter scanFilter =
                    new ScanFilter.Builder()
                            .setServiceUuid(ParcelUuid.fromString(LumosServices.lumosServiceUUID))
                            .build();

            ScanFilter scanFilter2 =
                    new ScanFilter.Builder()
                            .setServiceUuid(ParcelUuid.fromString(LumosServices.lumosRServiceUUID))
                            .build();
            // Other filter can also be added into this list of scanFilter
            List<ScanFilter> scanFilters = new ArrayList<ScanFilter>();
            scanFilters.add(scanFilter);
            scanFilters.add(scanFilter2);

            ScanSettings scanSettings =
                    new ScanSettings.Builder().build();

            // Start to scan devices
            mBluetoothLeScanner.startScan(scanFilters, scanSettings, scanCallback);

            mScanning = true;
        } else {
            // Stop scanning
            mBluetoothLeScanner.stopScan(scanCallback);
            mScanning = false;
        }
    }


    // This object implements scan callback and a series of corresponding operations
    @RequiresApi(api = Build.VERSION_CODES.M)
    private ScanCallback scanCallback = new ScanCallback() {

        // This callback is called after the "startScan" function as the result of it
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);

            // Obtain the address of the device scanned
            BluetoothDevice theDevice = result.getDevice();
            addBluetoothDevice(theDevice);

            Log.d(TAG, "the device name is!!!!!!!!!!" + theDevice.getName());
            String Dname = theDevice.getName();
            Log.d(TAG, "connecting to Left PCB!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
            // Connect to the device using the address

            if (theDevice.getName().equals("PCBPeripheral")){
                //if (RLConnectStatus){
                    LConnectStatus = connectL(theDevice.getAddress());
                //}
            }else if (theDevice.getName().equals("LumosRightPCBCentral")){
                RConnectStatus = connectR(theDevice.getAddress());
            }else {
                Log.d(TAG, "not connecting but!!!!!!!1" + Dname);
            }
        }

        // This callback is called after the "startScan" function failed
        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            //Toast.makeText(MainActivity.this,
            //        "onScanFailed: " + String.valueOf(errorCode),
            //        Toast.LENGTH_LONG).show();
            //scanLeDevice(true);
        }

        /*
        This method does not belong to scanCallBack, it adds the device to the list and update UI
         */
        private void addBluetoothDevice(BluetoothDevice device){
            //if(!listBluetoothDevice.contains(device)){
            //    listBluetoothDevice.add(device);
            //}
        }
    };


    // Connect to the left device with specified address
    public boolean connectL(final String address) {
        if (address == null) {
            Log.w(TAG, "Unspecified address.");
            return false;
        }

        if (mBluetoothAdapter == null) {
            Log.w(TAG, "BluetoothAdapter not initialized.");
            return false;
        }

        // Previously connected device.  Try to reconnect
        if (LBluetoothDeviceAddress != null && address.equals(LBluetoothDeviceAddress)
                && LBluetoothGatt != null) {
            Log.d(TAG, "Using an existing mBluetoothGatt to connect.");
            if (LBluetoothGatt.connect()) {
                LConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        // Create a new connection, obtain the target device object
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found. Unable to connect.");
            return false;
        }

        // To directly connect to the device, set the autoConnect parameter to false.
        LBluetoothGatt = device.connectGatt(this, false, LGattCallback);
        Log.d(TAG, "Creating a new connection.");

        LBluetoothDeviceAddress = address;
        LConnectionState = STATE_CONNECTING;

        return true;
    }


    // Connect to the right device with specified address
    public boolean connectR(final String address) {
        if (address == null) {
            Log.w(TAG, "Unspecified address.");
            return false;
        }

        if (mBluetoothAdapter == null) {
            Log.w(TAG, "BluetoothAdapter not initialized.");
            return false;
        }

        // Previously connected device.  Try to reconnect
        if (RBluetoothDeviceAddress != null && address.equals(RBluetoothDeviceAddress)
                && RBluetoothGatt != null) {
            Log.d(TAG, "Using an existing mBluetoothGatt to connect.");
            if (RBluetoothGatt.connect()) {
                LConnectionState = STATE_CONNECTING;
                return true;
            } else {
                return false;
            }
        }

        // Create a new connection, obtain the target device object
        final BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
        if (device == null) {
            Log.w(TAG, "Device not found. Unable to connect.");
            return false;
        }

        // To directly connect to the device, set the autoConnect parameter to false.
        RBluetoothGatt = device.connectGatt(this, false, RGattCallback);
        Log.d(TAG, "Creating a new connection.");

        LBluetoothDeviceAddress = address;
        LConnectionState = STATE_CONNECTING;

        return true;
    }


    // This object implements BluetoothGatt callback and a series of corresponding operations
    private final BluetoothGattCallback LGattCallback = new BluetoothGattCallback() {

        // This callback is called when there is a change in ble connection state
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "connection with device " + gatt.getDevice().getName());
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                LConnectionState = STATE_CONNECTED;
                Log.i(TAG, "Connected to GATT server for left.");
                // Discover the services of the current blue tooth gatt
                Log.i(TAG, "Attempting to start service discovery:" +
                        LBluetoothGatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                LConnectionState = STATE_DISCONNECTED;
                leftPCBConnected = false;
                // Prints True or False
                Log.i(TAG, "Disconnected from GATT server.");
                //to change ui, must use this thread
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Stuff that updates the UI
                        lcon.setText("Status: Connecting...");
                        disconnected();
                    }
                });
                scanLeDevice(true);
            }

        }

        // This callback is called after "discoverServices"
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {

                // Get a list of all the characteristics from the service
                characteristics = gatt.getService(UUID.fromString(LumosServices.lumosServiceUUID)).
                        getCharacteristics();
                // Assign every characteristic to an object
                for (BluetoothGattCharacteristic characteristic : characteristics){
                    String deviceUuid = characteristic.getUuid().toString();
                    if (deviceUuid.equals(LumosServices.buttonCharUUID)){
                        buttonChar = characteristic;
                    } else if (deviceUuid.equals(LumosServices.capCharUUID)){
                        capChar = characteristic;
                    } else if (deviceUuid.equals(LumosServices.ledCharUUID)){
                        ledChar = characteristic;
                    } else if (deviceUuid.equals(LumosServices.alsCharUUID)){
                        alsChar = characteristic;
                    } else if (deviceUuid.equals(LumosServices.lightCharUUID)){
                        lightChar = characteristic;
                    } else if (deviceUuid.equals(LumosServices.batteryCharUUID)){
                        batteryChar = characteristic;
                    }
                }

                if (ledChar != null && buttonChar != null && capChar != null && alsChar != null && lightChar != null && batteryChar != null){
                    Log.d(TAG, "left PCB chars all connected");

                    leftPCBConnected = true;

                    if (rightPCBConnected){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Stuff that updates the UI
                                offbutton();
                                lcon.setText("Status: Connected");
                                connectedUI();
                            }
                        });
                    }

                }

                for (BluetoothGattCharacteristic characteristic: LBluetoothGatt.getService(UUID.fromString("56f59bd1-dc9e-4447-9ba5-88d3c27c6281")).getCharacteristics()) {
                    chars.add(characteristic);
                }

                subscribeToCharacteristics(LBluetoothGatt);

            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }



        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.i("DESCRIPTOR", "WROTE DESCRIPTOR FOR CHARACTERISTIC");
            super.onDescriptorWrite(gatt, descriptor, status);
            chars.remove(0);
            subscribeToCharacteristics(gatt);
        }

        // This callback is called after "readCharacteristic" is called
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            // Read the characteristic and its value
            if (status == BluetoothGatt.GATT_SUCCESS) {

                Log.d(TAG, "a characteristic has been read" );

                Log.d(TAG, String.valueOf(characteristic.getValue()));

                //readChar(characteristic);
            }
        }
        // This callback is called after whenever a characteristic has changed
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            // Get update for the new value
            Log.w(TAG, "Certain value on the board has changed..." );
            String deviceUuid = characteristic.getUuid().toString();
                if (deviceUuid.equals(LumosServices.ledCharUUID)){
                    LEDLValue = characteristic.getValue();
                    Log.d(TAG, "LED has changed on left PCB " + LEDLValue[0]);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //lled.setText(String.valueOf(LEDLValue[0]));
                        }
                    });
                }else if  (deviceUuid.equals(LumosServices.buttonCharUUID)) {
                    buttonLValue = characteristic.getValue();
                    Log.d(TAG, "button has changed on left PCB " + buttonLValue[0]);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //lbtn.setText(String.valueOf(buttonLValue[0]));
                        }
                    });
                }else if  (deviceUuid.equals(LumosServices.capCharUUID)) {
                    capLValue = characteristic.getValue();
                    Log.d(TAG, "capsense has changed on left PCB " + capLValue[0]);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            lcap.setText(String.valueOf(capLValue[0]));
                        }
                    });
                }else if  (deviceUuid.equals(LumosServices.alsCharUUID)) {
                    alsLValue = characteristic.getValue();
                    int voltage1 = (int) alsLValue[1];
                    int voltage2 = (int) alsLValue[0];
                    int voltage = voltage1*16*16 + voltage2;

                    Log.d(TAG, "als has changed on left PCB " + voltage);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            lals.setText(String.valueOf(voltage));
                        }
                    });
                }else if  (deviceUuid.equals(LumosServices.batteryCharUUID)) {
                    batteryLValue = characteristic.getValue();
                    int voltage1 = (int) batteryLValue[1];
                    int voltage2 = (int) batteryLValue[0];
                    int voltage = voltage1*16*16 + voltage2;

                    Log.d(TAG, "battery has changed on left PCB " + voltage);

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            lbat.setText(voltage / 33 + "%");
                        }
                    });

                }
            //readChar(characteristic);
        }
    };

    private final BluetoothGattCallback RGattCallback = new BluetoothGattCallback() {

        // This callback is called when there is a change in ble connection state
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.d(TAG, "connection with right device " + gatt.getDevice().getName());
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                RConnectionState = STATE_CONNECTED;
                Log.i(TAG, "Connected to GATT server for right.");
                // Discover the services of the current blue tooth gatt
                Log.i(TAG, "Attempting to start service discovery for right side:" +
                        RBluetoothGatt.discoverServices());
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                RConnectionState = STATE_DISCONNECTED;
                rightPCBConnected = false;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        // Stuff that updates the UI
                        lcon.setText("Status: Connecting...");
                        disconnected();
                    }
                });

                // Prints True or False
                Log.i(TAG, "Disconnected from GATT server for right.");
                scanLeDevice(true);
            }
        }


        // This callback is called after "discoverServices"
        @RequiresApi(api = Build.VERSION_CODES.M)
        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if (status == BluetoothGatt.GATT_SUCCESS) {

                // Get a list of all the characteristics from the service
                characteristics = gatt.getService(UUID.fromString(LumosServices.lumosRServiceUUID)).
                        getCharacteristics();
                // Assign every characteristic to an object
                Log.d(TAG, "onServicesDiscovered for Right!!!!!!!!!!!!!!!!!!!!!!!! assigning characteristics");
                for (BluetoothGattCharacteristic characteristic : characteristics){
                    String deviceUuid = characteristic.getUuid().toString();
                    if (deviceUuid.equals(LumosServices.RbuttonCharUUID)){
                        RbuttonChar = characteristic;
                    } else if (deviceUuid.equals(LumosServices.RcapCharUUID)){
                        RcapChar = characteristic;
                    } else if (deviceUuid.equals(LumosServices.RledCharUUID)){
                        RledChar = characteristic;
                    } else if (deviceUuid.equals(LumosServices.RalsCharUUID)){
                        RalsChar = characteristic;
                    } else if (deviceUuid.equals(LumosServices.RlightCharUUID)){
                        RlightChar = characteristic;
                    } else if (deviceUuid.equals(LumosServices.RbatteryCharUUID)){
                        RbatteryChar = characteristic;
                    }//else if (deviceUuid.equals(LumosServices.RLconnectedUUID)){
                    //    RlconnectedChar = characteristic;
                    //}
                }

                if (RledChar != null && RbuttonChar != null && RcapChar != null && RalsChar != null && RlightChar != null && RbatteryChar != null){ // && RlconnectedChar != null
                    Log.d(TAG, "right PCB chars all connected");

                    rightPCBConnected = true;

                    if (leftPCBConnected){
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Stuff that updates the UI
                                lcon.setText("connected");
                                connectedUI();
                            }
                        });
                    }

                }

                for (BluetoothGattCharacteristic characteristic: RBluetoothGatt.getService(UUID.fromString("9ab00bd6-1ab9-4912-abc6-f1d44a0dd7a4")).getCharacteristics()) {
                    charsR.add(characteristic);
                }

                subscribeToCharacteristicsR(RBluetoothGatt);

            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            Log.i("DESCRIPTOR", "WROTE DESCRIPTOR FOR CHARACTERISTIC");
            super.onDescriptorWrite(gatt, descriptor, status);
            charsR.remove(0);
            subscribeToCharacteristicsR(gatt);

            if (charsR.size()==0){

                //if(rightPCBConnected){
                //    RBluetoothGatt.readCharacteristic(RlconnectedChar);
                //}
            }
        }

        // This callback is called after "readCharacteristic" is called
        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            // Read the characteristic and its value
            if (status == BluetoothGatt.GATT_SUCCESS) {

                Log.d(TAG, "a characteristic has been read" );

                Log.d(TAG, String.valueOf(characteristic.getValue()));

                String deviceUuid = characteristic.getUuid().toString();
                /*
                if (deviceUuid.equals(LumosServices.RLconnectedUUID)){
                    RLCValue = characteristic.getValue();
                    Log.d(TAG, "RLC has been read on right PCB " + RLCValue[0]);
                    if (RLCValue[0] == 1){
                        RLConnectStatus = true;
                    }else if (RLCValue[0] == 0){
                        RLConnectStatus = false;
                    }
                }
                */

            }
        }

        // This callback is called after whenever a characteristic has changed
        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt,
                                            BluetoothGattCharacteristic characteristic) {
            // Get update for the new value
            Log.w(TAG, "Certain value on the board has changed..." );
            String deviceUuid = characteristic.getUuid().toString();
            if (deviceUuid.equals(LumosServices.RledCharUUID)){
                LEDRValue = characteristic.getValue();
                Log.d(TAG, "LED has changed on right PCB " + LEDRValue[0]);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //rled.setText(String.valueOf(LEDRValue[0]));
                    }
                });
            }else if  (deviceUuid.equals(LumosServices.RbuttonCharUUID)) {
                buttonRValue = characteristic.getValue();
                Log.d(TAG, "button has changed on right PCB " + buttonRValue[0]);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //rbtn.setText(String.valueOf(buttonRValue[0]));
                    }
                });
            }else if  (deviceUuid.equals(LumosServices.RcapCharUUID)) {
                capRValue = characteristic.getValue();
                Log.d(TAG, "capsense has changed on right PCB " + capRValue[0]);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rcap.setText(String.valueOf(capRValue[0]));
                    }
                });
            }else if  (deviceUuid.equals(LumosServices.RalsCharUUID)) {
                alsRValue = characteristic.getValue();
                int voltage1 = (int) alsRValue[1];
                int voltage2 = (int) alsRValue[0];
                int voltage = voltage1*16*16 + voltage2;

                Log.d(TAG, "als has changed on right PCB " + voltage);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rals.setText(String.valueOf(voltage));
                    }
                });
            }else if  (deviceUuid.equals(LumosServices.RbatteryCharUUID)) {
                batteryRValue = characteristic.getValue();
                int voltage1 = (int) batteryRValue[1];
                int voltage2 = (int) batteryRValue[0];
                int voltage = voltage1*16*16 + voltage2;

                Log.d(TAG, "battery has changed on right PCB " + voltage);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        rbat.setText(voltage / 33 + "%");
                    }
                });
            }
            /*
            else if  (deviceUuid.equals(LumosServices.RLconnectedUUID)) {
                RLCValue = characteristic.getValue();
                Log.d(TAG, "RLC has been changed on right PCB " + RLCValue[0]);
                if (RLCValue[0] == 1){
                    RLConnectStatus = true;
                }else if (RLCValue[0] == 0){
                    RLConnectStatus = false;
                }
            }
            */
            //readChar(characteristic);
        }

    };


    private void subscribeToCharacteristics(BluetoothGatt gatt) {
        Log.w(TAG, "subscribing to chars");
        if(chars.size() == 0) return;
        BluetoothGattCharacteristic characteristic = chars.get(0);

        gatt.setCharacteristicNotification(characteristic, true);
        descriptor = characteristic.getDescriptor(
                UUID.fromString(LumosServices.CLIENT_CHARACTERISTIC_CONFIG));
        if(descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            if(gatt.writeDescriptor(descriptor)){
                Log.w(TAG, "~~~~~~~~~~~~~~~~~~~~~ Write Descriptor succeed ");
            }
        }else{
            chars.remove(0);
            subscribeToCharacteristics(gatt);
        }
    }

    private void subscribeToCharacteristicsR(BluetoothGatt gatt) {
        Log.w(TAG, "subscribing to charsr");
        if(charsR.size() == 0) return;
        BluetoothGattCharacteristic characteristic = charsR.get(0);

        gatt.setCharacteristicNotification(characteristic, true);
        descriptor = characteristic.getDescriptor(
                UUID.fromString(LumosServices.CLIENT_CHARACTERISTIC_CONFIG));
        if(descriptor != null) {
            descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
            if(gatt.writeDescriptor(descriptor)){
                Log.w(TAG, "~~~~~~~~~~~~~~~~~~~~~ Write Descriptor succeed ");
            }
        }else{
            charsR.remove(0);
            subscribeToCharacteristicsR(gatt);
        }
    }
    /*
    Obtain blue tooth manager from system service, adapter from manager, and scanner from adapter
    for later use.
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void getBluetoothAdapterAndLeScanner() throws InterruptedException {
        // Get BluetoothAdapter and BluetoothLeScanner.
        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            // Device does not support Bluetooth
        } else if (!mBluetoothAdapter.isEnabled()) {
            while(!mBluetoothAdapter.isEnabled()){
                mBluetoothAdapter.enable();
                if(!mBluetoothAdapter.isEnabled()){
                    Thread.sleep(3000);
                }
            }
        }
        mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

        mScanning = false;

    }

    public void connectedUI() {
        light_switch.setChecked(false);
        seekbar_frequency.setProgress(0);
        seekbar_intensity.setProgress(0);
        light_switch.setEnabled(true);
        seekbar_frequency.setEnabled(true);
        seekbar_intensity.setEnabled(true);
        reset_btn.setEnabled(true);
        reset_btn.setBackground(getDrawable(R.drawable.lightgreen_btn_style));
        offbutton();
        intensity_right.setEnabled(true);
        intensity_left.setEnabled(true);
        frequency_right.setEnabled(true);
        frequency_left.setEnabled(true);
    }

    @SuppressLint("ResourceAsColor")
    public void disconnected() {
        light_switch.setChecked(false);
        seekbar_frequency.setProgress(0);
        seekbar_intensity.setProgress(0);
        light_switch.setEnabled(false);
        seekbar_frequency.setEnabled(false);
        seekbar_intensity.setEnabled(false);
        reset_btn.setEnabled(false);
        reset_btn.setBackground(getDrawable(R.drawable.btn_disable));
        intensity_right.setEnabled(false);
        intensity_left.setEnabled(false);
        frequency_right.setEnabled(false);
        frequency_left.setEnabled(false);
    }

    public void reset() {

        //do resetting here
        if (leftPCBConnected){
            LBluetoothGatt.disconnect();
        }

        if (rightPCBConnected) {
            RBluetoothGatt.disconnect();
        }

        scanLeDevice(true);

    }


    public void onbutton() {
        // Convert int to UInt8 (8-bit byte)
        if(lightChar != null && RlightChar != null && ledChar != null &&RledChar != null) {
            // Convert int to UInt8 (8-bit byte), first byte is duty, second is frequency in Hz
            byte[] value = {(byte) (100), (byte) 255};//reverse
            byte[] value2 = {0};

            Log.i(TAG, "##############################" + "Switch to Noon mode");
            // Write value to characteristic on the device to change LED brightness
            lightChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            lightChar.setValue(value);

            if (LBluetoothGatt.writeCharacteristic(lightChar)) {
                Log.i(TAG, "##############################" + "lightChar written: 1");
            } else {
                Log.i(TAG, "##############################" + "lightChar not written");
            }

            // A gap for the previous operation to be finished
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Write value to characteristic on the right device to change LED brightness
            RlightChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            RlightChar.setValue(value);

            if (RBluetoothGatt.writeCharacteristic(RlightChar)){
                Log.i(TAG, "##############################"+"RlightChar written: 0");
            } else {
                Log.i(TAG, "##############################"+"RlightChar not written");
            }

            // A gap for the previous operation to be finished
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Write value to characteristic on the device to turn on LED
            ledChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            ledChar.setValue(value2);

            if (LBluetoothGatt.writeCharacteristic(ledChar)){
                Log.i(TAG, "##############################"+"ledChar written: 0");
            } else {
                Log.i(TAG, "##############################"+"ledChar not written");
            }

            // A gap for the previous operation to be finished
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Write value to characteristic on the right device to turn off LED
            RledChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            RledChar.setValue(value2);

            if (RBluetoothGatt.writeCharacteristic(RledChar)){
                Log.i(TAG, "##############################"+"RledChar written: 0");
            } else {
                Log.i(TAG, "##############################"+"RledChar not written");
            }
        }

    }

    public void offbutton() {
        if(lightChar != null && RlightChar != null && ledChar != null &&RledChar != null) {
            // Convert int to UInt8 (8-bit byte), first byte is duty, second is frequency in Hz
            byte[] value = {(byte) (100), (byte) 255};//reverse
            byte[] value2 = {1};

            Log.i(TAG, "##############################" + "Switch to Noon mode");
            // Write value to characteristic on the device to change LED brightness
            lightChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            lightChar.setValue(value);

            if (LBluetoothGatt.writeCharacteristic(lightChar)) {
                Log.i(TAG, "##############################" + "lightChar written: 1");
            } else {
                Log.i(TAG, "##############################" + "lightChar not written");
            }

            // A gap for the previous operation to be finished
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Write value to characteristic on the right device to change LED brightness
            RlightChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            RlightChar.setValue(value);

            if (RBluetoothGatt.writeCharacteristic(RlightChar)){
                Log.i(TAG, "##############################"+"RlightChar written: 0");
            } else {
                Log.i(TAG, "##############################"+"RlightChar not written");
            }

            // A gap for the previous operation to be finished
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Write value to characteristic on the device to turn on LED
            ledChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            ledChar.setValue(value2);

            if (LBluetoothGatt.writeCharacteristic(ledChar)){
                Log.i(TAG, "##############################"+"ledChar written: 0");
            } else {
                Log.i(TAG, "##############################"+"ledChar not written");
            }

            // A gap for the previous operation to be finished
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Write value to characteristic on the right device to turn off LED
            RledChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            RledChar.setValue(value2);

            if (RBluetoothGatt.writeCharacteristic(RledChar)){
                Log.i(TAG, "##############################"+"RledChar written: 0");
            } else {
                Log.i(TAG, "##############################"+"RledChar not written");
            }
        }
    }

    public void Set_Hz_duty(int duty,int frequency) {
        if(lightChar != null && RlightChar != null && ledChar != null &&RledChar != null) {
            // Convert int to UInt8 (8-bit byte), first byte is duty, second is frequency in Hz
            byte[] value = {(byte) (duty), (byte) frequency};//reverse
            byte[] value2 = {1};

            Log.i(TAG, "##############################" + "Switch to Noon mode");
            // Write value to characteristic on the device to change LED brightness
            lightChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            lightChar.setValue(value);

            if (LBluetoothGatt.writeCharacteristic(lightChar)) {
                Log.i(TAG, "##############################" + "lightChar written: 1");
            } else {
                Log.i(TAG, "##############################" + "lightChar not written");
            }

            // A gap for the previous operation to be finished
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Write value to characteristic on the right device to change LED brightness
            RlightChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            RlightChar.setValue(value);

            if (RBluetoothGatt.writeCharacteristic(RlightChar)){
                Log.i(TAG, "##############################"+"RlightChar written: 0");
            } else {
                Log.i(TAG, "##############################"+"RlightChar not written");
            }

            // A gap for the previous operation to be finished
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Write value to characteristic on the device to turn on LED
            ledChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            ledChar.setValue(value2);

            if (LBluetoothGatt.writeCharacteristic(ledChar)){
                Log.i(TAG, "##############################"+"ledChar written: 0");
            } else {
                Log.i(TAG, "##############################"+"ledChar not written");
            }

            // A gap for the previous operation to be finished
            try {
                Thread.sleep(150);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Write value to characteristic on the right device to turn off LED
            RledChar.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);
            RledChar.setValue(value2);

            if (RBluetoothGatt.writeCharacteristic(RledChar)){
                Log.i(TAG, "##############################"+"RledChar written: 0");
            } else {
                Log.i(TAG, "##############################"+"RledChar not written");
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()){
            case R.id.power:
                if(isChecked){
                    Log.i("felix","switch on");
                    onbutton();
                    Set_Hz_duty(seekbar_intensity.getProgress(),seekbar_frequency.getProgress());
                    on_off = 1;
                }else{
                    Log.i("felix","switch off");
                    offbutton();
                    on_off = 0;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.reset_btn:
                reset();
                break;
            case R.id.left_arrow_intensity:
                Log.i("felix","clicked");
                seekbar_intensity.setProgress(seekbar_intensity.getProgress()-1);
                break;
            case R.id.right_arrow_intensity:
                seekbar_intensity.setProgress(seekbar_intensity.getProgress()+1);
                break;
            case R.id.left_arrow_frequency:
                seekbar_frequency.setProgress(seekbar_frequency.getProgress()-1);
                break;
            case R.id.right_arrow_frequency:
                seekbar_frequency.setProgress(seekbar_frequency.getProgress()+1);
                break;
            default:
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()){
            case R.id.light_control_light_intensity_seekbar:
                intensity_seek_text.setText(progress+"%");
                break;
            case R.id.light_control_light_frequency_seekbar:
                frequency_seek_text.setText(progress+"");
                break;
            default:
                break;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        if(on_off == 1){
            Set_Hz_duty(seekbar_intensity.getProgress(),seekbar_frequency.getProgress());
        }
        Log.i("felix","frequency progress :"+seekbar_frequency.getProgress());
        Log.i("felix","intensity progress :"+seekbar_intensity.getProgress());
    }
}
