package com.jarvis.assistant.ai

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * Talks to YOUR backend (the Cloudflare Worker), never to Google directly.
 * The Gemini API key lives only on the backend.
 */
class GeminiBackendProvider(
    private val backendUrl: () -> String,
    private val appToken: () -> String
) : AiProvider {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    override suspend fun reply(history: List<ChatMessage>): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val url = backendUrl().trim()
                if (!url.startsWith("https://")) {
                    return@withContext Result.failure<String>(
                        IllegalStateException(
                            "Backend URL isn't set. Open Settings and paste your https:// backend URL."
                        )
                    )
                }

                val messages = JSONArray()
                history.filter { !it.isError }.takeLast(24).forEach { m ->
                    messages.put(
                        JSONObject()
                            .put("role", if (m.role == Role.USER) "user" else "model")
                            .put("text", m.text)
                    )
                }
                val payload = JSONObject().put("messages", messages).toString()

                val builder = Request.Builder()
                    .url(url)
                    .post(payload.toRequestBody("application/json; charset=utf-8".toMediaType()))
                val token = appToken().trim()
                if (token.isNotEmpty()) builder.header("X-App-Token", token)

                client.newCall(builder.build()).execute().use { response ->
                    val raw = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        val detail = runCatching { JSONObject(raw).optString("error") }
                            .getOrDefault("")
                        val message = if (response.code == 401) {
                            "Backend rejected the app token. Check it in Settings."
                        } else {
                            "Backend error ${response.code}. $detail".trim()
                        }
                        return@withContext Result.failure<String>(IllegalStateException(message))
                    }
                    val answer = JSONObject(raw).optString("reply").trim()
                    if (answer.isEmpty()) {
                        Result.failure<String>(IllegalStateException("The backend sent an empty reply."))
                    } else {
                        Result.success(answer)
                    }
                }
            } catch (e: IOException) {
                Result.failure<String>(
                    IllegalStateException("Network problem. Check your internet connection and backend URL.")
                )
            } catch (e: Exception) {
                Result.failure<String>(e)
            }
        }
}
