package com.spudg.spudgmoneymanager

class RecurringModel(
    val id: Int,
    val note: String,
    val category: Int,
    val amount: String,
    val account: Int,
    val lastMonth: Int,
    val lastDay: Int,
    val lastYear: Int,
    val lastDateMillis: String,
    val nextMonth: Int,
    val nextDay: Int,
    val nextYear: Int,
    val nextDateMillis: String
)