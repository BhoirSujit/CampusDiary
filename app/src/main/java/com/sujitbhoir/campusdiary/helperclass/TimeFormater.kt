package com.sujitbhoir.campusdiary.helperclass

import android.text.format.DateFormat
import com.google.firebase.Timestamp
import java.util.Calendar
import java.util.Locale

class TimeFormater {

    private fun getDate(timestamp: Long) :String {
        val calendar = Calendar.getInstance(Locale.ENGLISH)
        calendar.timeInMillis = timestamp * 1000L
        return DateFormat.format("dd-MM-yyyy",calendar).toString()

    }

    fun getHoursMin(timestamp : Any) : String
    {
        var ts : Long = 0
        if (timestamp is Timestamp)
        {
            ts = timestamp.seconds
        }
        val curr = Calendar.getInstance(Locale.ENGLISH)
        curr.timeInMillis = Timestamp.now().seconds * 1000L


        val time = Calendar.getInstance(Locale.ENGLISH)
        time.timeInMillis = ts * 1000L

        return DateFormat.format("hh:mm a",time).toString()
    }

    fun isDiffDay(time1 : Long, time2 : Long) : Boolean
    {
        if (DateFormat.format("dd-MM-yyyy",time1) == DateFormat.format("dd-MM-yyyy",time2))
            return false
        return true
    }

    fun isDiffDay(time1 : Timestamp, time2 : Timestamp) : Boolean
    {
        return isDiffDay(time1.seconds, time2.seconds)
    }
    fun getFormatedTime(time : Timestamp) : String
    {
        val t = time.seconds
        return getFormatedTime(t)
    }

    fun getFormatedTime(timestamp : Long) : String
    {
        val curr = Calendar.getInstance(Locale.ENGLISH)
        curr.timeInMillis = Timestamp.now().seconds * 1000L


        val time = Calendar.getInstance(Locale.ENGLISH)
        time.timeInMillis = timestamp * 1000L

        var t = ""

        if (DateFormat.format("dd-MM-yyyy",curr) == DateFormat.format("dd-MM-yyyy",time))
        {
            return DateFormat.format("hh:mm a",time).toString()


        }
        else
        {
            t = DateFormat.format("dd-MM-yyyy",time).toString()
        }



        return t
    }
}