package com.spudg.spudgmoneymanager

import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.PopupMenu
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.opencsv.CSVReader
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.day_month_year_picker.*
import kotlinx.android.synthetic.main.dialog_add_transaction.*
import kotlinx.android.synthetic.main.dialog_add_transaction.etAmountLayout
import kotlinx.android.synthetic.main.dialog_add_transaction.inc_exp_radio_group
import kotlinx.android.synthetic.main.dialog_add_transaction.tvCancel
import kotlinx.android.synthetic.main.dialog_add_transaction.view.*
import kotlinx.android.synthetic.main.dialog_add_transaction.view.etAmount
import kotlinx.android.synthetic.main.dialog_add_transaction.view.expenditure_radio
import kotlinx.android.synthetic.main.dialog_add_transaction.view.income_radio
import kotlinx.android.synthetic.main.dialog_backup.*
import kotlinx.android.synthetic.main.dialog_delete_transaction.*
import kotlinx.android.synthetic.main.dialog_export_confirm.*
import kotlinx.android.synthetic.main.dialog_import_confirm.*
import kotlinx.android.synthetic.main.dialog_update_transaction.*
import kotlinx.android.synthetic.main.dialog_update_transaction.view.*
import java.io.File
import java.io.FileReader
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private var isIncome = true
    private var selectedCategory = ""

    private val STORAGE_REQUEST_CODE_IMPORT = 1
    private val STORAGE_REQUEST_CODE_EXPORT = 2
    private lateinit var storagePermission: Array<String>

    lateinit var manager: ReviewManager
    var reviewInfo: ReviewInfo? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        storagePermission = arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)

        checkRecurringTransactions()

        setUpTransactionList()

        initReviews()

        add_transaction.setOnClickListener {
            addTransaction()
        }

        accounts_btn.setOnClickListener {
            val intent = Intent(this, AccountsActivity::class.java)
            startActivity(intent)
        }

        categories_btn.setOnClickListener {
            val intent = Intent(this, CategoriesActivity::class.java)
            startActivity(intent)
        }

        more_btn.setOnClickListener {
            val popupMenu: PopupMenu = PopupMenu(this, more_btn)
            popupMenu.menuInflater.inflate(R.menu.menu_popup, popupMenu.menu)
            popupMenu.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.action_analysis -> {
                        val intent = Intent(this, AnalyticsActivity::class.java)
                        startActivity(intent)
                    }
                    R.id.action_recurring -> {
                        val intent = Intent(this, RecurringsActivity::class.java)
                        startActivity(intent)
                    }
                    R.id.action_backup -> {
                        backupDialog()
                    }
                    R.id.action_about -> {
                        val intent = Intent(this, AboutActivity::class.java)
                        startActivity(intent)
                    }
                }
                true
            })

            popupMenu.show()
        }

        setBalanceText()
        setAccountName()

        if (noAccounts()) {
            val dbHandler = AccountsHandler(this, null)
            dbHandler.addAccount(AccountModel(0, "Main Account"))
            Constants.CURRENT_ACCOUNT = dbHandler.getAllAccounts().first().id
            setAccountName()
            setBalanceText()
            dbHandler.close()
        }

        checkDefaultCategories()

    }

    private fun checkRecurringTransactions() {
        val dbRec = RecurringsHandler(this, null)
        val dbTrans = TransactionsHandler(this, null)

        val currentDateMillis = Calendar.getInstance().timeInMillis

        val noItems = dbRec.filterRecurrings().size

        repeat(noItems) { index ->
            while (dbRec.filterRecurrings()[index].nextDateMillis.toLong() < currentDateMillis) {

                val rec = dbRec.filterRecurrings()[index]

                if (rec.frequency == "Weekly") {
                    val strDate = "${rec.nextDay}-${rec.nextMonth}-${rec.nextYear}"
                    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    val nextDateMillis = sdf.parse(strDate)?.time!!.plus(604800000)
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = nextDateMillis

                    val newNextMonth = calendar.get(Calendar.MONTH) + 1
                    val nextOGDay = rec.nextOGDay
                    val newNextDay = calendar.get(Calendar.DAY_OF_MONTH)
                    val newNextYear = calendar.get(Calendar.YEAR)

                    dbRec.updateRecurring(
                        RecurringModel(
                            rec.id,
                            rec.note,
                            rec.category,
                            rec.amount,
                            rec.account,
                            newNextMonth,
                            nextOGDay,
                            newNextDay,
                            newNextYear,
                            "",
                            rec.frequency
                        )
                    )
                    dbTrans.addTransaction(
                        TransactionModel(
                            0,
                            rec.note,
                            rec.category,
                            rec.amount,
                            rec.account,
                            rec.nextMonth,
                            rec.nextDay,
                            rec.nextYear,
                            ""
                        )
                    )
                }

                if (rec.frequency == "Bi-weekly") {
                    val strDate = "${rec.nextDay}-${rec.nextMonth}-${rec.nextYear}"
                    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    val nextDateMillis = sdf.parse(strDate)?.time!!.plus(2 * 604800000)
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = nextDateMillis

                    val newNextMonth = calendar.get(Calendar.MONTH) + 1
                    val nextOGDay = rec.nextOGDay
                    val newNextDay = calendar.get(Calendar.DAY_OF_MONTH)
                    val newNextYear = calendar.get(Calendar.YEAR)

                    dbRec.updateRecurring(
                        RecurringModel(
                            rec.id,
                            rec.note,
                            rec.category,
                            rec.amount,
                            rec.account,
                            newNextMonth,
                            nextOGDay,
                            newNextDay,
                            newNextYear,
                            "",
                            rec.frequency
                        )
                    )
                    dbTrans.addTransaction(
                        TransactionModel(
                            0,
                            rec.note,
                            rec.category,
                            rec.amount,
                            rec.account,
                            rec.nextMonth,
                            rec.nextDay,
                            rec.nextYear,
                            ""
                        )
                    )
                }

                if (rec.frequency == "Tri-weekly") {
                    val strDate = "${rec.nextDay}-${rec.nextMonth}-${rec.nextYear}"
                    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    val nextDateMillis = sdf.parse(strDate)?.time!!.plus(3 * 604800000)
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = nextDateMillis

                    val newNextMonth = calendar.get(Calendar.MONTH) + 1
                    val nextOGDay = rec.nextOGDay
                    val newNextDay = calendar.get(Calendar.DAY_OF_MONTH)
                    val newNextYear = calendar.get(Calendar.YEAR)

                    dbRec.updateRecurring(
                        RecurringModel(
                            rec.id,
                            rec.note,
                            rec.category,
                            rec.amount,
                            rec.account,
                            newNextMonth,
                            nextOGDay,
                            newNextDay,
                            newNextYear,
                            "",
                            rec.frequency
                        )
                    )
                    dbTrans.addTransaction(
                        TransactionModel(
                            0,
                            rec.note,
                            rec.category,
                            rec.amount,
                            rec.account,
                            rec.nextMonth,
                            rec.nextDay,
                            rec.nextYear,
                            ""
                        )
                    )
                }

                if (rec.frequency == "Four-weekly") {
                    val strDate = "${rec.nextDay}-${rec.nextMonth}-${rec.nextYear}"
                    val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    val nextDateMillis =
                        sdf.parse(strDate)?.time!!.plus(2 * 604800000).plus(2 * 604800000)
                    val calendar = Calendar.getInstance()
                    calendar.timeInMillis = nextDateMillis

                    val newNextMonth = calendar.get(Calendar.MONTH) + 1
                    val nextOGDay = rec.nextOGDay
                    val newNextDay = calendar.get(Calendar.DAY_OF_MONTH)
                    val newNextYear = calendar.get(Calendar.YEAR)

                    dbRec.updateRecurring(
                        RecurringModel(
                            rec.id,
                            rec.note,
                            rec.category,
                            rec.amount,
                            rec.account,
                            newNextMonth,
                            nextOGDay,
                            newNextDay,
                            newNextYear,
                            "",
                            rec.frequency
                        )
                    )
                    dbTrans.addTransaction(
                        TransactionModel(
                            0,
                            rec.note,
                            rec.category,
                            rec.amount,
                            rec.account,
                            rec.nextMonth,
                            rec.nextDay,
                            rec.nextYear,
                            ""
                        )
                    )
                }

                if (rec.frequency == "Monthly") {
                    var newNextMonth = rec.nextMonth + 1
                    val nextOGDay = rec.nextOGDay
                    var newNextDay = rec.nextDay
                    var newNextYear = rec.nextYear

                    if (newNextMonth > 12) {
                        newNextMonth -= 12
                        newNextYear += 1
                    }

                    if (nextOGDay == 29 && newNextMonth != 2) {
                        newNextDay = 29
                    } else if (nextOGDay == 29 && newNextMonth == 2) {
                        newNextDay = if (newNextYear % 4 == 0) {
                            29
                        } else {
                            28
                        }
                    }

                    if (nextOGDay == 30 && newNextMonth != 2) {
                        newNextDay = 30
                    } else if (nextOGDay == 30 && newNextMonth == 2) {
                        newNextDay = if (newNextYear % 4 == 0) {
                            29
                        } else {
                            28
                        }
                    }

                    if (nextOGDay == 31 && (newNextMonth == 1 || newNextMonth == 3 || newNextMonth == 5 || newNextMonth == 7 || newNextMonth == 8 || newNextMonth == 10 || newNextMonth == 12)) {
                        newNextDay = 31
                    } else if (nextOGDay == 31 && (newNextMonth == 4 || newNextMonth == 6 || newNextMonth == 9 || newNextMonth == 11)) {
                        newNextDay = 30
                    } else if (nextOGDay == 31 && newNextMonth == 2) {
                        newNextDay = if (newNextYear % 4 == 0) {
                            29
                        } else {
                            28
                        }
                    }

                    dbRec.updateRecurring(
                        RecurringModel(
                            rec.id,
                            rec.note,
                            rec.category,
                            rec.amount,
                            rec.account,
                            newNextMonth,
                            nextOGDay,
                            newNextDay,
                            newNextYear,
                            "",
                            rec.frequency
                        )
                    )
                    dbTrans.addTransaction(
                        TransactionModel(
                            0,
                            rec.note,
                            rec.category,
                            rec.amount,
                            rec.account,
                            rec.nextMonth,
                            rec.nextDay,
                            rec.nextYear,
                            ""
                        )
                    )

                }

                if (rec.frequency == "Bi-monthly") {
                    var newNextMonth = rec.nextMonth + 2
                    val nextOGDay = rec.nextOGDay
                    var newNextDay = rec.nextDay
                    var newNextYear = rec.nextYear

                    if (newNextMonth > 12) {
                        newNextMonth -= 12
                        newNextYear += 1
                    }

                    if (nextOGDay == 29 && newNextMonth != 2) {
                        newNextDay = 29
                    } else if (nextOGDay == 29 && newNextMonth == 2) {
                        newNextDay = if (newNextYear % 4 == 0) {
                            29
                        } else {
                            28
                        }
                    }

                    if (nextOGDay == 30 && newNextMonth != 2) {
                        newNextDay = 30
                    } else if (nextOGDay == 30 && newNextMonth == 2) {
                        newNextDay = if (newNextYear % 4 == 0) {
                            29
                        } else {
                            28
                        }
                    }

                    if (nextOGDay == 31 && (newNextMonth == 1 || newNextMonth == 3 || newNextMonth == 5 || newNextMonth == 7 || newNextMonth == 8 || newNextMonth == 10 || newNextMonth == 12)) {
                        newNextDay = 31
                    } else if (nextOGDay == 31 && (newNextMonth == 4 || newNextMonth == 6 || newNextMonth == 9 || newNextMonth == 11)) {
                        newNextDay = 30
                    } else if (nextOGDay == 31 && newNextMonth == 2) {
                        newNextDay = if (newNextYear % 4 == 0) {
                            29
                        } else {
                            28
                        }
                    }

                    dbRec.updateRecurring(
                        RecurringModel(
                            rec.id,
                            rec.note,
                            rec.category,
                            rec.amount,
                            rec.account,
                            newNextMonth,
                            nextOGDay,
                            newNextDay,
                            newNextYear,
                            "",
                            rec.frequency
                        )
                    )
                    dbTrans.addTransaction(
                        TransactionModel(
                            0,
                            rec.note,
                            rec.category,
                            rec.amount,
                            rec.account,
                            rec.nextMonth,
                            rec.nextDay,
                            rec.nextYear,
                            ""
                        )
                    )
                }

                if (rec.frequency == "Quarterly") {
                    var newNextMonth = rec.nextMonth + 3
                    val nextOGDay = rec.nextOGDay
                    var newNextDay = rec.nextDay
                    var newNextYear = rec.nextYear

                    if (newNextMonth > 12) {
                        newNextMonth -= 12
                        newNextYear += 1
                    }

                    if (nextOGDay == 29 && newNextMonth != 2) {
                        newNextDay = 29
                    } else if (nextOGDay == 29 && newNextMonth == 2) {
                        newNextDay = if (newNextYear % 4 == 0) {
                            29
                        } else {
                            28
                        }
                    }

                    if (nextOGDay == 30 && newNextMonth != 2) {
                        newNextDay = 30
                    } else if (nextOGDay == 30 && newNextMonth == 2) {
                        newNextDay = if (newNextYear % 4 == 0) {
                            29
                        } else {
                            28
                        }
                    }

                    if (nextOGDay == 31 && (newNextMonth == 1 || newNextMonth == 3 || newNextMonth == 5 || newNextMonth == 7 || newNextMonth == 8 || newNextMonth == 10 || newNextMonth == 12)) {
                        newNextDay = 31
                    } else if (nextOGDay == 31 && (newNextMonth == 4 || newNextMonth == 6 || newNextMonth == 9 || newNextMonth == 11)) {
                        newNextDay = 30
                    } else if (nextOGDay == 31 && newNextMonth == 2) {
                        newNextDay = if (newNextYear % 4 == 0) {
                            29
                        } else {
                            28
                        }
                    }

                    dbRec.updateRecurring(
                        RecurringModel(
                            rec.id,
                            rec.note,
                            rec.category,
                            rec.amount,
                            rec.account,
                            newNextMonth,
                            nextOGDay,
                            newNextDay,
                            newNextYear,
                            "",
                            rec.frequency
                        )
                    )
                    dbTrans.addTransaction(
                        TransactionModel(
                            0,
                            rec.note,
                            rec.category,
                            rec.amount,
                            rec.account,
                            rec.nextMonth,
                            rec.nextDay,
                            rec.nextYear,
                            ""
                        )
                    )
                }

                if (rec.frequency == "Yearly") {
                    val newNextMonth = rec.nextMonth
                    val nextOGDay = rec.nextOGDay
                    var newNextDay = rec.nextDay
                    val newNextYear = rec.nextYear + 1

                    if (newNextYear % 4 != 0 && newNextMonth == 2 && newNextDay == 29) {
                        newNextDay = 28
                    }

                    if (nextOGDay == 29 && newNextDay == 28 && newNextYear % 4 == 0) {
                        newNextDay = 29
                    }

                    dbRec.updateRecurring(
                        RecurringModel(
                            rec.id,
                            rec.note,
                            rec.category,
                            rec.amount,
                            rec.account,
                            newNextMonth,
                            nextOGDay,
                            newNextDay,
                            newNextYear,
                            "",
                            rec.frequency
                        )
                    )
                    dbTrans.addTransaction(
                        TransactionModel(
                            0,
                            rec.note,
                            rec.category,
                            rec.amount,
                            rec.account,
                            rec.nextMonth,
                            rec.nextDay,
                            rec.nextYear,
                            ""
                        )
                    )
                }

            }

        }

    }

    private fun setUpTransactionList() {
        if (getTransactionsList().size > 0) {
            rvTransactions.visibility = View.VISIBLE
            tvNoTransactions.visibility = View.GONE
            val manager = LinearLayoutManager(this)
            rvTransactions.layoutManager = manager
            val transactionAdapter = TransactionAdapter(this, getTransactionsList())
            rvTransactions.adapter = transactionAdapter
        } else {
            rvTransactions.visibility = View.GONE
            tvNoTransactions.visibility = View.VISIBLE
        }

        if (Constants.TRANSACTIONS_ADDED_SESSION == 8) {
            askForReview()
        }

    }

    private fun getTransactionsList(): ArrayList<TransactionModel> {
        val dbHandler = TransactionsHandler(this, null)
        val result = dbHandler.filterTransactions(Constants.CURRENT_ACCOUNT, -1)
        dbHandler.close()
        return result
    }

    private fun addTransaction() {
        val addDialog = Dialog(this, R.style.Theme_Dialog)
        addDialog.setCancelable(false)
        addDialog.setContentView(R.layout.dialog_add_transaction)
        addDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        var dayPicked = Calendar.getInstance()[Calendar.DAY_OF_MONTH]
        var monthPicked = Calendar.getInstance()[Calendar.MONTH] + 1
        var yearPicked = Calendar.getInstance()[Calendar.YEAR]

        addDialog.change_date_add.text =
            "$dayPicked ${Constants.getShortMonth(monthPicked)} $yearPicked"

        addDialog.change_date_add.setOnClickListener {
            val changeDateDialog = Dialog(this, R.style.Theme_Dialog)
            changeDateDialog.setCancelable(false)
            changeDateDialog.setContentView(R.layout.day_month_year_picker)
            changeDateDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            if (Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 4 || Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 6 || Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 9 || Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 11) {
                changeDateDialog.dmyp_day.maxValue = 30
                changeDateDialog.dmyp_day.minValue = 1
            } else if (Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 2 && (Calendar.getInstance()[Calendar.DAY_OF_MONTH] % 4 == 0)) {
                changeDateDialog.dmyp_day.maxValue = 29
                changeDateDialog.dmyp_day.minValue = 1
            } else if (Calendar.getInstance()[Calendar.DAY_OF_MONTH] == 2 && (Calendar.getInstance()[Calendar.DAY_OF_MONTH] % 4 != 0)) {
                changeDateDialog.dmyp_day.maxValue = 28
                changeDateDialog.dmyp_day.minValue = 1
            } else {
                changeDateDialog.dmyp_day.maxValue = 31
                changeDateDialog.dmyp_day.minValue = 1
            }

            changeDateDialog.dmyp_month.maxValue = 12
            changeDateDialog.dmyp_month.minValue = 1
            changeDateDialog.dmyp_year.maxValue = 2999
            changeDateDialog.dmyp_year.minValue = 1000

            changeDateDialog.dmyp_day.value = Calendar.getInstance()[Calendar.DAY_OF_MONTH]
            changeDateDialog.dmyp_month.value = Calendar.getInstance()[Calendar.MONTH] + 1
            changeDateDialog.dmyp_year.value = Calendar.getInstance()[Calendar.YEAR]
            dayPicked = Calendar.getInstance()[Calendar.DAY_OF_MONTH]
            monthPicked = Calendar.getInstance()[Calendar.MONTH] + 1
            yearPicked = Calendar.getInstance()[Calendar.YEAR]

            changeDateDialog.dmyp_month.displayedValues = Constants.MONTHS_SHORT_ARRAY

            changeDateDialog.dmyp_day.setOnValueChangedListener { _, _, newVal ->
                dayPicked = newVal
            }

            changeDateDialog.dmyp_month.setOnValueChangedListener { _, _, newVal ->
                if (newVal == 4 || newVal == 6 || newVal == 9 || newVal == 11) {
                    changeDateDialog.dmyp_day.maxValue = 30
                    changeDateDialog.dmyp_day.minValue = 1
                } else if (newVal == 2 && (changeDateDialog.dmyp_year.value % 4 == 0)) {
                    changeDateDialog.dmyp_day.maxValue = 29
                    changeDateDialog.dmyp_day.minValue = 1
                } else if (newVal == 2 && (changeDateDialog.dmyp_year.value % 4 != 0)) {
                    changeDateDialog.dmyp_day.maxValue = 28
                    changeDateDialog.dmyp_day.minValue = 1
                } else {
                    changeDateDialog.dmyp_day.maxValue = 31
                    changeDateDialog.dmyp_day.minValue = 1
                }
                monthPicked = newVal
            }

            changeDateDialog.dmyp_year.setOnValueChangedListener { _, _, newVal ->
                if (newVal % 4 == 0 && changeDateDialog.dmyp_month.value == 2) {
                    changeDateDialog.dmyp_day.maxValue = 29
                    changeDateDialog.dmyp_day.minValue = 1
                } else if (newVal % 4 != 0 && changeDateDialog.dmyp_month.value == 2) {
                    changeDateDialog.dmyp_day.maxValue = 28
                    changeDateDialog.dmyp_day.minValue = 1
                }
                yearPicked = newVal
            }

            changeDateDialog.submit_dmy.setOnClickListener {
                addDialog.change_date_add.text =
                    "$dayPicked ${Constants.getShortMonth(monthPicked)} $yearPicked"
                changeDateDialog.dismiss()
            }

            changeDateDialog.dmyp_day.wrapSelectorWheel = true
            changeDateDialog.dmyp_month.wrapSelectorWheel = true
            changeDateDialog.dmyp_year.wrapSelectorWheel = true

            changeDateDialog.cancel_dmy.setOnClickListener {
                dayPicked = Calendar.getInstance()[Calendar.DAY_OF_MONTH]
                monthPicked = Calendar.getInstance()[Calendar.MONTH] + 1
                yearPicked = Calendar.getInstance()[Calendar.YEAR]
                addDialog.change_date_add.text =
                    "$dayPicked ${Constants.getShortMonth(monthPicked)} $yearPicked"
                changeDateDialog.dismiss()
            }

            changeDateDialog.show()

        }


        addDialog.etAmountLayout.etAmount.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
            override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
            override fun afterTextChanged(arg0: Editable) {
                val str = addDialog.etAmountLayout.etAmount.text.toString()
                if (str.isEmpty()) return
                val str2: String = currencyInputFilter(str, 6, 2)
                if (str2 != str) {
                    addDialog.etAmountLayout.etAmount.setText(str2)
                    addDialog.etAmountLayout.etAmount.setSelection(str2.length)
                }
            }
        })

        val categoryListHandler = CategoriesHandler(this, null)
        val items = categoryListHandler.getAllCategoryTitles()
        categoryListHandler.close()
        val categoryAdapter = ArrayAdapter(this, R.layout.custom_spinner, items)
        addDialog.category_spinner_add_trans.adapter = categoryAdapter
        addDialog.category_spinner_add_trans.onItemSelectedListener = this

        addDialog.inc_exp_radio_group.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.income_radio -> {
                    isIncome = true
                }
                R.id.expenditure_radio -> {
                    isIncome = false
                }
                else -> {
                    Toast.makeText(
                        this,
                        "An error has occurred. Please try restarting the app.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        addDialog.tvAdd.setOnClickListener {

            Constants.TRANSACTIONS_ADDED_SESSION += 1

            val dbHandlerTrans = TransactionsHandler(this, null)
            val dbHandlerCat = CategoriesHandler(this, null)

            val category = dbHandlerCat.getCategoryId(selectedCategory)
            val amount = addDialog.etAmountLayout.etAmount.text.toString()
            val note = addDialog.etNoteLayoutAdd.etNoteAdd.text.toString()
            val account = Constants.CURRENT_ACCOUNT
            val month = monthPicked
            val day = dayPicked
            val year = yearPicked

            if (selectedCategory.isNotEmpty() && amount.isNotEmpty() && note.isNotEmpty()) {
                if (isIncome) {
                    dbHandlerTrans.addTransaction(
                        TransactionModel(
                            0,
                            note,
                            category,
                            amount,
                            account,
                            month,
                            day,
                            year,
                            ""
                        )
                    )
                } else if (!isIncome) {
                    dbHandlerTrans.addTransaction(
                        TransactionModel(
                            0,
                            note,
                            category,
                            (amount.toDouble() * -1).toString(),
                            account,
                            month,
                            day,
                            year,
                            ""
                        )
                    )
                }

                Toast.makeText(this, "Transaction added.", Toast.LENGTH_LONG).show()
                setBalanceText()
                setUpTransactionList()
                addDialog.dismiss()

            } else {
                Toast.makeText(this, "Category, amount or note can't be blank.", Toast.LENGTH_LONG)
                    .show()
            }

            dbHandlerTrans.close()
            dbHandlerCat.close()

        }

        addDialog.tvCancel.setOnClickListener {
            addDialog.dismiss()
        }

        addDialog.show()
    }

    fun updateTransaction(transaction: TransactionModel) {
        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)
        updateDialog.setContentView(R.layout.dialog_update_transaction)
        updateDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        var dayPicked = transaction.day
        var monthPicked = transaction.month
        var yearPicked = transaction.year

        updateDialog.change_date_update.text =
            "$dayPicked ${Constants.getShortMonth(monthPicked)} $yearPicked"

        updateDialog.change_date_update.setOnClickListener {
            val changeDateDialog = Dialog(this, R.style.Theme_Dialog)
            changeDateDialog.setCancelable(false)
            changeDateDialog.setContentView(R.layout.day_month_year_picker)
            changeDateDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            if (transaction.month == 4 || transaction.month == 6 || transaction.month == 9 || transaction.month == 11) {
                changeDateDialog.dmyp_day.maxValue = 30
                changeDateDialog.dmyp_day.minValue = 1
            } else if (transaction.month == 2 && transaction.month % 4 == 0) {
                changeDateDialog.dmyp_day.maxValue = 29
                changeDateDialog.dmyp_day.minValue = 1
            } else if (transaction.month == 2 && transaction.month % 4 != 0) {
                changeDateDialog.dmyp_day.maxValue = 28
                changeDateDialog.dmyp_day.minValue = 1
            } else {
                changeDateDialog.dmyp_day.maxValue = 31
                changeDateDialog.dmyp_day.minValue = 1
            }

            changeDateDialog.dmyp_month.maxValue = 12
            changeDateDialog.dmyp_month.minValue = 1
            changeDateDialog.dmyp_year.maxValue = 2999
            changeDateDialog.dmyp_year.minValue = 1000

            changeDateDialog.dmyp_day.value = transaction.day
            changeDateDialog.dmyp_month.value = transaction.month
            changeDateDialog.dmyp_year.value = transaction.year
            dayPicked = transaction.day
            monthPicked = transaction.month
            yearPicked = transaction.year

            changeDateDialog.dmyp_month.displayedValues = Constants.MONTHS_SHORT_ARRAY

            changeDateDialog.dmyp_day.setOnValueChangedListener { _, _, newVal ->
                dayPicked = newVal
            }

            changeDateDialog.dmyp_month.setOnValueChangedListener { _, _, newVal ->
                if (newVal == 4 || newVal == 6 || newVal == 9 || newVal == 11) {
                    changeDateDialog.dmyp_day.maxValue = 30
                    changeDateDialog.dmyp_day.minValue = 1
                } else if (newVal == 2 && (changeDateDialog.dmyp_year.value % 4 == 0)) {
                    changeDateDialog.dmyp_day.maxValue = 29
                    changeDateDialog.dmyp_day.minValue = 1
                } else if (newVal == 2 && (changeDateDialog.dmyp_year.value % 4 != 0)) {
                    changeDateDialog.dmyp_day.maxValue = 28
                    changeDateDialog.dmyp_day.minValue = 1
                } else {
                    changeDateDialog.dmyp_day.maxValue = 31
                    changeDateDialog.dmyp_day.minValue = 1
                }
                monthPicked = newVal
            }

            changeDateDialog.dmyp_year.setOnValueChangedListener { _, _, newVal ->
                if (newVal % 4 == 0 && changeDateDialog.dmyp_month.value == 2) {
                    changeDateDialog.dmyp_day.maxValue = 29
                    changeDateDialog.dmyp_day.minValue = 1
                } else if (newVal % 4 != 0 && changeDateDialog.dmyp_month.value == 2) {
                    changeDateDialog.dmyp_day.maxValue = 28
                    changeDateDialog.dmyp_day.minValue = 1
                }
                yearPicked = newVal
            }

            changeDateDialog.submit_dmy.setOnClickListener {
                updateDialog.change_date_update.text =
                    "$dayPicked ${Constants.getShortMonth(monthPicked)} $yearPicked"
                changeDateDialog.dismiss()
            }

            changeDateDialog.dmyp_day.wrapSelectorWheel = true
            changeDateDialog.dmyp_month.wrapSelectorWheel = true
            changeDateDialog.dmyp_year.wrapSelectorWheel = true

            changeDateDialog.cancel_dmy.setOnClickListener {
                dayPicked = transaction.day
                monthPicked = transaction.month
                yearPicked = transaction.year
                updateDialog.change_date_update.text =
                    "$dayPicked ${Constants.getShortMonth(monthPicked)} $yearPicked"
                changeDateDialog.dismiss()
            }

            changeDateDialog.show()

        }

        updateDialog.etAmountLayout.etAmount.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
            override fun beforeTextChanged(arg0: CharSequence, arg1: Int, arg2: Int, arg3: Int) {}
            override fun afterTextChanged(arg0: Editable) {
                val str = updateDialog.etAmountLayout.etAmount.text.toString()
                if (str.isEmpty()) return
                val str2: String = currencyInputFilter(str, 6, 2)
                if (str2 != str) {
                    updateDialog.etAmountLayout.etAmount.setText(str2)
                    updateDialog.etAmountLayout.etAmount.setSelection(str2.length)
                }
            }
        })

        val categoryListHandler = CategoriesHandler(this, null)
        val items = categoryListHandler.getAllCategoryTitles()
        categoryListHandler.close()
        val categoryAdapter = ArrayAdapter(this, R.layout.custom_spinner, items)
        updateDialog.category_spinner_update_trans.adapter = categoryAdapter
        updateDialog.category_spinner_update_trans.onItemSelectedListener = this
        updateDialog.category_spinner_update_trans.setSelection(transaction.category - 1)

        updateDialog.etNoteLayoutUpdate.etNoteUpdate.setText(transaction.note)

        if (transaction.amount.toFloat() >= 0) {
            updateDialog.inc_exp_radio_group.income_radio.isChecked = true
            updateDialog.inc_exp_radio_group.expenditure_radio.isChecked = false
            updateDialog.etAmountLayout.etAmount.setText(transaction.amount)
        } else {
            updateDialog.inc_exp_radio_group.expenditure_radio.isChecked = true
            updateDialog.inc_exp_radio_group.income_radio.isChecked = false
            updateDialog.etAmountLayout.etAmount.setText((transaction.amount.toFloat() * -1).toString())
        }

        updateDialog.inc_exp_radio_group.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.income_radio -> {
                    isIncome = true
                }
                R.id.expenditure_radio -> {
                    isIncome = false
                }
                else -> {
                    Toast.makeText(
                        this,
                        "An error has occurred. Please try restarting the app.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        updateDialog.tvUpdate.setOnClickListener {
            val dbHandler = TransactionsHandler(this, null)
            val dbHandlerCat = CategoriesHandler(this, null)

            val category = dbHandlerCat.getCategoryId(selectedCategory)
            val amount = updateDialog.etAmountLayout.etAmount.text.toString()
            val account = Constants.CURRENT_ACCOUNT
            val note = updateDialog.etNoteLayoutUpdate.etNoteUpdate.text.toString()
            val month = monthPicked
            val day = dayPicked
            val year = yearPicked

            if (selectedCategory.isNotEmpty() && amount.isNotEmpty() && note.isNotEmpty()) {
                if (isIncome) {
                    dbHandler.updateTransaction(
                        TransactionModel(
                            transaction.id,
                            note,
                            category,
                            amount,
                            account,
                            month,
                            day,
                            year,
                            ""
                        )
                    )
                } else if (!isIncome) {
                    dbHandler.updateTransaction(
                        TransactionModel(
                            transaction.id,
                            note,
                            category,
                            (amount.toDouble() * -1).toString(),
                            account,
                            month,
                            day,
                            year,
                            ""
                        )
                    )
                }

                Toast.makeText(this, "Transaction updated.", Toast.LENGTH_LONG).show()
                setBalanceText()
                setUpTransactionList()
                updateDialog.dismiss()

            } else {
                Toast.makeText(this, "Category, amount or note can't be blank.", Toast.LENGTH_LONG)
                    .show()
            }

            dbHandler.close()
            dbHandlerCat.close()

        }

        updateDialog.tvCancel.setOnClickListener {
            updateDialog.dismiss()
        }

        updateDialog.show()
    }

    fun deleteTransaction(transaction: TransactionModel) {
        val deleteDialog = Dialog(this, R.style.Theme_Dialog)
        deleteDialog.setCancelable(false)
        deleteDialog.setContentView(R.layout.dialog_delete_transaction)
        deleteDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        deleteDialog.tvDelete.setOnClickListener {
            val dbHandler = TransactionsHandler(this, null)
            dbHandler.deleteTransaction(TransactionModel(transaction.id, "", 0, "", 0, 0, 0, 0, ""))

            Toast.makeText(this, "Transaction deleted.", Toast.LENGTH_LONG).show()
            setBalanceText()
            setUpTransactionList()
            dbHandler.close()
            deleteDialog.dismiss()
        }

        deleteDialog.tvCancel.setOnClickListener {
            deleteDialog.dismiss()
        }

        deleteDialog.show()

    }

    private fun setBalanceText() {
        val dbHandler = TransactionsHandler(this, null)
        val balance = dbHandler.getBalanceForAccount(Constants.CURRENT_ACCOUNT)
        if (balance.isEmpty()) {
            balance_heading.text = "Error"
        } else {
            balance_heading.text = "Balance: $balance"
        }

        dbHandler.close()
    }

    private fun setAccountName() {
        val dbHandler = AccountsHandler(this, null)
        val name = dbHandler.getAccountName(Constants.CURRENT_ACCOUNT)
        account_heading.text = name
        dbHandler.close()
    }

    private fun noAccounts(): Boolean {
        val dbHandler = AccountsHandler(this, null)
        val result = dbHandler.getAllAccounts().size < 1
        dbHandler.close()
        return result
    }

    fun getTransactionCategoryColour(categoryId: Int): Int {
        val dbHandler = CategoriesHandler(this, null)
        val result = dbHandler.getCategoryColour(categoryId)
        dbHandler.close()
        return result
    }

    private fun checkDefaultCategories() {
        val dbHandler = CategoriesHandler(this, null)
        val allCategories = dbHandler.getAllCategoryTitles()

        if (!allCategories.contains("Entertainment")) {
            dbHandler.addCategory(CategoryModel(0, "Entertainment", "-16711861"))
        }
        if (!allCategories.contains("Insurance")) {
            dbHandler.addCategory(CategoryModel(0, "Insurance", "-16774657"))
        }
        if (!allCategories.contains("Travel")) {
            dbHandler.addCategory(CategoryModel(0, "Travel", "-65497"))
        }
        if (!allCategories.contains("Eating Out")) {
            dbHandler.addCategory(CategoryModel(0, "Eating Out", "-29696"))
        }
        if (!allCategories.contains("Other")) {
            dbHandler.addCategory(CategoryModel(0, "Other", "-65281"))
        }

        dbHandler.close()

    }

    fun currencyInputFilter(str: String, MAX_BEFORE_POINT: Int, MAX_DECIMAL: Int): String {
        var str = str
        if (str[0] == '.') str = "0$str"
        val max = str.length
        var rFinal = ""
        var after = false
        var i = 0
        var up = 0
        var decimal = 0
        var t: Char
        while (i < max) {
            t = str[i]
            if (t != '.' && !after) {
                up++
                if (up > MAX_BEFORE_POINT) return rFinal
            } else if (t == '.') {
                after = true
            } else {
                decimal++
                if (decimal > MAX_DECIMAL) return rFinal
            }
            rFinal += t
            i++
        }
        return rFinal
    }

    fun getTransactionCategoryTitle(CategoryId: Int): String {
        val dbHandlerCat = CategoriesHandler(this, null)
        val result = dbHandlerCat.getCategoryTitle(CategoryId)
        dbHandlerCat.close()
        return result
    }

    private fun initReviews() {
        manager = ReviewManagerFactory.create(this)
        manager.requestReviewFlow().addOnCompleteListener { request ->
            if (request.isSuccessful) {
                reviewInfo = request.result
            } else {
                Log.e("initReviews", "An error occurred.")
            }
        }
    }

    private fun askForReview() {
        if (reviewInfo != null) {
            manager.launchReviewFlow(this, reviewInfo!!).addOnFailureListener {
                Log.e("askForReview", "An error occurred.")
            }.addOnCompleteListener { _ ->
                Log.v("askForReview", "Success.")
            }
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        selectedCategory = parent?.getItemAtPosition(position).toString()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Toast.makeText(this, "Nothing's selected in category dropdown.", Toast.LENGTH_SHORT).show()
    }


    // Import / export code below

    private fun updateLastBackupDate() {
        val db = LastBackupHandler(this, null)
        db.addBackupDate(Calendar.getInstance().timeInMillis.toString())
    }

    private fun backupDialog() {
        val backupDialog = Dialog(this, R.style.Theme_Dialog)
        backupDialog.setCancelable(false)
        backupDialog.setContentView(R.layout.dialog_backup)
        backupDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        backupDialog.tvCancel.setOnClickListener {
            backupDialog.dismiss()
        }

        val db = LastBackupHandler(this, null)
        val date = db.getBackupDate()

        if (date.isNotEmpty()) {
            val sdf = SimpleDateFormat("d MMM yyyy HH:mm", Locale.getDefault())
            val formattedDate = sdf.format(date.toFloat())
            backupDialog.last_backup.text = "Last backup: $formattedDate"
        }

        db.close()

        backupDialog.import_text_export_location.text =
            "Backups will be exported to and imported from ${this.getExternalFilesDir(null)!!.absolutePath}/SMMBackups."

        backupDialog.import_btn.setOnClickListener {
            if (checkStoragePermission()) {
                val importConfirmDialog = Dialog(this, R.style.Theme_Dialog)
                importConfirmDialog.setCancelable(false)
                importConfirmDialog.setContentView(R.layout.dialog_import_confirm)
                importConfirmDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                importConfirmDialog.import_confirm_btn.setOnClickListener {
                    importFullCSV()
                    importConfirmDialog.dismiss()
                    backupDialog.dismiss()
                }

                importConfirmDialog.tvCancelImport.setOnClickListener {
                    importConfirmDialog.dismiss()
                }

                importConfirmDialog.show()

            } else {
                requestStoragePermissionImport()
            }
        }

        backupDialog.export_btn.setOnClickListener {
            if (checkStoragePermission()) {
                val exportConfirmDialog = Dialog(this, R.style.Theme_Dialog)
                exportConfirmDialog.setCancelable(false)
                exportConfirmDialog.setContentView(R.layout.dialog_export_confirm)
                exportConfirmDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

                exportConfirmDialog.export_confirm_btn.setOnClickListener {
                    exportFullCSV()
                    updateLastBackupDate()
                    exportConfirmDialog.dismiss()
                    backupDialog.dismiss()
                }

                exportConfirmDialog.tvCancelExport.setOnClickListener {
                    exportConfirmDialog.dismiss()
                }

                exportConfirmDialog.show()

            } else {
                requestStoragePermissionExport()
            }
        }

        backupDialog.show()

    }


    private fun exportFullCSV() {
        if (exportCategoriesCSV() && exportAccountsCSV() && exportTransactionsCSV()) {
            Toast.makeText(
                this,
                "Backup files successfully exported to ${this.getExternalFilesDir(null)!!.absolutePath}/SMMBackups",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                this,
                "An error occurred. Please try restarting the app.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun importFullCSV() {
        if (importCategoriesCSV() && importAccountsCSV() && importTransactionsCSV()) {
            Toast.makeText(
                this,
                "Backup files imported successfully from ${this.getExternalFilesDir(null)!!.absolutePath}/SMMBackups",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                this,
                "An error occurred. Please ensure backup files are in the correct place and that at least one transaction was to be imported.",
                Toast.LENGTH_LONG
            ).show()
        }
        setUpTransactionList()
        setBalanceText()
    }

    private fun checkStoragePermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            this,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == (PackageManager.PERMISSION_GRANTED)
    }

    private fun requestStoragePermissionExport() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE_EXPORT)
    }

    private fun requestStoragePermissionImport() {
        ActivityCompat.requestPermissions(this, storagePermission, STORAGE_REQUEST_CODE_IMPORT)
    }

    private fun exportTransactionsCSV(): Boolean {

        var success = false
        val dbTrans = TransactionsHandler(this, null)
        val folder = File(this.getExternalFilesDir(null)!!.absolutePath, "/SMMBackups")
        if (!folder.exists()) {
            folder.mkdir()
        }
        val csvFileName = "SMM_Transactions_Backup.csv"
        val fileNameAndPath = "$folder/$csvFileName"
        var recordList = ArrayList<TransactionModel>()
        recordList.clear()
        recordList = dbTrans.getAllTransactions()

        try {
            val fw = FileWriter(fileNameAndPath)
            for (i in recordList.indices) {
                fw.append("" + recordList[i].id)
                fw.append(",")
                fw.append("" + recordList[i].note)
                fw.append(",")
                fw.append("" + recordList[i].category)
                fw.append(",")
                fw.append("" + recordList[i].amount)
                fw.append(",")
                fw.append("" + recordList[i].account)
                fw.append(",")
                fw.append("" + recordList[i].month)
                fw.append(",")
                fw.append("" + recordList[i].day)
                fw.append(",")
                fw.append("" + recordList[i].year)
                fw.append(",")
                fw.append("" + recordList[i].dateMillis)
                fw.append("\n")
            }
            fw.flush()
            fw.close()
            success = true
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
        dbTrans.close()
        return success
    }

    private fun exportCategoriesCSV(): Boolean {

        var success = false
        val dbCats = CategoriesHandler(this, null)
        val folder = File(this.getExternalFilesDir(null)!!.absolutePath, "/SMMBackups")
        if (!folder.exists()) {
            folder.mkdir()
        }
        val csvFileName = "SMM_Categories_Backup.csv"
        val fileNameAndPath = "$folder/$csvFileName"
        var recordList = ArrayList<CategoryModel>()
        recordList.clear()
        recordList = dbCats.getAllCategories()

        try {
            val fw = FileWriter(fileNameAndPath)
            for (i in recordList.indices) {
                fw.append("" + recordList[i].id)
                fw.append(",")
                fw.append("" + recordList[i].title)
                fw.append(",")
                fw.append("" + recordList[i].colour)
                fw.append("\n")
            }
            fw.flush()
            fw.close()
            success = true
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
        dbCats.close()
        return success
    }

    private fun exportAccountsCSV(): Boolean {

        var success = false
        val dbAccs = AccountsHandler(this, null)
        val folder = File(this.getExternalFilesDir(null)!!.absolutePath, "/SMMBackups")
        if (!folder.exists()) {
            folder.mkdir()
        }
        val csvFileName = "SMM_Accounts_Backup.csv"
        val fileNameAndPath = "$folder/$csvFileName"
        var recordList = ArrayList<AccountModel>()
        recordList.clear()
        recordList = dbAccs.getAllAccounts()

        try {
            val fw = FileWriter(fileNameAndPath)
            for (i in recordList.indices) {
                fw.append("" + recordList[i].id)
                fw.append(",")
                fw.append("" + recordList[i].name)
                fw.append("\n")
            }
            fw.flush()
            fw.close()
            success = true
        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
        dbAccs.close()
        return success
    }

    private fun importTransactionsCSV(): Boolean {

        var success = false
        val dbTrans = TransactionsHandler(this, null)
        val fileNameAndPath =
            this.getExternalFilesDir(null)!!.absolutePath + "/SMMBackups/SMM_Transactions_Backup.csv"
        val csvFile = File(fileNameAndPath)

        if (csvFile.exists()) {
            try {
                dbTrans.resetOnImport()
                val csvReader = CSVReader(FileReader(csvFile.absolutePath))
                var nextLine: Array<String>
                while (csvReader.readNext().also { nextLine = it } != null) {
                    val id = nextLine[0]
                    val note = nextLine[1]
                    val category = nextLine[2]
                    val amount = nextLine[3]
                    val account = nextLine[4]
                    val month = nextLine[5]
                    val day = nextLine[6]
                    val year = nextLine[7]
                    val dateMillis = nextLine[8]

                    val transToAdd = TransactionModel(
                        id.toInt(),
                        note,
                        category.toInt(),
                        amount,
                        account.toInt(),
                        month.toInt(),
                        day.toInt(),
                        year.toInt(),
                        dateMillis
                    )
                    dbTrans.addTransaction(transToAdd)
                    success = true
                }
                csvReader.close()
            } catch (e: Exception) {
                Log.e("importTransactions", e.message.toString())
            }
        } else {
            Log.e("Import", "Transactions CSV file not found.")
        }
        dbTrans.close()
        return success
    }

    private fun importCategoriesCSV(): Boolean {

        var success = false
        val dbCats = CategoriesHandler(this, null)
        val fileNameAndPath =
            this.getExternalFilesDir(null)!!.absolutePath + "/SMMBackups/SMM_Categories_Backup.csv"
        val csvFile = File(fileNameAndPath)

        if (csvFile.exists()) {
            try {
                dbCats.resetOnImport()
                checkDefaultCategories()
                val csvReader = CSVReader(FileReader(csvFile.absolutePath))
                var nextLine: Array<String>
                while (csvReader.readNext().also { nextLine = it } != null) {
                    val id = nextLine[0]
                    val title = nextLine[1]
                    val colour = nextLine[2]

                    val catToAdd = CategoryModel(id.toInt(), title, colour)
                    dbCats.addCategory(catToAdd)
                    success = true
                }
                csvReader.close()
            } catch (e: Exception) {
                Log.e("importCategories", e.message.toString())
            }
        } else {
            Log.e("Import", "Categories CSV file not found.")
        }

        dbCats.close()
        return success
    }

    private fun importAccountsCSV(): Boolean {

        var success = false
        val dbAccs = AccountsHandler(this, null)
        val fileNameAndPath =
            this.getExternalFilesDir(null)!!.absolutePath + "/SMMBackups/SMM_Accounts_Backup.csv"
        val csvFile = File(fileNameAndPath)

        if (csvFile.exists()) {
            try {
                dbAccs.resetOnImport()
                if (noAccounts()) {
                    val dbHandler = AccountsHandler(this, null)
                    dbHandler.addAccount(AccountModel(0, "Main Account"))
                    Constants.CURRENT_ACCOUNT = dbHandler.getAllAccounts().first().id
                    dbHandler.close()
                }
                val csvReader = CSVReader(FileReader(csvFile.absolutePath))
                var nextLine: Array<String>
                while (csvReader.readNext().also { nextLine = it } != null) {
                    val id = nextLine[0]
                    val name = nextLine[1]

                    val accToAdd = AccountModel(id.toInt(), name)
                    dbAccs.addAccount(accToAdd)
                    success = true
                }
                csvReader.close()
            } catch (e: Exception) {
                Log.e("importAccounts", e.message.toString())
            }
        } else {
            Log.e("Import", "Accounts CSV file not found.")
        }

        dbAccs.close()
        return success
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            STORAGE_REQUEST_CODE_IMPORT -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    importFullCSV()
                } else {
                    Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show()
                }
            }
            STORAGE_REQUEST_CODE_EXPORT -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    exportFullCSV()
                } else {
                    Toast.makeText(this, "Permission denied.", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

}
