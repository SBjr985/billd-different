package com.beardedbiz.billddifferent

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*
import com.beardedbiz.billddifferent.PlanningActivity

class HomeActivity : AppCompatActivity() {

    private lateinit var editBankBalance: EditText
    private lateinit var saveBalanceButton: Button
    private lateinit var recyclerView: RecyclerView
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var fullTransactionList: MutableList<Transaction>
    private lateinit var filteredTransactionList: MutableList<Transaction>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        editBankBalance = findViewById(R.id.editBankBalance)
        saveBalanceButton = findViewById(R.id.saveBalanceButton)
        recyclerView = findViewById(R.id.transactionRecycler)

        fullTransactionList = getAllTransactions(this).toMutableList()
        fullTransactionList.sortBy { it.date }

        loadSavedBankBalance()
        filterToCurrentPayPeriod()
        setupRecycler()

        saveBalanceButton.setOnClickListener {
            saveBankBalance()
            filterToCurrentPayPeriod()
            refreshRecycler()
        }

        setupBottomNavigation()
    }

    private fun setupRecycler() {
        val currentBalance = getCurrentBalance()
        val balanceMap = calculateRunningBalanceMap(filteredTransactionList, currentBalance)

        transactionAdapter = TransactionAdapter(
            transactions = filteredTransactionList,
            balances = balanceMap,
            onEdit = { /* optional future logic */ },
            onDelete = { transaction ->
                fullTransactionList.remove(transaction)
                saveAllTransactions(fullTransactionList)
                filterToCurrentPayPeriod()
                refreshRecycler()
            },
            onPaidChecked = { updatedTransaction, isChecked ->
                updatedTransaction.isPaid = isChecked
                saveAllTransactions(fullTransactionList)
                filterToCurrentPayPeriod()
                refreshRecycler()
            },
            onDeleteRecurring = { _, _ -> }, // Safe default
            onSkipInstance = { /* Not used in this screen */ } // ✅ Added to fix build error
        )

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = transactionAdapter
    }

    private fun refreshRecycler() {
        val currentBalance = getCurrentBalance()
        val balanceMap = calculateRunningBalanceMap(filteredTransactionList, currentBalance)
        transactionAdapter.updateData(filteredTransactionList, balanceMap)
    }

    private fun loadSavedBankBalance() {
        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        val balance = prefs.getFloat("bank_balance", 0f)
        editBankBalance.setText("%.2f".format(balance))
    }

    private fun saveBankBalance() {
        val rawInput = editBankBalance.text.toString().replace("$", "").trim()
        val balance = rawInput.toFloatOrNull() ?: 0f
        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE).edit()
        prefs.putFloat("bank_balance", balance)
        prefs.apply()
    }

    private fun getCurrentBalance(): Double {
        val rawInput = editBankBalance.text.toString().replace("$", "").trim()
        return rawInput.toDoubleOrNull() ?: 0.0
    }

    private fun getAllTransactions(context: Context): List<Transaction> {
        val prefs = context.getSharedPreferences("transactions", Context.MODE_PRIVATE)
        val json = prefs.getString("transactions", "[]")
        val type = object : TypeToken<List<Transaction>>() {}.type
        return Gson().fromJson(json, type)
    }

    private fun saveAllTransactions(transactions: List<Transaction>) {
        val prefs = getSharedPreferences("transactions", Context.MODE_PRIVATE).edit()
        val json = Gson().toJson(transactions)
        prefs.putString("transactions", json)
        prefs.apply()
    }

    private fun calculateRunningBalanceMap(transactions: List<Transaction>, startingBalance: Double): Map<Int, Double> {
        val result = mutableMapOf<Int, Double>()
        var running = startingBalance
        transactions.sortedBy { it.date }.forEach {
            if (!it.isPaid) {
                running += it.amount
                result[it.id] = running
            } else {
                result[it.id] = running
            }
        }
        return result
    }

    private fun filterToCurrentPayPeriod() {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val today = Date()

        val safeParse: (String?) -> Date? = { dateString ->
            try {
                if (dateString.isNullOrBlank()) null else sdf.parse(dateString)
            } catch (e: Exception) {
                null
            }
        }

        val paychecks = fullTransactionList.filter { it.isPayroll && !it.date.isNullOrBlank() }
            .sortedBy { safeParse(it.date) }

        val mostRecent = paychecks.lastOrNull { safeParse(it.date)?.before(today) == true }
        val next = paychecks.firstOrNull { safeParse(it.date)?.after(today) == true }

        val startDate = safeParse(mostRecent?.date) ?: sdf.parse("1900-01-01")
        val endDate = safeParse(next?.date) ?: Date(Long.MAX_VALUE)

        filteredTransactionList = fullTransactionList.filter {
            val txDate = safeParse(it.date)
            txDate != null &&
                    !txDate.before(startDate) &&
                    txDate.before(endDate) &&
                    !it.isSkipped // ✅ Filter out skipped transactions
        }.toMutableList()
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_current

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_current -> true
                R.id.nav_planning -> {
                    startActivity(Intent(this, PlanningActivity::class.java))
                    true
                }
                R.id.nav_reports -> {
                    startActivity(Intent(this, ReportsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }
}
