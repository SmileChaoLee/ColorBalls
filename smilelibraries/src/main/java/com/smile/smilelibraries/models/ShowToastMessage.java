package com.smile.smilelibraries.models;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.smile.smilelibraries.R;
import com.smile.smilelibraries.utilities.ScreenUtil;

import java.util.LinkedList;
import java.util.Queue;

import static com.smile.smilelibraries.utilities.ScreenUtil.TAG;

// singleton class
public class ShowToastMessage {

    @SuppressLint("StaticFieldLeak")
    private static ShowToastMessage showToastMessage;
    @SuppressLint("StaticFieldLeak")
    private static Activity mActivity;
    private final Handler mHandler;
    private final ViewGroup mRootView;
    private final View layoutForTextView;
    private final Runnable setTextViewInvisible;

    private TextView mTextView = null;
    private Queue<Runnable> runnableQueue;
    private boolean isRunnableRunning;

    public static synchronized ShowToastMessage getInstance(final Activity activity) {
        if (activity == null) {
            return null;
        }
        if (!activity.equals(mActivity)) {
            Log.d(TAG, "Activity has changed.");
            if (showToastMessage != null) {
                showToastMessage.releaseMessageView();
            }
        }

        if (showToastMessage == null) {
            Log.d(TAG, "showToastMessage is null.");
            showToastMessage = new ShowToastMessage(activity);
        } else {
            Log.d(TAG, "showToastMessage is not null.");
        }
        return showToastMessage;
    }

    public static synchronized void showToast(final Activity activity, final String message, final float textFontSize, final int fontSize_Type, final int duration) {
        if (activity == null) {
            return;
        }
        showToastMessage = ShowToastMessage.getInstance(activity);
        showToastMessage.showMessageInTextView(message, textFontSize, fontSize_Type, duration);
    }

    private synchronized void showMessageInTextView(final String message, final float textFontSize, final int fontSize_Type, final int duration) {
        Log.d(TAG, "showMessageInTextView() --> mTextView = " + mTextView);
        if (mTextView == null) {
            return;
        }

        final Runnable setMessageToTextView = new Runnable() {
            @Override
            public void run() {
                isRunnableRunning = true;
                mTextView.setText(message);
                ScreenUtil.resizeTextSize(mTextView, textFontSize, fontSize_Type);
                mTextView.setVisibility(View.VISIBLE);
                mHandler.postDelayed(setTextViewInvisible, duration);
            }
        };

        runnableQueue.add(setMessageToTextView);
        Log.d(TAG, "isRunnableRunning = " + isRunnableRunning);
        if (!isRunnableRunning) {
            isRunnableRunning = true;
            mActivity.runOnUiThread(setMessageToTextView);
        } else {
            runnableQueue.add(setMessageToTextView);
        }
    }

    private ShowToastMessage(final Activity activity) {
        mActivity = activity;
        Log.d(TAG, "mActivity = " + mActivity);
        if (mActivity != null) {
            mHandler = new Handler(Looper.getMainLooper());
            // View rootView = mActivity.getWindow().getDecorView().getRootView();
            // or
            // ViewGroup rootView = (ViewGroup) ((ViewGroup)mActivity.findViewById(android.R.id.content)).getChildAt(0);
            // or
            mRootView = (ViewGroup) ((ViewGroup)mActivity.findViewById(android.R.id.content)).getRootView();
            layoutForTextView = mActivity.getLayoutInflater().inflate(R.layout.toast_message_linearlayout, null, false);
            mTextView = layoutForTextView.findViewById(R.id.toastMessageTextView);
            Log.d(TAG, "ShowToastMessage() --> mTextView = " + mTextView);
            mRootView.addView(layoutForTextView);

            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTextView.setVisibility(View.INVISIBLE);
                }
            });

            runnableQueue = new LinkedList<>();
            isRunnableRunning = false;

            setTextViewInvisible = new Runnable() {
                @Override
                public void run() {
                    mHandler.removeCallbacks(this);
                    mTextView.setVisibility(View.INVISIBLE);
                    if (!runnableQueue.isEmpty()) {
                        Runnable mRunnable = runnableQueue.poll();
                        mActivity.runOnUiThread(mRunnable);
                    } else {
                        // no more runnable object in queue
                        isRunnableRunning = false;
                    }
                }
            };
        } else {
            mHandler = null;
            mRootView = null;
            layoutForTextView = null;
            mTextView = null;
            setTextViewInvisible = null;
        }
    }

    private synchronized void releaseMessageView() {
        if ( (mRootView != null) && (layoutForTextView != null) && (mTextView != null) ) {
            mTextView.setVisibility(View.INVISIBLE);
            mRootView.removeView(layoutForTextView);
            // mTextView = null;
        }
        showToastMessage = null;
    }
}
