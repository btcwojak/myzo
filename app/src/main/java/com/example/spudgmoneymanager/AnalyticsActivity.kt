package com.example.spudgmoneymanager

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import kotlinx.android.synthetic.main.activity_analytics.*
import kotlinx.android.synthetic.main.activity_analytics.view.*
import kotlinx.android.synthetic.main.month_year_picker.*
import java.util.*
import kotlin.collections.ArrayList


class AnalyticsActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private var entriesInc: ArrayList<PieEntry> = ArrayList()
    private var entriesExp: ArrayList<PieEntry> = ArrayList()
    private var entriesBar: ArrayList<BarEntry> = ArrayList()

    private var categoryTitlesInc: ArrayList<String> = ArrayList()
    private var categoryTitlesExp: ArrayList<String> = ArrayList()

    private var categoryColoursInc: ArrayList<Int> = ArrayList()
    private var categoryColoursExp: ArrayList<Int> = ArrayList()

    private var categoryTotalsInc: ArrayList<Float> = ArrayList()
    private var categoryTotalsExp: ArrayList<Float> = ArrayList()

    private var daysInMonth: ArrayList<Int> = ArrayList()
    private var transactionTotalsPerDay: ArrayList<Float> = ArrayList()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)

        Constants.MONTH_FILTER = Calendar.getInstance()[Calendar.MONTH] + 1
        Constants.YEAR_FILTER = Calendar.getInstance()[Calendar.YEAR]
        Constants.CATEGORY_FILTER_BAR = 1

        makePieData(
            (Constants.MONTH_FILTER),
            Constants.YEAR_FILTER
        )

        makeBarData(Constants.MONTH_FILTER, Constants.YEAR_FILTER, Constants.CATEGORY_FILTER_BAR)

        setMonthHeader(
            Constants.MONTH_FILTER,
            Constants.YEAR_FILTER
        )

        val dbCategories = CategoriesHandler(this, null)
        category_spinner_bar_chart_layout.category_spinner_bar_chart
        val items = dbCategories.getAllCategoryTitles()
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        category_spinner_bar_chart_layout.category_spinner_bar_chart.adapter = categoryAdapter
        category_spinner_bar_chart_layout.category_spinner_bar_chart.onItemSelectedListener = this

        setupPieChartIncome()
        setupPieChartExpenditure()
        setupBarChart()

        select_new_month_header.setOnClickListener {
            
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
            filterDialog.myp_year.value = Constants.YEAR_FILTER
            filterDialog.myp_month.value = Constants.MONTH_FILTER

            filterDialog.myp_month.setOnValueChangedListener { _, _, newVal ->
                Constants.MONTH_FILTER = newVal
            }

            filterDialog.myp_year.setOnValueChangedListener { _, _, newVal ->
                Constants.YEAR_FILTER = newVal
            }

            filterDialog.submit_my.setOnClickListener {
                makePieData(Constants.MONTH_FILTER, Constants.YEAR_FILTER)
                makeBarData(Constants.MONTH_FILTER, Constants.YEAR_FILTER, 1)
                setMonthHeader(Constants.MONTH_FILTER, Constants.YEAR_FILTER)
                category_spinner_bar_chart.setSelection(0)
                setupPieChartIncome()
                setupPieChartExpenditure()
                setupBarChart()
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

    private fun setupBarChart() {

        val dbCategory = CategoriesHandler(this, null)
        var categoryColour = dbCategory.getCategoryColour(Constants.CATEGORY_FILTER_BAR)
        dbCategory.close()

        if (daysInMonth.size > 0) {
            for (i in 0 until daysInMonth.size) {
                entriesBar.add(BarEntry(daysInMonth[i].toFloat(), transactionTotalsPerDay[i]))
            }

            val dataSetBar = BarDataSet(entriesBar, "")
            val dataBar = BarData(dataSetBar)
            dataSetBar.color = categoryColour

            dataBar.setValueFormatter(object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return if (value > 0) {
                        super.getFormattedValue(value)
                    } else {
                        ""
                    }
                }
            })

            val chartBar: BarChart = chartBar
            if (entriesBar.size > 0) {
                chartBar.data = dataBar
            }

            chartBar.animateY(800)
            chartBar.setNoDataText("No data for the month selected.")
            chartBar.setNoDataTextColor(0xff000000.toInt())
            chartBar.setNoDataTextTypeface(ResourcesCompat.getFont(this, R.font.open_sans_light))
            chartBar.xAxis.setDrawGridLines(false)
            chartBar.axisRight.isEnabled = false
            chartBar.xAxis.position = XAxis.XAxisPosition.BOTTOM
            chartBar.legend.isEnabled = false

            chartBar.description.isEnabled = false

            val l: Legend = chartBar.legend
            l.verticalAlignment = Legend.LegendVerticalAlignment.BOTTOM
            l.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            l.orientation = Legend.LegendOrientation.HORIZONTAL
            l.setDrawInside(false)

            chartBar.invalidate()

        } else {
            chartBar.clear()
            chartBar.setNoDataText("No net expenditure categories for the month selected.")
            chartBar.setNoDataTextColor(0xff000000.toInt())
            chartBar.setNoDataTextTypeface(ResourcesCompat.getFont(this, R.font.open_sans_light))
        }


    }

    private fun makePieData(monthFilter: Int, yearFilter: Int) {

        resetPieData()

        val dbHandlerCategory = CategoriesHandler(this, null)
        val dbHandlerTransaction = TransactionsHandler(this, null)

        val categories = dbHandlerCategory.getAllCategories()

        for (category in categories) {
            if (dbHandlerTransaction.getTransactionTotalForCategoryMonthYear(
                    category.id,
                    monthFilter,
                    yearFilter
                ) > 0F
            ) {
                categoryTitlesInc.add(category.title)
            } else if (dbHandlerTransaction.getTransactionTotalForCategoryMonthYear(
                    category.id,
                    monthFilter,
                    yearFilter
                ) < 0F
            ) {
                categoryTitlesExp.add(category.title)
            }
        }

        for (category in categories) {
            val total = dbHandlerTransaction.getTransactionTotalForCategoryMonthYear(
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
            if (dbHandlerTransaction.getTransactionTotalForCategoryMonthYear(
                    category.id,
                    monthFilter,
                    yearFilter
                ) > 0F
            ) {
                categoryColoursInc.add(intColor)
            } else if (dbHandlerTransaction.getTransactionTotalForCategoryMonthYear(
                    category.id,
                    monthFilter,
                    yearFilter
                ) < 0F
            ) {
                categoryColoursExp.add(intColor)
            }
        }

    }

    private fun makeBarData(monthFilter: Int, yearFilter: Int, categoryFilter: Int) {

        resetBarData()

        val dbHandlerTransaction = TransactionsHandler(this, null)

        daysInMonth = if (yearFilter % 4 == 0) {
            when (monthFilter) {
                1, 3, 5, 7, 8, 10, 12 -> Constants.DAYS31
                4, 6, 9, 11 -> Constants.DAYS30
                2 -> Constants.DAYS29
                else -> arrayListOf(0)
            }
        } else {
            when (monthFilter) {
                1, 3, 5, 7, 8, 10, 12 -> Constants.DAYS31
                4, 6, 9, 11 -> Constants.DAYS30
                2 -> Constants.DAYS28
                else -> arrayListOf(0)
            }
        }

        for (day in daysInMonth) {
            var totalForDay: Float = dbHandlerTransaction.getTransactionTotalForCategoryDayMonthYear(
                categoryFilter,
                day,
                monthFilter,
                yearFilter
            )
            transactionTotalsPerDay.add(totalForDay)
        }

    }

    private fun resetPieData() {
        entriesInc = arrayListOf()
        entriesExp = arrayListOf()

        categoryTitlesInc = arrayListOf()
        categoryTitlesExp = arrayListOf()

        categoryColoursInc = arrayListOf()
        categoryColoursExp = arrayListOf()

        categoryTotalsInc = arrayListOf()
        categoryTotalsExp = arrayListOf()
    }

    private fun resetBarData() {
        entriesBar = arrayListOf()
        daysInMonth = arrayListOf()
        transactionTotalsPerDay = arrayListOf()
    }

    private fun resetAllData() {
        entriesInc = arrayListOf()
        entriesExp = arrayListOf()
        entriesBar = arrayListOf()

        categoryTitlesInc = arrayListOf()
        categoryTitlesExp = arrayListOf()

        categoryColoursInc = arrayListOf()
        categoryColoursExp = arrayListOf()

        categoryTotalsInc = arrayListOf()
        categoryTotalsExp = arrayListOf()

        daysInMonth = arrayListOf()
        transactionTotalsPerDay = arrayListOf()
    }

    private fun setMonthHeader(month: Int, year: Int) {
        month_selected_header.text = "${Constants.MONTHS_SHORT_ARRAY[month - 1]} $year"
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        Constants.CATEGORY_FILTER_BAR = position + 1
        makeBarData(Constants.MONTH_FILTER, Constants.YEAR_FILTER, Constants.CATEGORY_FILTER_BAR)
        setupBarChart()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Toast.makeText(this, "Nothing's selected in category dropdown.", Toast.LENGTH_SHORT).show()
    }


}
