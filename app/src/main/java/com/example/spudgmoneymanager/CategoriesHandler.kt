package com.example.spudgmoneymanager

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class CategoriesHandler(context: Context, factory: SQLiteDatabase.CursorFactory?) : SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 2
        private const val DATABASE_NAME = "SMMCategories.db"
        private const val TABLE_CATEGORIES = "categories"

        private const val KEY_ID = "_id"
        private const val KEY_TITLE = "category"
        private const val KEY_COLOUR = "colour"

    }


    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_CATEGORIES_TABLE = ("CREATE TABLE $TABLE_CATEGORIES($KEY_ID INTEGER PRIMARY KEY,$KEY_TITLE TEXT,$KEY_COLOUR TEXT)")
        db?.execSQL(CREATE_CATEGORIES_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_CATEGORIES")
        onCreate(db)
    }


    fun addCategory(category: CategoryModel): Long {
        val values = ContentValues()
        values.put(KEY_TITLE, category.title)
        values.put(KEY_COLOUR, category.colour)
        val db = this.writableDatabase
        val success = db.insert(TABLE_CATEGORIES, null, values)
        db.close()
        return success
    }

    fun updateCategory(category: CategoryModel): Int {
        val values = ContentValues()
        values.put(KEY_TITLE, category.title)
        values.put(KEY_COLOUR, category.colour)
        val db = this.writableDatabase
        val success = db.update(TABLE_CATEGORIES, values, KEY_ID + "=" + category.id, null)
        db.close()
        return success
    }

    fun deleteCategory(category: CategoryModel): Int {
        val db = this.writableDatabase
        val success = db.delete(TABLE_CATEGORIES, KEY_ID + "=" + category.id, null)
        db.close()
        return success
    }

    fun getAllCategories(): ArrayList<CategoryModel> {
        val list = ArrayList<CategoryModel>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM ${TABLE_CATEGORIES}", null)

        var id: Int
        var title: String
        var colour: String

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(KEY_ID))
                title = cursor.getString(cursor.getColumnIndex(KEY_TITLE))
                colour = cursor.getString(cursor.getColumnIndex(KEY_COLOUR))
                val category = CategoryModel(
                    id = id,
                    title = title,
                    colour = colour,
                )
                list.add(category)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return list

    }

    fun getAllCategoryTitles(): ArrayList<String> {
        val list = ArrayList<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_CATEGORIES", null)

        var title: String

        if (cursor.moveToFirst()) {
            do {
                title = cursor.getString(cursor.getColumnIndex(KEY_TITLE))
                list.add(title)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return list

    }

    fun getCategoryColour(categoryTitle: String): Int {
        val db = this.readableDatabase

        val cursor = db.rawQuery("SELECT * FROM $TABLE_CATEGORIES WHERE category = '$categoryTitle'", null)


        if (cursor.moveToFirst()) {
            return cursor.getString(cursor.getColumnIndex(KEY_COLOUR)).toInt()
        } else {
            return 0
        }

    }
    
}