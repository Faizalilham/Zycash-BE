package com.zycash.benotification.repository

import com.zycash.benotification.model.Notification
import org.springframework.stereotype.Repository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import java.time.LocalDateTime

@Repository
interface NotificationRepository : JpaRepository<Notification, Long> {

    // Find by user
    fun findByUserIdOrderByCreatedAtDesc(userId: String, pageable: Pageable): Page<Notification>

    // Find unread
    fun findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId: String): List<Notification>

    // Count unread
    fun countByUserIdAndIsReadFalse(userId: String): Long

    // Find by category
    fun findByUserIdAndCategoryOrderByCreatedAtDesc(
        userId: String,
        category: String,
        pageable: Pageable
    ): Page<Notification>

    // Find by date range
    fun findByUserIdAndCreatedAtBetweenOrderByCreatedAtDesc(
        userId: String,
        startDate: LocalDateTime,
        endDate: LocalDateTime
    ): List<Notification>

    // Mark as read
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.id = :id")
    fun markAsRead(id: Long, readAt: LocalDateTime)

    // Mark all as read
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :readAt WHERE n.userId = :userId AND n.isRead = false")
    fun markAllAsRead(userId: String, readAt: LocalDateTime)

    // Delete old notifications
    @Modifying
    @Query("DELETE FROM Notification n WHERE n.createdAt < :date")
    fun deleteOlderThan(date: LocalDateTime): Int
}