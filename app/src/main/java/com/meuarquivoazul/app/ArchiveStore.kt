package com.meuarquivoazul.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

data class ArchivedMessage(val sender: String, val message: String, val timestamp: Long)

object ArchiveStore {
    private const val PREFS = "arquivo_azul"
    private const val KEY_MESSAGES = "messages"

    fun add(context: Context, sender: String, message: String) {
        if (sender.isBlank() || message.isBlank()) return
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val array = try { JSONArray(prefs.getString(KEY_MESSAGES, "[]")) } catch (_: Exception) { JSONArray() }
        array.put(JSONObject().put("sender", sender).put("message", message).put("timestamp", System.currentTimeMillis()))
        while (array.length() > 1000) array.remove(0)
        prefs.edit().putString(KEY_MESSAGES, array.toString()).apply()
    }

    fun all(context: Context): List<ArchivedMessage> {
        val raw = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_MESSAGES, "[]")
        val array = try { JSONArray(raw) } catch (_: Exception) { JSONArray() }
        return (0 until array.length()).mapNotNull { i ->
            try {
                val item = array.getJSONObject(i)
                ArchivedMessage(item.optString("sender"), item.optString("message"), item.optLong("timestamp"))
            } catch (_: Exception) { null }
        }.reversed()
    }
}