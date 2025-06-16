package com.beardedbiz.billddifferent

import com.beardedbiz.billddifferent.util.TransactionUtils
import org.junit.Assert.assertEquals
import org.junit.Test

class TransactionLogicTest {

    @Test
    fun testCalculateRunningBalanceList_simpleCase() {
        val transactions = listOf(
            Transaction(source = "Job", amount = 100.0, date = "2024-01-01"),
            Transaction(source = "Groceries", amount = -20.0, date = "2024-01-02"),
            Transaction(source = "Gift", amount = 50.0, date = "2024-01-03")
        )

        val result = TransactionUtils.calculateRunningBalanceList(transactions, 0.0)

        assertEquals(3, result.size)
        assertEquals(100.0, result[0] ?: 0.0, 0.001)
        assertEquals(80.0, result[1] ?: 0.0, 0.001)
        assertEquals(130.0, result[2] ?: 0.0, 0.001)
    }

    @Test
    fun testFilterPaidTransactions_onlyPaid() {
        val transactions = listOf(
            Transaction(source = "Job", amount = 100.0, date = "2024-01-01", isPaid = true),
            Transaction(source = "Groceries", amount = -20.0, date = "2024-01-02", isPaid = false),
            Transaction(source = "Gift", amount = 50.0, date = "2024-01-03", isPaid = true)
        )

        val result = TestUtils.filterPaidTransactions(transactions)

        assertEquals(2, result.size)
        assertEquals(100.0, result[0].amount, 0.001)
        assertEquals(50.0, result[1].amount, 0.001)
    }
}