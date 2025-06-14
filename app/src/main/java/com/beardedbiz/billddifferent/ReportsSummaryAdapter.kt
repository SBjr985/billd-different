package com.beardedbiz.billddifferent

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ReportsSummaryAdapter(
    private val reports: List<MonthlyReportSummary>
) : RecyclerView.Adapter<ReportsSummaryAdapter.ReportViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReportViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_monthly_summary, parent, false)
        return ReportViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReportViewHolder, position: Int) {
        val report = reports[position]
        holder.monthText.text = report.month
        holder.incomeText.text = "Income: $%.2f".format(report.income)
        holder.expenseText.text = "Expenses: $%.2f".format(report.expenses)
        holder.netText.text = "Net: $%.2f".format(report.net)
        holder.balanceText.text = "Balance: $%.2f".format(report.balance)
    }

    override fun getItemCount(): Int = reports.size

    inner class ReportViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val monthText: TextView = view.findViewById(R.id.summaryMonth)
        val incomeText: TextView = view.findViewById(R.id.summaryIncome)
        val expenseText: TextView = view.findViewById(R.id.summaryExpenses)
        val netText: TextView = view.findViewById(R.id.summaryNet)
        val balanceText: TextView = view.findViewById(R.id.summaryBalance)
    }
}
