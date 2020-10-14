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
import kotlinx.android.synthetic.main.dialog_add_account.view.*

class AccountsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accounts)

        setUpAccountList()

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


            dbHandler.addAccount(AccountModel(0, name))


            if (name.isNotEmpty()) {
                Toast.makeText(this, "Account added.", Toast.LENGTH_LONG).show()
                setUpAccountList()
                addDialog.dismiss()
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
        CurrentAccountVariable.CURRENT_ACCOUNT = account.id
        var intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

}