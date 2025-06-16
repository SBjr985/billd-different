
package com.beardedbiz.billddifferent.util

import android.content.Context
import com.beardedbiz.billddifferent.Transaction
import com.beardedbiz.billddifferent.TransactionDisplay
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

object TransactionUtils {
    private const val FILE_NAME = "transactions.json"

    fun getAllTransactions(context: Context): List<Transaction> {
        val file = File(context.filesDir, FILE_NAME)
        if (!file.exists()) return emptyList()
        val json = file.readText()
        val type = object : TypeToken<List<Transaction>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun saveAllTransactions(context: Context, transactions: List<Transaction>) {
        val file = File(context.filesDir, FILE_NAME)
        val json = Gson().toJson(transactions)
        file.writeText(json)
    }

    fun getSavedBankBalance(context: Context, accountId: String): Double {
        val prefs = context.getSharedPreferences("bank_balance", Context.MODE_PRIVATE)
        return prefs.getFloat(accountId, 0f).toDouble()
    }

    fun saveBankBalance(context: Context, accountId: String, balance: Double) {
        val prefs = context.getSharedPreferences("bank_balance", Context.MODE_PRIVATE)
        prefs.edit().putFloat(accountId, balance.toFloat()).apply()
    }

    fun calculateRunningBalanceList(transactions: List<Transaction>, startingBalance: Double): List<TransactionDisplay> {
        val sortedTransactions = transactions.sortedBy { it.date }
        val runningList = mutableListOf<TransactionDisplay>()
        var balance = startingBalance
        for (transaction in sortedTransactions) {
            if (!transaction.isPaid && !transaction.isSkipped) {
                balance += transaction.amount
            }
            runningList.add(TransactionDisplay(transaction, balance))
        }
        return runningList
    }
}