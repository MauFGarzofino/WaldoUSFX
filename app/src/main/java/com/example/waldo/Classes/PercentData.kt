package com.example.waldo.Classes

import com.example.waldo.Models.DataInformBattery
import com.example.waldo.Models.LocationData

class PercentData(private var locationsData: List<LocationData?>) {
    private var levels : ArrayList<DataInformBattery> = ArrayList<DataInformBattery>()
    fun getPercentValueTotal(min: Int, max: Int) : String{
        val number : Float = locationsData.filter { it?.batteryLevel in min..max }?.size!!.toFloat()
        val total : Float = locationsData.size.toFloat()
        return "${(number/total)*100}%"
    }
    fun getBatteriesPercent(min : Int, max : Int) : Float{
        val numbers = locationsData.filter { it?.batteryLevel in min..max }?.size!!.toFloat()
        levels.add(DataInformBattery(min, max, numbers.toInt()))
        return numbers
    }
    fun getLevels() : ArrayList<DataInformBattery>{
        return levels
    }
    fun getDates() : Map<String, LocationData?>{
        val dates: Map<String, LocationData?> = mapOf(
            "first" to locationsData.first(),
            "last" to locationsData.last()
        )
        return dates
    }

}