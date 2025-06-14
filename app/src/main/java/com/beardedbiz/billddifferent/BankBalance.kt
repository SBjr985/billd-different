package com.beardedbiz.billddifferent

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bank_balance")
data class BankBalance(
    @PrimaryKey val id: Int = 1,
    val amount: Int
)
