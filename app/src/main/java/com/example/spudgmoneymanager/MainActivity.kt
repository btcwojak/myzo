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

    var isIncome = true
    var selectedCategory = ""

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
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        addDialog.category_spinner_add_trans.adapter = categoryAdapter
        addDialog.category_spinner_add_trans.onItemSelectedListener = this

        addDialog.inc_exp_radio_group.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.income_radio) {
                isIncome = true
            } else if (checkedId == R.id.expenditure_radio) {
                isIncome = false
            } else {
                Toast.makeText(
                    this,
                    "An error has occurred. Please try restarting the app.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        addDialog.tvAdd.setOnClickListener {
            val dbHandlerTrans = TransactionsHandler(this, null)
            val dbHandlerCat = CategoriesHandler(this, null)

            val category = dbHandlerCat.getCategoryId(selectedCategory)
            val amount = addDialog.etAmountLayout.etAmount.text.toString()
            val note = addDialog.etNoteLayoutAdd.etNoteAdd.text.toString()
            val account = Constants.CURRENT_ACCOUNT
            val calendar = Calendar.getInstance()
            val month = calendar[Calendar.MONTH] + 1
            val day = calendar[Calendar.DAY_OF_MONTH]
            val year = calendar[Calendar.YEAR]


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
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        updateDialog.category_spinner_update_trans.adapter = categoryAdapter
        updateDialog.category_spinner_update_trans.onItemSelectedListener = this

        updateDialog.etNoteLayoutUpdate.etNoteUpdate.setText(transaction.note)

        if (transaction.amount.toFloat() >= 0) {
            updateDialog.inc_exp_radio_group.income_radio.isChecked
            updateDialog.etAmountLayout.etAmount.setText(transaction.amount)
        } else {
            updateDialog.inc_exp_radio_group.expenditure_radio.isChecked
            updateDialog.etAmountLayout.etAmount.setText((transaction.amount.toFloat() * -1).toString())
        }

        updateDialog.inc_exp_radio_group.setOnCheckedChangeListener { group, checkedId ->
            if (checkedId == R.id.income_radio) {
                isIncome = true
            } else if (checkedId == R.id.expenditure_radio) {
                isIncome = false
            } else {
                Toast.makeText(
                    this,
                    "An error has occurred. Please try restarting the app.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        updateDialog.tvUpdate.setOnClickListener {
            val dbHandler = TransactionsHandler(this, null)
            val dbHandlerCat = CategoriesHandler(this, null)

            val category = dbHandlerCat.getCategoryId(selectedCategory)
            val amount = updateDialog.etAmountLayout.etAmount.text.toString()
            val account = Constants.CURRENT_ACCOUNT
            val note = updateDialog.etNoteLayoutUpdate.etNoteUpdate.text.toString()

            if (selectedCategory.isNotEmpty() && amount.isNotEmpty() && note.isNotEmpty()) {
                if (isIncome) {
                    dbHandler.updateTransaction(
                        TransactionModel(
                            transaction.id,
                            note,
                            category,
                            amount,
                            account,
                            transaction.month,
                            transaction.day,
                            transaction.year
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
                            transaction.month,
                            transaction.day,
                            transaction.year
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
