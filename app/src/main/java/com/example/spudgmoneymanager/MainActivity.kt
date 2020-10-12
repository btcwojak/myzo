package com.example.spudgmoneymanager

import android.app.Dialog
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_add.*

class MainActivity : AppCompatActivity() {

    var isIncome = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setUpTransactionList()
        
        add_transaction.setOnClickListener{
            addTransaction()
        }

        setBalanceText()

    }

    private fun setUpTransactionList() {
        if (getTransactionsList().size > 0) {
            rvTransactions.layoutManager = LinearLayoutManager(this)
            val transactionAdapter = TransactionAdapter(this, getTransactionsList())
            rvTransactions.adapter = transactionAdapter
        }
    }

    private fun getTransactionsList(): ArrayList<TransactionModel> {
        val dbHandler = SqliteOpenHelper(this, null)
        return dbHandler.getAllTransactions()
    }

    private fun addTransaction() {
        val addDialog = Dialog(this, R.style.Theme_Dialog)
        addDialog.setCancelable(false)
        addDialog.setContentView(R.layout.dialog_add)

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
            val category = addDialog.etCategory.text.toString()
            val amount = addDialog.etAmount.text.toString()
            val dbHandler = SqliteOpenHelper(this, null)

            if (category.isNotEmpty() && amount.isNotEmpty()) {

                if (isIncome) {
                    dbHandler.addTransaction(TransactionModel(0, category, amount))
                } else if (!isIncome) {
                    dbHandler.addTransaction(TransactionModel(0, category, (amount.toDouble()*-1).toString()
                        )
                    )
                }

                Toast.makeText(this, "Transaction added.", Toast.LENGTH_LONG).show()
                setBalanceText()
                setUpTransactionList()
                addDialog.dismiss()

            } else {
                Toast.makeText(this, "Category or amount can't be blank.", Toast.LENGTH_LONG).show()
            }

        }

        addDialog.tvCancel.setOnClickListener {
            addDialog.dismiss()
        }

        addDialog.show()
    }

    private fun setBalanceText() {
        val dbHandler = SqliteOpenHelper(this, null)
        val balance = dbHandler.getBalance()
        if (balance.isEmpty()) {
            balance_heading.text = "Error"
        } else {
            balance_heading.text = "Balance: $balance"
        }


    }


}
