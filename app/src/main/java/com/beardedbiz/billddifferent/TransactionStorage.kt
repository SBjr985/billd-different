package com.beardedbiz.billddifferent

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object TransactionStorage {

    fun getAllTransactions(context: Context): List<Transaction> {
        val prefs = context.getSharedPreferences("transactions", Context.MODE_PRIVATE)
        val json = prefs.getString("transactions", "[]")
        val type = object : TypeToken<List<Transaction>>() {}.type
        return Gson().fromJson(json, type)
    }

    fun getTransactionsForAccount(context: Context, accountName: String): List<Transaction> {
        return getAllTransactions(context).filter { it.accountName == accountName }
    }

    fun saveAllTransactions(context: Context, transactions: List<Transaction>) {
        val prefs = context.getSharedPreferences("transactions", Context.MODE_PRIVATE).edit()
        val json = Gson().toJson(transactions)
        prefs.putString("transactions", json)
        prefs.apply()
    }

    fun saveTransactionsForAccount(context: Context, accountName: String, newTransactions: List<Transaction>) {
        val all = getAllTransactions(context).filter { it.accountName != accountName } + newTransactions
        saveAllTransactions(context, all)
    }
}
