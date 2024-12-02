package com.example.waldo

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.waldo.API.REST
import com.example.waldo.Interfaces.ApiService
import com.example.waldo.Repository.HistoryRepository
import com.google.android.material.navigation.NavigationView

class HistoryActivity : AppCompatActivity() {
    private lateinit var historyRepository : HistoryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        historyRepository = HistoryRepository(REST.getRestEngine().create(ApiService::class.java), this)
        loadHistory()
    }
    private fun loadHistory(){
        historyRepository.getHistoryParent(this)
    }
}