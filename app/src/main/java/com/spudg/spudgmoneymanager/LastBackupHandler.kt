package com.spudg.spudgmoneymanager

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class LastBackupHandler(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 2
        private const val DATABASE_NAME = "SMMLastBackupDate.db"
        private const val TABLE_LAST_BACKUP = "last_backup_date"

        private const val KEY_ID = "_id"
        private const val KEY_DATE_MS = "last_backup_date"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createAccountsTable =
            ("CREATE TABLE $TABLE_LAST_BACKUP($KEY_ID INTEGER PRIMARY KEY,$KEY_DATE_MS TEXT)")
        db?.execSQL(createAccountsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_LAST_BACKUP")
        onCreate(db)
    }

    fun addBackupDate(date: String) {
        val values = ContentValues()
        values.put(KEY_ID, 1)
        values.put(KEY_DATE_MS, date)

        val db = this.writableDatabase

        if (!this.dateExists()) {
            db.insert(TABLE_LAST_BACKUP, null, values)
        } else {
            db.update(TABLE_LAST_BACKUP, values, "$KEY_ID=1", null)
        }

        db.close()

    }

    fun getBackupDate(): String {
        val db = this.writableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_LAST_BACKUP",
            null
        )
        var date = ""

        if (cursor.moveToFirst()) {
            do {
                date = cursor.getString(cursor.getColumnIndex(KEY_DATE_MS))
            } while (cursor.moveToNext())
        }

        cursor.close()
        db.close()
        return date

    }

    private fun dateExists(): Boolean {
        val db = this.writableDatabase
        val cursor = db.rawQuery(
            "SELECT * FROM $TABLE_LAST_BACKUP",
            null
        )
        val exists = cursor.moveToFirst()

        cursor.close()

        return exists


    }

}