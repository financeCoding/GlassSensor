
package me.izen.glasssensor.app;



import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

/**
 * Created by joe on 2/20/14.
 */
public class SensorView extends FrameLayout {


    /**
     * Interface to listen for changes on the view layout.
     */
    public interface ChangeListener {
        /** Notified of a change in the view. */
        public void onChange();
    }

    // About 24 FPS.
    private static final long DELAY_MILLIS = 41;

    private final TextView mMinuteView;
    private final TextView mSecondView;
    private final TextView mCentiSecondView;

    private final TextView mTemperature;
    private final TextView mHumidity;
    private final TextView mPressure;
    private final TextView mIRTemperature;
    private final TextView mIlluminance;
    private final TextView mGas;
    private final TextView mProximity;
    private final TextView mVoltage;
    private final TextView mAltitude;
    private final TextView mBattery;


    private boolean mStarted;
    private boolean mForceStart;
    private boolean mVisible;
    private boolean mRunning;

    private long mBaseMillis;

    private ChangeListener mChangeListener;

    public SensorView(Context context) {
        this(context, null, 0);
    }

    public SensorView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SensorView(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);
        LayoutInflater.from(context).inflate(R.layout.card_sensor, this);

        mMinuteView = (TextView) findViewById(R.id.minute);
        mSecondView = (TextView) findViewById(R.id.second);
        mCentiSecondView = (TextView) findViewById(R.id.centi_second);

        mTemperature = (TextView) findViewById(R.id.temperature);
        mHumidity = (TextView) findViewById(R.id.humidity);
        mPressure = (TextView) findViewById(R.id.pressure);
        mIRTemperature = (TextView) findViewById(R.id.irTemperature);
        mIlluminance = (TextView) findViewById(R.id.illuminance);
        mGas = (TextView) findViewById(R.id.gas);
        mProximity = (TextView) findViewById(R.id.proximity);
        mVoltage = (TextView) findViewById(R.id.voltage);
        mAltitude = (TextView) findViewById(R.id.altitude);
        mBattery = (TextView) findViewById(R.id.battery);

        initSensorView(SystemClock.elapsedRealtime());
    }

    /**
     * Set the base value of the chronometer in milliseconds.
     */
    public void initSensorView(long baseMillis) {
        mBaseMillis = baseMillis;
        mTemperature.setText("--");
        mHumidity.setText("--");
        mPressure.setText("--");
        mIRTemperature.setText("--");
        mIlluminance.setText("--");
        mGas.setText("--");
        mProximity.setText("--");
        mVoltage.setText("--");
        mAltitude.setText("--");
        mBattery.setText("--");
        updateSensorView();
    }

    /**
     * Get the base value of the chronometer in milliseconds.
     */
    public long getBaseMillis() {
        return mBaseMillis;
    }

    /**
     * Set a {@link ChangeListener}.
     */
    public void setListener(ChangeListener listener) {
        mChangeListener = listener;
    }

    /**
     * Set whether or not to force the start of the chronometer when a window has not been attached
     * to the view.
     */
    public void setForceStart(boolean forceStart) {
        mForceStart = forceStart;
        updateRunning();
    }

    /**
     * Start the chronometer.
     */
    public void start() {
        mStarted = true;
        updateRunning();
    }

    /**
     * Stop the chronometer.
     */
    public void stop() {
        mStarted = false;
        updateRunning();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mVisible = false;
        updateRunning();
    }

    @Override
    protected void onWindowVisibilityChanged(int visibility) {
        super.onWindowVisibilityChanged(visibility);
        mVisible = (visibility == VISIBLE);
        updateRunning();
    }


    private final Handler mHandler = new Handler();

    private final Runnable mUpdateTextRunnable = new Runnable() {
        @Override
        public void run() {
            if (mRunning) {
                updateSensorView();
                mHandler.postDelayed(mUpdateTextRunnable, DELAY_MILLIS);
            }
        }
    };

    /**
     * Update the running state of the chronometer.
     */
    private void updateRunning() {
        boolean running = (mVisible || mForceStart) && mStarted;
        if (running != mRunning) {
            if (running) {
                mHandler.post(mUpdateTextRunnable);
            } else {
                mHandler.removeCallbacks(mUpdateTextRunnable);
            }
            mRunning = running;
        }
    }

    /**
     * Update the value of the chronometer.
     */
    private void updateSensorView() {
        long millis = SystemClock.elapsedRealtime() - mBaseMillis;
        // Cap chronometer to one hour.
        millis %= TimeUnit.HOURS.toMillis(1);

        mMinuteView.setText(String.format("%02d", TimeUnit.MILLISECONDS.toMinutes(millis)));
        millis %= TimeUnit.MINUTES.toMillis(1);
        mSecondView.setText(String.format("%02d", TimeUnit.MILLISECONDS.toSeconds(millis)));
        millis = (millis % TimeUnit.SECONDS.toMillis(1)) / 10;
        mCentiSecondView.setText(String.format("%02d", millis));
        if (mChangeListener != null) {
            mChangeListener.onChange();
        }

    }
}
