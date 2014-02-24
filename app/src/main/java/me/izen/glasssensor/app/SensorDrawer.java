package me.izen.glasssensor.app;


import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Canvas;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;

import java.net.BindException;
import java.util.concurrent.TimeUnit;


/**
 * Created by joe on 2/20/14.
 */
public class SensorDrawer implements SurfaceHolder.Callback {


    private static String TAG = SensorDrawer.class.getName();
    private SurfaceHolder mHolder;
    private final SensorView mSensorView;
    private Context context;
    private Messenger mService;
    private boolean mBound;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            Log.d(TAG, "onServiceConnected()");

            mService = new Messenger(service);

            Intent start = new Intent("android.intent.action.MAIN");
            start.setClassName(context.getString(R.string.sensordrone_package_name), context.getString(R.string.sensordrone_class_name));
            start.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            try {
                mService.send(Message.obtain(null, 1002, start));
                mBound = true;
            } catch (RemoteException e) {
                Log.e(TAG, "Failed: ", e);
            }
        }

        public void onServiceDisconnected(ComponentName className) {
            Log.e(TAG, "onServiceDisconnected()");

            mService = null;
            mBound = false;
        }
    };

    public SensorDrawer(Context context) {
        Log.d(TAG, "SensorDrawer()");
        this.context = context;
        try {
            Intent in = new Intent();
            in.setClassName(context.getString(R.string.intenttunnel_package_name), context.getString(R.string.intenttunnel_class_name));
            if (!this.context.bindService(in, mConnection, Context.BIND_AUTO_CREATE)) {
                throw new BindException("failed to bind");
            }
        } catch (Exception e) {
            Log.e(TAG, "onStart error", e);
        }

        mSensorView = new SensorView(this.context);
        mSensorView.setListener(new SensorView.ChangeListener() {

            @Override
            public void onChange() {
                draw(mSensorView);
            }
        });
        mSensorView.setForceStart(true);

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surfaceChanged()");
        // Measure and layout the view with the canvas dimensions.
        int measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);

        mSensorView.measure(measuredWidth, measuredHeight);
        mSensorView.layout(
                0, 0, mSensorView.getMeasuredWidth(), mSensorView.getMeasuredHeight());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "Surface created");
        mHolder = holder;
        mSensorView.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "Surface destroyed");
        mSensorView.stop();
        mHolder = null;
    }

    public void stop() {
        Log.d(TAG, "stop()");
        mSensorView.stop();
        if (mBound) {
            this.context.unbindService(mConnection);
            mBound = false;
        }
    }

    /**
     * Draws the view in the SurfaceHolder's canvas.
     */
    private void draw(View view) {
        Canvas canvas;
        try {
            canvas = mHolder.lockCanvas();
        } catch (Exception e) {
            return;
        }
        if (canvas != null) {
            view.draw(canvas);
            mHolder.unlockCanvasAndPost(canvas);
        }
    }


}
