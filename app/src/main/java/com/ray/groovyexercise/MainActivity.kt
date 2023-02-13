package com.ray.groovyexercise

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.ray.commonres.CommonRes
import com.ray.utils.CommonUtils

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        CommonUtils.showLog("logs")
        CommonRes.getCommonRes(this)
    }
}


