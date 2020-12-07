package com.spudg.spudgmoneymanager

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.recurring_row.view.*
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat

class RecurringAdapter(val context: Context, private val items: ArrayList<RecurringModel>) :
    RecyclerView.Adapter<RecurringAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val transactionItem = view.transaction_row_layout_recurring!!
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

        val recurring = items[position]

        val sdf = SimpleDateFormat("EEEE d MMM yyyy")
        val date = sdf.format(recurring.nextDateMillis.toLong())

        holder.dateView.text = "Next posting date: $date"

        if (context is RecurringsActivity) {
            holder.categoryView.text = context.getTransactionCategoryTitle(recurring.category)
        }

        if (context is RecurringsActivity) {
            try {
                if (recurring.nextDateMillis.toLong() == items[position - 1].nextDateMillis.toLong()) {
                    holder.dateView.visibility = View.GONE
                }
            } catch (e: Exception) {
                Log.v("Transactions", e.message.toString())
            }
        }

        holder.amountView.text = formatter.format((recurring.amount).toDouble()).toString()
        holder.noteView.text = recurring.note

        if (context is RecurringsActivity) {
            val colour = context.getTransactionCategoryColour(recurring.category)
            holder.colourView.setBackgroundColor(colour)
        }

        holder.mainRowItem.setOnClickListener {
            if (context is RecurringsActivity) {
                context.updateRecurring(recurring)
            }
        }

        holder.mainRowItem.setOnLongClickListener {
            if (context is RecurringsActivity) {
                context.deleteRecurring(recurring)
            }
            true
        }

    }

    override fun getItemCount(): Int {
        return items.size
    }

}