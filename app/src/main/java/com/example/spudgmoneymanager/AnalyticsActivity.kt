package com.example.spudgmoneymanager

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartView
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import kotlinx.android.synthetic.main.activity_analytics.*

class AnalyticsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        back_to_trans_from_analytics.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val aaChartView = findViewById<AAChartView>(R.id.aa_chart_view)

        val dbHandlerCategory = CategoriesHandler(this, null)
        val dbHandlerTransaction = TransactionsHandler(this, null)
        var categories = dbHandlerCategory.getAllCategories()
        var categoryTitles: ArrayList<String> = ArrayList()
        var categoryColours: ArrayList<String> = ArrayList()

        for (category in categories) {
            categoryTitles.add(category.title)
        }

        for (category in categories) {
            var intColor = category.colour.toInt()
            categoryColours.add(java.lang.String.format("#%06X", 0xFFFFFF and intColor))
        }

        var graphElements: Array<AASeriesElement> = Array(categories.size) { AASeriesElement() }

        for (i in 0 until categories.size) {
            var totalForCategory = dbHandlerTransaction.getTransactionTotalForCategory(i + 1)
            var categoryNameTotal = arrayOf(categoryTitles[i], totalForCategory)
            graphElements[i] = AASeriesElement().name(categoryTitles[i]).data(arrayOf(categoryNameTotal)).color(categoryColours[i])
        }

        val aaChartModel : AAChartModel = AAChartModel()
            .chartType(AAChartType.Column)
            .title("Transactions")
            .subtitle("per category")
            .backgroundColor("#FFFFFF")
            .yAxisAllowDecimals(true)
            .dataLabelsEnabled(true)
            .xAxisLabelsEnabled(false)
            .yAxisTitle("Total transactions")
            .series(
                graphElements
            )

        aaChartView.aa_drawChartWithChartModel(aaChartModel)

    }

}