package com.givewaygames.q;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.StreamCorruptedException;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.os.Parcel;
import android.util.Log;
import android.view.MotionEvent;

import com.givewaygames.q.QService.BluetoothDataListener;

public class Q {
	private static final String TAG = "Q";
	private static final boolean DEBUG = true;
	
	private QService qService = null;
    private BluetoothAdapter bluetoothAdapter = null;
    private BluetoothDataListener dataListener = null;
    private Context context;
	
	public Q(Context context, BluetoothDataListener dataListener) {
		this.context = context;
		this.dataListener = dataListener;
	}
	
	public boolean onCreate() {
        // Get local Bluetooth adapter
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        // If the adapter is null, then Bluetooth is not supported
        return (bluetoothAdapter != null);
	}
	
	public void onStart() {
		if(DEBUG) Log.e(TAG, "onStart");
		
		if (qService == null) {
			setupQ();
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
        if (qService != null) qService.stop();
	}
	
	private void setupQ() {
        if (DEBUG) Log.d(TAG, "setupQ()");

        // Initialize the QService to perform bluetooth connections
        qService = new QService(context, dataListener);
    }
}
