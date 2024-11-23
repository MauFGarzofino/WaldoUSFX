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

import io.reactivex.rxjava3.subjects.PublishSubject

class Observer {

    val nestToken: NestToken = NestToken.instance
    private val manualTrigger: PublishSubject<Unit> = PublishSubject.create() // Para actualizaciones manuales

    fun observeData(
        apiService: ApiService,
        disposables: CompositeDisposable,
        activity: Activity,
        kidsAdapter: KidsAdapter
    ) {
        val token = nestToken.getToken(activity)

        if (token == null) {
            Log.e("AuthError", "No se encontró el token JWT en SharedPreferences")
            return
        }

        // Combina actualizaciones periódicas con manuales
        val observable = Observable.merge(
            Observable.interval(0, 3, TimeUnit.SECONDS).map { Unit }, // Actualización periódica
            manualTrigger // Actualización manual
        )
            .flatMap {
                apiService.getEnrollmentsKids("Bearer $token")
            }
            .distinctUntilChanged()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

        val disposable = observable.subscribe({ dataList ->
            Log.d("Observer", "Datos recibidos: ${dataList.size}")
            val displayModels = dataList.map { kid ->
                KidDisplayModel(
                    id_User = kid.id_Kid, // Pasa el id_User desde el modelo original
                    name = "${kid.familyName} ${kid.givenName}",
                    connectionStatus = if (kid.connectionStatus == "true") "Tiene acceso a internet" else "Sin acceso a internet",
                    photo = kid.photo
                )
            }
            kidsAdapter.updateKidsList(displayModels)
        }, { error ->
            Log.e("Observer", "Error en cargar los Hijos", error)
        })

        disposables.add(disposable)
    }

    // Método para disparar actualizaciones manuales
    fun triggerUpdate() {
        manualTrigger.onNext(Unit)
    }
}

