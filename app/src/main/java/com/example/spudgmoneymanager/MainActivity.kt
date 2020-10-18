package com.example.spudgmoneymanager

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.account_heading
import kotlinx.android.synthetic.main.dialog_add_transaction.*
import kotlinx.android.synthetic.main.dialog_add_transaction.etAmountLayout
import kotlinx.android.synthetic.main.dialog_add_transaction.etCategoryLayout
import kotlinx.android.synthetic.main.dialog_add_transaction.inc_exp_radio_group
import kotlinx.android.synthetic.main.dialog_add_transaction.tvCancel
import kotlinx.android.synthetic.main.dialog_add_transaction.view.etAmount
import kotlinx.android.synthetic.main.dialog_add_transaction.view.etCategory
import kotlinx.android.synthetic.main.dialog_add_transaction.view.expenditure_radio
import kotlinx.android.synthetic.main.dialog_add_transaction.view.income_radio
import kotlinx.android.synthetic.main.dialog_delete_transaction.*
import kotlinx.android.synthetic.main.dialog_update_transaction.*

class MainActivity : AppCompatActivity() {

    var isIncome = true


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

        setBalanceText()
        setAccountName()

        if (noAccounts()) {
            val dbHandler = AccountsHandler(this, null)
            dbHandler.addAccount(AccountModel(0, "Main Account"))
            CurrentAccountVariable.CURRENT_ACCOUNT = dbHandler.getAllAccounts().first().id
            setAccountName()
            setBalanceText()
        }

    }

    private fun setUpTransactionList() {
        if (getTransactionsList().size > 0) {
            rvTransactions.layoutManager = LinearLayoutManager(this)
            val transactionAdapter = TransactionAdapter(this, getTransactionsList())
            rvTransactions.adapter = transactionAdapter
        }
    }

    private fun getTransactionsList(): ArrayList<TransactionModel> {
        val dbHandler = TransactionsHandler(this, null)
        return dbHandler.filterTransactions(CurrentAccountVariable.CURRENT_ACCOUNT)
    }

    private fun addTransaction() {
        val addDialog = Dialog(this, R.style.Theme_Dialog)
        addDialog.setCancelable(false)
        addDialog.setContentView(R.layout.dialog_add_transaction)
        addDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));

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
            val category = addDialog.etCategoryLayout.etCategory.text.toString()
            val amount = addDialog.etAmountLayout.etAmount.text.toString()
            val account = CurrentAccountVariable.CURRENT_ACCOUNT

            val dbHandler = TransactionsHandler(this, null)

            if (category.isNotEmpty() && amount.isNotEmpty()) {
                if (isIncome) {
                    dbHandler.addTransaction(TransactionModel(0, category, amount, account))
                } else if (!isIncome) {
                    dbHandler.addTransaction(
                        TransactionModel(0, category, (amount.toDouble() * -1).toString(), account)
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

    fun updateTransaction(transaction: TransactionModel) {
        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)
        updateDialog.setContentView(R.layout.dialog_update_transaction)
        updateDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));

        updateDialog.etCategoryLayout.etCategory.setText(transaction.category)

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
            val category = updateDialog.etCategoryLayout.etCategory.text.toString()
            val amount = updateDialog.etAmountLayout.etAmount.text.toString()
            val account = CurrentAccountVariable.CURRENT_ACCOUNT

            val dbHandler = TransactionsHandler(this, null)

            if (category.isNotEmpty() && amount.isNotEmpty()) {
                if (isIncome) {
                    dbHandler.updateTransaction(TransactionModel(transaction.id, category, amount, account))
                } else if (!isIncome) {
                    dbHandler.updateTransaction(
                        TransactionModel(transaction.id, category, (amount.toDouble() * -1).toString(), account)
                    )
                }

                Toast.makeText(this, "Transaction updated.", Toast.LENGTH_LONG).show()
                setBalanceText()
                setUpTransactionList()
                updateDialog.dismiss()

            } else {
                Toast.makeText(this, "Category or amount can't be blank.", Toast.LENGTH_LONG).show()
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
            dbHandler.deleteTransaction(TransactionModel(transaction.id, "", "", 0))

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
        val balance = dbHandler.getBalance(CurrentAccountVariable.CURRENT_ACCOUNT)
        if (balance.isEmpty()) {
            balance_heading.text = "Error"
        } else {
            balance_heading.text = "Balance: $balance"
        }

    }

    private fun setAccountName() {
        val dbHandler = AccountsHandler(this, null)
        val name = dbHandler.getAccountName(CurrentAccountVariable.CURRENT_ACCOUNT)
        account_heading.text = name

    }

    private fun noAccounts(): Boolean {
        val dbHandler = AccountsHandler(this, null)
        return dbHandler.getAllAccounts().size < 1
    }

}
