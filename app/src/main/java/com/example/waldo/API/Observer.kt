package com.example.waldo.API

import android.app.Activity
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.waldo.Classes.NestToken
import com.example.waldo.Interfaces.ApiService
import com.example.waldo.Models.KidDisplayModel
import com.example.waldo.R
import com.example.waldo.ui.KidsAdapter
import com.google.firebase.auth.FirebaseAuth
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit

class Observer {
    val nestToken : NestToken = NestToken.instance
    private var firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private lateinit var kidsAdapter: KidsAdapter

    fun observeData(apiService: ApiService,disposables: CompositeDisposable ,activity: Activity) {
        val recyclerView = activity.findViewById<RecyclerView>(R.id.kidsRecyclerView)
        val token = nestToken.getToken(activity)

        if (token == null) {
            Log.e("AuthError", "No se encontró el token JWT en SharedPreferences")
        }
        val observable = Observable.interval(0,10, TimeUnit.SECONDS)
            .flatMap { apiService.getEnrollmentsKids("Bearer $token")}
            .distinctUntilChanged()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

        val disposable = observable.subscribe ({
                dataList -> kidsAdapter = KidsAdapter( dataList.map { kid ->  KidDisplayModel("${kid.familyName} ${kid.givenName}",
            if (kid.connectionStatus == "true")  "Tiene acceso a internet" else "Sin acceso a internet" , kid.photo) }.toMutableList() )
                recyclerView.adapter = kidsAdapter
        }, {
                error ->
            Toast.makeText(activity, "Error en cargar los Hijos", Toast.LENGTH_SHORT).show()
        })
        disposables.add(disposable)
    }
}