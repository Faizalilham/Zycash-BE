package com.zycash.bereport.service


import com.zycash.bereport.dto.CategoryPieChartData
import com.zycash.bereport.dto.PieChartResponse
import com.zycash.bereport.dto.TransactionEvent
import com.zycash.bereport.model.CategorySummary
import com.zycash.bereport.model.DailySummary
import com.zycash.bereport.repository.CategorySummaryRepository
import com.zycash.bereport.repository.DailySummaryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

@Service
class ReportService(
    private val categorySummaryRepository: CategorySummaryRepository,
    private val dailySummaryRepository: DailySummaryRepository
) {

    @Transactional
    fun handleTransactionCreated(event: TransactionEvent) {
        val date = event.timestamp.toLocalDate()

        // Update Daily Summary
        updateDailySummary(event.userId, date, event.amount)

        // Update Category Summary (Daily)
        updateCategorySummary(event.userId, event.category, date, event.amount, "DAILY")

        // Update Category Summary (Monthly)
        val monthStart = YearMonth.from(date).atDay(1)
        updateCategorySummary(event.userId, event.category, monthStart, event.amount, "MONTHLY")
    }

    fun handleTransactionUpdated(event: TransactionEvent) {
        // TODO: Implement update logic (need old amount to recalculate)
    }

    fun handleTransactionDeleted(event: TransactionEvent) {
        // TODO: Implement delete logic
    }

    private fun updateDailySummary(userId: String, date: LocalDate, amount: BigDecimal) {
        val summary = dailySummaryRepository.findByUserIdAndDate(userId, date)
            ?: DailySummary(userId = userId, date = date)

        summary.totalAmount = summary.totalAmount.add(amount)
        summary.transactionCount += 1

        dailySummaryRepository.save(summary)
    }

    private fun updateCategorySummary(
        userId: String,
        category: String,
        date: LocalDate,
        amount: BigDecimal,
        period: String
    ) {
        val summary = categorySummaryRepository.findByUserIdAndCategoryAndDateAndPeriod(
            userId, category, date, period
        ) ?: CategorySummary(
            userId = userId,
            category = category,
            date = date,
            period = period
        )

        summary.totalAmount = summary.totalAmount.add(amount)
        summary.transactionCount += 1
        summary.averageAmount = summary.totalAmount.divide(
            BigDecimal(summary.transactionCount),
            2,
            RoundingMode.HALF_UP
        )

        categorySummaryRepository.save(summary)
    }

    // ============================================
    // Pie Chart Methods
    // ============================================

    fun getDailyPieChart(userId: String, date: LocalDate): PieChartResponse {
        val summaries = categorySummaryRepository.findByUserIdAndPeriodAndDate(
            userId, "DAILY", date
        )

        val totalAmount = summaries.sumOf { it.totalAmount }

        val data = summaries.map { summary ->
            CategoryPieChartData(
                category = summary.category,
                totalAmount = summary.totalAmount,
                percentage = calculatePercentage(summary.totalAmount, totalAmount),
                transactionCount = summary.transactionCount,
                color = getCategoryColor(summary.category)
            )
        }.sortedByDescending { it.totalAmount }

        return PieChartResponse(
            title = "Pengeluaran Harian",
            period = "DAILY",
            totalAmount = totalAmount,
            data = data,
            metadata = mapOf(
                "date" to date.toString(),
                "totalCategories" to data.size,
                "totalTransactions" to summaries.sumOf { it.transactionCount }
            )
        )
    }

    fun getMonthlyPieChart(userId: String, yearMonth: YearMonth): PieChartResponse {
        val startDate = yearMonth.atDay(1)
        val endDate = yearMonth.atEndOfMonth()

        val summaries = categorySummaryRepository.findByUserIdAndPeriodBetweenDates(
            userId, "MONTHLY", startDate, endDate
        )

        val totalAmount = summaries.sumOf { it.totalAmount }

        val data = summaries.map { summary ->
            CategoryPieChartData(
                category = summary.category,
                totalAmount = summary.totalAmount,
                percentage = calculatePercentage(summary.totalAmount, totalAmount),
                transactionCount = summary.transactionCount,
                color = getCategoryColor(summary.category)
            )
        }.sortedByDescending { it.totalAmount }

        return PieChartResponse(
            title = "Pengeluaran Bulanan",
            period = "MONTHLY",
            totalAmount = totalAmount,
            data = data,
            metadata = mapOf(
                "month" to yearMonth.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                "totalCategories" to data.size,
                "totalTransactions" to summaries.sumOf { it.transactionCount }
            )
        )
    }

    fun getYearlyPieChart(userId: String, year: Int): PieChartResponse {
        val startDate = LocalDate.of(year, 1, 1)
        val endDate = LocalDate.of(year, 12, 31)

        val summaries = categorySummaryRepository.findByUserIdAndPeriodBetweenDates(
            userId, "MONTHLY", startDate, endDate
        )

        // Group by category across all months
        val categoryTotals = summaries.groupBy { it.category }
            .map { (category, list) ->
                CategoryPieChartData(
                    category = category,
                    totalAmount = list.sumOf { it.totalAmount },
                    percentage = 0.0, // Will be calculated below
                    transactionCount = list.sumOf { it.transactionCount },
                    color = getCategoryColor(category)
                )
            }

        val totalAmount = categoryTotals.sumOf { it.totalAmount }

        val data = categoryTotals.map { it.copy(
            percentage = calculatePercentage(it.totalAmount, totalAmount)
        )}.sortedByDescending { it.totalAmount }

        return PieChartResponse(
            title = "Pengeluaran Tahunan",
            period = "YEARLY",
            totalAmount = totalAmount,
            data = data,
            metadata = mapOf(
                "year" to year,
                "totalCategories" to data.size,
                "totalTransactions" to categoryTotals.sumOf { it.transactionCount }
            )
        )
    }

    private fun calculatePercentage(amount: BigDecimal, total: BigDecimal): Double {
        return if (total > BigDecimal.ZERO) {
            amount.divide(total, 4, RoundingMode.HALF_UP)
                .multiply(BigDecimal(100))
                .toDouble()
        } else {
            0.0
        }
    }

    private fun getCategoryColor(category: String): String {
        return when (category.lowercase()) {
            "makanan" -> "#FF6384"
            "transport" -> "#36A2EB"
            "belanja" -> "#FFCE56"
            "tagihan" -> "#4BC0C0"
            "hiburan" -> "#9966FF"
            "kesehatan" -> "#FF9F40"
            "pendidikan" -> "#FF6384"
            "lainnya" -> "#C9CBCF"
            else -> "#${(Math.random() * 16777215).toInt().toString(16)}"
        }
    }
}
