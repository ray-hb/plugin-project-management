package com.ray.commonres

import android.content.Context

class CommonRes {
    companion object {

        fun getCommonRes(context: Context): String {
            return "this is common"
        }

        fun getCommonRes2(context: Context): String {
            return "this is common2"
        }
    }
}