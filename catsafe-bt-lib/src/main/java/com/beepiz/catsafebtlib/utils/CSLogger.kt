package com.beepiz.catsafebtlib.utils

import android.util.Log

object CSLogger {
    private const val TAG = "CatSafeLib"

    fun debug(tag: String = TAG, message: String) {
        Log.d(TAG, message)
    }

    fun info(tag: String = TAG, message: String) {
        Log.i(TAG, message)
    }

    fun verbose(tag: String = TAG, message: String) {
        Log.v(TAG, message)
    }

    fun error(tag: String = TAG, message: String) {
        Log.e(TAG, message)
    }

    fun warn(tag: String = TAG, message: String) {
        Log.w(TAG, message)
    }

    fun whatAFailure(tag: String = TAG, message: String) {
        Log.wtf(TAG, message)
    }

}