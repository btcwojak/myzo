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
import kotlinx.android.synthetic.main.dialog_add_transaction.etAmountLayout
import kotlinx.android.synthetic.main.dialog_add_transaction.inc_exp_radio_group
import kotlinx.android.synthetic.main.dialog_add_transaction.tvAdd
import kotlinx.android.synthetic.main.dialog_add_transaction.tvCancel
import kotlinx.android.synthetic.main.dialog_add_transaction.view.*
import kotlinx.android.synthetic.main.dialog_add_transaction.view.etAmount
import kotlinx.android.synthetic.main.dialog_add_transaction.view.expenditure_radio
import kotlinx.android.synthetic.main.dialog_add_transaction.view.income_radio
import kotlinx.android.synthetic.main.dialog_delete_transaction.*
import kotlinx.android.synthetic.main.dialog_update_transaction.*
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
        if (getCategoriesList().size >= 0) {
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

    fun updateCategory(category: CategoryModel) {
        val updateDialog = Dialog(this, R.style.Theme_Dialog)
        updateDialog.setCancelable(false)
        updateDialog.setContentView(R.layout.dialog_update_category)
        updateDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));

        updateDialog.etTitleLayout.etTitle.setText(category.title)
        updateDialog.etColourLayout.etColour.setText(category.colour)

        updateDialog.tvUpdate.setOnClickListener {
            val title = updateDialog.etTitleLayout.etTitle.text.toString()
            val colour = updateDialog.etColourLayout.etColour.text.toString()

            val dbHandler = CategoriesHandler(this, null)

            if (title.isNotEmpty() && colour.isNotEmpty()) {
                dbHandler.updateCategory(CategoryModel(category.id, title, colour))
                Toast.makeText(this, "Category updated.", Toast.LENGTH_LONG).show()
                setUpCategoryList()
                updateDialog.dismiss()
            } else {
                Toast.makeText(this, "Title or colour can't be blank.", Toast.LENGTH_LONG).show()
            }

        }

        updateDialog.tvCancel.setOnClickListener {
            updateDialog.dismiss()
        }

        updateDialog.show()
    }

    fun deleteCategory(category: CategoryModel) {
        val deleteDialog = Dialog(this, R.style.Theme_Dialog)
        deleteDialog.setCancelable(false)
        deleteDialog.setContentView(R.layout.dialog_delete_category)
        deleteDialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));

        deleteDialog.tvDelete.setOnClickListener {
            val dbHandler = CategoriesHandler(this, null)
            dbHandler.deleteCategory(CategoryModel(category.id, "", ""))

            Toast.makeText(this, "Category deleted.", Toast.LENGTH_LONG).show()

            setUpCategoryList()
            deleteDialog.dismiss()
        }

        deleteDialog.tvCancel.setOnClickListener {
            deleteDialog.dismiss()
        }

        deleteDialog.show()

    }


}