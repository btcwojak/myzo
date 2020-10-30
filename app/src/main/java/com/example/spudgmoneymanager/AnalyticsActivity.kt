package com.example.spudgmoneymanager

import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartModel
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartType
import com.github.aachartmodel.aainfographics.aachartcreator.AAChartView
import com.github.aachartmodel.aainfographics.aachartcreator.AASeriesElement
import java.lang.Math.E

class AnalyticsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        val aaChartView = findViewById<AAChartView>(R.id.aa_chart_view)

        val dbHandlerCategory = CategoriesHandler(this, null)
        val dbHandlerTransaction = TransactionsHandler(this, null)
        var categories = dbHandlerCategory.getAllCategoryTitles()

        var graphElements: Array<AASeriesElement> = Array(categories.size) { AASeriesElement() }


        for (i in 0 until categories.size) {
            var transactions = dbHandlerTransaction.getTransactionsForCategory(i+1)
            graphElements[i] = AASeriesElement().name(categories[i]).data(transactions)
        }

        val aaChartModel : AAChartModel = AAChartModel()
            .chartType(AAChartType.Column)
            .title("title")
            .subtitle("subtitle")
            .backgroundColor("#4b2b7f")
            .dataLabelsEnabled(true)
            .series(
                graphElements
            )

        aaChartView.aa_drawChartWithChartModel(aaChartModel)

    }
}