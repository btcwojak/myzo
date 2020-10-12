package com.example.spudgmoneymanager

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.NumberFormat

class SqliteOpenHelper (context: Context, factory: SQLiteDatabase.CursorFactory?) : SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "SpudgMoneyManager.db"
        private const val TABLE_TRANSACTIONS = "transactions"

        private const val KEY_ID = "_id"
        private const val KEY_CATEGORY = "category"
        private const val KEY_AMOUNT = "amount"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_TRANSACTIONS_TABLE = ("CREATE TABLE $TABLE_TRANSACTIONS($KEY_ID INTEGER PRIMARY KEY,$KEY_CATEGORY TEXT,$KEY_AMOUNT TEXT)")
        db?.execSQL(CREATE_TRANSACTIONS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS")
        onCreate(db)
    }

    fun addTransaction(trans: TransactionModel): Long {
        val values = ContentValues()
        values.put(KEY_CATEGORY, trans.category)
        values.put(KEY_AMOUNT, trans.amount)
        val db = this.writableDatabase
        val success = db.insert(TABLE_TRANSACTIONS, null, values)
        db.close()
        return success
    }

    fun getAllTransactions(): ArrayList<TransactionModel> {
        val list = ArrayList<TransactionModel>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_TRANSACTIONS", null)

        var id: Int
        var category: String
        var amount: String

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(KEY_ID))
                category = cursor.getString(cursor.getColumnIndex(KEY_CATEGORY))
                amount = cursor.getString(cursor.getColumnIndex(KEY_AMOUNT))
                val transaction = TransactionModel(id = id, category = category, amount = amount)
                list.add(transaction)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return list

    }

    fun getBalance(): String {
        val list = ArrayList<Double>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_TRANSACTIONS", null)

        var amount: String
        var runningBalance: Double = 0.00

        if (cursor.moveToFirst()) {
            do {
                amount = cursor.getString(cursor.getColumnIndex(KEY_AMOUNT))
                var freshAmount = amount.toDouble()
                list.add(freshAmount)
            } while (cursor.moveToNext())
        }

        for (item in list) {
            runningBalance += item
        }

        cursor.close()

        val formatter: NumberFormat = DecimalFormat("#,##0.00")
        return formatter.format(runningBalance).toString()

    }


}