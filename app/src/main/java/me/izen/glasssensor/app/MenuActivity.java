package me.izen.glasssensor.app;


import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.TextView;

import java.net.BindException;

/**
 * Created by joe on 2/20/14.
 */
public class MenuActivity extends Activity {
    private static String TAG = MenuActivity.class.getName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate()");
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart()");
        super.onStart();
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();
        openOptionsMenu();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop()");
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.d(TAG, "onCreateOptionsMenu()");
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.sensor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.d(TAG, "onOptionsItemSelected()");
        // Handle item selection.
        switch (item.getItemId()) {
            case R.id.stop:
                stopService(new Intent(this, SensorService.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onOptionsMenuClosed(Menu menu) {
        Log.d(TAG, "onOptionsMenuClosed()");
        // Nothing else to do, closing the Activity.
        finish();
    }


}
