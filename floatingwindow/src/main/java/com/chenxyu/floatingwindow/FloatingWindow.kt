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
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Toast
import androidx.annotation.Px
import java.lang.ref.WeakReference

/**
 * @Author:        ChenXingYu
 * @CreateDate:    2021/2/17 16:26
 * @Description:   悬浮窗
 * @Version:       1.0
 */
private var isShow: Boolean = false
private var mWindowManager: WindowManager? = null
private var mLayoutParams: WindowManager.LayoutParams? = null
private var mActivityReference: WeakReference<Activity>? = null
private var mCustomView: View? = null
private var mCustomViewWidth: Int = 200
private var mCustomViewHeight: Int = 200
private var mWindowWidth: Int = 0
private var mWindowHeight: Int = 0
private var mCheckPermission: Boolean = true

class FloatingWindow(builder: Builder) {
    init {
        mActivityReference = builder.activityReference
        val activity = mActivityReference?.get() as Activity
        setWindowWH(activity)
        mCustomView = builder.customView
        mCustomViewWidth = builder.customViewWidth
        mCustomViewHeight = builder.customViewHeight
        mCheckPermission = builder.checkPermission
    }

    companion object {
        private const val REQUEST_CODE = 101
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
        val activity = mActivityReference?.get()
        activity?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(it.applicationContext) && mCheckPermission) {
                    Toast.makeText(
                        it.applicationContext,
                        "No permission", Toast.LENGTH_SHORT
                    ).show()
                    it.startActivityForResult(
                        Intent(
                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + it.applicationContext.packageName)
                        ), REQUEST_CODE
                    )
                } else {
                    showFloatingWindow()
                }
            } else {
                showFloatingWindow()
            }
        }
        return this
    }

    fun dismiss() {
        mCustomView?.let {
            isShow = false
            mWindowManager?.removeView(it)
        }
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
            setTouchListener(it)
            mWindowManager?.addView(it, mLayoutParams)
            isShow = true
        }

    }

    private fun setTouchListener(view: View) {
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                view.getChildAt(i).setOnTouchListener(object : MoveTouchListener(view) {
                    override fun actionUpOrCancel(v: View) {
                        magnet(view)
                    }
                })
            }
        }
        view.setOnTouchListener(object : MoveTouchListener(view) {
            override fun actionUpOrCancel(v: View) {
                magnet(view)
            }
        })
    }

    private fun magnet(v: View) {
        if (isShow) {
            val x = mLayoutParams?.x
            x?.let {
                if (it < 0) {
                    intValueAnim(v, it, -mWindowWidth)
                } else {
                    intValueAnim(v, it, mWindowWidth)
                }
            }
        }
    }

    private fun intValueAnim(view: View, from: Int, to: Int) {
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
            REQUEST_CODE -> {
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

    private abstract class MoveTouchListener(private val view: View) : View.OnTouchListener {
        private var oldX = 0
        private var oldY = 0

        override fun onTouch(v: View, event: MotionEvent?): Boolean {
            when (event?.action) {
                MotionEvent.ACTION_DOWN -> {
                    oldX = event.rawX.toInt()
                    oldY = event.rawY.toInt()
                }
                MotionEvent.ACTION_MOVE -> {
                    val newX = event.rawX.toInt()
                    val newY = event.rawY.toInt()
                    val moveX = newX - oldX
                    val moveY = newY - oldY
                    oldX = newX
                    oldY = newY
                    val x = mLayoutParams?.x?.plus(moveX)
                    val y = mLayoutParams?.y?.plus(moveY)
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
                    actionUpOrCancel(v)
                }
            }
            return false
        }

        abstract fun actionUpOrCancel(v: View)
    }

    class Builder(activity: Activity) {
        var activityReference = WeakReference(activity)
        var customView: View? = null
        var customViewWidth: Int = 200
        var customViewHeight: Int = 200
        var checkPermission: Boolean = true

        /**
         * 自定义View
         */
        fun setCustomView(view: View, @Px width: Int, @Px height: Int): Builder {
            customView = view
            customViewWidth = width
            customViewHeight = height
            return this
        }

        /**
         * 检查权限（默认：true）
         */
        fun checkPermission(check: Boolean) {
            checkPermission = check
        }

        fun build(): FloatingWindow = FloatingWindow(this).apply {

        }
    }
}