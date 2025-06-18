package com.beardedbiz.billddifferent

import android.app.AlertDialog
import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.beardedbiz.billddifferent.util.getCategoryIconResId

class TransactionAdapter(
    private var transactions: List<Transaction>,
    private var balances: Map<Int, Double>,
    private val onEdit: (Transaction) -> Unit,
    private val onDelete: (Transaction) -> Unit,
    private val onDeleteRecurring: (Transaction, DeleteMode) -> Unit,
    private val onPaidChecked: (Transaction, Boolean) -> Unit,
    private val onSkipInstance: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    enum class DeleteMode {
        THIS_ONLY, THIS_AND_FUTURE, ENTIRE_SERIES
    }

    private val displayFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        val context = holder.itemView.context
        val balance = balances[position] ?: 0.0

        holder.source.text = transaction.source
        holder.date.text = try {
            LocalDate.parse(transaction.date).format(displayFormatter)
        } catch (e: Exception) {
            transaction.date
        }
        holder.amount.text = String.format("$%.2f", transaction.amount)
        holder.balance.text = String.format("$%.2f", balance)

        holder.recurringIcon.visibility = if (transaction.isRecurring) View.VISIBLE else View.GONE
        holder.skippedIcon.visibility = if (transaction.isSkipped) View.VISIBLE else View.GONE

        val amountColor = if (transaction.amount < 0)
            ContextCompat.getColor(context, R.color.dark_red)
        else
            ContextCompat.getColor(context, R.color.dark_green)
        holder.amount.setTextColor(amountColor)

        holder.source.paintFlags = holder.source.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
        holder.amount.paintFlags = holder.amount.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()

        if (transaction.isSkipped) {
            holder.source.paintFlags = holder.source.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
            holder.amount.paintFlags = holder.amount.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
        }

        holder.checkBox.setOnCheckedChangeListener(null)
        holder.checkBox.isChecked = transaction.isPaid
        holder.layout.alpha = if (transaction.isPaid || transaction.isSkipped) 0.4f else 1.0f
        holder.checkBox.setOnCheckedChangeListener { _, isChecked ->
            transaction.isPaid = isChecked
            onPaidChecked(transaction, isChecked)
        }

        val backgroundColor = if (transaction.amount < 0)
            ContextCompat.getColor(context, R.color.light_red)
        else
            ContextCompat.getColor(context, R.color.light_green)
        holder.layout.setBackgroundColor(backgroundColor)

        holder.categoryIcon.setImageResource(getCategoryIconResId(transaction.category))

        holder.itemView.setOnClickListener { onEdit(transaction) }

        holder.itemView.setOnLongClickListener {
            if (transaction.isRecurring) {
                AlertDialog.Builder(context)
                    .setTitle("Recurring Transaction")
                    .setItems(arrayOf("Skip This Instance", "Delete This Only", "Delete This and Future", "Delete Entire Series")) { _, which ->
                        when (which) {
                            0 -> onSkipInstance(transaction)
                            1 -> onDeleteRecurring(transaction, DeleteMode.THIS_ONLY)
                            2 -> onDeleteRecurring(transaction, DeleteMode.THIS_AND_FUTURE)
                            3 -> onDeleteRecurring(transaction, DeleteMode.ENTIRE_SERIES)
                        }
                    }
                    .setNegativeButton("Cancel", null)
                    .show()
            } else {
                AlertDialog.Builder(context)
                    .setTitle("Delete Transaction")
                    .setMessage("Are you sure you want to delete this transaction?")
                    .setPositiveButton("Delete") { _, _ -> onDelete(transaction) }
                    .setNegativeButton("Cancel", null)
                    .show()
            }
            true
        }
    }

    override fun getItemCount(): Int = transactions.size

    fun updateData(newTransactions: List<Transaction>, newBalances: Map<Int, Double>) {
        transactions = newTransactions
        balances = newBalances
        notifyDataSetChanged()
    }

    inner class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layout: View = view.findViewById(R.id.transactionItemLayout)
        val recurringIcon: ImageView = view.findViewById(R.id.itemRecurringIcon)
        val skippedIcon: ImageView = view.findViewById(R.id.itemSkippedIcon)
        val categoryIcon: ImageView = view.findViewById(R.id.itemCategoryIcon)
        val source: TextView = view.findViewById(R.id.itemSource)
        val date: TextView = view.findViewById(R.id.itemDate)
        val amount: TextView = view.findViewById(R.id.itemAmount)
        val balance: TextView = view.findViewById(R.id.itemBalance)
        val checkBox: CheckBox = view.findViewById(R.id.itemPaidCheckbox)
    }
}
