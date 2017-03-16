package com.jkm.rgbcontroller;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Toast;

import java.util.HashMap;
import java.util.List;

public class ControllerActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = ControllerActivity.class.getSimpleName();
    private static final long SCAN_PERIOD = 10000;

    private SeekBar sbRed, sbGreen, sbBlue;
    private Button btMode1, btMode2, btMode3;
    private Button btKeypad1, btKeypad2, btKeypad3, btKeypad4, btKeypad5, btKeypad6, btKeypad7, btKeypad8, btKeypad9;
    private AnalogueView avController;

    private BluetoothAdapter mBluetoothAdapter;
    private HashMap<String, String> mDevices = new HashMap<>();
    private String mDeviceAddress;
    private BluetoothGattCharacteristic characteristicTX;
    private BluetoothGattCharacteristic characteristicRX;

    private ArrayAdapter<String> mArrayAdapter;
    private AlertDialog.Builder mDialogBuilder;

    private Handler mHandler = new Handler();
    private Runnable mRunnable;
    private boolean isScanning = false;
    private boolean isConnected = false;
    private boolean isServiceBind = false;

    private DataService mDataService;

    private final ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            Log.i(TAG, "OnServiceConnected");
            mDataService = ((DataService.LocalBinder) service).getService();
            if (!mDataService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
            } else {
                Log.i(TAG, "Try to connect to BLE.");
                if (mDeviceAddress != null) {
                    mDataService.connect(mDeviceAddress);
                } else {
                    Log.i(TAG, "Can't connect because address is null.");
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mDataService = null;
        }
    };

    private void GattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            // get characteristic when UUID matches RX/TX UUID
            characteristicTX = gattService.getCharacteristic(DataService.UUID_HM_RX_TX);
            characteristicRX = gattService.getCharacteristic(DataService.UUID_HM_RX_TX);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        if (isScanning) {
            menu.findItem(R.id.action_scanning).setVisible(true).setActionView(R.layout.actionbar_indeterminate_progress);
            menu.findItem(R.id.action_connect).setVisible(false);
        } else {
            menu.findItem(R.id.action_scanning).setVisible(false);
            if (isConnected) {
                menu.findItem(R.id.action_connect).setVisible(true).setTitle(getResources().getString(R.string.disconnect));
            } else {
                menu.findItem(R.id.action_connect).setVisible(true).setTitle(getResources().getString(R.string.connect));
            }
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_connect) {
            if (isConnected) {
                mDataService.disconnect();
            } else {
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, 1);
                } else {
                    mDevices.clear();
                    mArrayAdapter.clear();
                    scanLeDevice(true);
                    mDialogBuilder.show();
                }
            }
        } else if (id == R.id.action_scanning) {
            // Wait until BLE is found
            Log.d(TAG, "scanning...");
        }
        return super.onOptionsItemSelected(item);
    }

    private void scanLeDevice(boolean enable) {
        if (enable) {
            mRunnable = new Runnable() {
                @Override
                public void run() {
                    isScanning = false;
                    mBluetoothAdapter.stopLeScan(mLeScanCallback);
                    invalidateOptionsMenu();
                }
            };
            mHandler.postDelayed(mRunnable, SCAN_PERIOD);
            isScanning = true;
            mBluetoothAdapter.startLeScan(mLeScanCallback);
        } else {
            isScanning = false;
            mBluetoothAdapter.stopLeScan(mLeScanCallback);
        }
        invalidateOptionsMenu();
    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            String deviceName = device.getName();
            String deviceAddress = device.getAddress();
            Log.i(TAG, "deviceName = " + deviceName + ", deviceAddress = " + deviceAddress);

            mDevices.put(deviceName, deviceAddress);
            mArrayAdapter.add(deviceName);
            mArrayAdapter.notifyDataSetChanged();
        }
    };

    private void sendData(String data) {
        data += "\n";
        byte[] tx = data.getBytes();
        if (isConnected) {
            characteristicTX.setValue(tx);
            mDataService.writeCharacteristic(characteristicTX);
            mDataService.setCharacteristicNotification(characteristicRX, true);
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.bt_keypad_1:
                sendData("501");
                break;
            case R.id.bt_keypad_2:
                sendData("502");
                break;
            case R.id.bt_keypad_3:
                sendData("503");
                break;
            case R.id.bt_keypad_4:
                sendData("504");
                break;
            case R.id.bt_keypad_5:
                sendData("505");
                break;
            case R.id.bt_keypad_6:
                sendData("506");
                break;
            case R.id.bt_keypad_7:
                sendData("507");
                break;
            case R.id.bt_keypad_8:
                sendData("508");
                break;
            case R.id.bt_keypad_9:
                sendData("509");
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_controller);

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, getResources().getString(R.string.ble_not_supported), Toast.LENGTH_LONG).show();
            finish();
        }
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            Toast.makeText(this, getResources().getString(R.string.bluetooth_not_supported), Toast.LENGTH_LONG).show();
            finish();
            return;
        }

        sbRed = (SeekBar) findViewById(R.id.sb_red);
        sbGreen = (SeekBar) findViewById(R.id.sb_green);
        sbBlue = (SeekBar) findViewById(R.id.sb_blue);

        btMode1 = (Button) findViewById(R.id.bt_mode_1);
        btMode2 = (Button) findViewById(R.id.bt_mode_2);
        btMode3 = (Button) findViewById(R.id.bt_mode_3);

        btKeypad1 = (Button) findViewById(R.id.bt_keypad_1);
        btKeypad2 = (Button) findViewById(R.id.bt_keypad_2);
        btKeypad3 = (Button) findViewById(R.id.bt_keypad_3);
        btKeypad4 = (Button) findViewById(R.id.bt_keypad_4);
        btKeypad5 = (Button) findViewById(R.id.bt_keypad_5);
        btKeypad6 = (Button) findViewById(R.id.bt_keypad_6);
        btKeypad7 = (Button) findViewById(R.id.bt_keypad_7);
        btKeypad8 = (Button) findViewById(R.id.bt_keypad_8);
        btKeypad9 = (Button) findViewById(R.id.bt_keypad_9);

        btKeypad1.setOnClickListener(this);
        btKeypad2.setOnClickListener(this);
        btKeypad3.setOnClickListener(this);
        btKeypad4.setOnClickListener(this);
        btKeypad5.setOnClickListener(this);
        btKeypad6.setOnClickListener(this);
        btKeypad7.setOnClickListener(this);
        btKeypad8.setOnClickListener(this);
        btKeypad9.setOnClickListener(this);

        avController = (AnalogueView) findViewById(R.id.av_controller);

        mArrayAdapter = new ArrayAdapter<>(ControllerActivity.this, android.R.layout.select_dialog_item);
        mDialogBuilder = new AlertDialog.Builder(ControllerActivity.this);
        mDialogBuilder.setTitle("Choose Bluetooth");

        mDialogBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                scanLeDevice(false);
                dialog.dismiss();
            }
        });

        mDialogBuilder.setAdapter(mArrayAdapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                scanLeDevice(false);
                mHandler.removeCallbacks(mRunnable);

                String deviceName = mArrayAdapter.getItem(which);
                if (mDevices.containsKey(deviceName)) {
                    mDeviceAddress = mDevices.get(deviceName);
                    Log.d(TAG, "Address = " + mDeviceAddress);

                    if (!isServiceBind) {
                        Intent gattServiceIntent = new Intent(ControllerActivity.this, DataService.class);
                        bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE);
                        isServiceBind = true;
                    } else {
                        if (mDataService != null) mDataService.connect(mDeviceAddress);
                    }
                }
            }
        });

        sbRed.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int data = progress + 600;
                sendData(String.valueOf(data));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // must-override method
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // must-override method
            }
        });

        sbGreen.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int data = progress + 700;
                sendData(String.valueOf(data));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // must-override method
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // must-override method
            }
        });

        sbBlue.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int data = progress + 800;
                sendData(String.valueOf(data));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // must-override method
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // must-override method
            }
        });

        btMode1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData("401");
            }
        });

        btMode2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData("402");
            }
        });

        btMode3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendData("403");
            }
        });

        avController.setOnMoveListener(new AnalogueView.OnMoveListener() {
            @Override
            public void onHalfMoveInDirection(double polarAngle) {
                // must-override method
            }

            @Override
            public void onMaxMoveInDirection(double polarAngle) {
                int angle = (int) (100 * polarAngle);
                sendData(String.valueOf(angle));
            }
        });
    }

    /*
       Handles various events fired by the Service.
       ACTION_GATT_CONNECTED: connected to a GATT server.
       ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
       ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
       ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read or notification operations.
    */
    private final BroadcastReceiver mServiceBroadcast = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (DataService.ACTION_GATT_CONNECTED.equals(action)) {
                isConnected = true;
                invalidateOptionsMenu();
                Log.d(TAG, "Connected");
            } else if (DataService.ACTION_GATT_DISCONNECTED.equals(action)) {
                isConnected = false;
                invalidateOptionsMenu();
                Log.d(TAG, "Disconnected");
            } else if (DataService.ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                GattServices(mDataService.getSupportedGattServices());
            } else if (DataService.ACTION_DATA_AVAILABLE.equals(action)) {
                // In case need to receive data
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter filter = new IntentFilter(DataService.ACTION_GATT_CONNECTED);
        filter.addAction(DataService.ACTION_GATT_DISCONNECTED);
        filter.addAction(DataService.ACTION_GATT_SERVICES_DISCOVERED);
        filter.addAction(DataService.ACTION_DATA_AVAILABLE);
        registerReceiver(mServiceBroadcast, filter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        scanLeDevice(false);
        unregisterReceiver(mServiceBroadcast);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isConnected) mDataService.disconnect();
        if (isServiceBind) {
            unbindService(mServiceConnection);
            isServiceBind = false;
        }
        mDataService = null;
    }
}