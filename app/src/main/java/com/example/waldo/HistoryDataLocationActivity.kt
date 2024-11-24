package com.example.waldo

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.waldo.API.REST
import com.example.waldo.Interfaces.ApiService
import com.example.waldo.Repository.LocationDataRepository

class HistoryDataLocationActivity : AppCompatActivity() {
    private lateinit var locationDataRepository : LocationDataRepository
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history_data_location)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val intent = intent
        val idKid = intent.getStringExtra("id_Kid")
        locationDataRepository = LocationDataRepository(REST.getRestEngine().create(ApiService::class.java), this)
        locationDataRepository.getHistoryForKid(idKid.toString())
    }
}