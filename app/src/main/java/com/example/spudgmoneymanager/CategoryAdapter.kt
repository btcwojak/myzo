package com.example.spudgmoneymanager

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.category_row.view.*


class CategoryAdapter(private val context: Context, private val items: ArrayList<CategoryModel>) :
    RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val categoryItem = view.category_row_layout!!
        val titleView = view.title_category!!
        val colourView = view.colour_category!!
        val updateView = view.update_category!!
        val deleteView = view.delete_category!!
        val defaultView = view.default_category!!

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryAdapter.ViewHolder {
        return CategoryAdapter.ViewHolder(
            LayoutInflater.from(context).inflate(R.layout.category_row, parent, false)
        )
    }

    override fun onBindViewHolder(holder: CategoryAdapter.ViewHolder, position: Int) {
        val category = items[position]
        holder.titleView.text = category.title
        holder.colourView.setBackgroundColor(category.colour.toInt())

        if (category.title == "Entertainment" || category.title == "Insurance" || category.title == "Travel" || category.title == "Eating Out" || category.title == "Other") {
            holder.updateView.visibility = View.GONE
            holder.deleteView.visibility = View.GONE
            holder.defaultView.visibility = View.VISIBLE
        } else {
            holder.updateView.visibility = View.VISIBLE
            holder.deleteView.visibility = View.VISIBLE
            holder.defaultView.visibility = View.GONE

            holder.updateView.setOnClickListener { view ->
                if (context is CategoriesActivity) {
                    context.updateCategory(category)
                }
            }

            holder.deleteView.setOnClickListener { view ->
                if (context is CategoriesActivity) {
                    context.deleteCategory(category)
                }
            }
        }


    }

    override fun getItemCount(): Int {
        return items.size
    }


}