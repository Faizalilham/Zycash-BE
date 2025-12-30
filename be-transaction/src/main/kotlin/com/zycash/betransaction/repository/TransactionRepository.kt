package com.zycash.betransaction.repository

import com.zycash.betransaction.model.Transaction
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDateTime

@Repository
interface TransactionRepository : JpaRepository<Transaction, Long> {

    fun findByCategory(category: String): List<Transaction>

    fun findByTransactionDateBetween(start: LocalDateTime, end: LocalDateTime): List<Transaction>

    @Query("SELECT t FROM Transaction t ORDER BY t.transactionDate DESC")
    fun findAllOrderByDateDesc(): List<Transaction>
}