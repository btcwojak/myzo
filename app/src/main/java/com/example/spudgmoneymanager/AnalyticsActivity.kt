package com.example.spudgmoneymanager

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.WindowManager
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import kotlinx.android.synthetic.main.activity_analytics.*

class AnalyticsActivity : AppCompatActivity() {

    var rainFall: ArrayList<Float> = arrayListOf(98.8f, 123.8f, 161.6f)
    var monthNames: ArrayList<String> = arrayListOf("First", "Second", "Third")
    var entries: ArrayList<PieEntry> = ArrayList()
    var categoryTitles: ArrayList<String> = ArrayList()
    var categoryColours: ArrayList<Int> = ArrayList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        setupPieChart()

        back_to_trans_from_analytics.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val dbHandlerCategory = CategoriesHandler(this, null)
        var categories = dbHandlerCategory.getAllCategories()

        for (category in categories) {
            categoryTitles.add(category.title)
        }

        for (category in categories) {
            var intColor = category.colour.toInt()
            //categoryColours.add(java.lang.String.format("#%06X", 0xFFFFFF and intColor))
            categoryColours.add(intColor)
        }


    }


    private fun setupPieChart() {

        for (i in 0 until 3) {
            entries.add(PieEntry(rainFall[i], monthNames[i]))
        }

        var dataSet: PieDataSet = PieDataSet(entries, "Rainfall")
        //dataSet.colors = categoryColours.toMutableList()
        var data: PieData = PieData(dataSet)

        var chart: PieChart = chart1
        chart.data = data
        chart.animateY(600)
        chart.invalidate()

    }





}




