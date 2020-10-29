package com.example.spudgmoneymanager

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Color.TRANSPARENT
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.spudgmoneymanager.Constants.Companion.CAT_COL_SELECTED
import com.example.spudgmoneymanager.Constants.Companion.CAT_UNIQUE_TITLE
import com.madrapps.pikolo.listeners.SimpleColorSelectionListener
import kotlinx.android.synthetic.main.activity_categories.*
import kotlinx.android.synthetic.main.dialog_add_category.*
import kotlinx.android.synthetic.main.dialog_add_category.view.*
import kotlinx.android.synthetic.main.dialog_add_transaction.tvAdd
import kotlinx.android.synthetic.main.dialog_add_transaction.tvCancel
import kotlinx.android.synthetic.main.dialog_delete_transaction.*
import kotlinx.android.synthetic.main.dialog_update_transaction.*

class CategoriesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_categories)

        var selectedColour: Int

        setUpCategoryList()

        add_category.setOnClickListener {
            addCategory()
        }

        back_to_trans_from_categories.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
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
        addDialog.window!!.setBackgroundDrawable(ColorDrawable(TRANSPARENT));

        val colorPicker = addDialog.colourPicker
        colorPicker.setColorSelectionListener(object : SimpleColorSelectionListener() {
            override fun onColorSelected(color: Int) {
                Constants.CAT_COL_SELECTED = color
            }
        })

        addDialog.tvAdd.setOnClickListener {
            val title = addDialog.etTitleLayout.etTitle.text.toString()
            val colour = Constants.CAT_COL_SELECTED.toString()

            val dbHandler = CategoriesHandler(this, null)

            if (title.isNotEmpty() && colour.isNotEmpty()) {
                dbHandler.addCategory(CategoryModel(0, title, colour))
                if (Constants.CAT_UNIQUE_TITLE == 1) {
                    Toast.makeText(this, "Category added.", Toast.LENGTH_LONG).show()
                    addDialog.dismiss()
                } else {
                    Toast.makeText(this, "Category title already exists.", Toast.LENGTH_LONG).show()
                }

                setUpCategoryList()


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

        val colorPicker = updateDialog.colourPicker
        colorPicker.setColorSelectionListener(object : SimpleColorSelectionListener() {
            override fun onColorSelected(color: Int) {
                Constants.CAT_COL_SELECTED = color
            }
        })

        updateDialog.tvUpdate.setOnClickListener {
            val title = updateDialog.etTitleLayout.etTitle.text.toString()
            val colour = Constants.CAT_COL_SELECTED.toString()

            val dbHandler = CategoriesHandler(this, null)

            if (title.isNotEmpty() && colour.isNotEmpty()) {
                dbHandler.updateCategory(CategoryModel(category.id, title, colour))
                if (Constants.CAT_UNIQUE_TITLE == 1) {
                    Toast.makeText(this, "Category updated.", Toast.LENGTH_LONG).show()
                    updateDialog.dismiss()
                } else {
                    Toast.makeText(this, "Category title already exists.", Toast.LENGTH_LONG).show()
                }
                setUpCategoryList()
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
            val dbHandlerCat = CategoriesHandler(this, null)
            val dbHandlerTrans = TransactionsHandler(this, null)
            dbHandlerCat.deleteCategory(CategoryModel(category.id, "", ""))

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