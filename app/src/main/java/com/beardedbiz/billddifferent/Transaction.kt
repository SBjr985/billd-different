package com.beardedbiz.billddifferent

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val source: String,
    val amount: Double,
    val date: String,
    val isRecurring: Boolean = false,
    val frequency: String = "",
    val category: String = "",
    val isPayroll: Boolean = false,
    var isPaid: Boolean = false, // ✅ must be var
    val accountName: String = "Default", // ✅ Multi-account support
    var isSkipped: Boolean = false, // ✅ "Skip This Instance" support
    val endDate: String? = null // ✅ Needed for recurring transaction logic
)
