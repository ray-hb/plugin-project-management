package com.ray.utils

import android.content.Context
import android.util.Log
import com.ray.commonres.CommonRes

class CommonUtils {

    companion object {
        fun showLog(logs: String) {

            Log.e("Log", logs)
        }

        fun getCommonRes2(context: Context) = CommonRes.getCommonRes2(context)

        fun getCommonRes(context: Context) = CommonRes.getCommonRes(context)

        fun getCommonRes3(context: Context) = CommonRes.getCommonRes(context)
    }
}