package com.beardedbiz.billddifferent

import android.app.Dialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import com.google.android.material.button.MaterialButton

class AddTransactionDialog(
    context: Context,
    private val transaction: Transaction? = null,
    private val onSave: (List<Transaction>) -> Unit,
    private val onReplaceExisting: ((Transaction, List<Transaction>) -> Unit)? = null
) : Dialog(context) {

    private val displayFormatter = DateTimeFormatter.ofPattern("MM/dd/yyyy")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dialog_add_transaction)

        val amountInput = findViewById<EditText>(R.id.amountInput)
        val dateInput = findViewById<EditText>(R.id.dateInput)
        val endDateInput = findViewById<EditText>(R.id.endDateInput)
        val sourceInput = findViewById<EditText>(R.id.sourceInput)
        val isPayrollCheckbox = findViewById<CheckBox>(R.id.isPayrollCheckbox)
        val isRecurringCheckbox = findViewById<CheckBox>(R.id.isRecurringCheckbox)
        val isSkippedCheckbox = findViewById<CheckBox>(R.id.isSkippedCheckbox)
        val categorySpinner = findViewById<Spinner>(R.id.categorySpinner)
        val frequencySpinner = findViewById<Spinner>(R.id.frequencySpinner)
        val saveButton = findViewById<MaterialButton>(R.id.saveTransactionButton)
        val cancelButton = findViewById<MaterialButton>(R.id.cancelButton)
        val frequencyLabel = findViewById<TextView>(R.id.frequencyLabel)

        amountInput.inputType = InputType.TYPE_CLASS_NUMBER or
                InputType.TYPE_NUMBER_FLAG_DECIMAL or
                InputType.TYPE_NUMBER_FLAG_SIGNED

        fun highlightField(view: View) {
            val background = GradientDrawable().apply {
                setStroke(3, Color.RED)
                cornerRadius = 8f
            }
            view.background = background
        }

        fun resetField(view: View) {
            view.background = context.getDrawable(android.R.drawable.edit_text)
        }

        amountInput.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                val clean = amountInput.text.toString().replace(Regex("[^\\-\\d.]"), "")
                val value = clean.toDoubleOrNull() ?: 0.0
                amountInput.setText("$%.2f".format(value))
            }
        }

        fun setupAutoFormatDate(editText: EditText): (String) -> Unit {
            var isProgrammatic = false

            editText.addTextChangedListener(object : TextWatcher {
                private var isFormatting = false

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    if (isFormatting || s == null || isProgrammatic) return

                    val input = s.toString()
                    if (Regex("^\\d{2}/\\d{2}/\\d{4}$").matches(input)) return

                    val digits = input.replace(Regex("[^\\d]"), "")
                    isFormatting = true

                    val builder = StringBuilder()
                    val len = digits.length

                    if (len >= 2) builder.append(digits.substring(0, 2)).append("/")
                    else builder.append(digits)

                    if (len >= 4) builder.append(digits.substring(2, 4)).append("/")
                    else if (len > 2) builder.append(digits.substring(2))

                    if (len > 4) builder.append(digits.substring(4, len.coerceAtMost(8)))

                    editText.setText(builder.toString())
                    editText.setSelection(editText.text.length.coerceAtMost(builder.length))
                    isFormatting = false
                }
            })

            return { text: String ->
                isProgrammatic = true
                editText.setText(text)
                editText.setSelection(text.length)
                isProgrammatic = false
            }
        }

        val setDateInput = setupAutoFormatDate(dateInput)
        val setEndDateInput = setupAutoFormatDate(endDateInput)

        val categories = listOf("Select a Category", "Income", "Bills", "Groceries", "Fuel", "Entertainment", "Other")
        val frequencies = listOf("Daily", "Weekly", "Bi-Weekly", "Monthly", "Quarterly", "Annually")

        categorySpinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, categories)
        frequencySpinner.adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, frequencies)
        frequencySpinner.setSelection(frequencies.indexOf("Monthly"))

        isRecurringCheckbox.setOnCheckedChangeListener { _, isChecked ->
            endDateInput.visibility = if (isChecked) View.VISIBLE else View.GONE
            frequencySpinner.visibility = if (isChecked) View.VISIBLE else View.GONE
            frequencyLabel.visibility = if (isChecked) View.VISIBLE else View.GONE
        }

        val isEdit = transaction != null
        if (!isEdit) isSkippedCheckbox.visibility = View.GONE

        transaction?.let {
            amountInput.setText("$%.2f".format(it.amount))

            try {
                val parsed = LocalDate.parse(it.date)
                setDateInput(parsed.format(displayFormatter))
            } catch (_: Exception) {
                setDateInput(it.date)
            }

            sourceInput.setText(it.source)
            isPayrollCheckbox.isChecked = it.isPayroll
            isRecurringCheckbox.isChecked = it.isRecurring
            isSkippedCheckbox.isChecked = it.isSkipped

            val catIndex = categories.indexOf(it.category)
            if (catIndex >= 0) categorySpinner.setSelection(catIndex)

            val freqIndex = frequencies.indexOf(it.frequency)
            if (freqIndex >= 0) frequencySpinner.setSelection(freqIndex)

            if (it.isRecurring) {
                endDateInput.visibility = View.VISIBLE
                frequencySpinner.visibility = View.VISIBLE
                frequencyLabel.visibility = View.VISIBLE

                try {
                    val parsedEnd = LocalDate.parse(it.endDate ?: it.date)
                    setEndDateInput(parsedEnd.format(displayFormatter))
                } catch (_: Exception) {
                    setEndDateInput(it.endDate ?: it.date)
                }
            }
        }

        cancelButton.setOnClickListener { dismiss() }

        saveButton.setOnClickListener {
            resetField(amountInput)
            resetField(dateInput)
            resetField(sourceInput)
            resetField(endDateInput)

            try {
                val amountStr = amountInput.text.toString().replace(Regex("[^\\-\\d.]"), "")
                val amount = amountStr.toDouble()
                val dateStr = dateInput.text.toString()
                val sourceStr = sourceInput.text.toString()
                val endDateStr = endDateInput.text.toString()
                val isRecurring = isRecurringCheckbox.isChecked
                val frequency = frequencySpinner.selectedItem.toString()
                val category = categorySpinner.selectedItem.toString()
                val isPayroll = isPayrollCheckbox.isChecked
                val isPaid = transaction?.isPaid ?: false
                val isSkipped = isSkippedCheckbox.isChecked

                var hasError = false
                if (amountStr.isBlank()) highlightField(amountInput).also { hasError = true }
                if (dateStr.isBlank()) highlightField(dateInput).also { hasError = true }
                if (sourceStr.isBlank()) highlightField(sourceInput).also { hasError = true }
                if (category == "Select a Category") Toast.makeText(context, "Please select a category.", Toast.LENGTH_SHORT).show().also { hasError = true }
                if (isRecurring && endDateStr.isBlank()) highlightField(endDateInput).also { hasError = true }
                if (hasError) return@setOnClickListener

                val startDate = LocalDate.parse(dateStr, displayFormatter)
                val endDate = if (isRecurring) LocalDate.parse(endDateStr, displayFormatter) else null
                val formattedStartDate = startDate.format(displayFormatter)
                val formattedEndDate = endDate?.format(displayFormatter)

                val buildTransactions: (LocalDate, LocalDate?) -> List<Transaction> = { start, end ->
                    val txs = mutableListOf<Transaction>()
                    var date = start
                    while (end != null && !date.isAfter(end)) {
                        txs.add(
                            Transaction(
                                source = sourceStr,
                                amount = amount,
                                date = date.format(displayFormatter),
                                isRecurring = true,
                                frequency = frequency,
                                category = category,
                                isPayroll = isPayroll,
                                isPaid = false,
                                isSkipped = false,
                                endDate = formattedEndDate
                            )
                        )
                        date = when (frequency) {
                            "Daily" -> date.plusDays(1)
                            "Weekly" -> date.plusWeeks(1)
                            "Bi-Weekly" -> date.plusWeeks(2)
                            "Monthly" -> date.plusMonths(1)
                            "Quarterly" -> date.plusMonths(3)
                            "Annually" -> date.plusYears(1)
                            else -> break
                        }
                    }
                    txs
                }

                if (isEdit && transaction?.isRecurring == true && isRecurring && endDate != null) {
                    AlertDialog.Builder(context)
                        .setTitle("Edit Recurring Transaction")
                        .setItems(arrayOf("This Only", "This and Future", "Entire Series")) { _, which ->
                            when (which) {
                                0 -> {
                                    val updated = listOf(transaction.copy(
                                        source = sourceStr,
                                        amount = amount,
                                        date = formattedStartDate,
                                        isRecurring = false,
                                        frequency = frequency,
                                        category = category,
                                        isPayroll = isPayroll,
                                        isPaid = isPaid,
                                        isSkipped = isSkipped,
                                        endDate = null
                                    ))
                                    onSave(updated)
                                }
                                1 -> {
                                    val allFuture = buildTransactions(startDate, endDate)
                                    onReplaceExisting?.invoke(transaction.copy(endDate = formattedEndDate), allFuture)
                                }
                                2 -> {
                                    val allSeries = buildTransactions(LocalDate.parse(transaction.date, displayFormatter), endDate)
                                    onReplaceExisting?.invoke(transaction.copy(endDate = formattedEndDate), allSeries)
                                }
                            }
                            dismiss()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                } else if (isRecurring && endDate != null) {
                    onSave(buildTransactions(startDate, endDate))
                    dismiss()
                } else {
                    onSave(listOf(Transaction(
                        source = sourceStr,
                        amount = amount,
                        date = formattedStartDate,
                        isRecurring = false,
                        frequency = frequency,
                        category = category,
                        isPayroll = isPayroll,
                        isPaid = isPaid,
                        isSkipped = isSkipped
                    )))
                    dismiss()
                }
            } catch (e: Exception) {
                Toast.makeText(context, e.message ?: "Invalid input. Please check all fields.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        window?.setLayout((context.resources.displayMetrics.widthPixels * 0.95).toInt(), LinearLayout.LayoutParams.WRAP_CONTENT)
    }
}
