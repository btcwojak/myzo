package com.spudg.spudgmoneymanager


class TransactionModel(
    val id: Int,
    val note: String,
    val category: Int,
    val amount: String,
    val account: Int,
    val month: Int,
    val day: Int,
    val year: Int
)