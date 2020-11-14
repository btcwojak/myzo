package com.example.spudgmoneymanager

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.PercentFormatter
import kotlinx.android.synthetic.main.activity_analytics.*
import kotlinx.android.synthetic.main.month_year_picker.*
import java.util.*
import kotlin.collections.ArrayList


class AnalyticsActivity : AppCompatActivity() {

    private var entriesInc: ArrayList<PieEntry> = ArrayList()
    private var entriesExp: ArrayList<PieEntry> = ArrayList()
    private var entriesBar: ArrayList<BarEntry> = ArrayList()

    private var categoryTitlesInc: ArrayList<String> = ArrayList()
    private var categoryTitlesExp: ArrayList<String> = ArrayList()

    private var categoryColoursInc: ArrayList<Int> = ArrayList()
    private var categoryColoursExp: ArrayList<Int> = ArrayList()

    private var categoryTotalsInc: ArrayList<Float> = ArrayList()
    private var categoryTotalsExp: ArrayList<Float> = ArrayList()

    private var categoryTransactions: ArrayList<Float> = ArrayList()
    private var categoryDates: ArrayList<Float> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        makeData(
            (Calendar.getInstance()[Calendar.MONTH] + 1),
            Calendar.getInstance()[Calendar.YEAR]
        )

        setMonthHeader(
            Calendar.getInstance()[Calendar.MONTH] + 1,
            Calendar.getInstance()[Calendar.YEAR]
        )
        setupPieChartIncome()
        setupPieChartExpenditure()
        //setupBarChart()

        select_new_month_header.setOnClickListener {
            val calendar = Calendar.getInstance()
            var yearSelected = calendar[Calendar.YEAR]
            var monthSelected = calendar[Calendar.MONTH] + 1

            val filterDialog = Dialog(this, R.style.Theme_Dialog)
            filterDialog.setCancelable(false)
            filterDialog.setContentView(R.layout.month_year_picker)
            filterDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            filterDialog.myp_year.minValue = 1000
            filterDialog.myp_year.maxValue = 2999
            filterDialog.myp_month.minValue = 1
            filterDialog.myp_month.maxValue = 12
            filterDialog.myp_month.displayedValues = Constants.MONTHS_SHORT_ARRAY

            filterDialog.myp_year.wrapSelectorWheel = true
            filterDialog.myp_month.wrapSelectorWheel = true
            filterDialog.myp_year.value = calendar[Calendar.YEAR]
            filterDialog.myp_month.value = calendar[Calendar.MONTH] + 1

            filterDialog.myp_month.setOnValueChangedListener { _, _, newVal ->
                monthSelected = newVal
            }

            filterDialog.myp_year.setOnValueChangedListener { _, _, newVal ->
                yearSelected = newVal
            }

            filterDialog.submit_my.setOnClickListener {
                makeData(monthSelected, yearSelected)
                setMonthHeader(monthSelected, yearSelected)
                setupPieChartIncome()
                setupPieChartExpenditure()
                //setupBarChart()
                filterDialog.dismiss()
            }

            filterDialog.cancel_my.setOnClickListener {
                filterDialog.dismiss()
            }

            filterDialog.show()

        }

        back_to_trans_from_analytics.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }


    private fun setupPieChartIncome() {

        if (categoryTotalsInc.size > 0) {
            for (i in 0 until categoryTotalsInc.size) {
                entriesInc.add(PieEntry(categoryTotalsInc[i], categoryTitlesInc[i]))
            }

            val dataSetInc = PieDataSet(entriesInc, "")
            dataSetInc.colors = categoryColoursInc.toMutableList()
            val dataInc = PieData(dataSetInc)
            dataSetInc.valueFormatter = PercentFormatter()

            val chartInc: PieChart = chartInc
            if (entriesInc.size > 0) {
                chartInc.data = dataInc
            }
            chartInc.animateY(800)
            chartInc.setNoDataText("No net income categories for the month selected.")
            chartInc.setNoDataTextColor(0xff000000.toInt())
            chartInc.setNoDataTextTypeface(ResourcesCompat.getFont(this, R.font.open_sans_light))
            chartInc.setEntryLabelTypeface(ResourcesCompat.getFont(this, R.font.open_sans_light))
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

        } else {
            chartInc.clear()
            chartInc.setNoDataText("No net income categories for the month selected.")
            chartInc.setNoDataTextColor(0xff000000.toInt())
            chartInc.setNoDataTextTypeface(ResourcesCompat.getFont(this, R.font.open_sans_light))
        }

    }

    private fun setupPieChartExpenditure() {

        if (categoryTotalsExp.size > 0) {
            for (i in 0 until categoryTotalsExp.size) {
                entriesExp.add(PieEntry(categoryTotalsExp[i], categoryTitlesExp[i]))
            }

            val dataSetExp = PieDataSet(entriesExp, "")
            dataSetExp.colors = categoryColoursExp.toMutableList()
            val dataExp = PieData(dataSetExp)

            val chartExp: PieChart = chartExp
            if (entriesExp.size > 0) {
                chartExp.data = dataExp
            }
            chartExp.animateY(800)
            chartExp.setNoDataText("No net expenditure categories for the month selected.")
            chartExp.setNoDataTextColor(0xff000000.toInt())
            chartExp.setNoDataTextTypeface(ResourcesCompat.getFont(this, R.font.open_sans_light))
            chartExp.setEntryLabelTypeface(ResourcesCompat.getFont(this, R.font.open_sans_light))
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

        } else {
            chartExp.clear()
            chartExp.setNoDataText("No net expenditure categories for the month selected.")
            chartExp.setNoDataTextColor(0xff000000.toInt())
            chartExp.setNoDataTextTypeface(ResourcesCompat.getFont(this, R.font.open_sans_light))
        }


    }

/*
    private fun setupBarChart() {

        val labels = arrayListOf("Jan", "Feb", "March", "April")

        for (i in 0 until categoryTransactions.size) {
            entriesBar.add(BarEntry(categoryDates[i], categoryTransactions[i]))
        }

        val dataSetBar = BarDataSet(entriesBar, "")

        val dataBar = BarData(dataSetBar)

        val chartBar: BarChart = chartBar
        chartBar.data = dataBar

        chartBar.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chartBar.xAxis.position = XAxis.XAxisPosition.BOTTOM

        val xAxis: XAxis = chartBar.xAxis
        xAxis.granularity = 1f
        xAxis.labelCount = 7

        val leftAxis: YAxis = chartBar.axisLeft
        leftAxis.setLabelCount(8, false)

        val rightAxis: YAxis = chartBar.axisRight
        rightAxis.isEnabled = false

    }
*/

    private fun makeData(monthFilter: Int, yearFilter: Int) {

        resetData()

        val dbHandlerCategory = CategoriesHandler(this, null)
        val dbHandlerTransaction = TransactionsHandler(this, null)

        val categories = dbHandlerCategory.getAllCategories()

        for (category in categories) {
            if (dbHandlerTransaction.getTransactionTotalForCategory(
                    category.id,
                    monthFilter,
                    yearFilter
                ) > 0F
            ) {
                categoryTitlesInc.add(category.title)
            } else if (dbHandlerTransaction.getTransactionTotalForCategory(
                    category.id,
                    monthFilter,
                    yearFilter
                ) < 0F
            ) {
                categoryTitlesExp.add(category.title)
            }
        }

        for (category in categories) {
            val total = dbHandlerTransaction.getTransactionTotalForCategory(
                category.id,
                monthFilter,
                yearFilter
            )
            if (total > 0F) {
                categoryTotalsInc.add(String.format("%.2f", total).toFloat())
            } else if (total < 0F) {
                categoryTotalsExp.add(String.format("%.2f", -total).toFloat())
            }
        }

        for (category in categories) {
            val intColor = category.colour.toInt()
            //categoryColours.add(java.lang.String.format("#%06X", 0xFFFFFF and intColor))
            if (dbHandlerTransaction.getTransactionTotalForCategory(
                    category.id,
                    monthFilter,
                    yearFilter
                ) > 0F
            ) {
                categoryColoursInc.add(intColor)
            } else if (dbHandlerTransaction.getTransactionTotalForCategory(
                    category.id,
                    monthFilter,
                    yearFilter
                ) < 0F
            ) {
                categoryColoursExp.add(intColor)
            }
        }

    }

    private fun resetData() {
        entriesInc = arrayListOf()
        entriesExp = arrayListOf()
        entriesBar = arrayListOf()

        categoryTitlesInc = arrayListOf()
        categoryTitlesExp = arrayListOf()

        categoryColoursInc = arrayListOf()
        categoryColoursExp = arrayListOf()

        categoryTotalsInc = arrayListOf()
        categoryTotalsExp = arrayListOf()

        categoryTransactions = arrayListOf()
        categoryDates = arrayListOf()

    }

    private fun setMonthHeader(month: Int, year: Int) {
        month_selected_header.text = "${Constants.MONTHS_SHORT_ARRAY[month-1]} $year"
    }


}




