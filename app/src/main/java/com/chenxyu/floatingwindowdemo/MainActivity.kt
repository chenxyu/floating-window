package com.chenxyu.floatingwindowdemo

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.chenxyu.floatingwindow.FloatingWindow

class MainActivity : AppCompatActivity() {
    private lateinit var mFloatingWindow: FloatingWindow
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val textView = TextView(this)
        textView.text = "FloatingWindow"
        textView.gravity = Gravity.CENTER
        textView.setBackgroundColor(Color.GRAY)
        mFloatingWindow = FloatingWindow.Builder(this)
            .setCustomView(textView, 250, 150)
            .build()
        mFloatingWindow.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mFloatingWindow.onActivityResult(requestCode, resultCode, data)
    }

    override fun onDestroy() {
        mFloatingWindow.dismiss()
        super.onDestroy()
    }
}