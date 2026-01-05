package com.zycash.benotification.controller


import com.zycash.benotification.dto.NotificationMessage
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller
import java.time.LocalDateTime
import java.util.UUID

@Controller
class NotificationController {

    @MessageMapping("/test")
    @SendTo("/topic/notifications")
    fun testNotification(message: String): NotificationMessage {
        return NotificationMessage(
            id = UUID.randomUUID().toString(),
            type = "INFO",
            title = "Test Notification",
            message = message,
            category = null,
            amount = null,
            timestamp = LocalDateTime.now()
        )
    }
}