package com.givewaygames.q;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.util.Log;
import android.view.MotionEvent;

import com.givewaygames.q.QService.BluetoothDataListener;

/**
 * QHub is the hub that communicates with the Q.  All data
 * is displayed on a mobile device to the user through this.
 */
public class QHub {
	private static final String TAG = "QHub";
	private static final boolean DEBUG = true;
	
	private QService qService = null;
    private BluetoothAdapter bluetoothAdapter = null;
    private Context context;
	
	private static final int REQUEST_CONNECT_DEVICE_INSECURE = 0xFA01;
	private static final int REQUEST_ENABLE_BT = 0xFA02;

	public QHub(Context context) {
		this.context = context;
	}

	public boolean onCreate() {
        // Get local Bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        return (bluetoothAdapter != null);
	}
	
	public void onStart() {
		if(DEBUG) Log.e(TAG, "onStart");
		
		// If BT is not on, request that it be enabled.
        // setupChat() will then be called during onActivityResult
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            ((Activity)context).startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
        // Otherwise, setup the chat session
        } else {
            if (qService == null) setupQ();
        }
	}
	
	public void onResume() {
		if(DEBUG) Log.e(TAG, "onResume");
		
		if (qService != null && qService.getState() == QService.STATE_NONE) {
			qService.start();
        }
	}
	
	public void onDestroy() {
		if(DEBUG) Log.e(TAG, "onDestroy");
        if (qService != null) {
        	qService.stop();
        }
	}
	
	private void setupQ() {
        if (DEBUG) Log.d(TAG, "setupQ()");

        // Initialize the QService to perform bluetooth connections
        qService = new QService(context, new BluetoothDataListener() {
			@Override
			public void onDataReceived(byte[] buffer, int bytes) {
				Log.v(TAG, "Data received of size: "+buffer.length);
			}
		});
    }
	
	public boolean sendMessage(String message) {
		// Check that we're actually connected before trying anything
        if (qService.getState() != QService.STATE_CONNECTED) {
            return false;
        }

        // Check that there's actually something to send
        if (message.length() > 0) {
            // Get the message bytes and tell the BluetoothChatService to write
            byte[] send = message.getBytes();
            qService.write(send);
            return true;
        }
        return false;
	}
	
	public boolean sendMotionEvent(MotionEvent event) {
		// Check that we're actually connected before trying anything
        if (qService.getState() != QService.STATE_CONNECTED) {
            return false;
        }
        
        final Parcel parcel = Parcel.obtain();
        final byte[] bytes;

        boolean success = false;
        try {
        	parcel.writeValue(event);
            bytes = parcel.marshall();
            qService.write(bytes);
            success = true;
        } finally {
        	parcel.recycle();
        }
        
        return success;
	}
	
    public boolean startRequestActivity() {
    	Intent serverIntent = new Intent(context, DeviceListActivity.class);
        ((Activity)context).startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
        return false;
    }
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
    	if (resultCode == Activity.RESULT_OK) {
            connectDevice(data, false);
        }
    }
	
	private void connectDevice(Intent data, boolean secure) {
        // Get the device MAC address
        String address = data.getExtras().getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
        
        // Get the BluetoothDevice object
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        
        // Attempt to connect to the device
        qService.connect(device);
    }
}
