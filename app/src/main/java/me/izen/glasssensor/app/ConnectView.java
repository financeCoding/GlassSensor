package me.izen.glasssensor.app;

import android.content.Context;
import android.os.Handler;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.lang.Long;
import java.util.concurrent.TimeUnit;


/**
 * Created by joe on 2/20/14.
 */
public class ConnectView extends FrameLayout {
    private static final String TAG = "ConnectView";

    /**
     * Interface to listen for changes in the countdown.
     */
    public interface CountDownListener {
        /**
         * Notified of a tick, indicating a layout change.
         */
        public void onTick(long millisUntilFinish);

        /**
         * Notified when the countdown is finished.
         */
        public void onFinish(boolean connected);
    }

    /** Time delimiter specifying when the second component is fully shown. */
    public static final float ANIMATION_DURATION_IN_MILLIS = 850.0f;

    // About 24 FPS.
    private static final long DELAY_MILLIS = 41;
    private static final int MAX_TRANSLATION_Y = 30;
    private static final float ALPHA_DELIMITER = 0.95f;
    private static final long SEC_TO_MILLIS = TimeUnit.SECONDS.toMillis(1);

    private final TextView mConnectStatusView;

    private long mTimeSeconds;
    private long mStopTimeInFuture;
    private CountDownListener mListener;
    private boolean mStarted;

    public ConnectView(Context context) {
        this(context, null, 0);
    }

    public ConnectView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ConnectView(Context context, AttributeSet attrs, int style) {
        super(context, attrs, style);
        LayoutInflater.from(context).inflate(R.layout.card_connect, this);

        mConnectStatusView =  (TextView) findViewById(R.id.connect_status_view);
    }

    public void setCountDown(long timeSeconds) {
        mTimeSeconds = timeSeconds;
    }

    public long getCountDown() {
        return mTimeSeconds;
    }

    /**
     * Set a {@link CountDownListener}.
     */
    public void setListener(CountDownListener listener) {
        mListener = listener;
    }

    private final Handler mHandler = new Handler();

    private final Runnable mUpdateViewRunnable = new Runnable() {
        @Override
        public void run() {
            final long millisLeft = mStopTimeInFuture - SystemClock.elapsedRealtime();

            // Count down is done.
            if (millisLeft <= 0) {
                mStarted = false;
                updateStatusView("no connection");
                if (mListener != null) {
                    mListener.onFinish(false);
                }
            } else {
                // TODO : check if the sensor is connected, if so then show connected and finish up this activity
                if(millisLeft < 100) {
                    mListener.onFinish(true);
                }
                updateView(millisLeft, "searching...");
                if (mListener != null) {
                    mListener.onTick(millisLeft);
                }
                mHandler.postDelayed(mUpdateViewRunnable, DELAY_MILLIS);
            }
        }
    };

    private void updateStatusView(String connectStatus) {
        Log.d(TAG, "Status = " + connectStatus);
        mConnectStatusView.setText(connectStatus);
        mConnectStatusView.setAlpha(1.0f);
    }

    /**
     * Starts the countdown animation if not yet started.
     */
    public void start() {
        if (!mStarted) {
            mStopTimeInFuture =
                    TimeUnit.SECONDS.toMillis(mTimeSeconds) + SystemClock.elapsedRealtime();
            mStarted = true;
            mHandler.postDelayed(mUpdateViewRunnable, DELAY_MILLIS);
        }
    }

    /**
     * Stop the counter.
     */
    public void stop() {
        mStarted = false;
        mStopTimeInFuture = SystemClock.elapsedRealtime();
    }


    /**
     * Updates the views to reflect the current state of animation.
     *
     * @params millisUntilFinish milliseconds until the countdown is done
     */
    private void updateView(long millisUntilFinish, String connectStatus) {
        long frame = SEC_TO_MILLIS - (millisUntilFinish % SEC_TO_MILLIS);

        mConnectStatusView.setText(connectStatus);
        if (frame <= ANIMATION_DURATION_IN_MILLIS) {
            float factor = frame / ANIMATION_DURATION_IN_MILLIS;
            mConnectStatusView.setAlpha(factor * ALPHA_DELIMITER);
            mConnectStatusView.setTranslationY(MAX_TRANSLATION_Y * (1 - factor));
        } else {
            float factor = (frame - ANIMATION_DURATION_IN_MILLIS) / ANIMATION_DURATION_IN_MILLIS;
            mConnectStatusView.setAlpha(ALPHA_DELIMITER + factor * (1 - ALPHA_DELIMITER));
        }
    }


}
