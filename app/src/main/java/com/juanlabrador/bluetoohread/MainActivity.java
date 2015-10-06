package com.juanlabrador.bluetoohread;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private static final int ENABLE_BT = 1;
    private Toolbar mToolbar;
    private SwitchCompat mSwitchBluetooh;
    private TextView mStatusBluetooh;
    private ListView mList;
    private ArrayAdapter<String> mListAdapter;
    private BluetoothAdapter mBluetoothAdapter;
    private Set<BluetoothDevice> mDevices;
    private Button mShowList;
    private Button mFindDevices;
    private ProgressDialog mProgressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
    }

    private void init() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            mSwitchBluetooh.setEnabled(false);
            mShowList.setEnabled(false);
            mFindDevices.setEnabled(false);
            mStatusBluetooh.setText("Not supported");
            Toast.makeText(getApplicationContext(), "Your device does not support Bluetooth",
                    Toast.LENGTH_LONG).show();
        } else {
            mToolbar = (Toolbar) findViewById(R.id.toolbar);
            mSwitchBluetooh = (SwitchCompat) mToolbar.findViewById(R.id.switchBluetooh);
            mSwitchBluetooh.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        if (!mBluetoothAdapter.isEnabled()) {
                            Intent onIntent = new Intent(
                                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
                            startActivityForResult(onIntent, ENABLE_BT);
                            Toast.makeText(getApplicationContext(),"Bluetooth turned on" ,
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        mBluetoothAdapter.disable();
                        mStatusBluetooh.setText("Disconnected");
                        Toast.makeText(getApplicationContext(),"Bluetooth turned off",
                                Toast.LENGTH_LONG).show();

                    }
                }
            });
            mStatusBluetooh = (TextView) mToolbar.findViewById(R.id.statusBlueTooh);

            if (mBluetoothAdapter.isEnabled()) {
                mStatusBluetooh.setText("Enabled");
                mSwitchBluetooh.setChecked(true);
            }

            mShowList = (Button) findViewById(R.id.showList);
            mShowList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    showList(view);
                }
            });

            mFindDevices = (Button) findViewById(R.id.findDevices);
            mFindDevices.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    findDevices(view);
                }
            });

            mList = (ListView) findViewById(R.id.deviceList);
            mListAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_list_item_1);
            mList.setAdapter(mListAdapter);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ENABLE_BT) {
            if (mBluetoothAdapter.isEnabled()) {
                mStatusBluetooh.setText("Enabled");
            } else {
                mStatusBluetooh.setText("Disabled");
                mSwitchBluetooh.setChecked(false);
            }
        }
    }

    public void showList(View view) {
        mDevices = mBluetoothAdapter.getBondedDevices();
        for (BluetoothDevice device : mDevices) {
            mListAdapter.add(device.getName() + "\n" + device.getAddress());
        }
    }

    final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ((BluetoothAdapter.ACTION_DISCOVERY_STARTED).equals(action)) {
                Log.i("TAG", "Searching...");
                mProgressDialog = new ProgressDialog(MainActivity.this);
                mProgressDialog.setCancelable(false);
                mProgressDialog.setMessage("Searching...");
                mProgressDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mBluetoothAdapter.cancelDiscovery();
                    }
                });
                mProgressDialog.show();
            } else if ((BluetoothAdapter.ACTION_DISCOVERY_FINISHED).equals(action)) {
                mProgressDialog.dismiss();
                mListAdapter.notifyDataSetChanged();
                Log.i("TAG", "Finished");
            } else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.i("TAG", "Find one!");
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i("TAG", device.getName() + "\n" + device.getAddress());
                mListAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
    };

    public void findDevices(View view) {
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        } else {
            mListAdapter.clear();
            IntentFilter filter = new IntentFilter();
            filter.addAction(BluetoothDevice.ACTION_FOUND);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
            filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
            registerReceiver(mReceiver, filter);
            mBluetoothAdapter.startDiscovery();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mBluetoothAdapter != null) {
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }
}
