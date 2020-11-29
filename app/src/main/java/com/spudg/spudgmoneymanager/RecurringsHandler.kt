package com.spudg.spudgmoneymanager

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class RecurringsHandler(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    companion object {

        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "SMMRecurringTemplates.db"
        private const val TABLE_RECURRINGS = "recurring_templates"

        private const val KEY_ID = "_id"
        private const val KEY_NOTE = "note"
        private const val KEY_CATEGORY = "category"
        private const val KEY_AMOUNT = "amount"
        private const val KEY_ACCOUNT = "account"
        private const val KEY_LAST_MONTH = "last_month"
        private const val KEY_LAST_DAY = "last_day"
        private const val KEY_LAST_YEAR = "last_year"
        private const val KEY_LAST_DATE_MS = "last_date_millis"
        private const val KEY_NEXT_MONTH = "next_month"
        private const val KEY_NEXT_DAY = "next_day"
        private const val KEY_NEXT_YEAR = "next_year"
        private const val KEY_NEXT_DATE_MS = "next_date_millis"

    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createTransactionsTable =
            ("CREATE TABLE $TABLE_RECURRINGS($KEY_ID INTEGER PRIMARY KEY,$KEY_NOTE TEXT,$KEY_CATEGORY INTEGER,$KEY_AMOUNT TEXT,$KEY_ACCOUNT INTEGER,$KEY_LAST_MONTH INTEGER,$KEY_LAST_DAY INTEGER,$KEY_LAST_YEAR INTEGER,$KEY_LAST_DATE_MS TEXT,$KEY_NEXT_MONTH INTEGER,$KEY_NEXT_DAY INTEGER,$KEY_NEXT_YEAR INTEGER,$KEY_NEXT_DATE_MS TEXT)")
        db?.execSQL(createTransactionsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
    }

    fun getAllRecurringTransactions(): ArrayList<RecurringModel> {
        val list = ArrayList<RecurringModel>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_RECURRINGS", null)

        var id: Int
        var category: Int
        var amount: String
        var account: Int
        var note: String
        var lastMonth: Int
        var lastDay: Int
        var lastYear: Int
        var lastDateMillis: String
        var nextMonth: Int
        var nextDay: Int
        var nextYear: Int
        var nextDateMillis: String

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(KEY_ID))
                category = cursor.getInt(cursor.getColumnIndex(KEY_CATEGORY))
                amount = cursor.getString(cursor.getColumnIndex(KEY_AMOUNT))
                account = cursor.getInt(cursor.getColumnIndex(KEY_ACCOUNT))
                note = cursor.getString(cursor.getColumnIndex(KEY_NOTE))
                nextMonth = cursor.getInt(cursor.getColumnIndex(KEY_NEXT_MONTH))
                nextDay = cursor.getInt(cursor.getColumnIndex(KEY_NEXT_DAY))
                nextYear = cursor.getInt(cursor.getColumnIndex(KEY_NEXT_YEAR))
                nextDateMillis = cursor.getString(cursor.getColumnIndex(KEY_NEXT_DATE_MS))
                lastMonth = cursor.getInt(cursor.getColumnIndex(KEY_LAST_MONTH))
                lastDay = cursor.getInt(cursor.getColumnIndex(KEY_LAST_DAY))
                lastYear = cursor.getInt(cursor.getColumnIndex(KEY_LAST_YEAR))
                lastDateMillis = cursor.getString(cursor.getColumnIndex(KEY_LAST_DATE_MS))
                val recurringTransaction = RecurringModel(
                    id = id,
                    category = category,
                    amount = amount,
                    account = account,
                    note = note,
                    lastMonth = lastMonth,
                    lastDay = lastDay,
                    lastYear = lastYear,
                    lastDateMillis = lastDateMillis,
                    nextMonth = nextMonth,
                    nextDay = nextDay,
                    nextYear = nextYear,
                    nextDateMillis = nextDateMillis
                )
                list.add(recurringTransaction)
            } while (cursor.moveToNext())
        }

        return list

    }

    fun addRecurringTransaction(recurringTrans: RecurringModel): Long {

        val strDate = "${recurringTrans.nextDay}-${recurringTrans.nextMonth}-${recurringTrans.nextYear}"
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val nextDateMillis = sdf.parse(strDate)?.time
        val lastDateMillis = "0"

        val values = ContentValues()
        values.put(KEY_NOTE, recurringTrans.note)
        values.put(KEY_CATEGORY, recurringTrans.category)
        values.put(KEY_AMOUNT, recurringTrans.amount)
        values.put(KEY_ACCOUNT, recurringTrans.account)
        values.put(KEY_LAST_MONTH, recurringTrans.lastMonth)
        values.put(KEY_LAST_DAY, recurringTrans.lastDay)
        values.put(KEY_LAST_YEAR, recurringTrans.lastYear)
        values.put(KEY_LAST_DATE_MS, lastDateMillis)
        values.put(KEY_NEXT_MONTH, recurringTrans.nextMonth)
        values.put(KEY_NEXT_DAY, recurringTrans.nextDay)
        values.put(KEY_NEXT_YEAR, recurringTrans.nextYear)
        values.put(KEY_NEXT_DATE_MS, nextDateMillis)
        val db = this.writableDatabase
        val success = db.insert(TABLE_RECURRINGS, null, values)
        db.close()
        return success
    }


    fun updateRecurringTransaction(recurringTrans: RecurringModel): Int {

        var calendar: Calendar = Calendar.getInstance()
        calendar.timeInMillis = recurringTrans.nextDateMillis.toLong()
        var nextMonth = calendar.get(Calendar.MONTH)
        var nextDay = calendar.get(Calendar.DAY_OF_MONTH)
        var nextYear = calendar.get(Calendar.YEAR)

        val values = ContentValues()
        values.put(KEY_NOTE, recurringTrans.note)
        values.put(KEY_CATEGORY, recurringTrans.category)
        values.put(KEY_AMOUNT, recurringTrans.amount)
        values.put(KEY_ACCOUNT, recurringTrans.account)
        values.put(KEY_LAST_MONTH, recurringTrans.lastMonth)
        values.put(KEY_LAST_DAY, recurringTrans.lastDay)
        values.put(KEY_LAST_YEAR, recurringTrans.lastYear)
        values.put(KEY_LAST_DATE_MS, recurringTrans.lastDateMillis)
        values.put(KEY_NEXT_MONTH, nextMonth)
        values.put(KEY_NEXT_DAY, nextDay)
        values.put(KEY_NEXT_YEAR, nextYear)
        values.put(KEY_NEXT_DATE_MS, recurringTrans.nextDateMillis)
        val db = this.writableDatabase
        val success = db.update(TABLE_RECURRINGS, values, KEY_ID + "=" + recurringTrans.id, null)
        db.close()
        return success
    }

    fun deleteRecurringTransaction(recurringTrans: RecurringModel): Int {
       val db = this.writableDatabase
       val success = db.delete(TABLE_RECURRINGS, KEY_ID + "=" + recurringTrans.id, null)
       db.close()
       return success
    }

    /*

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

    fun resetOnImport() {
       val db = this.writableDatabase
       db?.execSQL("DROP TABLE IF EXISTS $TABLE_TRANSACTIONS")
       onCreate(db)
    }

*/

}
