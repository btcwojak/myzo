package com.example.spudgmoneymanager

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.account_row.view.*

class AccountAdapter(private val context: Context, private val items: ArrayList<AccountModel>) :
    RecyclerView.Adapter<AccountAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val accountItem = view.account_row_layout!!
        val nameView = view.name_account!!
        val updateView = view.update_account!!
        val deleteView = view.delete_account!!
        val balanceView = view.balance!!
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.account_row, parent, false)
        )

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val account = items[position]
        holder.nameView.text = account.name

        if (context is AccountsActivity) {
            val db = context.getBalance()
            val bal = db.getBalanceForAccount(account.id)
            holder.balanceView.text = "Balance: $bal"
        }



        holder.accountItem.setOnClickListener {
            if (context is AccountsActivity) {
                context.selectAccount(account)
            }
        }

        holder.updateView.setOnClickListener {
            if (context is AccountsActivity) {
                context.updateAccount(account)
            }
        }

        holder.deleteView.setOnClickListener {
            if (context is AccountsActivity) {
                context.deleteAccount(account)
            }
        }


    }

    override fun getItemCount(): Int {
        return items.size
    }

}