package com.officinetop.officine.views

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.officinetop.officine.R
import io.blackbox_vision.wheelview.view.WheelView
import kotlinx.android.synthetic.main.dialog_wheel_timepicker.*

@SuppressLint("SetTextI18n")
class TimeWheelPicker {

    class Builder(context: Context, private var is12Hour: Boolean = false) {
        private val dialog = BottomSheetDialog(context)
        private var confirmBtn: TextView
        private var cancelBtn: TextView

        private var wheelViewHour: WheelView
        private var wheelViewMinute: WheelView

        private var hourList: MutableList<String> = ArrayList()
        private var minuteList: MutableList<String> = ArrayList()

        private var initialMinuteList: MutableList<String> = ArrayList()

        private var startHour = 0
        private var endHour = 23

        private var startMin = 0
        private var endMin = 59
        private var endMinl = 59
        private var startMinl = 0
        private var onTimePicked: OnTimePicked? = null

        init {
            dialog.setContentView(R.layout.dialog_wheel_timepicker)
            dialog.setCancelable(false)

            confirmBtn = dialog.done
            cancelBtn = dialog.cancel
            wheelViewHour = dialog.wheel_view_hour
            wheelViewMinute = dialog.wheel_view_minute

            wheelViewHour.setInitPosition(0)
            wheelViewHour.setCanLoop(false)

            wheelViewMinute.setInitPosition(0)
            wheelViewMinute.setCanLoop(false)


            if (is12Hour) {
                startHour = 1
                endHour = 12
                with(dialog.wheel_view_am_pm) {
                    visibility = View.VISIBLE
                    setInitPosition(0)
                    setCanLoop(false)
                    setItems(listOf("AM", "PM"))
                }
            }

            /* for (i in startHour..endHour)
                 hourList.add(addFormatter(i) + i.toString())

             for (i in startMin..endMin)
                 minuteList.add(addFormatter(i) + i.toString())



             initialMinuteList.addAll(minuteList)

             wheelViewMinute.setItems(minuteList)
             wheelViewHour.setItems(hourList)*/

            cancelBtn.setOnClickListener { this.dialog.dismiss() }

            confirmBtn.setOnClickListener {
                this.dialog.dismiss()

                onTimePicked?.onPicked(hourList[wheelViewHour.selectedItem].toInt(), minuteList[wheelViewMinute.selectedItem].toInt())

            }

            wheelViewHour.setLoopListener { position ->
                if (!is12Hour) {
                    setWheelValues(position)
                }


            }


        }

        private fun setWheelValues(position: Int) {
            Log.d("Builder", "setWheelValues: position = $position")
            Log.d("Builder", "setWheelValues: $startHour:$startMin - $endHour:$endMin")

            when {
                hourList[position].toInt() == startHour && startHour != endHour -> {
                    Log.d("start_end_time", "tartHour && startHour != endHour:                  " + "startHour: " + startHour + " startMin: " + startMin + " endHour:" + endHour + " endMin: " + endMin)

                    minuteList.clear()
                    if (hourList.size == 1) {
                        for (i in startMin..endMin)
                            minuteList.add(addFormatter(i) + i.toString())
                    } else {
                        for (i in startMin..endMinl)
                            minuteList.add(addFormatter(i) + i.toString())
                    }
                    wheelViewMinute.setItems(minuteList)
                }

                hourList[position].toInt() == startHour || startHour != endHour && hourList[position].toInt() != endHour -> {
                    minuteList.clear()
                    if (hourList.size == 1) {
                        for (i in startMin..endMin)
                            minuteList.add(addFormatter(i) + i.toString())
                    } else {
                        for (i in startMinl..endMinl)
                            minuteList.add(addFormatter(i) + i.toString())
                    }


                    wheelViewMinute.setItems(minuteList)
                }

                hourList[position].toInt() == endHour -> {

                    Log.d("start_end_time", "endHour:                  " + "startHour: " + startHour + " startMin: " + startMin + " endHour:" + endHour + " endMin: " + endMin)

                    minuteList.clear()

                    if (hourList.size == 1) {
                        minuteList.clear()
                        for (i in startMin..endMin)
                            minuteList.add(addFormatter(i) + i.toString())
                    } else {
                        for (i in startMinl..endMin)
                            minuteList.add(addFormatter(i) + i.toString())
                    }

                    wheelViewMinute.setItems(minuteList)
                }
                else -> {
                    minuteList.clear()
                    minuteList.addAll(initialMinuteList)
                    wheelViewMinute.setItems(initialMinuteList)
                }

            }

            wheelViewMinute.setInitPosition(0)
        }

        fun setOnTimePickedListener(onTimePicked: OnTimePicked): Builder {
            this.onTimePicked = onTimePicked
            return this
        }


        fun setStartEndTime(startHour: Int, endHour: Int, startMin: Int, endMin: Int): Builder {

            if (is12Hour) {
                throw IllegalStateException("Time Range is not allowed in 12 hour mode")
            }

            this.startHour = startHour
            this.startMin = startMin
            this.endHour = endHour
            this.endMin = endMin
            if (startHour > endHour)
                throw IllegalStateException("Start hour must not be greater than end hour, start hour = $startHour, end hour = $endHour")


            hourList.clear()
            minuteList.clear()
            for (i in startHour..endHour) {
                hourList.add(addFormatter(i) + i.toString())
            }

            for (i in startMin..endMinl)
                minuteList.add(addFormatter(i) + i.toString())

            if (hourList.size == 1) {
                minuteList.clear()
                for (i in startMin..endMin)
                    minuteList.add(addFormatter(i) + i.toString())
            }

            wheelViewHour.setItems(hourList)
            wheelViewMinute.setItems(minuteList)
            wheelViewHour.setInitPosition(0)
            wheelViewMinute.setInitPosition(0)

            return this

        }

        fun setConfirmText(text: String): Builder {
            confirmBtn.text = text
            return this
        }

        fun setCancelText(text: String): Builder {
            cancelBtn.text = text
            return this
        }

        fun setHourTextSize(sizeInPx: Float): Builder {
            wheelViewHour.setTextSize(sizeInPx)
            return this
        }

        fun setMinuteTextSize(sizeInPx: Float): Builder {
            wheelViewMinute.setTextSize(sizeInPx)
            return this
        }


        fun setConfirmTextColor(color: Int): Builder {
            confirmBtn.setTextColor(color)
            return this
        }


        fun setCancelTextColor(color: Int): Builder {
            cancelBtn.setTextColor(color)
            return this
        }

        fun setTitle(title: String, color: Int = Color.BLACK): Builder {
            dialog.dialog_title.text = title
            dialog.dialog_title.setTextColor(color)
            return this
        }

        fun setSubtitle(subtitle: String, color: Int = Color.BLACK): Builder {
            dialog.dialog_subtitle.text = subtitle
            dialog.dialog_subtitle.setTextColor(color)
            return this
        }

        fun build(): Dialog {

            return dialog
        }

        private fun addFormatter(number: Int) = if (number < 10) "0" else ""

    }


    interface OnTimePicked {
        fun onPicked(hourOfDay: Int, minute: Int)
    }

}