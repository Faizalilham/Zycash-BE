package com.zycash.benotification.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.zycash.benotification.dto.NotificationResponse
import com.zycash.benotification.dto.NotificationStatsResponse
import com.zycash.benotification.model.Notification
import com.zycash.benotification.repository.NotificationRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class NotificationHistoryService(
    private val notificationRepository: NotificationRepository
) {

    private val objectMapper = jacksonObjectMapper()

    fun getAllNotifications(userId: String, pageable: Pageable): Page<NotificationResponse> {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
            .map { it.toResponse() }
    }

    fun getNotificationsByCategory(
        userId: String,
        category: String,
        pageable: Pageable
    ): Page<NotificationResponse> {
        return notificationRepository.findByUserIdAndCategoryOrderByCreatedAtDesc(userId, category, pageable)
            .map { it.toResponse() }
    }

    fun getUnreadNotifications(userId: String): List<NotificationResponse> {
        return notificationRepository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
            .map { it.toResponse() }
    }

    fun getNotificationStats(userId: String): NotificationStatsResponse {
        val allNotifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(
            userId,
            Pageable.unpaged()
        ).content

        val unreadCount = notificationRepository.countByUserIdAndIsReadFalse(userId)

        val byCategory = allNotifications.groupBy { it.category }
            .mapValues { it.value.size.toLong() }

        val byType = allNotifications.groupBy { it.type }
            .mapValues { it.value.size.toLong() }

        return NotificationStatsResponse(
            totalCount = allNotifications.size.toLong(),
            unreadCount = unreadCount,
            byCategory = byCategory,
            byType = byType
        )
    }

    @Transactional
    fun markAsRead(id: Long, userId: String) {
        val notification = notificationRepository.findById(id).orElse(null)

        if (notification != null && notification.userId == userId) {
            notificationRepository.markAsRead(id, LocalDateTime.now())
        }
    }

    @Transactional
    fun markAllAsRead(userId: String) {
        notificationRepository.markAllAsRead(userId, LocalDateTime.now())
    }

    @Transactional
    fun deleteNotification(id: Long, userId: String) {
        val notification = notificationRepository.findById(id).orElse(null)

        if (notification != null && notification.userId == userId) {
            notificationRepository.deleteById(id)
        }
    }

    @Transactional
    fun deleteReadNotifications(userId: String): Int {
        val readNotifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(
            userId,
            Pageable.unpaged()
        ).content.filter { it.isRead }

        readNotifications.forEach { notificationRepository.delete(it) }

        return readNotifications.size
    }

    private fun Notification.toResponse(): NotificationResponse {
        return NotificationResponse(
            id = this.id!!,
            type = this.type,
            category = this.category,
            title = this.title,
            message = this.message,
            isRead = this.isRead,
            relatedTransactionId = this.relatedTransactionId,
            metadata = this.metadata?.let { parseMetadata(it) },
            createdAt = this.createdAt,
            readAt = this.readAt
        )
    }

    private fun parseMetadata(json: String): Map<String, Any>? {
        return try {
            objectMapper.readValue(json, Map::class.java) as Map<String, Any>
        } catch (e: Exception) {
            null
        }
    }
}