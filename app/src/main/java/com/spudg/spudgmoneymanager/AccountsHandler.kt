package com.spudg.spudgmoneymanager

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class AccountsHandler(context: Context, factory: SQLiteDatabase.CursorFactory?) :
    SQLiteOpenHelper(context, DATABASE_NAME, factory, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_VERSION = 2
        private const val DATABASE_NAME = "SMMAccounts.db"
        private const val TABLE_ACCOUNTS = "accounts"

        private const val KEY_ID = "_id"
        private const val KEY_NAME = "name"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val createAccountsTable =
            ("CREATE TABLE $TABLE_ACCOUNTS($KEY_ID INTEGER PRIMARY KEY,$KEY_NAME TEXT)")
        db?.execSQL(createAccountsTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_ACCOUNTS")
        onCreate(db)
    }

    fun addAccount(account: AccountModel) {
        val values = ContentValues()
        values.put(KEY_NAME, account.name)
        val db = this.writableDatabase

        val existingNames = getAllAccountNames()
        var alreadyExists = false
        for (name in existingNames) {
            if (name == account.name) {
                alreadyExists = true
            }
        }

        if (!alreadyExists) {
            db.insert(TABLE_ACCOUNTS, null, values)
            db.close()
            Constants.CAT_UNIQUE_TITLE = 1
        } else {
            Constants.CAT_UNIQUE_TITLE = 0
        }

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

    private fun getAllAccountNames(): ArrayList<String> {
        val list = ArrayList<String>()
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_ACCOUNTS", null)

        var name: String

        if (cursor.moveToFirst()) {
            do {
                name = cursor.getString(cursor.getColumnIndex(KEY_NAME)).toString()
                list.add(name)
            } while (cursor.moveToNext())
        }

        cursor.close()
        return list

    }

    fun getAccountName(accountId: Int): String {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $TABLE_ACCOUNTS WHERE $KEY_ID = $accountId", null)

        val name: String

        name = if (cursor.moveToFirst()) {
            cursor.getString(cursor.getColumnIndex(KEY_NAME))
        } else {
            "Error"
        }

        cursor.close()
        db.close()

        return name

    }

    fun updateAccount(account: AccountModel) {
        val values = ContentValues()
        values.put(KEY_NAME, account.name)
        val dbForSearch = this.readableDatabase
        val dbForUpdate = this.writableDatabase

        val existingNames = getAllAccountNames()

        if (existingNames.contains(account.name)) {
            Constants.CAT_UNIQUE_TITLE = 0
        } else {
            dbForUpdate.update(TABLE_ACCOUNTS, values, KEY_ID + "=" + account.id, null)
            Constants.CAT_UNIQUE_TITLE = 1
        }

        if (Constants.CAT_UNIQUE_TITLE == 0) {
            val cursor =
                dbForSearch.rawQuery(
                    "SELECT * FROM $TABLE_ACCOUNTS WHERE _id = ${account.id}",
                    null
                )
            if (cursor.moveToFirst()) {
                val oldName = cursor.getString(cursor.getColumnIndex(KEY_NAME))
                val newName = account.name
                if (oldName == newName) {
                    dbForUpdate.update(
                        TABLE_ACCOUNTS,
                        values,
                        KEY_ID + "=" + account.id,
                        null
                    )
                    Constants.CAT_UNIQUE_TITLE = 1
                } else {
                    Constants.CAT_UNIQUE_TITLE = 0
                }
            }

            cursor.close()
            dbForSearch.close()
            dbForUpdate.close()

        }
    }

    fun deleteAccount(account: AccountModel): Int {
        val db = this.writableDatabase
        val success = db.delete(TABLE_ACCOUNTS, KEY_ID + "=" + account.id, null)
        db.close()
        return success
    }

    fun resetOnImport() {
        val db = this.writableDatabase
        db?.execSQL("DROP TABLE IF EXISTS $TABLE_ACCOUNTS")
        onCreate(db)
    }


}