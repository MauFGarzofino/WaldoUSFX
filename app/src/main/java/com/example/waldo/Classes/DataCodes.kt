package com.example.waldo.Classes

import com.example.waldo.Models.LocationData

class DataLocations {
    private var locations: ArrayList<LocationData> = ArrayList<LocationData>()
    companion object{
        val instance : DataLocations by lazy {DataLocations()}
    }
    fun addDataLocation(locationData: LocationData){
        locations.add(locationData)
    }
    fun removeDataLocation(locationData : LocationData){
        locations.remove(locationData)
    }
}