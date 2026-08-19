package com.fureva.veripup.service

import com.fureva.veripup.model.Conversation
import com.fureva.veripup.model.Message
import java.time.Instant

/** A buyer can view a public litter page without an account, but must have one to message. */
class MessagingService {

    fun startConversation(
        litterId: String?,
        participantUserIds: Set<String>,
        now: Instant
    ): Conversation {
        require(participantUserIds.size >= 2) { "A conversation needs at least two participants" }
        return Conversation(
            id = "conv_${now.toEpochMilli()}",
            litterId = litterId,
            participantUserIds = participantUserIds,
            createdAt = now
        )
    }

    fun postMessage(conversation: Conversation, senderUserId: String, body: String, now: Instant): Message {
        require(senderUserId in conversation.participantUserIds) {
            "Sender is not a participant in this conversation"
        }
        require(body.isNotBlank()) { "Message body cannot be empty" }
        return Message(
            id = "msg_${now.toEpochMilli()}",
            conversationId = conversation.id,
            senderUserId = senderUserId,
            body = body,
            createdAt = now
        )
    }

    fun markRead(message: Message, now: Instant): Message = message.copy(readAt = now)
}
