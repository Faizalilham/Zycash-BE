package com.zycash.bereport.repository


import com.zycash.bereport.model.DailySummary
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
interface DailySummaryRepository : JpaRepository<DailySummary, Long> {

    fun findByUserIdAndDate(userId: String, date: LocalDate): DailySummary?

    fun findByUserIdAndDateBetween(
        userId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): List<DailySummary>
}