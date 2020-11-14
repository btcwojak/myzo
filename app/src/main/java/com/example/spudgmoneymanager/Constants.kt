package com.example.spudgmoneymanager

class Constants {

    companion object {
        var CURRENT_ACCOUNT: Int = 1
        var CAT_COL_SELECTED: Int = 0
        var CAT_UNIQUE_TITLE: Int = 0

        fun getShortMonth(month: Int): String {
            return when (month) {
                1 -> "Jan"
                2 -> "Feb"
                3 -> "Mar"
                4 -> "Apr"
                5 -> "May"
                6 -> "Jun"
                7 -> "Jul"
                8 -> "Aug"
                9 -> "Sep"
                10 -> "Oct"
                11 -> "Nov"
                12 -> "Dec"
                else -> "Error"
            }
        }

    }

}