package com.example.spudgmoneymanager

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
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
import kotlinx.android.synthetic.main.dialog_delete_transaction.*
import kotlinx.android.synthetic.main.dialog_update_transaction.*
import kotlinx.android.synthetic.main.dialog_update_transaction.view.*
import java.util.*

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private var isIncome = true
    private var selectedCategory = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUpTransactionList()

        add_transaction.setOnClickListener {
            addTransaction()
        }

        switch_accounts.setOnClickListener {
            val intent = Intent(this, AccountsActivity::class.java)
            startActivity(intent)
        }

        analysis_btn.setOnClickListener {
            val intent = Intent(this, AnalyticsActivity::class.java)
            startActivity(intent)
        }

        categories_btn.setOnClickListener {
            val intent = Intent(this, CategoriesActivity::class.java)
            startActivity(intent)
        }

        export_btn.setOnClickListener {
            Toast.makeText(this, "To be added soon...", Toast.LENGTH_SHORT).show()
        }

        setBalanceText()
        setAccountName()

        if (noAccounts()) {
            val dbHandler = AccountsHandler(this, null)
            dbHandler.addAccount(AccountModel(0, "Main Account"))
            Constants.CURRENT_ACCOUNT = dbHandler.getAllAccounts().first().id
            setAccountName()
            setBalanceText()
        }

        checkDefaultCategories()

    }

    private fun setUpTransactionList() {
        if (getTransactionsList().size >= 0) {
            rvTransactions.layoutManager = LinearLayoutManager(this)
            val transactionAdapter = TransactionAdapter(this, getTransactionsList())
            rvTransactions.adapter = transactionAdapter
        }
    }

    private fun getTransactionsList(): ArrayList<TransactionModel> {
        val dbHandler = TransactionsHandler(this, null)
        return dbHandler.filterTransactions(Constants.CURRENT_ACCOUNT)
    }

    private fun addTransaction() {
        val addDialog = Dialog(this, R.style.Theme_Dialog)
        addDialog.setCancelable(false)
        addDialog.setContentView(R.layout.dialog_add_transaction)
        addDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        var dayPicked = Calendar.getInstance()[Calendar.DAY_OF_MONTH]
        var monthPicked = Calendar.getInstance()[Calendar.MONTH] + 1
        var yearPicked = Calendar.getInstance()[Calendar.YEAR]

        addDialog.change_date_add.text = "$dayPicked ${Constants.getShortMonth(monthPicked)} $yearPicked"

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
                addDialog.change_date_add.text = "$dayPicked ${Constants.getShortMonth(monthPicked)} $yearPicked"
                changeDateDialog.dismiss()
            }

            changeDateDialog.dmyp_day.wrapSelectorWheel = true
            changeDateDialog.dmyp_month.wrapSelectorWheel = true
            changeDateDialog.dmyp_year.wrapSelectorWheel = true

            changeDateDialog.cancel_dmy.setOnClickListener {
                dayPicked = Calendar.getInstance()[Calendar.DAY_OF_MONTH]
                monthPicked = Calendar.getInstance()[Calendar.MONTH] + 1
                yearPicked = Calendar.getInstance()[Calendar.YEAR]
                addDialog.change_date_add.text = "$dayPicked ${Constants.getShortMonth(monthPicked)} $yearPicked"
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
                            year
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
                            year
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

        updateDialog.change_date_update.text = "$dayPicked ${Constants.getShortMonth(monthPicked)} $yearPicked"

        updateDialog.change_date_update.setOnClickListener {
            val changeDateDialog = Dialog(this, R.style.Theme_Dialog)
            changeDateDialog.setCancelable(false)
            changeDateDialog.setContentView(R.layout.day_month_year_picker)
            changeDateDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            if (transaction.day == 4 || transaction.day == 6 || transaction.day == 9 || transaction.day == 11) {
                changeDateDialog.dmyp_day.maxValue = 30
                changeDateDialog.dmyp_day.minValue = 1
            } else if (transaction.day == 2 && transaction.day % 4 == 0) {
                changeDateDialog.dmyp_day.maxValue = 29
                changeDateDialog.dmyp_day.minValue = 1
            } else if (transaction.day == 2 && transaction.day % 4 != 0) {
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
                updateDialog.change_date_update.text = "$dayPicked ${Constants.getShortMonth(monthPicked)} $yearPicked"
                changeDateDialog.dismiss()
            }

            changeDateDialog.dmyp_day.wrapSelectorWheel = true
            changeDateDialog.dmyp_month.wrapSelectorWheel = true
            changeDateDialog.dmyp_year.wrapSelectorWheel = true

            changeDateDialog.cancel_dmy.setOnClickListener {
                dayPicked = transaction.day
                monthPicked = transaction.month
                yearPicked = transaction.year
                updateDialog.change_date_update.text = "$dayPicked ${Constants.getShortMonth(monthPicked)} $yearPicked"
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
        val categoryAdapter = ArrayAdapter(this, R.layout.custom_spinner, items)
        updateDialog.category_spinner_update_trans.adapter = categoryAdapter
        updateDialog.category_spinner_update_trans.onItemSelectedListener = this
        updateDialog.category_spinner_update_trans.setSelection(transaction.category-1)

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
                            year
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
                            year
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
            dbHandler.deleteTransaction(TransactionModel(transaction.id, "", 0, "", 0, 0, 0, 0))

            Toast.makeText(this, "Transaction deleted.", Toast.LENGTH_LONG).show()
            setBalanceText()
            setUpTransactionList()
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

    }

    private fun setAccountName() {
        val dbHandler = AccountsHandler(this, null)
        val name = dbHandler.getAccountName(Constants.CURRENT_ACCOUNT)
        account_heading.text = name

    }

    private fun noAccounts(): Boolean {
        val dbHandler = AccountsHandler(this, null)
        return dbHandler.getAllAccounts().size < 1
    }

    fun getTransactionCategoryColour(categoryId: Int): Int {
        val dbHandler = CategoriesHandler(this, null)
        return dbHandler.getCategoryColour(categoryId)
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
        return dbHandlerCat.getCategoryTitle(CategoryId)
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        selectedCategory = parent?.getItemAtPosition(position).toString()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Toast.makeText(this, "Nothing's selected in category dropdown.", Toast.LENGTH_SHORT).show()
    }

}
