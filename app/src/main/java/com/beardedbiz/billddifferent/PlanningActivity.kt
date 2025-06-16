package com.beardedbiz.billddifferent

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class PlanningActivity : AppCompatActivity() {

    private lateinit var accountSpinner: Spinner
    private lateinit var recyclerView: RecyclerView
    private lateinit var transactionAdapter: TransactionAdapter
    private lateinit var transactionList: MutableList<Transaction>
    private lateinit var balanceTextView: TextView
    private lateinit var lowestBalanceTextView: TextView
    private lateinit var togglePaidButton: Button
    private var currentBalance: Double = 0.0
    private var showPaidAndSkipped = true
    private var currentAccount: String = "Default"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_planning)

        accountSpinner = findViewById(R.id.accountSpinner)
        recyclerView = findViewById(R.id.transactionRecycler)
        balanceTextView = findViewById(R.id.bankBalanceInput)
        lowestBalanceTextView = findViewById(R.id.lowestBalanceTextView)
        togglePaidButton = findViewById(R.id.togglePaidButton)

        transactionList = mutableListOf()
        recyclerView.layoutManager = LinearLayoutManager(this)

        val bankBalanceInput = findViewById<EditText>(R.id.bankBalanceInput)
        bankBalanceInput.setText(String.format("%.2f", currentBalance))
        bankBalanceInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val enteredText = bankBalanceInput.text.toString().replace("$", "").trim()
                val newBalance = enteredText.toDoubleOrNull()
                if (newBalance != null && newBalance != currentBalance) {
                    currentBalance = newBalance
                    saveBankBalance(currentBalance)
                    refreshList()
                }
            }
        }
        bankBalanceInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val enteredText = bankBalanceInput.text.toString().replace("$", "").trim()
                val newBalance = enteredText.toDoubleOrNull()
                if (newBalance != null && newBalance != currentBalance) {
                    currentBalance = newBalance
                    saveBankBalance(currentBalance)
                    refreshList()
                }
            }
        }

        setupAccountSpinner()
    }


    private fun setupAccountSpinner() {
        val accounts = getAllAccounts().toMutableList()
        if (!accounts.contains("Default")) accounts.add(0, "Default")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, accounts + listOf("+ Add Account"))
        accountSpinner.adapter = adapter

        accountSpinner.setSelection(accounts.indexOf(currentAccount))

        accountSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selected = parent.getItemAtPosition(position).toString()
                if (selected == "+ Add Account") {
                    promptAddAccount()
                } else {
                    currentAccount = selected
                    loadAccountData()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun promptAddAccount() {
        val input = EditText(this)
        input.hint = "Enter new account name"
        AlertDialog.Builder(this)
            .setTitle("New Account")
            .setView(input)
            .setPositiveButton("Add") { _, _ ->
                val name = input.text.toString().trim()
                if (name.isNotBlank()) {
                    saveNewAccount(name)
                    currentAccount = name
                    val bankBalanceInput = findViewById<EditText>(R.id.bankBalanceInput)
                    bankBalanceInput.setOnFocusChangeListener { _, hasFocus ->
                        if (!hasFocus) {
                            val enteredText = bankBalanceInput.text.toString().replace("$", "").trim()
                            val newBalance = enteredText.toDoubleOrNull()
                            if (newBalance != null && newBalance != currentBalance) {
                                currentBalance = newBalance
                                saveBankBalance(currentBalance)
                                refreshList()
                            }
                        }
                    }

                    setupAccountSpinner()
                    loadAccountData()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun getAllAccounts(): Set<String> {
        val prefs = getSharedPreferences("accounts", Context.MODE_PRIVATE)
        return prefs.getStringSet("account_names", setOf("Default")) ?: setOf("Default")
    }

    private fun saveNewAccount(name: String) {
        val prefs = getSharedPreferences("accounts", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        val current = prefs.getStringSet("account_names", mutableSetOf())?.toMutableSet() ?: mutableSetOf()
        current.add(name)
        editor.putStringSet("account_names", current)
        editor.apply()
    }

    private fun loadAccountData() {
        val formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")
        transactionList = getAllTransactions(this)
            .sortedBy { LocalDate.parse(it.date, formatter) }
            .toMutableList()

        currentBalance = getSavedBankBalance()
        updateBankBalanceText(currentBalance)

        setupAdapter()

        findViewById<Button>(R.id.addNewButton).setOnClickListener {
            AddTransactionDialog(
                context = this,
                onSave = { newTransactions ->
                    transactionList.addAll(newTransactions)
                    transactionList.sortWith(compareBy<Transaction> {
                        LocalDate.parse(it.date, DateTimeFormatter.ofPattern("MM/dd/yyyy"))
                    }.thenByDescending { it.amount })
                    saveAllTransactions(transactionList)
                    refreshList()
                },
                onReplaceExisting = { original, newList ->
                    val fromDate = original.date
                    transactionList.removeAll {
                        it.isRecurring &&
                                it.source == original.source &&
                                it.category == original.category &&
                                it.frequency == original.frequency &&
                                it.date >= fromDate
                    }
                    transactionList.addAll(newList)
                    transactionList.sortWith(compareBy<Transaction> {
                        LocalDate.parse(it.date, DateTimeFormatter.ofPattern("MM/dd/yyyy"))
                    }.thenByDescending { it.amount })
                    saveAllTransactions(transactionList)
                    refreshList()
                }
            ).show()
        }

        togglePaidButton.setOnClickListener {
            showPaidAndSkipped = !showPaidAndSkipped
            togglePaidButton.text = if (showPaidAndSkipped) "Hide Paid & Skipped" else "Show Paid & Skipped"
            refreshList()
        }

        findViewById<ImageButton>(R.id.filterSortButton).setOnClickListener {
            val sortOptions = arrayOf("Date", "Amount", "Source")
            val filterOptions = arrayOf("All", "Paid Only", "Unpaid Only")
            val renameButton = findViewById<ImageButton>(R.id.manageAccountButton)
            val addButton = findViewById<ImageButton>(R.id.addAccountButton)
            val dialogView = layoutInflater.inflate(R.layout.dialog_filter_sort, null)
            val sortSpinner = dialogView.findViewById<Spinner>(R.id.sortSpinner)
            val filterSpinner = dialogView.findViewById<Spinner>(R.id.filterSpinner)

            sortSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, sortOptions)
            filterSpinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, filterOptions)

            AlertDialog.Builder(this)
                .setTitle("Sort & Filter Transactions")
                .setView(dialogView)
                .setPositiveButton("Apply") { _, _ ->
                    val selectedSort = sortSpinner.selectedItemPosition
                    val selectedFilter = filterSpinner.selectedItemPosition
                    applySortAndFilter(selectedSort, selectedFilter)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        findViewById<ImageView>(R.id.balanceInfoIcon).setOnClickListener {
            startActivity(Intent(this, HomeActivity::class.java))
        }

        findViewById<ImageButton>(R.id.manageAccountButton).setOnClickListener {
            showAccountMenu()
        }

        updateLowestBalanceDisplay(transactionList, calculateRunningBalanceList(transactionList, currentBalance))
        setupBottomNavigation()
    }

    private fun showAccountMenu() {
        val options = arrayOf("Rename Account", "Delete Account")
        AlertDialog.Builder(this)
            .setTitle("Manage Account: $currentAccount")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> promptRenameAccount()
                    1 -> promptDeleteAccount()
                }
            }
            .show()
    }

    private fun promptRenameAccount() {
        val input = EditText(this)
        input.setText(currentAccount)
        AlertDialog.Builder(this)
            .setTitle("Rename Account")
            .setView(input)
            .setPositiveButton("Rename") { _, _ ->
                val newName = input.text.toString().trim()
                if (newName.isNotBlank() && newName != currentAccount) {
                    renameAccount(currentAccount, newName)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun renameAccount(oldName: String, newName: String) {
        val accounts = getAllAccounts().toMutableSet()
        accounts.remove(oldName)
        accounts.add(newName)

        val accountPrefs = getSharedPreferences("accounts", Context.MODE_PRIVATE).edit()
        accountPrefs.putStringSet("account_names", accounts)
        accountPrefs.apply()

        val oldTransactions = getSharedPreferences("transactions_$oldName", Context.MODE_PRIVATE)
        val newTransactions = getSharedPreferences("transactions_$newName", Context.MODE_PRIVATE).edit()
        newTransactions.putString("transactions", oldTransactions.getString("transactions", "[]"))
        newTransactions.apply()

        getSharedPreferences("transactions_$oldName", Context.MODE_PRIVATE).edit().clear().apply()

        currentAccount = newName
        val bankBalanceInput = findViewById<EditText>(R.id.bankBalanceInput)
        bankBalanceInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val enteredText = bankBalanceInput.text.toString().replace("$", "").trim()
                val newBalance = enteredText.toDoubleOrNull()
                if (newBalance != null && newBalance != currentBalance) {
                    currentBalance = newBalance
                    saveBankBalance(currentBalance)
                    refreshList()
                }
            }
        }

        setupAccountSpinner()
        loadAccountData()
    }

    private fun promptDeleteAccount() {
        AlertDialog.Builder(this)
            .setTitle("Delete Account")
            .setMessage("Are you sure you want to delete '$currentAccount'? This cannot be undone.")
            .setPositiveButton("Delete") { _, _ ->
                val prefs = getSharedPreferences("accounts", Context.MODE_PRIVATE)
                val accounts = prefs.getStringSet("account_names", setOf())?.toMutableSet() ?: mutableSetOf()
                accounts.remove(currentAccount)
                prefs.edit().putStringSet("account_names", accounts).apply()
                getSharedPreferences("transactions_$currentAccount", Context.MODE_PRIVATE).edit().clear().apply()
                currentAccount = "Default"
                val bankBalanceInput = findViewById<EditText>(R.id.bankBalanceInput)
                bankBalanceInput.setOnFocusChangeListener { _, hasFocus ->
                    if (!hasFocus) {
                        val enteredText = bankBalanceInput.text.toString().replace("$", "").trim()
                        val newBalance = enteredText.toDoubleOrNull()
                        if (newBalance != null && newBalance != currentBalance) {
                            currentBalance = newBalance
                            saveBankBalance(currentBalance)
                            refreshList()
                        }
                    }
                }

                setupAccountSpinner()
                loadAccountData()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun setupAdapter() {
        val runningBalances = calculateRunningBalanceList(transactionList, currentBalance)

        transactionAdapter = TransactionAdapter(
            transactions = transactionList,
            balances = runningBalances,
            onEdit = { transaction ->
                val index = transactionList.indexOf(transaction)
                if (index != -1) {
                    AddTransactionDialog(
                        context = this,
                        transaction = transaction,
                        onSave = { updatedTransactions ->
                            transactionList.removeAt(index)
                            transactionList.addAll(updatedTransactions)
                            transactionList.sortWith(compareBy<Transaction> {
                                LocalDate.parse(it.date, DateTimeFormatter.ofPattern("MM/dd/yyyy"))
                            }.thenByDescending { it.amount })
                            saveAllTransactions(transactionList)
                            refreshList()
                        },
                        onReplaceExisting = { original, newList ->
                            val fromDate = original.date
                            transactionList.removeAll {
                                it.isRecurring &&
                                        it.source == original.source &&
                                        it.category == original.category &&
                                        it.frequency == original.frequency &&
                                        it.date >= fromDate
                            }
                            transactionList.addAll(newList)
                            transactionList.sortWith(compareBy<Transaction> {
                                LocalDate.parse(it.date, DateTimeFormatter.ofPattern("MM/dd/yyyy"))
                            }.thenByDescending { it.amount })
                            saveAllTransactions(transactionList)
                            refreshList()
                        }
                    ).show()
                }
            },
            onDelete = { transaction ->
                transactionList.remove(transaction)
                saveAllTransactions(transactionList)
                refreshList()
            },
            onDeleteRecurring = { transaction, mode ->
                when (mode) {
                    TransactionAdapter.DeleteMode.THIS_ONLY -> {
                        transactionList.remove(transaction)
                    }
                    TransactionAdapter.DeleteMode.THIS_AND_FUTURE -> {
                        transactionList.removeAll {
                            it.isRecurring &&
                                    it.source == transaction.source &&
                                    it.category == transaction.category &&
                                    it.frequency == transaction.frequency &&
                                    it.date >= transaction.date
                        }
                    }
                    TransactionAdapter.DeleteMode.ENTIRE_SERIES -> {
                        transactionList.removeAll {
                            it.isRecurring &&
                                    it.source == transaction.source &&
                                    it.category == transaction.category &&
                                    it.frequency == transaction.frequency
                        }
                    }
                }
                saveAllTransactions(transactionList)
                refreshList()
            },
            onPaidChecked = { transaction, isChecked ->
                transaction.isPaid = isChecked
                saveAllTransactions(transactionList)
                refreshList()
            },
            onSkipInstance = { skippedTxn ->
                val index = transactionList.indexOfFirst {
                    it.date == skippedTxn.date &&
                            it.amount == skippedTxn.amount &&
                            it.source == skippedTxn.source &&
                            it.frequency == skippedTxn.frequency &&
                            it.isRecurring
                }
                if (index != -1) {
                    transactionList[index].isSkipped = true
                    saveAllTransactions(transactionList)
                    refreshList()
                }
            }
        )

        recyclerView.adapter = transactionAdapter
    }

    private fun getAllTransactions(context: Context): List<Transaction> {
        val prefs = context.getSharedPreferences("transactions_$currentAccount", Context.MODE_PRIVATE)
        val json = prefs.getString("transactions", "[]")
        val type = object : TypeToken<List<Transaction>>() {}.type
        return Gson().fromJson(json, type)
    }

    private fun saveAllTransactions(transactions: List<Transaction>) {
        val prefs = getSharedPreferences("transactions_$currentAccount", Context.MODE_PRIVATE).edit()
        val json = Gson().toJson(transactions)
        prefs.putString("transactions", json)
        prefs.apply()
    }


    private fun saveBankBalance(balance: Double) {
        val prefs = getSharedPreferences("prefs_$currentAccount", Context.MODE_PRIVATE)
        prefs.edit().putFloat("bank_balance", balance.toFloat()).apply()
    }

    private fun getSavedBankBalance(): Double {
        val prefs = getSharedPreferences("prefs_$currentAccount", Context.MODE_PRIVATE)
        return prefs.getFloat("bank_balance", 0f).toDouble()
    }

    private fun updateBankBalanceText(balance: Double) {
        balanceTextView.text = "$" + String.format("%.2f", balance)
    }

    private fun calculateRunningBalanceList(transactions: List<Transaction>, startingBalance: Double): Map<Int, Double> {
        val runningBalances = mutableMapOf<Int, Double>()
        var running = startingBalance
        for ((index, txn) in transactions.withIndex()) {
            if (!txn.isPaid && !txn.isSkipped) {
                running += txn.amount
            }
            runningBalances[index] = running
        }
        return runningBalances
    }

    private fun updateLowestBalanceDisplay(transactions: List<Transaction>, balances: Map<Int, Double>) {
        if (transactions.isEmpty() || balances.isEmpty()) {
            lowestBalanceTextView.text = ""
            return
        }

        var lowestBalance = Double.MAX_VALUE
        var lowestDate = ""
        for ((index, txn) in transactions.withIndex()) {
            val balance = balances[index] ?: continue
            if (balance < lowestBalance) {
                lowestBalance = balance
                lowestDate = txn.date
            }
        }

        if (lowestBalance == Double.MAX_VALUE) {
            lowestBalanceTextView.text = ""
            return
        }

        val balanceColor = if (lowestBalance < 0)
            ContextCompat.getColor(this, android.R.color.holo_red_dark)
        else
            ContextCompat.getColor(this, android.R.color.holo_green_dark)

        val styled = android.text.SpannableStringBuilder()
        styled.append("Lowest Balance: ")
        styled.setSpan(android.text.style.StyleSpan(android.graphics.Typeface.BOLD), 0, styled.length, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        val balanceStr = "$" + String.format("%.2f", lowestBalance)
        val startBalance = styled.length
        styled.append(balanceStr)
        styled.setSpan(android.text.style.ForegroundColorSpan(balanceColor), startBalance, styled.length, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        styled.setSpan(android.text.style.StyleSpan(android.graphics.Typeface.BOLD), startBalance, styled.length, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        styled.append("  ")
        val startDate = styled.length
        styled.append(lowestDate)
        styled.setSpan(android.text.style.ForegroundColorSpan(ContextCompat.getColor(this, android.R.color.darker_gray)), startDate, styled.length, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

        lowestBalanceTextView.text = styled
    }

    private fun refreshList() {
        transactionList.sortWith(compareBy<Transaction> {
            LocalDate.parse(it.date, DateTimeFormatter.ofPattern("MM/dd/yyyy"))
        }.thenByDescending { it.amount })
        val baseList = if (showPaidAndSkipped) {
            transactionList
        } else {
            transactionList.filter { !it.isPaid && !it.isSkipped }
        }
        val runningBalances = calculateRunningBalanceList(baseList, currentBalance)
        transactionAdapter.updateData(baseList, runningBalances)
        updateLowestBalanceDisplay(baseList, runningBalances)
        findViewById<TextView>(R.id.emptyMessage).visibility =
            if (baseList.isEmpty()) View.VISIBLE else View.GONE
    }

    private fun applySortAndFilter(sortBy: Int, filterBy: Int) {
        var filteredList = if (showPaidAndSkipped) {
            transactionList
        } else {
            transactionList.filter { !it.isPaid && !it.isSkipped }
        }

        filteredList = when (filterBy) {
            1 -> filteredList.filter { it.isPaid }
            2 -> filteredList.filter { !it.isPaid && !it.isSkipped }
            else -> filteredList
        }

        filteredList = when (sortBy) {
            1 -> filteredList.sortedBy { it.amount }
            2 -> filteredList.sortedBy { it.source }
            else -> filteredList.sortedWith(compareBy<Transaction> {
                LocalDate.parse(it.date, DateTimeFormatter.ofPattern("MM/dd/yyyy"))
            }.thenByDescending { it.amount })
        }

        val updatedBalances = calculateRunningBalanceList(filteredList, currentBalance)
        transactionAdapter.updateData(filteredList, updatedBalances)
        updateLowestBalanceDisplay(filteredList, updatedBalances)
    }

    private fun setupBottomNavigation() {
        val bottomNavigation = findViewById<BottomNavigationView>(R.id.bottomNavigation)

        bottomNavigation.setSelectedItemId(R.id.nav_planning)
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_current -> {
                    startActivity(Intent(this, HomeActivity::class.java))
                    true
                }
                R.id.nav_planning -> true
                R.id.nav_reports -> {
                    startActivity(Intent(this, ReportsActivity::class.java))
                    true
                }
                else -> false
            }
        }
    }

}