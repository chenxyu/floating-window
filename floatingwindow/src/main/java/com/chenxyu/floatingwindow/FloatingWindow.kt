package com.chenxyu.floatingwindow

import android.animation.ValueAnimator
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.Px
import java.lang.ref.WeakReference

/**
 * @Author:        ChenXingYu
 * @CreateDate:    2021/2/17 16:26
 * @Description:
 * @Version:       1.0
 */
private var mWindowManager: WindowManager? = null
private var mLayoutParams: WindowManager.LayoutParams? = null
private var mActivityReference: WeakReference<Activity>? = null
private var mCustomView: View? = null
private var mCustomViewWidth: Int = 200
private var mCustomViewHeight: Int = 200
private var mWindowWidth: Int = 0
private var mWindowHeight: Int = 0

class FloatingWindow(builder: Builder) {
    init {
        mActivityReference = builder.activityReference
        val activity = mActivityReference?.get() as Activity
        setWindowWH(activity)
        mCustomView = builder.customView
        mCustomViewWidth = builder.customViewWidth
        mCustomViewHeight = builder.customViewHeight
    }

    private fun setWindowWH(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            mWindowWidth = activity.windowManager.currentWindowMetrics.bounds.width() / 2 - 100
            mWindowHeight = activity.windowManager.currentWindowMetrics.bounds.height() / 2 - 100
        } else {
            mWindowWidth = activity.windowManager.defaultDisplay.width / 2 - 100
            mWindowHeight = activity.windowManager.defaultDisplay.height / 2 - 100
        }
    }

    fun show(): FloatingWindow {
        val activity = mActivityReference?.get() as Activity
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(activity.applicationContext)) {
                Toast.makeText(
                    activity.applicationContext,
                    "No permission", Toast.LENGTH_SHORT
                ).show()
                activity.startActivityForResult(
                    Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + activity.applicationContext.packageName)
                    ), 0
                )
            } else {
                showFloatingWindow()
            }
        } else {
            showFloatingWindow()
        }
        return this
    }

    fun dismiss() {
        mWindowManager?.removeView(mCustomView)
    }

    private fun showFloatingWindow() {
        val activity = mActivityReference?.get() as Activity
        mWindowManager =
            activity.application.getSystemService(Context.WINDOW_SERVICE) as WindowManager?
        mLayoutParams = WindowManager.LayoutParams()
        mLayoutParams?.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mLayoutParams?.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            mLayoutParams?.type = WindowManager.LayoutParams.TYPE_PHONE
        }
        mLayoutParams?.width = mCustomViewWidth
        mLayoutParams?.height = mCustomViewHeight
        mLayoutParams?.x = mWindowWidth
        mLayoutParams?.y = -mWindowWidth / 2
        mCustomView?.let {
            draggable(it)
            mWindowManager?.addView(it, mLayoutParams)
        }

    }

    private fun draggable(view: View) {
        var oldX = 0
        var oldY = 0
        view.setOnTouchListener { v, event ->
            v.performClick()
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    oldX = event.rawX.toInt()
                    oldY = event.rawY.toInt()
                }
                MotionEvent.ACTION_MOVE -> {
                    val newX = event.rawX.toInt()
                    val newY = event.rawY.toInt()
                    val movX = newX - oldX
                    val movY = newY - oldY
                    oldX = newX
                    oldY = newY
                    val x = mLayoutParams?.x?.plus(movX)
                    val y = mLayoutParams?.y?.plus(movY)
                    x?.let {
                        when {
                            it > mWindowWidth -> {
                                mLayoutParams?.x = mWindowWidth
                            }
                            it < -mWindowWidth -> {
                                mLayoutParams?.x = -mWindowWidth
                            }
                            else -> mLayoutParams?.x = it
                        }
                    }
                    y?.let {
                        when {
                            it > mWindowHeight -> {
                                mLayoutParams?.y = mWindowHeight
                            }
                            it < -mWindowHeight -> {
                                mLayoutParams?.y = -mWindowHeight
                            }
                            else -> mLayoutParams?.y = it
                        }
                    }

                    mWindowManager?.updateViewLayout(view, mLayoutParams)
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val x = mLayoutParams?.x
                    x?.let {
                        if (it < 0) {
                            magnetAnim(v, it, -mWindowWidth)
                        } else {
                            magnetAnim(v, it, mWindowWidth)
                        }
                    }
                }
                else -> {
                }
            }
            return@setOnTouchListener false
        }
    }

    private fun magnetAnim(view: View, from: Int, to: Int) {
        val va = ValueAnimator.ofInt(from, to)
        va.duration = 300
        va.addUpdateListener {
            mLayoutParams?.x = it.animatedValue as Int
            mWindowManager?.updateViewLayout(view, mLayoutParams)
        }
        va.start()
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            0 -> {
                val activity = mActivityReference?.get() as Activity
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!Settings.canDrawOverlays(activity.applicationContext)) {
                        Toast.makeText(
                            activity.applicationContext,
                            "Authorization failed", Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            activity.applicationContext,
                            "Authorization success", Toast.LENGTH_SHORT
                        ).show()
                        showFloatingWindow()
                    }
                } else {
                    showFloatingWindow()
                }
            }
        }
    }

    class Builder(activity: Activity) {
        var activityReference = WeakReference(activity)
        var customView: View? = null
        var customViewWidth: Int = 200
        var customViewHeight: Int = 200

        fun setCustomView(view: View, @Px width: Int, @Px height: Int): Builder {
            customView = view
            customViewWidth = width
            customViewHeight = height
            return this
        }

        fun build(): FloatingWindow = FloatingWindow(this).apply {

        }
    }
}