package com.example.data.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import android.util.Log
import com.example.BuildConfig
import com.example.data.repository.ParsedCourseSlot
import com.example.data.repository.TimetableParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

data class ExtractedTimetableResult(
    val slots: List<ParsedCourseSlot>,
    val startDate: String,
    val endDate: String,
    val isSuccess: Boolean,
    val statusMessage: String
)

object GeminiTimetableExtractor {
    private const val TAG = "GeminiExtractor"

    // Primary model confirmed by user
    private const val PRIMARY_MODEL = "gemini-3.8-flash"
    private const val FALLBACK_MODEL = "gemini-2.5-flash"

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(45, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    suspend fun extractFromUri(context: Context, imageUri: Uri): ExtractedTimetableResult {
        return withContext(Dispatchers.IO) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(imageUri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                    ?: return@withContext ExtractedTimetableResult(
                        emptyList(), "", "", false, "Could not load selected image."
                    )
                extractFromBitmap(originalBitmap)
            } catch (e: Exception) {
                Log.e(TAG, "Error opening image uri", e)
                ExtractedTimetableResult(
                    emptyList(), "", "", false, "Image error: ${e.localizedMessage}"
                )
            }
        }
    }

    suspend fun extractFromBitmap(originalBitmap: Bitmap): ExtractedTimetableResult {
        return withContext(Dispatchers.IO) {
            try {
                val scaledBitmap = scaleBitmap(originalBitmap, maxDimension = 1400)
                val base64Image = bitmapToBase64Jpeg(scaledBitmap)

                val apiKey = BuildConfig.GEMINI_API_KEY
                if (apiKey.isBlank()) {
                    return@withContext ExtractedTimetableResult(
                        emptyList(), "", "", false, "Gemini API key is not configured."
                    )
                }

                val prompt = """
You are an expert academic and medical college timetable parser.
Analyze this timetable image and extract every scheduled class, clinical posting, and practical lab session.

Extract each session with:
- "courseName": Subject or department name (e.g., General Surgery, Pathology, Internal Medicine, Microbiology, Biochemistry, Computer Networks)
- "courseCode": Short code if visible (e.g., SURG301, PATH202), otherwise empty string
- "dayOfWeek": Integer 1 to 7 (1 for Monday, 2 for Tuesday, 3 for Wednesday, 4 for Thursday, 5 for Friday, 6 for Saturday, 7 for Sunday)
- "startTime": Start time in 24-hour "HH:mm" format (e.g. "08:30", "12:00", "14:00")
- "endTime": End time in 24-hour "HH:mm" format (e.g. "11:30", "13:00", "16:00")
- "sessionType": EXACTLY one of "POSTING" (clinical/ward/hospital posting/internship), "LAB" (practical/laboratory/dissection), or "CLASS" (theory lecture/discussion)

Also check if any semester or term start and end date is visible (e.g. "Aug 2026 to Dec 2026" or "2026-08-01").
If found, format as "YYYY-MM-DD", otherwise use null.

Return ONLY a valid JSON object matching this structure:
{
  "startDate": "YYYY-MM-DD" or null,
  "endDate": "YYYY-MM-DD" or null,
  "slots": [
    {
      "courseName": "General Surgery",
      "courseCode": "SURG301",
      "dayOfWeek": 1,
      "startTime": "08:30",
      "endTime": "11:30",
      "sessionType": "POSTING"
    }
  ]
}
Do NOT include markdown code blocks or backticks. Return raw JSON only.
""".trimIndent()

                // Attempt primary model, fallback if needed
                val result = callGeminiApi(PRIMARY_MODEL, apiKey, prompt, base64Image)
                if (result.isSuccess) {
                    return@withContext result
                } else {
                    Log.w(TAG, "Primary model failed: ${result.statusMessage}. Trying fallback model $FALLBACK_MODEL")
                    val fallbackResult = callGeminiApi(FALLBACK_MODEL, apiKey, prompt, base64Image)
                    if (fallbackResult.isSuccess) {
                        return@withContext fallbackResult
                    }
                    return@withContext result
                }
            } catch (e: Exception) {
                Log.e(TAG, "Extraction failed", e)
                ExtractedTimetableResult(
                    emptyList(), "", "", false, "Extraction error: ${e.localizedMessage}"
                )
            }
        }
    }

    private fun callGeminiApi(
        modelName: String,
        apiKey: String,
        prompt: String,
        base64Jpeg: String
    ): ExtractedTimetableResult {
        val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"

        val requestJson = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        put(JSONObject().apply { put("text", prompt) })
                        put(JSONObject().apply {
                            val inlineData = JSONObject().apply {
                                put("mimeType", "image/jpeg")
                                put("data", base64Jpeg)
                            }
                            put("inlineData", inlineData)
                        })
                    }
                    put("parts", partsArray)
                }
                put(contentObj)
            }
            put("contents", contentsArray)

            val genConfig = JSONObject().apply {
                put("temperature", 0.2)
                put("responseMimeType", "application/json")
            }
            put("generationConfig", genConfig)
        }

        val requestBody = requestJson.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        val response = httpClient.newCall(request).execute()
        val responseBody = response.body?.string().orEmpty()

        if (!response.isSuccessful) {
            val errorMsg = parseErrorMessage(responseBody) ?: "HTTP ${response.code}: ${response.message}"
            Log.e(TAG, "Gemini API error ($modelName): $errorMsg")
            return ExtractedTimetableResult(emptyList(), "", "", false, errorMsg)
        }

        return parseGeminiResponse(responseBody)
    }

    private fun parseErrorMessage(jsonStr: String): String? {
        return try {
            val obj = JSONObject(jsonStr)
            obj.optJSONObject("error")?.optString("message")
        } catch (_: Exception) {
            null
        }
    }

    private fun parseGeminiResponse(jsonResponse: String): ExtractedTimetableResult {
        try {
            val root = JSONObject(jsonResponse)
            val candidates = root.optJSONArray("candidates")
            if (candidates == null || candidates.length() == 0) {
                return ExtractedTimetableResult(emptyList(), "", "", false, "No schedule content recognized by Gemini AI.")
            }

            val content = candidates.getJSONObject(0).optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val rawText = parts?.getJSONObject(0)?.optString("text")?.trim().orEmpty()

            // Clean code fences if present
            var cleanJson = rawText
            if (cleanJson.startsWith("```json")) {
                cleanJson = cleanJson.removePrefix("```json").trim()
            } else if (cleanJson.startsWith("```")) {
                cleanJson = cleanJson.removePrefix("```").trim()
            }
            if (cleanJson.endsWith("```")) {
                cleanJson = cleanJson.removeSuffix("```").trim()
            }

            val dataObj = JSONObject(cleanJson)
            val slotsArray = dataObj.optJSONArray("slots") ?: JSONArray()

            val defaultStart = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
            val defaultEnd = LocalDate.now().plusMonths(4).format(DateTimeFormatter.ISO_LOCAL_DATE)

            val startDate = dataObj.optString("startDate").takeIf { it.isNotBlank() && it != "null" } ?: defaultStart
            val endDate = dataObj.optString("endDate").takeIf { it.isNotBlank() && it != "null" } ?: defaultEnd

            val parsedSlots = mutableListOf<ParsedCourseSlot>()
            var shadeIdx = 0

            for (i in 0 until slotsArray.length()) {
                val item = slotsArray.getJSONObject(i)
                val courseName = item.optString("courseName").trim()
                if (courseName.isBlank()) continue

                val courseCode = item.optString("courseCode").trim()
                val dayOfWeek = item.optInt("dayOfWeek", 1).coerceIn(1, 7)
                val rawStart = item.optString("startTime", "09:00")
                val rawEnd = item.optString("endTime", "10:30")
                val sessionType = item.optString("sessionType", "CLASS").uppercase()
                val validSessionType = if (sessionType in listOf("POSTING", "LAB", "CLASS")) sessionType else "CLASS"

                val shade = TimetableParser.MONO_SHADES[shadeIdx % TimetableParser.MONO_SHADES.size]
                shadeIdx++

                parsedSlots.add(
                    ParsedCourseSlot(
                        courseName = courseName,
                        courseCode = courseCode,
                        professor = "",
                        room = "",
                        dayOfWeek = dayOfWeek,
                        startTime = formatTime(rawStart),
                        endTime = formatTime(rawEnd),
                        colorHex = shade,
                        sessionType = validSessionType
                    )
                )
            }

            if (parsedSlots.isEmpty()) {
                return ExtractedTimetableResult(
                    emptyList(),
                    startDate,
                    endDate,
                    false,
                    "No classes or postings could be extracted from this photo. Please ensure timetable is clearly visible."
                )
            }

            return ExtractedTimetableResult(
                slots = parsedSlots,
                startDate = startDate,
                endDate = endDate,
                isSuccess = true,
                statusMessage = "Successfully extracted ${parsedSlots.size} sessions from timetable photo."
            )
        } catch (e: Exception) {
            Log.e(TAG, "JSON parsing error", e)
            return ExtractedTimetableResult(
                emptyList(), "", "", false, "Failed to parse schedule JSON: ${e.localizedMessage}"
            )
        }
    }

    private fun scaleBitmap(source: Bitmap, maxDimension: Int): Bitmap {
        val width = source.width
        val height = source.height
        if (width <= maxDimension && height <= maxDimension) return source

        val ratio = width.toFloat() / height.toFloat()
        val newWidth: Int
        val newHeight: Int
        if (width > height) {
            newWidth = maxDimension
            newHeight = (maxDimension / ratio).toInt()
        } else {
            newHeight = maxDimension
            newWidth = (maxDimension * ratio).toInt()
        }
        return Bitmap.createScaledBitmap(source, newWidth, newHeight, true)
    }

    private fun bitmapToBase64Jpeg(bitmap: Bitmap): String {
        val stream = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.JPEG, 85, stream)
        val byteArray = stream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

    private fun formatTime(timeStr: String): String {
        val clean = timeStr.trim().replace(".", ":")
        val parts = clean.split(":")
        val h = parts.getOrNull(0)?.filter { it.isDigit() }?.toIntOrNull() ?: 9
        val m = parts.getOrNull(1)?.filter { it.isDigit() }?.toIntOrNull() ?: 0
        return "%02d:%02d".format(h.coerceIn(0, 23), m.coerceIn(0, 59))
    }
}
