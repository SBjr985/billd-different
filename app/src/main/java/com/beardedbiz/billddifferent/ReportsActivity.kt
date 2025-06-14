package com.beardedbiz.billddifferent

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class ReportsActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var summaryAdapter: ReportsSummaryAdapter
    private lateinit var transactionList: MutableList<Transaction>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        recyclerView = findViewById(R.id.reportsRecyclerView)

        transactionList = getAllTransactions(this).toMutableList()
        transactionList.sortBy { it.date }

        val bankBalance = getSavedBankBalance()
        val summaries = generateMonthlySummaries(transactionList, bankBalance)

        summaryAdapter = ReportsSummaryAdapter(summaries)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = summaryAdapter

        setupBottomNavigation()
    }

    private fun getAllTransactions(context: Context): List<Transaction> {
        val prefs = context.getSharedPreferences("transactions", Context.MODE_PRIVATE)
        val json = prefs.getString("transactions", "[]")
        val type = object : TypeToken<List<Transaction>>() {}.type
        return Gson().fromJson(json, type)
    }

    private fun getSavedBankBalance(): Double {
        val prefs = getSharedPreferences("prefs", Context.MODE_PRIVATE)
        return prefs.getFloat("bank_balance", 0f).toDouble()
    }

    private fun generateMonthlySummaries(transactions: List<Transaction>, startingBalance: Double): List<MonthlyReportSummary> {
        val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
        val grouped = transactions.groupBy { it.date.substring(0, 7) }

        var runningBalance = startingBalance
        val result = mutableListOf<MonthlyReportSummary>()

        grouped.toSortedMap().forEach { (month, txList) ->
            var income = 0.0
            var expense = 0.0

            txList.forEach { tx ->
                if (tx.amount >= 0) income += tx.amount
                else expense += tx.amount
            }

            val net = income + expense
            runningBalance += net

            val formattedMonth = try {
                val parsedDate = sdf.parse(month)
                SimpleDateFormat("MMMM yyyy", Locale.US).format(parsedDate!!)
            } catch (e: Exception) {
                month
            }

            result.add(
                MonthlyReportSummary(
                    month = formattedMonth,
                    income = income,
                    expenses = expense,
                    net = net,
                    balance = runningBalance
                )
            )
        }

        return result
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)
        bottomNav.selectedItemId = R.id.nav_reports

        bottomNav.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.nav_current -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.nav_planning -> {
                    startActivity(Intent(this, PlanningActivity::class.java))
                    true
                }
                R.id.nav_reports -> true
                else -> false
            }
        }
    }
}
