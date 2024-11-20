package com.example.waldo.Classes

import android.app.Activity
import android.content.Context
import com.google.zxing.integration.android.IntentIntegrator

class IntegratorCamera {
    companion object{
        fun getIntegrator(activity: Activity) : IntentIntegrator{
            val integrator = IntentIntegrator(activity)
            integrator.setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)
            integrator.setPrompt("Escanea el código QR")
            integrator.setCameraId(0)
            integrator.setBeepEnabled(true)
            integrator.setBarcodeImageEnabled(false)
            integrator.setOrientationLocked(true)

            return integrator
        }
    }
}