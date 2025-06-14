package com.beardedbiz.billddifferent

data class MonthlyReportSummary(
    val month: String,
    val income: Double,
    val expenses: Double,
    val net: Double,
    val balance: Double
)
