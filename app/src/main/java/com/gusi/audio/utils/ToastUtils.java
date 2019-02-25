package com.gusi.audio.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

/**
 * @Author ylw  2019/2/23 18:50
 */
public class ToastUtils {


    private static Handler HANDLER = null;

    private static Toast sToast;
    private static Context mContext;

    public void setContext(Context context) {
        mContext = context;
        HANDLER = new Handler(Looper.getMainLooper());
    }

    public static ToastUtils getInstance() {
        return Holder.single;
    }

    private static final class Holder {
        private static final ToastUtils single = new ToastUtils();
    }

    private ToastUtils() {
    }

    public static void showShort(final String msg) {
        HANDLER.post(new Runnable() {
            @Override
            public void run() {
                if (sToast != null) {
                    sToast.cancel();
                }
                sToast = Toast.makeText(mContext, msg, Toast.LENGTH_SHORT);
                sToast.show();
            }
        });
    }

    public static void showLong(final String msg) {
        HANDLER.post(new Runnable() {
            @Override
            public void run() {
                if (sToast != null) {
                    sToast.cancel();
                }
                sToast = Toast.makeText(mContext, msg, Toast.LENGTH_LONG);
                sToast.show();
            }
        });
    }
}
