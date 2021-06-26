package com.spudg.myzo

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_accounts.*
import kotlinx.android.synthetic.main.dialog_add_account.*
import kotlinx.android.synthetic.main.dialog_add_account.etNameLayout
import kotlinx.android.synthetic.main.dialog_add_account.tvCancel
import kotlinx.android.synthetic.main.dialog_add_account.view.etName
import kotlinx.android.synthetic.main.dialog_delete_transaction.tvDelete
import kotlinx.android.synthetic.main.dialog_transfer_between.*
import kotlinx.android.synthetic.main.dialog_transfer_between.view.*
import kotlinx.android.synthetic.main.dialog_update_account.*
import java.util.*

class AccountsActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private var selectedAccountFrom = ""
    private var selectedAccountTo = ""
    private var selectedCategory = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accounts)

        setUpAccountList()
        setTotalBalance()

        add_account.setOnClickListener {
            addAccount()
        }

        btnTransfer.setOnClickListener {
            transferBetween()
        }

    }

    private fun transferBetween() {
        val transferDialog = Dialog(this, R.style.Theme_Dialog)
        transferDialog.setCancelable(false)
        transferDialog.setContentView(R.layout.dialog_transfer_between)
        transferDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val accountsListHandler = AccountsHandler(this, null)
        val itemsAccounts = accountsListHandler.getAllAccountNames()
        accountsListHandler.close()
        val accountsAdapter = ArrayAdapter(this, R.layout.custom_spinner, itemsAccounts)
        transferDialog.accountFromDropdownTransfer.adapter = accountsAdapter
        transferDialog.accountFromDropdownTransfer.onItemSelectedListener = this
        transferDialog.accountToDropdownTransfer.adapter = accountsAdapter
        transferDialog.accountToDropdownTransfer.onItemSelectedListener = this

        val categoryListHandler = CategoriesHandler(this, null)
        val itemsCategories = categoryListHandler.getAllCategoryTitles()
        accountsListHandler.close()
        val categoryAdapter = ArrayAdapter(this, R.layout.custom_spinner, itemsCategories)
        transferDialog.categoryDropdownTransfer.adapter = categoryAdapter
        transferDialog.categoryDropdownTransfer.onItemSelectedListener = this

        transferDialog.tvTransfer.setOnClickListener {
            val transactionsHandler = TransactionsHandler(this, null)
            val accountsHandler = AccountsHandler(this, null)
            val accountFromId = accountsHandler.getAccountId(selectedAccountFrom)
            val accountToId = accountsHandler.getAccountId(selectedAccountTo)
            val amountToTransfer =
                transferDialog.etAmountTransferLayout.etAmountTransfer.text.toString()
            val category = selectedCategory

            val cal = Calendar.getInstance()
            val day = cal.get(Calendar.DAY_OF_MONTH)
            val month = cal.get(Calendar.MONTH)
            val year = cal.get(Calendar.YEAR)

            if (amountToTransfer.isNotEmpty()) {
                if (accountToId != accountFromId) {
                    transactionsHandler.addTransaction(
                        TransactionModel(
                            0,
                            "Transfer to $selectedAccountFrom",
                            categoryListHandler.getCategoryId(category),
                            (amountToTransfer.toFloat() * -1).toString(),
                            accountFromId,
                            month,
                            day,
                            year,
                            cal.timeInMillis.toString()
                        )
                    )
                    transactionsHandler.addTransaction(
                        TransactionModel(
                            0,
                            "Transfer from $selectedAccountTo",
                            categoryListHandler.getCategoryId(category),
                            amountToTransfer,
                            accountToId,
                            month,
                            day,
                            year,
                            cal.timeInMillis.toString()
                        )
                    )

                    setUpAccountList()

                    Toast.makeText(this, "Transfer added successfully.", Toast.LENGTH_SHORT).show()

                    transferDialog.dismiss()
                } else {
                    Toast.makeText(
                        this,
                        "You can't transfer between the same account.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                Toast.makeText(this, "Amount can't be blank.", Toast.LENGTH_SHORT).show()
            }
        }

        transferDialog.tvCancelTransfer.setOnClickListener {
            transferDialog.dismiss()
        }

        transferDialog.show()
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
        val result = dbHandler.getAllAccounts()
        dbHandler.close()
        return result
    }

    private fun addAccount() {
        val addDialog = Dialog(this, R.style.Theme_Dialog)
        addDialog.setCancelable(false)
        addDialog.setContentView(R.layout.dialog_add_account)
        addDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        addDialog.tvAdd.setOnClickListener {
            val name = addDialog.etNameLayout.etName.text.toString()

            val dbHandler = AccountsHandler(this, null)

            if (name.isNotEmpty()) {
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

            dbHandler.close()

        }

        addDialog.tvCancel.setOnClickListener {
            addDialog.dismiss()
        }

        addDialog.show()
    }

    fun selectAccount(account: AccountModel) {
        Constants.CURRENT_ACCOUNT = account.id
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    fun updateAccount(account: AccountModel) {
        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)
        updateDialog.setContentView(R.layout.dialog_update_account)
        updateDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

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

            dbHandler.close()

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
        deleteDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        deleteDialog.tvDelete.setOnClickListener {
            val dbHandlerAcc = AccountsHandler(this, null)
            val dbHandlerTrans = TransactionsHandler(this, null)
            dbHandlerAcc.deleteAccount(AccountModel(account.id, ""))
            dbHandlerTrans.deleteTransactionDueToAccountDeletion(AccountModel(account.id, ""))

            Toast.makeText(this, "Account deleted.", Toast.LENGTH_LONG).show()
            setUpAccountList()
            dbHandlerAcc.close()
            dbHandlerTrans.close()
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

    private fun setTotalBalance() {
        val dbHandler = TransactionsHandler(this, null)
        balance_heading.text =
            getString(R.string.total_account_balance, dbHandler.getBalanceForAllAccounts())
        dbHandler.close()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when (parent!!.id) {
            R.id.accountFromDropdownTransfer -> selectedAccountFrom =
                parent.getItemAtPosition(position).toString()
            R.id.accountToDropdownTransfer -> selectedAccountTo =
                parent.getItemAtPosition(position).toString()
            R.id.categoryDropdownTransfer -> selectedCategory =
                parent.getItemAtPosition(position).toString()
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        Toast.makeText(this, "Nothing's selected in the accounts dropdown.", Toast.LENGTH_SHORT)
            .show()
    }


}