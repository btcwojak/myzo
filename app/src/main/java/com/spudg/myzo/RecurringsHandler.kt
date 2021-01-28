package com.spudg.myzo

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.*


class RecurringsHandler(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 3
        private const val DATABASE_NAME = "SMMRecurrings.db"
        private const val TABLE_RECURRINGS = "recurrings"

        private const val KEY_ID = "_id"
        private const val KEY_NOTE = "note"
        private const val KEY_CATEGORY = "category"
        private const val KEY_AMOUNT = "amount"
        private const val KEY_ACCOUNT = "account"
        private const val KEY_NEXT_MONTH = "next_month"
        private const val KEY_NEXT_OG_DAY = "original_next_day"
        private const val KEY_NEXT_DAY = "next_day"
        private const val KEY_NEXT_YEAR = "next_year"
        private const val KEY_NEXT_DATE_MS = "next_date_millis"
        private const val KEY_FREQUENCY = "frequency"
    }


    override fun onCreate(db: SQLiteDatabase?) {
        val createRecurringsTable =
            ("CREATE TABLE $TABLE_RECURRINGS($KEY_ID INTEGER PRIMARY KEY,$KEY_NOTE TEXT,$KEY_CATEGORY INTEGER,$KEY_AMOUNT TEXT,$KEY_ACCOUNT INTEGER,$KEY_NEXT_MONTH INTEGER,$KEY_NEXT_OG_DAY INTEGER,$KEY_NEXT_DAY INTEGER,$KEY_NEXT_YEAR INTEGER,$KEY_NEXT_DATE_MS TEXT,$KEY_FREQUENCY TEXT)")
        db?.execSQL(createRecurringsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_RECURRINGS")
        onCreate(db)
    }

    fun filterRecurrings(sortBy: Int = 0): ArrayList<RecurringModel> {
        val list = ArrayList<RecurringModel>()
        val db = this.readableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_RECURRINGS",
            null
        )

        var id: Int
        var category: Int
        var amount: String
        var account: Int
        var note: String
        var nextMonth: Int
        var nextOGDay: Int
        var nextDay: Int
        var nextYear: Int
        var nextDateMillis: String
        var frequency: String

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(KEY_ID))
                category = cursor.getInt(cursor.getColumnIndex(KEY_CATEGORY))
                amount = cursor.getString(cursor.getColumnIndex(KEY_AMOUNT))
                account = cursor.getInt(cursor.getColumnIndex(KEY_ACCOUNT))
                note = cursor.getString(cursor.getColumnIndex(KEY_NOTE))
                nextMonth = cursor.getInt(cursor.getColumnIndex(KEY_NEXT_MONTH))
                nextOGDay = cursor.getInt(cursor.getColumnIndex(KEY_NEXT_OG_DAY))
                nextDay = cursor.getInt(cursor.getColumnIndex(KEY_NEXT_DAY))
                nextYear = cursor.getInt(cursor.getColumnIndex(KEY_NEXT_YEAR))
                nextDateMillis = cursor.getString(cursor.getColumnIndex(KEY_NEXT_DATE_MS))
                frequency = cursor.getString(cursor.getColumnIndex(KEY_FREQUENCY))
                val recurring = RecurringModel(
                    id = id,
                    category = category,
                    amount = amount,
                    account = account,
                    note = note,
                    nextMonth = nextMonth,
                    nextOGDay = nextOGDay,
                    nextDay = nextDay,
                    nextYear = nextYear,
                    nextDateMillis = nextDateMillis,
                    frequency = frequency
                )
                list.add(recurring)
            } while (cursor.moveToNext())
        }

        if (sortBy == -1) {
            list.sortByDescending {
                it.nextDateMillis
            }
        }

        if (sortBy == 1) {
            list.sortBy {
                it.nextDateMillis
            }
        }

        cursor.close()
        db.close()

        return list

    }

    fun addRecurring(recurring: RecurringModel): Long {
        val strDate = "${recurring.nextDay}-${recurring.nextMonth}-${recurring.nextYear}"
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val nextDateMillis = sdf.parse(strDate)?.time

        val values = ContentValues()
        values.put(KEY_NOTE, recurring.note)
        values.put(KEY_CATEGORY, recurring.category)
        values.put(KEY_AMOUNT, recurring.amount)
        values.put(KEY_ACCOUNT, recurring.account)
        values.put(KEY_NEXT_MONTH, recurring.nextMonth)
        values.put(KEY_NEXT_OG_DAY, recurring.nextOGDay)
        values.put(KEY_NEXT_DAY, recurring.nextDay)
        values.put(KEY_NEXT_YEAR, recurring.nextYear)
        values.put(KEY_NEXT_DATE_MS, nextDateMillis)
        values.put(KEY_FREQUENCY, recurring.frequency)
        val db = this.writableDatabase
        val success = db.insert(TABLE_RECURRINGS, null, values)
        db.close()
        return success
    }

    fun updateRecurring(recurring: RecurringModel): Int {
        val strDate = "${recurring.nextDay}-${recurring.nextMonth}-${recurring.nextYear}"
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        val nextDateMillis = sdf.parse(strDate)?.time

        val values = ContentValues()
        values.put(KEY_NOTE, recurring.note)
        values.put(KEY_CATEGORY, recurring.category)
        values.put(KEY_AMOUNT, recurring.amount)
        values.put(KEY_ACCOUNT, recurring.account)
        values.put(KEY_NEXT_MONTH, recurring.nextMonth)
        values.put(KEY_NEXT_OG_DAY, recurring.nextOGDay)
        values.put(KEY_NEXT_DAY, recurring.nextDay)
        values.put(KEY_NEXT_YEAR, recurring.nextYear)
        values.put(KEY_NEXT_DATE_MS, nextDateMillis)
        values.put(KEY_FREQUENCY, recurring.frequency)
        val db = this.writableDatabase
        val success = db.update(TABLE_RECURRINGS, values, KEY_ID + "=" + recurring.id, null)
        db.close()
        return success
    }

    fun deleteRecurring(recurring: RecurringModel): Int {
        val db = this.writableDatabase
        val success = db.delete(TABLE_RECURRINGS, KEY_ID + "=" + recurring.id, null)
        db.close()
        return success
    }

    fun getAllRecurrings(): ArrayList<RecurringModel> {
        val list = ArrayList<RecurringModel>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_RECURRINGS", null)

        var id: Int
        var category: Int
        var amount: String
        var account: Int
        var note: String
        var nextMonth: Int
        var nextOGDay: Int
        var nextDay: Int
        var nextYear: Int
        var nextDateMillis: String
        var frequency: String

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(KEY_ID))
                category = cursor.getInt(cursor.getColumnIndex(KEY_CATEGORY))
                amount = cursor.getString(cursor.getColumnIndex(KEY_AMOUNT))
                account = cursor.getInt(cursor.getColumnIndex(KEY_ACCOUNT))
                note = cursor.getString(cursor.getColumnIndex(KEY_NOTE))
                nextMonth = cursor.getInt(cursor.getColumnIndex(KEY_NEXT_MONTH))
                nextOGDay = cursor.getInt(cursor.getColumnIndex(KEY_NEXT_OG_DAY))
                nextDay = cursor.getInt(cursor.getColumnIndex(KEY_NEXT_DAY))
                nextYear = cursor.getInt(cursor.getColumnIndex(KEY_NEXT_YEAR))
                nextDateMillis = cursor.getString(cursor.getColumnIndex(KEY_NEXT_DATE_MS))
                frequency = cursor.getString(cursor.getColumnIndex(KEY_FREQUENCY))

                val recurring = RecurringModel(
                    id = id,
                    category = category,
                    amount = amount,
                    account = account,
                    note = note,
                    nextMonth = nextMonth,
                    nextOGDay = nextOGDay,
                    nextDay = nextDay,
                    nextYear = nextYear,
                    nextDateMillis = nextDateMillis,
                    frequency = frequency
                )
                list.add(recurring)
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()

        return list

    }

    fun resetOnImport() {
        val db = this.writableDatabase
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_RECURRINGS")
        onCreate(db)
    }


}