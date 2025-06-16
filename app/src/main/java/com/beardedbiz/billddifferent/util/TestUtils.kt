package com.beardedbiz.billddifferent

object TestUtils {
    fun filterPaidTransactions(transactions: List<Transaction>): List<Transaction> {
        return transactions.filter { it.isPaid }
    }
}