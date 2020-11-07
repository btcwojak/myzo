package com.example.spudgmoneymanager

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlinx.android.synthetic.main.activity_analytics.*

class AnalyticsActivity : AppCompatActivity() {


    var entriesInc: ArrayList<PieEntry> = ArrayList()
    var entriesExp: ArrayList<PieEntry> = ArrayList()

    var categoryTitlesInc: ArrayList<String> = ArrayList()
    var categoryTitlesExp: ArrayList<String> = ArrayList()

    var categoryColoursInc: ArrayList<Int> = ArrayList()
    var categoryColoursExp: ArrayList<Int> = ArrayList()

    var categoryTotalsInc: ArrayList<Float> = ArrayList()
    var categoryTotalsExp: ArrayList<Float> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        val dbHandlerCategory = CategoriesHandler(this, null)
        val dbHandlerTransaction = TransactionsHandler(this, null)
        var categories = dbHandlerCategory.getAllCategories()

        back_to_trans_from_analytics.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        for (category in categories) {
            if (dbHandlerTransaction.getTransactionTotalForCategory(category.id) > 0F) {
                categoryTitlesInc.add(category.title)
            } else if (dbHandlerTransaction.getTransactionTotalForCategory(category.id) < 0F) {
                categoryTitlesExp.add(category.title)
            }
        }

        for (category in categories) {
            var total = dbHandlerTransaction.getTransactionTotalForCategory(category.id)
            if (total > 0F) {
                categoryTotalsInc.add(String.format("%.2f", total).toFloat())
            } else if (total < 0F) {
                categoryTotalsExp.add(String.format("%.2f", -total).toFloat())
            }
        }

        for (category in categories) {
            var intColor = category.colour.toInt()
            //categoryColours.add(java.lang.String.format("#%06X", 0xFFFFFF and intColor))
            if (dbHandlerTransaction.getTransactionTotalForCategory(category.id) > 0F) {
                categoryColoursInc.add(intColor)
            } else if (dbHandlerTransaction.getTransactionTotalForCategory(category.id) < 0F) {
                categoryColoursExp.add(intColor)
            }
        }

        setupPieChartIncome()
        setupPieChartExpenditure()

    }


    private fun setupPieChartIncome() {

        for (i in 0 until categoryTotalsInc.size) {
            entriesInc.add(PieEntry(categoryTotalsInc[i], categoryTitlesInc[i]))
        }

        var dataSetInc: PieDataSet = PieDataSet(entriesInc, "")
        dataSetInc.colors = categoryColoursInc.toMutableList()
        var dataInc: PieData = PieData(dataSetInc)

        var chartInc: PieChart = chartInc
        chartInc.data = dataInc
        chartInc.animateY(800)
        chartInc.setNoDataText("There aren't any income categories with data yet.")
        chartInc.dragDecelerationFrictionCoef = .95f
        chartInc.setDrawEntryLabels(false)

        val l: Legend = chartInc.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)

        chartInc.description.isEnabled = false

        dataSetInc.valueLinePart1OffsetPercentage = 80f
        dataSetInc.valueLinePart1Length = 0.4f
        dataSetInc.valueLinePart2Length = 0.8f
        dataSetInc.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE
        dataSetInc.isDrawValuesEnabled

        dataInc.setValueFormatter(PercentFormatter())
        dataInc.setValueTextSize(11f)
        dataInc.setValueTextColor(Color.BLACK)

        chartInc.invalidate()

    }

    private fun setupPieChartExpenditure() {

        for (i in 0 until categoryTotalsExp.size) {
            entriesExp.add(PieEntry(categoryTotalsExp[i], categoryTitlesExp[i]))
        }

        var dataSetExp: PieDataSet = PieDataSet(entriesExp, "")
        dataSetExp.colors = categoryColoursExp.toMutableList()
        var dataExp: PieData = PieData(dataSetExp)

        var chartExp: PieChart = chartExp
        chartExp.data = dataExp
        chartExp.animateY(800)
        chartExp.setNoDataText("There aren't any expenditure categories with data yet.")
        chartExp.dragDecelerationFrictionCoef = .95f
        chartExp.setDrawEntryLabels(false)

        chartExp.description.isEnabled = false

        val l: Legend = chartExp.legend
        l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
        l.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
        l.orientation = Legend.LegendOrientation.HORIZONTAL
        l.setDrawInside(false)

        dataSetExp.valueLinePart1OffsetPercentage = 80f
        dataSetExp.valueLinePart1Length = 0.4f
        dataSetExp.valueLinePart2Length = 0.8f
        dataSetExp.yValuePosition = PieDataSet.ValuePosition.OUTSIDE_SLICE

        dataExp.setValueFormatter(PercentFormatter())
        dataExp.setValueTextSize(11f)
        dataExp.setValueTextColor(Color.BLACK)

        chartExp.invalidate()

    }





}




