package com.spudg.spudgmoneymanager

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class TransactionsHandler(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    companion object {

        private const val DATABASE_VERSION = 14
        private const val DATABASE_NAME = "SMMTransactions.db"
        private const val TABLE_TRANSACTIONS = "transactions"

        private const val KEY_ID = "_id"
        private const val KEY_NOTE = "note"
        private const val KEY_CATEGORY = "category"
        private const val KEY_AMOUNT = "amount"
        private const val KEY_ACCOUNT = "account"
        private const val KEY_MONTH = "month"
        private const val KEY_DAY = "day"
        private const val KEY_YEAR = "year"
        private const val KEY_DATE_MS = "date_millis"

    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTransactionsTable =
            ("CREATE TABLE $TABLE_TRANSACTIONS($KEY_ID INTEGER PRIMARY KEY,$KEY_NOTE TEXT,$KEY_CATEGORY INTEGER,$KEY_AMOUNT TEXT,$KEY_ACCOUNT INTEGER,$KEY_MONTH INTEGER,$KEY_DAY INTEGER,$KEY_YEAR INTEGER,$KEY_DATE_MS TEXT)")
        db?.execSQL(createTransactionsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS")
        onCreate(db)
    }

    fun addTransaction(trans: TransactionModel): Long {

        var strDate = "${trans.day}-${trans.month}-${trans.year}"
        var sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        var dateMillis = sdf.parse(strDate)?.time

        val values = ContentValues()
        values.put(KEY_NOTE, trans.note)
        values.put(KEY_CATEGORY, trans.category)
        values.put(KEY_AMOUNT, trans.amount)
        values.put(KEY_ACCOUNT, trans.account)
        values.put(KEY_MONTH, trans.month)
        values.put(KEY_DAY, trans.day)
        values.put(KEY_YEAR, trans.year)
        values.put(KEY_DATE_MS, dateMillis)
        val db = this.writableDatabase
        val success = db.insert(TABLE_TRANSACTIONS, null, values)
        db.close()
        return success
    }

    fun updateTransaction(trans: TransactionModel): Int {

        var strDate = "${trans.day}-${trans.month}-${trans.year}"
        var sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        var dateMillis = sdf.parse(strDate)?.time

        val values = ContentValues()
        values.put(KEY_NOTE, trans.note)
        values.put(KEY_CATEGORY, trans.category)
        values.put(KEY_AMOUNT, trans.amount)
        values.put(KEY_ACCOUNT, trans.account)
        values.put(KEY_MONTH, trans.month)
        values.put(KEY_DAY, trans.day)
        values.put(KEY_YEAR, trans.year)
        values.put(KEY_DATE_MS, dateMillis)
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

    fun deleteTransactionDueToAccountDeletion(account: AccountModel) {
        val db = this.writableDatabase
        db.delete(TABLE_TRANSACTIONS, KEY_ACCOUNT + "=" + account.id, null)
        db.close()
    }

    fun changeTransactionCategoryDueToCategoryDeletion(category: CategoryModel) {
        val values = ContentValues()
        values.put(KEY_CATEGORY, 5)
        val db = this.writableDatabase
        db.update(TABLE_TRANSACTIONS, values, KEY_CATEGORY + "=" + category.id, null)
        db.close()
    }

    fun getBalanceForAccount(accountFilter: Int): String {
        val list = ArrayList<Double>()
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_TRANSACTIONS WHERE $KEY_ACCOUNT = $accountFilter",
            null
        )

        var amount: String
        var runningBalance = 0.00

        if (cursor.moveToFirst()) {
            do {
                amount = cursor.getString(cursor.getColumnIndex(KEY_AMOUNT))
                val freshAmount = amount.toDouble()
                list.add(freshAmount)
            } while (cursor.moveToNext())
        }

        for (item in list) {
            runningBalance += item
        }

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
        var runningBalance = 0.00

        if (cursor.moveToFirst()) {
            do {
                amount = cursor.getString(cursor.getColumnIndex(KEY_AMOUNT))
                val freshAmount = amount.toDouble()
                list.add(freshAmount)
            } while (cursor.moveToNext())
        }

        for (item in list) {
            runningBalance += item
        }

        val formatter: NumberFormat = DecimalFormat("#,##0.00")
        return formatter.format(runningBalance).toString()

    }

    fun filterTransactions(accountFilter: Int, sortBy: Int = 0): ArrayList<TransactionModel> {
        val list = ArrayList<TransactionModel>()
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_TRANSACTIONS WHERE $KEY_ACCOUNT = $accountFilter",
            null
        )

        var id: Int
        var category: Int
        var amount: String
        var account: Int
        var note: String
        var month: Int
        var day: Int
        var year: Int
        var dateMillis: String

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(KEY_ID))
                category = cursor.getInt(cursor.getColumnIndex(KEY_CATEGORY))
                amount = cursor.getString(cursor.getColumnIndex(KEY_AMOUNT))
                account = cursor.getInt(cursor.getColumnIndex(KEY_ACCOUNT))
                note = cursor.getString(cursor.getColumnIndex(KEY_NOTE))
                month = cursor.getInt(cursor.getColumnIndex(KEY_MONTH))
                day = cursor.getInt(cursor.getColumnIndex(KEY_DAY))
                year = cursor.getInt(cursor.getColumnIndex(KEY_YEAR))
                dateMillis = cursor.getString(cursor.getColumnIndex(KEY_DATE_MS))
                val transaction = TransactionModel(
                    id = id,
                    category = category,
                    amount = amount,
                    account = account,
                    note = note,
                    month = month,
                    day = day,
                    year = year,
                    dateMillis = dateMillis
                )
                list.add(transaction)
            } while (cursor.moveToNext())
        }

        if (sortBy == -1) {
            list.sortByDescending {
                it.dateMillis
            }
        }

        if (sortBy == 1) {
            list.sortBy {
                it.dateMillis
            }
        }

        return list


    }

    fun getTransactionTotalForCategoryMonthYear(categoryId: Int, month: Int, year: Int): Float {
        var amount: String
        var runningTotal = 0.00F
        val dbTrans = this.readableDatabase
        val cursor = dbTrans.rawQuery(
            "SELECT * FROM $TABLE_TRANSACTIONS WHERE $KEY_CATEGORY = $categoryId AND $KEY_MONTH = $month AND $KEY_YEAR = $year",
            null
        )

        if (cursor.moveToFirst()) {
            do {
                amount = cursor.getString(cursor.getColumnIndex(KEY_AMOUNT))
                runningTotal += amount.toFloat()
            } while (cursor.moveToNext())
        }

        return runningTotal

    }

    fun getTransactionTotalForCategoryDayMonthYear(
        categoryId: Int,
        day: Int,
        month: Int,
        year: Int
    ): Float {
        var amount: String
        var runningTotal = 0.00F
        val dbTrans = this.readableDatabase
        val cursor = dbTrans.rawQuery(
            "SELECT * FROM $TABLE_TRANSACTIONS WHERE $KEY_CATEGORY = $categoryId AND $KEY_DAY = $day AND $KEY_MONTH = $month AND $KEY_YEAR = $year",
            null
        )

        if (cursor.moveToFirst()) {
            do {
                amount = cursor.getString(cursor.getColumnIndex(KEY_AMOUNT))
                runningTotal += amount.toFloat()
            } while (cursor.moveToNext())
        }

        return runningTotal

    }

/*
    fun getTransactionsForCategory(categoryId: Int): ArrayList<Float> {
        var amount: String
        val list: ArrayList<Float> = ArrayList()
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_TRANSACTIONS WHERE $KEY_CATEGORY = $categoryId",
            null
        )

        if (cursor.moveToFirst()) {
            do {
                amount = cursor.getString(cursor.getColumnIndex(KEY_AMOUNT))
                list.add(amount.toFloat())
            } while (cursor.moveToNext())
        }

        return list

    }
*/

    fun getAllTransactions(): ArrayList<TransactionModel> {
        val list = ArrayList<TransactionModel>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_TRANSACTIONS", null)

        var id: Int
        var category: Int
        var amount: String
        var account: Int
        var note: String
        var month: Int
        var day: Int
        var year: Int
        var dateMillis: String

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(KEY_ID))
                category = cursor.getInt(cursor.getColumnIndex(KEY_CATEGORY))
                amount = cursor.getString(cursor.getColumnIndex(KEY_AMOUNT))
                account = cursor.getInt(cursor.getColumnIndex(KEY_ACCOUNT))
                note = cursor.getString(cursor.getColumnIndex(KEY_NOTE))
                month = cursor.getInt(cursor.getColumnIndex(KEY_MONTH))
                day = cursor.getInt(cursor.getColumnIndex(KEY_DAY))
                year = cursor.getInt(cursor.getColumnIndex(KEY_YEAR))
                dateMillis = cursor.getString(cursor.getColumnIndex(KEY_DATE_MS))
                val transaction = TransactionModel(
                    id = id,
                    category = category,
                    amount = amount,
                    account = account,
                    note = note,
                    month = month,
                    day = day,
                    year = year,
                    dateMillis = dateMillis
                )
                list.add(transaction)
            } while (cursor.moveToNext())
        }

        return list

    }

    fun resetOnImport() {
        val db = this.writableDatabase
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS")
        onCreate(db)
    }

}
