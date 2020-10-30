package com.example.spudgmoneymanager

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_accounts.*
import kotlinx.android.synthetic.main.dialog_add_account.*
import kotlinx.android.synthetic.main.dialog_add_account.etNameLayout
import kotlinx.android.synthetic.main.dialog_add_account.tvCancel
import kotlinx.android.synthetic.main.dialog_add_account.view.etName
import kotlinx.android.synthetic.main.dialog_delete_transaction.tvDelete
import kotlinx.android.synthetic.main.dialog_update_account.*

class AccountsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accounts)

        setUpAccountList()
        setTotalBalance()

        add_account.setOnClickListener {
            addAccount()
        }

    }

    private fun setUpAccountList() {
        if (getAccountsList().size > 0) {
            rvAccounts.layoutManager = LinearLayoutManager(this)
            val accountAdapter = AccountAdapter(this, getAccountsList())
            rvAccounts.adapter = accountAdapter
        }
    }

    private fun getAccountsList(): ArrayList<AccountModel> {
        val dbHandler = AccountsHandler(this, null)
        return dbHandler.getAllAccounts()
    }

    private fun addAccount() {
        val addDialog = Dialog(this, R.style.Theme_Dialog)
        addDialog.setCancelable(false)
        addDialog.setContentView(R.layout.dialog_add_account)
        addDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));

        addDialog.tvAdd.setOnClickListener {
            val name = addDialog.etNameLayout.etName.text.toString()

            val dbHandler = AccountsHandler(this, null)

            if (title.isNotEmpty()) {
                dbHandler.addAccount(AccountModel(0, name))
                if (Constants.CAT_UNIQUE_TITLE == 1) {
                    Toast.makeText(this, "Account added.", Toast.LENGTH_LONG).show()
                    setUpAccountList()
                    addDialog.dismiss()
                } else {
                    Toast.makeText(this, "Account name already exists.", Toast.LENGTH_LONG).show()
                }
            } else {
                Toast.makeText(this, "Account name can't be blank.", Toast.LENGTH_LONG).show()
            }

        }

        addDialog.tvCancel.setOnClickListener {
            addDialog.dismiss()
        }

        addDialog.show()
    }

    fun selectAccount(account: AccountModel) {
        Constants.CURRENT_ACCOUNT = account.id
        var intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun updateAccount(account: AccountModel) {
        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)
        updateDialog.setContentView(R.layout.dialog_update_account)
        updateDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));

        updateDialog.etNameLayout.etName.setText(account.name)

        updateDialog.tvUpdate.setOnClickListener {
            val name = updateDialog.etNameLayout.etName.text.toString()

            val dbHandler = AccountsHandler(this, null)

            if (name.isNotEmpty()) {
                dbHandler.updateAccount(AccountModel(account.id, name))
                if (Constants.CAT_UNIQUE_TITLE == 1) {
                    Toast.makeText(this, "Account updated.", Toast.LENGTH_LONG).show()
                    setUpAccountList()
                    updateDialog.dismiss()
                } else {
                    Toast.makeText(this, "Account name already exists.", Toast.LENGTH_LONG).show()
                }
                setUpAccountList()
            } else {
                Toast.makeText(this, "Account name can't be blank.", Toast.LENGTH_LONG).show()
            }

        }

        updateDialog.tvCancel.setOnClickListener {
            updateDialog.dismiss()
        }

        updateDialog.show()
    }

    fun deleteAccount(account: AccountModel) {
        val deleteDialog = Dialog(this, R.style.Theme_Dialog)
        deleteDialog.setCancelable(false)
        deleteDialog.setContentView(R.layout.dialog_delete_account)
        deleteDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));

        deleteDialog.tvDelete.setOnClickListener {
            val dbHandlerAcc = AccountsHandler(this, null)
            val dbHandlerTran = TransactionsHandler(this, null)
            dbHandlerAcc.deleteAccount(AccountModel(account.id, ""))
            dbHandlerTran.deleteTransactionDueToAccountDeletion(AccountModel(account.id, ""))

            Toast.makeText(this, "Account deleted.", Toast.LENGTH_LONG).show()
            setUpAccountList()
            deleteDialog.dismiss()
        }

        deleteDialog.tvCancel.setOnClickListener {
            deleteDialog.dismiss()
        }

        deleteDialog.show()

    }

    fun getBalance(): TransactionsHandler {
        return TransactionsHandler(this, null)
    }

    fun setTotalBalance() {
        val dbHandler = TransactionsHandler(this, null)
        balance_heading.text = "Total balance: ${dbHandler.getBalanceForAllAccounts()}"
    }


}