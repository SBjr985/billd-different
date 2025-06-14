package com.beardedbiz.billddifferent

import androidx.room.*

@Dao
interface TransactionDao {

    @Insert
    suspend fun insert(transaction: Transaction)

    @Update
    suspend fun update(transaction: Transaction)

    @Delete
    suspend fun delete(transaction: Transaction)

    @Query("SELECT * FROM transactions ORDER BY date ASC")
    suspend fun getAllTransactionsSorted(): List<Transaction>
}
