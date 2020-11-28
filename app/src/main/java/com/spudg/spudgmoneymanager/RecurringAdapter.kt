package com.spudg.spudgmoneymanager

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recurring_row.view.*
import kotlinx.android.synthetic.main.transaction_row.view.*
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat


class RecurringAdapter(val context: Context, private val items: ArrayList<RecurringModel>) :
    RecyclerView.Adapter<RecurringAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val recurringItem = view.recurring_transaction_row_layout!!
        val mainRowItem = view.main_row_layout_recurring!!
        val categoryView = view.category_recurring!!
        val amountView = view.amount_recurring!!
        val noteView = view.note_recurring!!
        val colourView = view.category_colour_recurring!!
        val dateView = view.next_date_header_recurring!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.recurring_row, parent, false)
        )
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val formatter: NumberFormat = DecimalFormat("#,##0.00")

        val recurringTransaction = items[position]

        var sdf = SimpleDateFormat("EEEE d MMM yyyy")
        var nextDate = sdf.format(recurringTransaction.nextDateMillis.toLong())

        holder.dateView.text = "Next posting date - $nextDate"

        if (context is RecurringActivity) {
            holder.categoryView.text = context.getRecurringTransactionCategoryTitle(recurringTransaction.category)
        }

        holder.amountView.text = formatter.format((recurringTransaction.amount).toDouble()).toString()
        holder.noteView.text = recurringTransaction.note

        if (context is RecurringActivity) {
            val colour = context.getRecurringTransactionCategoryColour(recurringTransaction.category)
            holder.colourView.setBackgroundColor(colour)
        }

        holder.mainRowItem.setOnClickListener {
            if (context is RecurringActivity) {
                context.updateRecurringTransaction(recurringTransaction)
            }
        }

        holder.mainRowItem.setOnLongClickListener {
            if (context is RecurringActivity) {
                context.deleteRecurringTransaction(recurringTransaction)
            }
            true
        }

    }

    override fun getItemCount(): Int {
        return items.size
    }
}