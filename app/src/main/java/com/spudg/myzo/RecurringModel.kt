package com.spudg.myzo

class RecurringModel(
    val id: Int,
    val note: String,
    val category: Int,
    val amount: String,
    val account: Int,
    val nextMonth: Int,
    val nextOGDay: Int,
    val nextDay: Int,
    val nextYear: Int,
    val nextDateMillis: String,
    val frequency: String
)