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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_add_transaction.*
import kotlinx.android.synthetic.main.dialog_add_transaction.etAmountLayout
import kotlinx.android.synthetic.main.dialog_add_transaction.inc_exp_radio_group
import kotlinx.android.synthetic.main.dialog_add_transaction.tvCancel
import kotlinx.android.synthetic.main.dialog_add_transaction.view.*
import kotlinx.android.synthetic.main.dialog_delete_transaction.*
import kotlinx.android.synthetic.main.dialog_update_transaction.*
import kotlinx.android.synthetic.main.dialog_add_transaction.etNoteLayout as etNoteLayout1

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
            Toast.makeText(this, "To be added soon...", Toast.LENGTH_SHORT).show()
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
            val category = selectedCategory
            val amount = addDialog.etAmountLayout.etAmount.text.toString()
            val note = addDialog.etNoteLayout.etNote.text.toString()
            val account = Constants.CURRENT_ACCOUNT

            val dbHandler = TransactionsHandler(this, null)

            if (category.isNotEmpty() && amount.isNotEmpty() && note.isNotEmpty()) {
                if (isIncome) {
                    dbHandler.addTransaction(TransactionModel(0, note, category, amount, account))
                } else if (!isIncome) {
                    dbHandler.addTransaction(
                        TransactionModel(
                            0,
                            note,
                            category,
                            (amount.toDouble() * -1).toString(),
                            account
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
        updateDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));

        val categoryListHandler = CategoriesHandler(this, null)
        val items = categoryListHandler.getAllCategoryTitles()
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, items)
        updateDialog.category_spinner_update_trans.adapter = categoryAdapter
        updateDialog.category_spinner_update_trans.onItemSelectedListener = this

        updateDialog.etNoteLayout.etNote.setText(transaction.note)

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
            val category = selectedCategory
            val amount = updateDialog.etAmountLayout.etAmount.text.toString()
            val account = Constants.CURRENT_ACCOUNT
            val note = updateDialog.etNoteLayout.etNote.text.toString()

            val dbHandler = TransactionsHandler(this, null)

            if (category.isNotEmpty() && amount.isNotEmpty() && note.isNotEmpty()) {
                if (isIncome) {
                    dbHandler.updateTransaction(
                        TransactionModel(
                            transaction.id,
                            note,
                            category,
                            amount,
                            account
                        )
                    )
                } else if (!isIncome) {
                    dbHandler.updateTransaction(
                        TransactionModel(
                            transaction.id,
                            note,
                            category,
                            (amount.toDouble() * -1).toString(),
                            account
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
        deleteDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));

        deleteDialog.tvDelete.setOnClickListener {
            val dbHandler = TransactionsHandler(this, null)
            dbHandler.deleteTransaction(TransactionModel(transaction.id, "", "", "", 0))

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

    fun getTransactionCategoryColour(categoryTitle: String): Int {
        val dbHandler = CategoriesHandler(this, null)
        return dbHandler.getCategoryColour(categoryTitle)
    }

    fun checkDefaultCategories() {
        val dbHandler = CategoriesHandler(this, null)
        val allCategories = dbHandler.getAllCategoryTitles()

        if (!allCategories.contains("Entertainment")) {
            dbHandler.addCategory(CategoryModel(0,"Entertainment", "-16711861"))
        }
        if (!allCategories.contains("Insurance")) {
            dbHandler.addCategory(CategoryModel(0,"Insurance","-16774657"))
        }
        if (!allCategories.contains("Travel")) {
            dbHandler.addCategory(CategoryModel(0,"Travel","-65497"))
        }
        if (!allCategories.contains("Eating Out")) {
            dbHandler.addCategory(CategoryModel(0,"Eating Out","-29696"))
        }
        if (!allCategories.contains("Other")) {
            dbHandler.addCategory(CategoryModel(0,"Other","-65281"))
        }


    }


    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        selectedCategory = parent?.getItemAtPosition(position).toString()
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Toast.makeText(this, "Nothing's selected in category dropdown.", Toast.LENGTH_SHORT).show()
    }

}
