package me.izen.glasssensor.app;


import android.content.Context;
import android.graphics.Canvas;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.SystemClock;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.View;

import java.util.concurrent.TimeUnit;

/**
 * Created by joe on 2/20/14.
 */
public class SensorDrawer  implements SurfaceHolder.Callback {

    private static final String TAG = "SensorDrawer";

    private static final long SEC_TO_MILLIS = TimeUnit.SECONDS.toMillis(1);
    private static final int SOUND_PRIORITY = 1;
    private static final int MAX_STREAMS = 1;
    private static final int COUNT_DOWN_VALUE = 5;

    private final SoundPool mSoundPool;
    private final int mStartSoundId;
    private final int mCountDownSoundId;

    private final ConnectView mConnectView;
    private final SensorView mSensorView;

    private long mCurrentTimeSeconds;
    private boolean mCountDownSoundPlayed;

    private SurfaceHolder mHolder;
    private boolean mCountDownDone;

    public SensorDrawer(Context context) {
        mConnectView = new ConnectView(context);
        mConnectView.setCountDown(COUNT_DOWN_VALUE);
        mConnectView.setListener(new ConnectView.CountDownListener() {

            @Override
            public void onTick(long millisUntilFinish) {
                maybePlaySound(millisUntilFinish);
                draw(mConnectView);
            }

            @Override
            public void onFinish(boolean connected) {
                if(connected) {
                    mCountDownDone = true;
                    mSensorView.initSensorView(SystemClock.elapsedRealtime());
                    if (mHolder != null) {
                        mSensorView.start();
                    }
                    playSound(mStartSoundId);
                }
                else {
                    draw(mConnectView);
                }
            }

            private void maybePlaySound(long millisUntilFinish) {
                long currentTimeSeconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinish);
                long milliSecondsPart = millisUntilFinish % SEC_TO_MILLIS;

                if (currentTimeSeconds != mCurrentTimeSeconds) {
                    mCountDownSoundPlayed = false;
                    mCurrentTimeSeconds = currentTimeSeconds;
                }
                if (!mCountDownSoundPlayed
                        && (milliSecondsPart <= ConnectView.ANIMATION_DURATION_IN_MILLIS)) {
                    playSound(mCountDownSoundId);
                    mCountDownSoundPlayed = true;
                }
            }
        });

        mSensorView = new SensorView(context);
        mSensorView.setListener(new SensorView.ChangeListener() {

            @Override
            public void onChange() {
                draw(mSensorView);
            }
        });
        mSensorView.setForceStart(true);

        mSoundPool = new SoundPool(MAX_STREAMS, AudioManager.STREAM_MUSIC, 0);
        mStartSoundId = mSoundPool.load(context, R.raw.start, SOUND_PRIORITY);
        mCountDownSoundId = mSoundPool.load(context, R.raw.countdown_bip, SOUND_PRIORITY);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        // Measure and layout the view with the canvas dimensions.
        int measuredWidth = View.MeasureSpec.makeMeasureSpec(width, View.MeasureSpec.EXACTLY);
        int measuredHeight = View.MeasureSpec.makeMeasureSpec(height, View.MeasureSpec.EXACTLY);

        mConnectView.measure(measuredWidth, measuredHeight);
        mConnectView.layout(
                0, 0, mConnectView.getMeasuredWidth(), mConnectView.getMeasuredHeight());

        mSensorView.measure(measuredWidth, measuredHeight);
        mSensorView.layout(
                0, 0, mSensorView.getMeasuredWidth(), mSensorView.getMeasuredHeight());
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "Surface created");
        mHolder = holder;
        if (mCountDownDone) {
            mSensorView.start();
        } else {
            mConnectView.start();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "Surface destroyed");
        mSensorView.stop();
        mHolder = null;
    }

    public void stop() {
        mConnectView.stop();
        mSensorView.stop();
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

    /**
     * Plays the provided {@code soundId}.
     */
    private void playSound(int soundId) {
        mSoundPool.play(soundId,
                1 /* leftVolume */,
                1 /* rightVolume */,
                SOUND_PRIORITY,
                0 /* loop */,
                1 /* rate */);
    }

}
