package com.spudg.spudgmoneymanager

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_categories.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_recurring.*
import java.util.ArrayList

class RecurringActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recurring)

        setUpRecurringTransactionList()

        back_to_trans_from_recurring.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }

    private fun setUpRecurringTransactionList() {
        if (getRecurringTransactionsList().size >= 0) {
            var manager = LinearLayoutManager(this)
            rvRecurring.layoutManager = manager
            val transactionAdapter = TransactionAdapter(this, getRecurringTransactionsList())
            rvRecurring.adapter = transactionAdapter
        }
    }

    private fun getRecurringTransactionsList(): ArrayList<TransactionModel> {
        val dbHandler = TransactionsHandler(this, null)
        return dbHandler.filterTransactions(Constants.CURRENT_ACCOUNT, -1,true)
    }

    fun getRecurringTransactionCategoryColour(categoryId: Int): Int {
        val dbHandler = CategoriesHandler(this, null)
        return dbHandler.getCategoryColour(categoryId)
    }

    fun getRecurringTransactionCategoryTitle(CategoryId: Int): String {
        val dbHandlerCat = CategoriesHandler(this, null)
        return dbHandlerCat.getCategoryTitle(CategoryId)
    }

}