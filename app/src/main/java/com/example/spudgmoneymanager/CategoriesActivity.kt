package com.example.spudgmoneymanager

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.activity_categories.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_add_category.*
import kotlinx.android.synthetic.main.dialog_add_category.view.*
import kotlinx.android.synthetic.main.dialog_add_transaction.*
import kotlinx.android.synthetic.main.dialog_add_transaction.tvAdd
import kotlinx.android.synthetic.main.dialog_add_transaction.tvCancel
import kotlinx.android.synthetic.main.dialog_add_transaction.view.*
import kotlinx.android.synthetic.main.dialog_delete_transaction.*
import kotlinx.android.synthetic.main.transaction_row.*

class CategoriesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        setUpCategoryList()

        add_category.setOnClickListener {
            addCategory()
        }

    }

    private fun getCategoriesList(): ArrayList<CategoryModel> {
        val dbHandler = CategoriesHandler(this, null)
        return dbHandler.getAllCategories()
    }


    private fun setUpCategoryList() {
        if (getCategoriesList().size > 0) {
            rvCategories.layoutManager = LinearLayoutManager(this)
            val categoriesAdapter = CategoryAdapter(this, getCategoriesList())
            rvCategories.adapter = categoriesAdapter
        }
    }

    private fun addCategory() {
        val addDialog = Dialog(this, R.style.Theme_Dialog)
        addDialog.setCancelable(false)
        addDialog.setContentView(R.layout.dialog_add_category)
        addDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));

        addDialog.tvAdd.setOnClickListener {
            val title = addDialog.etTitleLayout.etTitle.text.toString()
            val colour = addDialog.etColourLayout.etColour.text.toString()

            val dbHandler = CategoriesHandler(this, null)

            if (title.isNotEmpty() && colour.isNotEmpty()) {

                dbHandler.addCategory(CategoryModel(0, title, colour))

                Toast.makeText(this, "Category added.", Toast.LENGTH_LONG).show()
                setUpCategoryList()
                addDialog.dismiss()

            } else {
                Toast.makeText(this, "Title or colour can't be blank.", Toast.LENGTH_LONG).show()
            }

        }

        addDialog.tvCancel.setOnClickListener {
            addDialog.dismiss()
        }

        addDialog.show()
    }


}