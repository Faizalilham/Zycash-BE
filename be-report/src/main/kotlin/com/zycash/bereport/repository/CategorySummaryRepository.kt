package com.zycash.bereport.repository

import com.example.report.model.CategorySummary
import com.zycash.bereport.model.CategorySummary
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface CategorySummaryRepository : JpaRepository<CategorySummary, Long> {

    fun findByUserIdAndCategoryAndDateAndPeriod(
        userId: String,
        category: String,
        date: LocalDate,
        period: String
    ): CategorySummary?

    fun findByUserIdAndPeriodAndDate(
        userId: String,
        period: String,
        date: LocalDate
    ): List<CategorySummary>

    @Query("""
        SELECT c FROM CategorySummary c 
        WHERE c.userId = :userId 
        AND c.period = :period 
        AND c.date BETWEEN :startDate AND :endDate
        ORDER BY c.totalAmount DESC
    """)
    fun findByUserIdAndPeriodBetweenDates(
        userId: String,
        period: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<CategorySummary>
}