package com.officinetop.officine.views

import android.annotation.SuppressLint
import android.content.Context
import androidx.viewpager.widget.ViewPager
import android.util.AttributeSet
import android.view.MotionEvent
import com.officinetop.officine.R


class AutoViewPager : ViewPager, Runnable {

    private var offset = 0f

    var duration = DEFAULT_DURATION

    var indeterminate = false

    var autoScroll = false
        set(value) {
            field = value
            if (value) start() else stop()
        }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init(context, attrs)
    }

    private fun init(context: Context, attrs: AttributeSet?) {
        attrs?.let {
            val a = context.obtainStyledAttributes(it, R.styleable.AutoViewPager)
            with(a) {
                indeterminate = getBoolean(R.styleable.AutoViewPager_avp_indeterminate, false)
                autoScroll = getBoolean(R.styleable.AutoViewPager_avp_autoScroll, false)
                duration = getInt(R.styleable.AutoViewPager_avp_duration, DEFAULT_DURATION)
                recycle()
            }
        }
    }

    override fun onInterceptTouchEvent(event: MotionEvent?): Boolean {
        event?.takeIf { indeterminate || autoScroll }
                ?.actionMasked
                .takeIf { it == MotionEvent.ACTION_DOWN }
                ?.run {
                    offset = event!!.x
                    reset()
                }
        return super.onInterceptTouchEvent(event)
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.takeIf { indeterminate }
                ?.actionMasked
                .takeIf { it == MotionEvent.ACTION_UP }
                ?.run {
                    event?.x
                            ?.takeIf { it < offset && currentItem == lastItem }
                            ?.run {
                                post { setCurrentItem(0, false) }
                            }
                }
        return super.onTouchEvent(event)
    }

    override fun run() {
        if (!isShown) return
        currentItem = if (currentItem == lastItem) 0 else currentItem + 1
        start()
    }

    fun start() = postDelayed(this, duration.toLong())

    fun stop() = removeCallbacks(this)

    fun reset() {
        stop()
        start()
    }

    companion object {
        const val DEFAULT_DURATION = 5000
    }


    val ViewPager.lastItem: Int?
        get() = adapter?.count?.minus(1)
}