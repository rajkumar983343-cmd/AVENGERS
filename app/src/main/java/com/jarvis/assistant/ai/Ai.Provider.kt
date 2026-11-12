package com.jarvis.assistant.ai

enum class Role { USER, ASSISTANT }

data class ChatMessage(
    val role: Role,
    val text: String,
    val isError: Boolean = false,
    val time: Long = System.currentTimeMillis()
)

/**
 * Anything that can answer a conversation. Gemini is one implementation;
 * add another class implementing this interface to use a different model.
 */
interface AiProvider {
    suspend fun reply(history: List<ChatMessage>): Result<String>
}
