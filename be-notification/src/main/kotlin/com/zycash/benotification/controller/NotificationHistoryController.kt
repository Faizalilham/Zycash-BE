package com.zycash.benotification.controller


import com.zycash.benotification.dto.NotificationResponse
import com.zycash.benotification.dto.NotificationStatsResponse
import com.zycash.benotification.service.NotificationHistoryService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notifications")
class NotificationHistoryController(
    private val notificationHistoryService: NotificationHistoryService
) {

    // ============================================
    // Get notifications with pagination
    // ============================================
    @GetMapping
    fun getNotifications(
        @RequestParam userId: String,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) category: String?
    ): ResponseEntity<Page<NotificationResponse>> {
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())

        val notifications = if (category != null) {
            notificationHistoryService.getNotificationsByCategory(userId, category, pageable)
        } else {
            notificationHistoryService.getAllNotifications(userId, pageable)
        }

        return ResponseEntity.ok(notifications)
    }

    // ============================================
    // Get unread notifications
    // ============================================
    @GetMapping("/unread")
    fun getUnreadNotifications(
        @RequestParam userId: String
    ): ResponseEntity<List<NotificationResponse>> {
        val notifications = notificationHistoryService.getUnreadNotifications(userId)
        return ResponseEntity.ok(notifications)
    }

    // ============================================
    // Get notification statistics
    // ============================================
    @GetMapping("/stats")
    fun getNotificationStats(
        @RequestParam userId: String
    ): ResponseEntity<NotificationStatsResponse> {
        val stats = notificationHistoryService.getNotificationStats(userId)
        return ResponseEntity.ok(stats)
    }

    // ============================================
    // Mark notification as read
    // ============================================
    @PutMapping("/{id}/read")
    fun markAsRead(
        @PathVariable id: Long,
        @RequestParam userId: String
    ): ResponseEntity<Void> {
        notificationHistoryService.markAsRead(id, userId)
        return ResponseEntity.ok().build()
    }

    // ============================================
    // Mark all notifications as read
    // ============================================
    @PutMapping("/read-all")
    fun markAllAsRead(
        @RequestParam userId: String
    ): ResponseEntity<Void> {
        notificationHistoryService.markAllAsRead(userId)
        return ResponseEntity.ok().build()
    }

    // ============================================
    // Delete notification
    // ============================================
    @DeleteMapping("/{id}")
    fun deleteNotification(
        @PathVariable id: Long,
        @RequestParam userId: String
    ): ResponseEntity<Void> {
        notificationHistoryService.deleteNotification(id, userId)
        return ResponseEntity.noContent().build()
    }

    // ============================================
    // Delete all read notifications
    // ============================================
    @DeleteMapping("/read")
    fun deleteReadNotifications(
        @RequestParam userId: String
    ): ResponseEntity<Map<String, Int>> {
        val deletedCount = notificationHistoryService.deleteReadNotifications(userId)
        return ResponseEntity.ok(mapOf("deletedCount" to deletedCount))
    }
}


