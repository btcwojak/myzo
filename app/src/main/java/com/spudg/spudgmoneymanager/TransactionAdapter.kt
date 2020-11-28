package com.spudg.spudgmoneymanager

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.transaction_row.view.*
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat


class TransactionAdapter(val context: Context, private val items: ArrayList<TransactionModel>) :
    RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val transactionItem = view.transaction_row_layout!!
        val mainRowItem = view.main_row_layout!!
        val categoryView = view.category!!
        val amountView = view.amount!!
        val noteView = view.note!!
        val colourView = view.category_colour!!
        val dateView = view.date_header!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.transaction_row, parent, false)
        )
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val formatter: NumberFormat = DecimalFormat("#,##0.00")

        val transaction = items[position]

        var sdf = SimpleDateFormat("EEEE d MMM yyyy")
        var date = sdf.format(transaction.dateMillis.toLong())

        if (context is MainActivity) {
            holder.dateView.visibility = View.VISIBLE
            holder.dateView.text = date.toString()

            try {
                if (transaction.dateMillis.toLong() == items[position - 1].dateMillis.toLong()) {
                    holder.dateView.visibility = View.GONE
                }
            } catch (e: Exception) {
                Log.v("Transactions", e.message.toString())
            }

            holder.categoryView.text = context.getTransactionCategoryTitle(transaction.category)

            val colour = context.getTransactionCategoryColour(transaction.category)
            holder.colourView.setBackgroundColor(colour)

            holder.mainRowItem.setOnClickListener {
                    context.updateTransaction(transaction)
            }

            holder.mainRowItem.setOnLongClickListener {
                    context.deleteTransaction(transaction)
                true
            }
        }

        if (context is RecurringActivity) {
            holder.dateView.visibility = View.VISIBLE
            holder.dateView.text = date.toString()

            try {
                if (transaction.dateMillis.toLong() == items[position - 1].dateMillis.toLong()) {
                    holder.dateView.visibility = View.GONE
                }
            } catch (e: Exception) {
                Log.v("Transactions", e.message.toString())
            }

            holder.categoryView.text = context.getRecurringTransactionCategoryTitle(transaction.category)

            val colour = context.getRecurringTransactionCategoryColour(transaction.category)
            holder.colourView.setBackgroundColor(colour)

        }

        holder.amountView.text = formatter.format((transaction.amount).toDouble()).toString()
        holder.noteView.text = transaction.note

    }

    override fun getItemCount(): Int {
        return items.size
    }
}