package com.beardedbiz.billddifferent

import androidx.room.*

@Dao
interface BankBalanceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(balance: BankBalance)

    @Query("SELECT * FROM bank_balance WHERE id = 1") // ✅ Matches the Entity tableName
    suspend fun getLatestBalance(): BankBalance?
}
