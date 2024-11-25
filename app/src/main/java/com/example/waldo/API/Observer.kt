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

        val observable = Observable.merge(
            Observable.interval(0, 3, TimeUnit.SECONDS).map { Unit }, // Actualización periódica
            manualTrigger // Actualización manual
        )
            .flatMap {
                apiService.getEnrollmentsKids(FirebaseAuth.getInstance().currentUser?.uid.toString(), "Bearer $token")
            }
            .distinctUntilChanged()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())

        val disposable = observable.subscribe({ dataList ->
            val currentTime = System.currentTimeMillis()

            dataList.forEach { kid ->
                val existingKid = kidsAdapter.kids.find { it.id_User == kid.id_Kid }
                if (existingKid != null) {
                    if (existingKid.connectionStatus != "Internet Disponible") {
                        existingKid.connectionStatus = "Internet Disponible"
                        existingKid.lastUpdated = currentTime // Actualiza solo si el estado cambia
                        Log.d("Observer", "Niño actualizado: ${existingKid.name}, Última actualización: $currentTime")
                    }
                } else {
                    // Agrega un nuevo niño si no existe
                    val newKid = KidDisplayModel(
                        id_Enrollment = kid.id,
                        id_User = kid.id_Kid,
                        name = "${kid.familyName} ${kid.givenName}",
                        connectionStatus = "Internet Disponible",
                        photo = kid.photo,
                        lastUpdated = currentTime
                    )
                    kidsAdapter.kids.add(newKid)
                    Log.d("Observer", "Nuevo niño agregado: ${newKid.name}")
                }
            }


            // Elimina niños que ya no están en la respuesta del backend
            val receivedUserIds = dataList.map { it.id_Kid }
            kidsAdapter.kids.removeAll { it.id_User !in receivedUserIds }

            kidsAdapter.kids.forEach {
                Log.d("Observer", """
        Niño procesado:
        Nombre: ${it.name}
        ID: ${it.id_User}
        Estado: ${it.connectionStatus}
        Última actualización: ${it.lastUpdated}
    """.trimIndent())
            }

            // Actualización final
            kidsAdapter.notifyDataSetChanged()

            // Log de resumen después de procesar todos los niños
            kidsAdapter.kids.forEach {
                Log.d("Observer", "Niño en la lista final: ${it.name}, ID: ${it.id_User}, Estado: ${it.connectionStatus}, Última actualización: ${it.lastUpdated}")
            }
        }, { error ->
            Log.e("Observer", "Error en el Observer: ${error.message}", error)
        })

        disposables.add(disposable)
    }

    // Método para disparar actualizaciones manuales
    fun triggerUpdate() {
        manualTrigger.onNext(Unit)
    }
}

