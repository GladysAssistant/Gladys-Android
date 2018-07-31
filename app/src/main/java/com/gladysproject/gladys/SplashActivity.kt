package com.gladysproject.gladys

import android.os.Bundle
import android.app.Activity
import android.content.Intent
import android.os.Handler

import kotlinx.android.synthetic.main.activity_splash.*

class SplashActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler().postDelayed({
            val intent = Intent(this@SplashActivity, MainActivity::class.java)
            startActivity(intent)
        }, 2000)
    }

}
