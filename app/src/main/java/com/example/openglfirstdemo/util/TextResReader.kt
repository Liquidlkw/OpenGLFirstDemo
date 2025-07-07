package com.example.openglfirstdemo.util

import android.content.Context
import android.util.Log
import java.io.BufferedReader
import java.io.InputStreamReader

object TextResReader {
    private const val TAG = "TextResReader"
    fun readTextFromResource(context: Context, resId: Int): String {
        val body: StringBuilder = StringBuilder()
        val inputStream = context.resources.openRawResource(resId)
        val inputStreamReader = InputStreamReader(inputStream)
        val bufferedReader = BufferedReader(inputStreamReader)
        var nextLine: String? = null
        while ((bufferedReader.readLine().also { nextLine = it }) != null) {
            body.append(nextLine)
            body.append('\n')
        }
        Log.i(TAG, "readTextFromResource: $body")
        return body.toString()
    }


}