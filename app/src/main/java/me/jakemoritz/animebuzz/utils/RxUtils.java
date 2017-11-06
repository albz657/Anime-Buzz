package me.jakemoritz.animebuzz.utils;

import io.reactivex.disposables.Disposable;

/**
 * This is a utility class to use with RxJava calls
 */
public class RxUtils {

    public static void disposeOf(Disposable disposable){
        if (disposable != null && !disposable.isDisposed()){
            disposable.dispose();
        }
    }
}
