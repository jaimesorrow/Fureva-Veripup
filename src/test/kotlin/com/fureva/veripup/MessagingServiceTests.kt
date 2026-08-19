package com.fureva.veripup

import com.fureva.veripup.service.MessagingService
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class MessagingServiceTests {

    private val service = MessagingService()
    private val now = Instant.parse("2026-05-15T00:00:00Z")

    @Test
    fun startConversationRequiresAtLeastTwoParticipants() {
        assertFailsWith<IllegalArgumentException> {
            service.startConversation("l1", setOf("u1"), now)
        }
    }

    @Test
    fun startConversationSucceedsWithTwoParticipants() {
        val conversation = service.startConversation("l1", setOf("u1", "u2"), now)
        assertEquals(setOf("u1", "u2"), conversation.participantUserIds)
    }

    @Test
    fun postMessageRequiresSenderToBeParticipant() {
        val conversation = service.startConversation("l1", setOf("u1", "u2"), now)
        assertFailsWith<IllegalArgumentException> {
            service.postMessage(conversation, "u3", "Hi", now)
        }
    }

    @Test
    fun postMessageRejectsBlankBody() {
        val conversation = service.startConversation("l1", setOf("u1", "u2"), now)
        assertFailsWith<IllegalArgumentException> {
            service.postMessage(conversation, "u1", "   ", now)
        }
    }

    @Test
    fun postMessageSucceedsForParticipant() {
        val conversation = service.startConversation("l1", setOf("u1", "u2"), now)
        val message = service.postMessage(conversation, "u1", "Is this litter still available?", now)
        assertEquals("u1", message.senderUserId)
        assertEquals(conversation.id, message.conversationId)
    }

    @Test
    fun markReadSetsReadTimestamp() {
        val conversation = service.startConversation("l1", setOf("u1", "u2"), now)
        val message = service.postMessage(conversation, "u1", "Hello", now)
        val read = service.markRead(message, now.plusSeconds(30))
        assertNotNull(read.readAt)
        assertEquals(now.plusSeconds(30), read.readAt)
    }
}
