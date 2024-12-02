package com.example.waldo.Classes

import android.app.Activity
import android.graphics.Color
import android.util.TypedValue
import android.widget.LinearLayout
import android.widget.TextView
import com.example.waldo.Models.DataInformBattery
import com.example.waldo.Models.LocationData

class InformBattery(private var linearLayout: LinearLayout, private var activity: Activity) {
    private var total = 0
    private var count = 0

    fun generateInform(levels : ArrayList<DataInformBattery>, datas : Map<String, LocationData?>, colors : MutableList<Int?>){
        val textViewTittle = TextView(activity).apply {
            text = "Desde la fecha ${datas["first"]?.created_at} hasta esta fecha ${datas["last"]?.created_at} se tiene los siguientes niveles de bateria"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#8db2ec"))
            setPadding(16, 16, 16, 16)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        val paramsTittle = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(16, 20, 16, 20)
        }
        textViewTittle.layoutParams = paramsTittle
        linearLayout.addView(textViewTittle)

        levels.forEach{level->
            total += level.number
            val textView = TextView(activity).apply {
                text = "La cantidad de veces que el niño tuvo un nivel de batería entre ${level.min}% al ${level.max}% fue de ${level.number} veces"
                setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
                setBackgroundColor(Color.parseColor("#${Integer.toHexString(colors[count]!!).toUpperCase()}"))
                setTextColor(Color.WHITE)
                setPadding(16, 12, 16, 12)
                setTypeface(null, android.graphics.Typeface.NORMAL)
                elevation = 4f // Sombra
            }
            val params = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(16, 8, 16, 8)
            }
            textView.layoutParams = params
            linearLayout.addView(textView)
            count++
        }
        val textViewTotal = TextView(activity).apply {
            text = "Con un total de ${total} registros de batería"
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 18f)
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.parseColor("#4A90E2"))
            setPadding(16, 16, 16, 16)
            setTypeface(null, android.graphics.Typeface.BOLD)
        }
        val paramsTotal = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        ).apply {
            setMargins(16, 20, 16, 20)
        }
        textViewTotal.layoutParams = paramsTotal
        linearLayout.addView(textViewTotal)
    }

}