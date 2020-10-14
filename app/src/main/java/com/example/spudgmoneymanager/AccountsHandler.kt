package com.example.spudgmoneymanager

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AccountsHandler(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 1
        private const val DATABASE_NAME = "SMMAccounts.db"
        private const val TABLE_ACCOUNTS = "accounts"

        private const val KEY_ID = "_id"
        private const val KEY_NAME = "category"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_ACCOUNTS_TABLE =
            ("CREATE TABLE $TABLE_ACCOUNTS($KEY_ID INTEGER PRIMARY KEY,$KEY_NAME TEXT)")
        db?.execSQL(CREATE_ACCOUNTS_TABLE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_ACCOUNTS")
        onCreate(db)
    }

    fun addAccount(trans: AccountModel): Long {
        val values = ContentValues()
        values.put(KEY_NAME, trans.name)
        val db = this.writableDatabase
        val success = db.insert(TABLE_ACCOUNTS, null, values)
        db.close()
        return success
    }

    fun getAllAccounts(): ArrayList<AccountModel> {
        val list = ArrayList<AccountModel>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_ACCOUNTS", null)

        var id: Int
        var name: String

        if (cursor.moveToFirst()) {
            do {
                id = cursor.getInt(cursor.getColumnIndex(KEY_ID))
                name = cursor.getString(cursor.getColumnIndex(KEY_NAME))
                val account = AccountModel(id = id, name = name)
                list.add(account)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return list

    }

    fun getAccountName(accountId: Int): String? {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_ACCOUNTS WHERE $KEY_ID = $accountId", null)

        if (cursor.moveToFirst()) {
            return cursor.getString(cursor.getColumnIndex(KEY_NAME));
        } else {
            return "Error"
        }


    }


}