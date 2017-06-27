package me.jakemoritz.animebuzz.utils;

import android.content.Context;
import android.os.PowerManager;

import me.jakemoritz.animebuzz.activities.MainActivity;

public abstract class WakeLocker {

    private static PowerManager.WakeLock wakeLock;

    public static void acquire(Context context){
        if (wakeLock != null){
            wakeLock.release();
        }

        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, MainActivity.class.getSimpleName());
        wakeLock.acquire();
    }

    public static void release(){
        if (wakeLock != null){
            wakeLock.release();
        }
        wakeLock = null;
    }

}
