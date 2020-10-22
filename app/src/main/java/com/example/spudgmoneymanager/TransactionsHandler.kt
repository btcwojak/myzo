package com.example.spudgmoneymanager

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.DecimalFormat
import java.text.NumberFormat

class TransactionsHandler(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 3
        private const val DATABASE_NAME = "SMMTransactions.db"
        private const val TABLE_TRANSACTIONS = "transactions"

        private const val KEY_ID = "_id"
        private const val KEY_CATEGORY = "category"
        private const val KEY_AMOUNT = "amount"
        private const val KEY_ACCOUNT = "account"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_TRANSACTIONS_TABLE =
            ("CREATE TABLE $TABLE_TRANSACTIONS($KEY_ID INTEGER PRIMARY KEY,$KEY_CATEGORY TEXT,$KEY_AMOUNT TEXT,$KEY_ACCOUNT INTEGER)")
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
        values.put(KEY_ACCOUNT, trans.account)
        val db = this.writableDatabase
        val success = db.insert(TABLE_TRANSACTIONS, null, values)
        db.close()
        return success
    }

    fun updateTransaction(trans: TransactionModel): Int {
        val values = ContentValues()
        values.put(KEY_CATEGORY, trans.category)
        values.put(KEY_AMOUNT, trans.amount)
        values.put(KEY_ACCOUNT, trans.account)
        val db = this.writableDatabase
        val success = db.update(TABLE_TRANSACTIONS, values, KEY_ID + "=" + trans.id, null)
        db.close()
        return success
    }

    fun deleteTransaction(trans: TransactionModel): Int {
        val db = this.writableDatabase
        val success = db.delete(TABLE_TRANSACTIONS, KEY_ID + "=" + trans.id, null)
        db.close()
        return success
    }

    fun deleteTransactionDueToAccountDeletion(acc: AccountModel): Int {
        val db = this.writableDatabase
        val success = db.delete(TABLE_TRANSACTIONS, KEY_ACCOUNT + "=" + acc.id, null)
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
        var account: Int

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(KEY_ID))
                category = cursor.getString(cursor.getColumnIndex(KEY_CATEGORY))
                amount = cursor.getString(cursor.getColumnIndex(KEY_AMOUNT))
                account = cursor.getInt(cursor.getColumnIndex(KEY_ACCOUNT))
                val transaction = TransactionModel(
                    id = id,
                    category = category,
                    amount = amount,
                    account = account
                )
                list.add(transaction)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return list

    }

    fun getBalanceForAccount(accountFilter: Int): String {
        val list = ArrayList<Double>()
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_TRANSACTIONS WHERE $KEY_ACCOUNT = $accountFilter",
            null
        )

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

    fun getBalanceForAllAccounts(): String {
        val list = ArrayList<Double>()
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_TRANSACTIONS",
            null
        )

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

    fun filterTransactions(accountFilter: Int): ArrayList<TransactionModel> {
        val list = ArrayList<TransactionModel>()
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_TRANSACTIONS WHERE $KEY_ACCOUNT = $accountFilter",
            null
        )

        var id: Int
        var category: String
        var amount: String
        var account: Int

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(KEY_ID))
                category = cursor.getString(cursor.getColumnIndex(KEY_CATEGORY))
                amount = cursor.getString(cursor.getColumnIndex(KEY_AMOUNT))
                account = cursor.getInt(cursor.getColumnIndex(KEY_ACCOUNT))
                val transaction = TransactionModel(
                    id = id,
                    category = category,
                    amount = amount,
                    account = account
                )
                list.add(transaction)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return list

    }


}