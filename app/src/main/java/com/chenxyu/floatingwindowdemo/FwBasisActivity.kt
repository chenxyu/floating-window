package com.chenxyu.floatingwindowdemo

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.setMargins
import com.chenxyu.floatingwindow.FloatingWindow

private lateinit var mFloatingWindow: FloatingWindow

class FwBasisActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fw_basis)

        val relativeLayout = RelativeLayout(this).apply {
            setBackgroundColor(Color.GRAY)
            addView(TextView(this@FwBasisActivity).apply {
                layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    addRule(RelativeLayout.CENTER_IN_PARENT)
                    setMargins(10)
                }
                text = "Test Window"
                gravity = Gravity.CENTER
                setBackgroundColor(Color.GRAY)
                setOnClickListener {
                    Toast.makeText(this@FwBasisActivity, "FloatingWindow", Toast.LENGTH_SHORT)
                        .show()
                }
            })
            addView(TextView(this@FwBasisActivity).apply {
                setBackgroundColor(Color.GRAY)
                layoutParams = RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT,
                    RelativeLayout.LayoutParams.WRAP_CONTENT
                ).apply {
                    addRule(RelativeLayout.ALIGN_PARENT_TOP)
                    addRule(RelativeLayout.ALIGN_PARENT_END)
                }
                text = "X"
                setTextColor(Color.BLACK)
                setOnClickListener {
                    mFloatingWindow.dismiss()
                }
            })
        }

        mFloatingWindow = FloatingWindow.Builder(this)
            .setCustomView(relativeLayout, 250, 150)
            .build()

        findViewById<Button>(R.id.show).setOnClickListener {
            mFloatingWindow.show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        mFloatingWindow.onActivityResult(requestCode, resultCode, data)
    }
}