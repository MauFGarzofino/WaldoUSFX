package com.example.waldo

import android.graphics.Color
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.waldo.API.REST
import com.example.waldo.Classes.InformBattery
import com.example.waldo.Classes.PercentData
import com.example.waldo.Interfaces.ApiService
import com.example.waldo.Models.LocationData
import com.example.waldo.Repository.LocationDataRepository
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry

class BatteryLevelReportActivity : AppCompatActivity() {
    private lateinit var pieChart: PieChart
    private lateinit var locationDataRepository : LocationDataRepository
    private lateinit var informBattery : InformBattery
    private lateinit var percentData: PercentData
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_battery_level_report_acitvity)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val intent = intent
        val idKid : String? = intent.getStringExtra("id_Kid")
        pieChart = findViewById(R.id.pieChart)
        val linearLayoutInformBattery = findViewById<LinearLayout>(R.id.linearLayoutInformBattery)
        informBattery = InformBattery(linearLayoutInformBattery, this)
        locationDataRepository = LocationDataRepository(REST.getRestEngine().create(ApiService::class.java), this)
        locationDataRepository.getHistoryForKid(idKid){ locationsData ->
            percentData = PercentData(locationsData!!)
            val dataSet = PieDataSet(setUpEntries(percentData), "Niveles de Bateria")
            dataSet.colors = listOf(
                Color.parseColor("#FF6384"),
                Color.parseColor("#FFCE56"),
                Color.parseColor("#f99a24"),
                Color.parseColor("#aff0a4"),
            )
            val data = PieData(dataSet)
            data.setValueTextSize(12f)
            data.setValueTextColor(Color.WHITE)
            pieChart.data = data
            pieChart.description.isEnabled = false
            pieChart.isDrawHoleEnabled = true
            pieChart.holeRadius = 40f
            pieChart.setEntryLabelColor(Color.BLACK)
            pieChart.setEntryLabelTextSize(14f)

            pieChart.invalidate()

            informBattery.generateInform(percentData.getLevels(), percentData.getDates(), dataSet.colors)
        }
    }
    private fun setUpEntries(percentData: PercentData) : ArrayList<PieEntry>{
        val entries = ArrayList<PieEntry>()
        entries.add(PieEntry(percentData.getBatteriesPercent(1,20), percentData.getPercentValueTotal(1,20)))
        entries.add(PieEntry(percentData.getBatteriesPercent(21,50), percentData.getPercentValueTotal(21,50)))
        entries.add(PieEntry(percentData.getBatteriesPercent(51,70), percentData.getPercentValueTotal(51,70)))
        entries.add(PieEntry(percentData.getBatteriesPercent(71,100), percentData.getPercentValueTotal(71,100)))
        return entries
    }
}