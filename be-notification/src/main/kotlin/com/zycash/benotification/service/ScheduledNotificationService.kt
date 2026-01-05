package com.zycash.benotification.service

import com.zycash.benotification.model.Notification
import com.zycash.benotification.repository.NotificationRepository
import org.slf4j.LoggerFactory
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.LocalDateTime
import java.math.BigDecimal

@Service
class ScheduledNotificationService(
    private val notificationRepository: NotificationRepository,
    private val messagingTemplate: SimpMessagingTemplate,
) {

    private val log = LoggerFactory.getLogger(ScheduledNotificationService::class.java)

    companion object {
        const val TRANSACTION_SERVICE_URL = "http://transaction-service:8081"
    }

    // ============================================
    // 1. No Income Today - Setiap jam 3 sore (15:00)
    // ============================================
    @Scheduled(cron = "0 0 15 * * *") // Setiap hari jam 15:00
    fun checkNoIncomeToday() {
        log.info("🔔 Running scheduled task: Check no income today")

        try {
            // TODO: Get all active users from User Service
            val activeUsers = listOf("888", "user123") // Hardcoded for now

            activeUsers.forEach { userId ->
                val hasIncome = checkUserHasIncomeToday(userId)

                if (!hasIncome) {
                    val notification = createAndSaveNotification(
                        userId = userId,
                        type = "WARNING",
                        category = "INCOME",
                        title = "⚠️ Belum Ada Pemasukan Hari Ini",
                        message = "Anda belum mencatat pemasukan hari ini. Jangan lupa catat jika ada pemasukan ya!",
                        metadata = """{"date":"${LocalDate.now()}","type":"NO_INCOME"}"""
                    )

                    sendNotificationViaWebSocket(userId, notification)
                }
            }
        } catch (e: Exception) {
            log.error("Error in checkNoIncomeToday", e)
        }
    }

    // ============================================
    // 2. Daily Summary - Setiap jam 9 malam (21:00)
    // ============================================
    @Scheduled(cron = "0 0 21 * * *") // Setiap hari jam 21:00
    fun sendDailySummary() {
        log.info("🔔 Running scheduled task: Send daily summary")

        try {
            val activeUsers = listOf("888", "user123")

            activeUsers.forEach { userId ->
                val summary = fetchDailySummary(userId)

                if (summary.transactionCount > 0) {
                    val notification = createAndSaveNotification(
                        userId = userId,
                        type = "INFO",
                        category = "TRANSACTION",
                        title = "📊 Ringkasan Pengeluaran Hari Ini",
                        message = buildDailySummaryMessage(summary),
                        metadata = """{"date":"${LocalDate.now()}","type":"DAILY_SUMMARY","total":${summary.totalAmount},"count":${summary.transactionCount}}"""
                    )

                    sendNotificationViaWebSocket(userId, notification)
                }
            }
        } catch (e: Exception) {
            log.error("Error in sendDailySummary", e)
        }
    }

    // ============================================
    // 3. Budget Alert - Check setiap jam
    // ============================================
    @Scheduled(cron = "0 0 * * * *") // Setiap jam
    fun checkBudgetAlert() {
        log.info("🔔 Running scheduled task: Check budget alert")

        try {
            val activeUsers = listOf("888", "user123")

            activeUsers.forEach { userId ->
                val dailyBudget = getUserDailyBudget(userId) // Default 100000
                val todayExpense = fetchTodayExpense(userId)

                // Alert jika pengeluaran > 80% budget
                if (todayExpense > dailyBudget * 0.8.toBigDecimal()) {
                    val percentage = (todayExpense.toDouble() / dailyBudget.toDouble() * 100).toInt()

                    val notification = createAndSaveNotification(
                        userId = userId,
                        type = "WARNING",
                        category = "BUDGET",
                        title = "⚠️ Budget Harian Hampir Habis!",
                        message = "Pengeluaran hari ini sudah mencapai $percentage% dari budget harian (${formatCurrency(todayExpense)} dari ${formatCurrency(dailyBudget)}). Hati-hati ya!",
                        metadata = """{"date":"${LocalDate.now()}","type":"BUDGET_ALERT","expense":${todayExpense},"budget":${dailyBudget},"percentage":${percentage}}"""
                    )

                    sendNotificationViaWebSocket(userId, notification)
                }
            }
        } catch (e: Exception) {
            log.error("Error in checkBudgetAlert", e)
        }
    }

    // ============================================
    // 4. Bill Reminder - Check setiap pagi jam 9
    // ============================================
    @Scheduled(cron = "0 0 9 * * *") // Setiap hari jam 09:00
    fun checkBillReminders() {
        log.info("🔔 Running scheduled task: Check bill reminders")

        try {
            val activeUsers = listOf("888", "user123")
            val today = LocalDate.now().dayOfMonth

            // Common bill due dates
            val billReminders = mapOf(
                1 to "Tagihan bulanan (listrik, air, internet)",
                10 to "Tagihan kartu kredit",
                15 to "Cicilan bulanan",
                25 to "Asuransi"
            )

            billReminders[today]?.let { billType ->
                activeUsers.forEach { userId ->
                    val notification = createAndSaveNotification(
                        userId = userId,
                        type = "REMINDER",
                        category = "BILL",
                        title = "🔔 Pengingat Tagihan",
                        message = "Jangan lupa bayar $billType hari ini!",
                        metadata = """{"date":"${LocalDate.now()}","type":"BILL_REMINDER","day":$today}"""
                    )

                    sendNotificationViaWebSocket(userId, notification)
                }
            }
        } catch (e: Exception) {
            log.error("Error in checkBillReminders", e)
        }
    }

    // ============================================
    // 5. Weekly Report - Setiap Minggu jam 10 pagi
    // ============================================
    @Scheduled(cron = "0 0 10 * * SUN") // Setiap Minggu jam 10:00
    fun sendWeeklyReport() {
        log.info("🔔 Running scheduled task: Send weekly report")

        try {
            val activeUsers = listOf("888", "user123")

            activeUsers.forEach { userId ->
                val weeklySummary = fetchWeeklySummary(userId)

                val notification = createAndSaveNotification(
                    userId = userId,
                    type = "INFO",
                    category = "TRANSACTION",
                    title = "📈 Laporan Mingguan",
                    message = buildWeeklySummaryMessage(weeklySummary),
                    metadata = """{"type":"WEEKLY_SUMMARY","total":${weeklySummary.totalAmount},"count":${weeklySummary.transactionCount}}"""
                )

                sendNotificationViaWebSocket(userId, notification)
            }
        } catch (e: Exception) {
            log.error("Error in sendWeeklyReport", e)
        }
    }

    // ============================================
    // 6. Achievement Milestone
    // ============================================
    fun checkAchievementMilestone(userId: String, totalSavings: BigDecimal) {
        val milestones = listOf(
            100000.toBigDecimal() to "Hebat! Anda sudah hemat Rp 100.000!",
            500000.toBigDecimal() to "Luar biasa! Hemat Rp 500.000 tercapai!",
            1000000.toBigDecimal() to "🎉 Wow! Anda berhasil hemat Rp 1.000.000!",
            5000000.toBigDecimal() to "🏆 Amazing! Hemat Rp 5.000.000!"
        )

        milestones.forEach { (milestone, message) ->
            if (totalSavings >= milestone) {
                val notification = createAndSaveNotification(
                    userId = userId,
                    type = "SUCCESS",
                    category = "ACHIEVEMENT",
                    title = "🎊 Pencapaian Baru!",
                    message = message,
                    metadata = """{"type":"ACHIEVEMENT","milestone":${milestone}}"""
                )

                sendNotificationViaWebSocket(userId, notification)
            }
        }
    }

    // ============================================
    // 7. Cleanup old notifications - Setiap tengah malam
    // ============================================
    @Scheduled(cron = "0 0 0 * * *") // Setiap tengah malam
    fun cleanupOldNotifications() {
        log.info("🔔 Running scheduled task: Cleanup old notifications")

        try {
            // Delete notifications older than 30 days
            val thirtyDaysAgo = LocalDateTime.now().minusDays(30)
            val deletedCount = notificationRepository.deleteOlderThan(thirtyDaysAgo)

            log.info("Deleted $deletedCount old notifications")
        } catch (e: Exception) {
            log.error("Error in cleanupOldNotifications", e)
        }
    }

    // ============================================
    // Helper Methods
    // ============================================

    private fun createAndSaveNotification(
        userId: String,
        type: String,
        category: String,
        title: String,
        message: String,
        metadata: String? = null
    ): Notification {
        val notification = Notification(
            userId = userId,
            type = type,
            category = category,
            title = title,
            message = message,
            metadata = metadata,
            isSent = false
        )

        return notificationRepository.save(notification)
    }

    private fun sendNotificationViaWebSocket(userId: String, notification: Notification) {
        try {
            val message = mapOf(
                "id" to notification.id.toString(),
                "type" to notification.type,
                "category" to notification.category,
                "title" to notification.title,
                "message" to notification.message,
                "timestamp" to notification.createdAt.toString()
            )

            messagingTemplate.convertAndSendToUser(
                userId,
                "/queue/notifications",
                message
            )

            // Mark as sent
            notification.isSent = true
            notification.sentAt = LocalDateTime.now()
            notificationRepository.save(notification)

            log.info("✅ Scheduled notification sent to user $userId: ${notification.title}")
        } catch (e: Exception) {
            log.error("Failed to send notification", e)
        }
    }

    private fun checkUserHasIncomeToday(userId: String): Boolean {
        // TODO: Call Transaction Service to check income
        // For now, return false to trigger notification
        return false
    }

    private fun fetchDailySummary(userId: String): DailySummaryData {
        // TODO: Call Report Service API
        return DailySummaryData(
            totalAmount = 150000.toBigDecimal(),
            transactionCount = 5,
            topCategory = "Makanan"
        )
    }

    private fun fetchTodayExpense(userId: String): BigDecimal {
        // TODO: Call Transaction Service API
        return 80000.toBigDecimal()
    }

    private fun getUserDailyBudget(userId: String): BigDecimal {
        // TODO: Get from User Settings
        return 100000.toBigDecimal()
    }

    private fun fetchWeeklySummary(userId: String): WeeklySummaryData {
        // TODO: Call Report Service API
        return WeeklySummaryData(
            totalAmount = 850000.toBigDecimal(),
            transactionCount = 25,
            topCategory = "Makanan",
            comparisonWithLastWeek = 5.5
        )
    }

    private fun buildDailySummaryMessage(summary: DailySummaryData): String {
        return """
            Hari ini Anda melakukan ${summary.transactionCount} transaksi dengan total ${formatCurrency(summary.totalAmount)}.
            Kategori terbanyak: ${summary.topCategory}.
            
            Tetap jaga keuangan Anda! 💪
        """.trimIndent()
    }

    private fun buildWeeklySummaryMessage(summary: WeeklySummaryData): String {
        val trend = if (summary.comparisonWithLastWeek > 0) "naik" else "turun"
        return """
            Minggu ini Anda menghabiskan ${formatCurrency(summary.totalAmount)} untuk ${summary.transactionCount} transaksi.
            
            Pengeluaran $trend ${Math.abs(summary.comparisonWithLastWeek)}% dibanding minggu lalu.
            
            Kategori terbanyak: ${summary.topCategory}
        """.trimIndent()
    }

    private fun formatCurrency(amount: BigDecimal): String {
        return "Rp ${String.format("%,.0f", amount.toDouble())}"
    }
}

// ============================================
// Data Classes
// ============================================
data class DailySummaryData(
    val totalAmount: BigDecimal,
    val transactionCount: Int,
    val topCategory: String
)

data class WeeklySummaryData(
    val totalAmount: BigDecimal,
    val transactionCount: Int,
    val topCategory: String,
    val comparisonWithLastWeek: Double
)