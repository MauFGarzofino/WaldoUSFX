package com.example.waldo.Classes

import com.example.waldo.Models.Code
import com.example.waldo.Models.LocationData

class DataCodes {

    private var codes: ArrayList<Code?> = ArrayList<Code?>()
    companion object{
        val instance : DataCodes by lazy {DataCodes()}
    }
    fun addDataLocation(code: Code?){
        codes.add(code)
    }
    fun removeDataLocation(code : Code?){
        codes.remove(code)
    }
    fun getCodes() : ArrayList<Code?>{
        return codes
    }
}