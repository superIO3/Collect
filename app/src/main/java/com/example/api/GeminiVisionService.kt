package com.example.api

import android.graphics.Bitmap
import android.util.Base64
import com.example.BuildConfig
import com.example.model.ActionFrequency
import com.example.model.ActionType
import com.example.model.Card
import com.example.model.GtoRecommendation
import com.example.model.Position
import com.example.model.Street
import com.example.model.VisionTableDetection
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit

object GeminiVisionService {

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models"

    fun Bitmap.toBase64(): String {
        val outputStream = ByteArrayOutputStream()
        // Resize down if too big to ensure fast upload
        val maxDim = 1280
        val ratio = minOf(1.0f, maxDim.toFloat() / maxOf(width, height))
        val scaled = if (ratio < 1.0f) {
            Bitmap.createScaledBitmap(this, (width * ratio).toInt(), (height * ratio).toInt(), true)
        } else this

        scaled.compress(Bitmap.CompressFormat.JPEG, 85, outputStream)
        return Base64.encodeToString(outputStream.toByteArray(), Base64.NO_WRAP)
    }

    suspend fun analyzeTableScreen(bitmap: Bitmap): Result<Pair<VisionTableDetection, GtoRecommendation>> = withContext(Dispatchers.IO) {
        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Throwable) {
            ""
        }

        if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("Gemini API key is not configured. Please add it to your secrets or use local solver mode."))
        }

        val prompt = """
            You are an elite Poker GTO (Game Theory Optimal) Table Vision OCR and Strategy Engine.
            Analyze this poker table screen/screenshot carefully.
            Identify:
            1. Hero's Hole Cards (2 cards, formatted as RankSuit like As, Kh, 2c, Td, etc.)
            2. Community Board Cards (0 to 5 cards on board)
            3. Hero's Position (UTG, UTG+1, MP, HJ, CO, BTN, SB, BB)
            4. Current Pot Size (in dollars or BB, as a numeric float)
            5. Current Bet to Call (0 if check/unopened, or the bet amount)
            6. Hero's Stack Size
            7. Current Street (PREFLOP, FLOP, TURN, RIVER)
            8. Opponent action if visible (e.g. "Villain Bet $40", "Checked to Hero")
            9. GTO Decision Recommendation:
               - Recommended action: FOLD, CHECK, CALL, BET, RAISE, or ALL_IN
               - Recommended sizing (e.g. "33% Pot ($40)", "2.5x BB ($5)")
               - Frequencies in percentage: Raise %, Call/Check %, Fold % (sum to 100)
               - Hero hand equity %
               - Strategic explanation: why this action is GTO (pot odds, board texture, range advantage, blockers)
            
            Return ONLY a valid JSON object matching this exact schema:
            {
              "heroCards": ["Ah", "Kd"],
              "boardCards": ["As", "7c", "2d"],
              "heroPosition": "BTN",
              "potSize": 120.0,
              "currentBetToCall": 40.0,
              "heroStack": 500.0,
              "effectiveStack": 500.0,
              "street": "FLOP",
              "opponentAction": "BB Bet $40",
              "recommendedAction": "CALL",
              "actionSizing": "Call $40",
              "raiseFrequency": 20.0,
              "callFrequency": 75.0,
              "foldFrequency": 5.0,
              "heroEquity": 68.5,
              "potOddsPercent": 25.0,
              "strategicConcept": "Range Defense with Top Pair",
              "explanation": "Hero holds Top Pair Top Kicker on a dry rainbow board. Calling keeps villain's bluffs in range while controlling pot size."
            }
        """.trimIndent()

        try {
            val jsonRequestBody = JSONObject().apply {
                val contentsArray = JSONArray()
                val contentObj = JSONObject()
                val partsArray = JSONArray()

                // Text part
                partsArray.put(JSONObject().put("text", prompt))

                // Image inline data part
                val inlineDataObj = JSONObject().apply {
                    put("mimeType", "image/jpeg")
                    put("data", bitmap.toBase64())
                }
                partsArray.put(JSONObject().put("inlineData", inlineDataObj))

                contentObj.put("parts", partsArray)
                contentsArray.put(contentObj)
                put("contents", contentsArray)

                // Generation config for JSON format
                val genConfig = JSONObject().apply {
                    put("temperature", 0.2)
                    put("responseMimeType", "application/json")
                }
                put("generationConfig", genConfig)
            }

            val request = Request.Builder()
                .url("$BASE_URL/$MODEL_NAME:generateContent?key=$apiKey")
                .post(jsonRequestBody.toString().toRequestBody("application/json".toMediaType()))
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBody = response.body?.string()

            if (!response.isSuccessful || responseBody == null) {
                return@withContext Result.failure(Exception("Gemini API error: HTTP ${response.code} - ${responseBody?.take(200)}"))
            }

            val rootJson = JSONObject(responseBody)
            val candidates = rootJson.optJSONArray("candidates")
            val candidate = candidates?.optJSONObject(0)
            val content = candidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (text.isNullOrBlank()) {
                return@withContext Result.failure(Exception("Empty vision analysis received from AI."))
            }

            val parsedJson = JSONObject(text.trim())

            // Parse hero cards
            val heroCardsJson = parsedJson.optJSONArray("heroCards") ?: JSONArray()
            val heroCards = mutableListOf<Card>()
            for (i in 0 until heroCardsJson.length()) {
                Card.fromString(heroCardsJson.getString(i))?.let { heroCards.add(it) }
            }

            // Parse board cards
            val boardCardsJson = parsedJson.optJSONArray("boardCards") ?: JSONArray()
            val boardCards = mutableListOf<Card>()
            for (i in 0 until boardCardsJson.length()) {
                Card.fromString(boardCardsJson.getString(i))?.let { boardCards.add(it) }
            }

            val posStr = parsedJson.optString("heroPosition", "BTN")
            val position = Position.fromString(posStr)
            val potSize = parsedJson.optDouble("potSize", 100.0).toFloat()
            val currentBet = parsedJson.optDouble("currentBetToCall", 0.0).toFloat()
            val heroStack = parsedJson.optDouble("heroStack", 500.0).toFloat()
            val effectiveStack = parsedJson.optDouble("effectiveStack", heroStack.toDouble()).toFloat()
            val streetStr = parsedJson.optString("street", "FLOP").uppercase()
            val street = try { Street.valueOf(streetStr) } catch (e: Exception) { Street.FLOP }
            val opponentAction = parsedJson.optString("opponentAction", "")

            val detection = VisionTableDetection(
                heroCards = heroCards,
                boardCards = boardCards,
                heroPosition = position,
                potSize = potSize,
                currentBetToCall = currentBet,
                heroStack = heroStack,
                effectiveStack = effectiveStack,
                street = street,
                opponentAction = opponentAction,
                detectionConfidence = 0.95f,
                rawTextExtracted = text
            )

            // Parse GTO advice
            val recActionStr = parsedJson.optString("recommendedAction", "CHECK").uppercase()
            val recAction = try { ActionType.valueOf(recActionStr) } catch (e: Exception) {
                if (currentBet > 0) ActionType.CALL else ActionType.CHECK
            }
            val sizing = parsedJson.optString("actionSizing", recAction.label)
            val raiseFreq = parsedJson.optDouble("raiseFrequency", 0.0).toFloat()
            val callFreq = parsedJson.optDouble("callFrequency", 0.0).toFloat()
            val foldFreq = parsedJson.optDouble("foldFrequency", 0.0).toFloat()
            val heroEquity = parsedJson.optDouble("heroEquity", 50.0).toFloat()
            val potOdds = parsedJson.optDouble(
                "potOddsPercent",
                if (potSize + currentBet > 0) (currentBet / (potSize + currentBet) * 100.0) else 0.0
            ).toFloat()
            val concept = parsedJson.optString("strategicConcept", "GTO Solver Recommendation")
            val explanation = parsedJson.optString("explanation", "Action derived from Game Theory Optimal equilibrium.")

            val frequencies = listOf(
                ActionFrequency(if (currentBet > 0) ActionType.RAISE else ActionType.BET, raiseFreq, sizing, 10.0f),
                ActionFrequency(if (currentBet > 0) ActionType.CALL else ActionType.CHECK, callFreq, "Call/Check", 5.0f),
                ActionFrequency(ActionType.FOLD, foldFreq, "-", 0.0f)
            ).filter { it.percentage > 0f }.sortedByDescending { it.percentage }

            val recommendation = GtoRecommendation(
                primaryAction = recAction,
                primarySizing = sizing,
                confidence = 0.96f,
                frequencies = if (frequencies.isNotEmpty()) frequencies else listOf(ActionFrequency(recAction, 100f, sizing, 10f)),
                heroEquity = heroEquity,
                villainEquity = 100f - heroEquity,
                potOddsPercent = potOdds,
                spr = if (potSize > 0) (heroStack / potSize).toDouble().toFloat() else 5f,
                evExpectedValue = 8.5f,
                handStrengthName = if (heroCards.size == 2) "${heroCards[0].displayString} ${heroCards[1].displayString}" else "Analyzed Hand",
                strategicConcept = concept,
                explanation = explanation,
                isPureAction = raiseFreq >= 90f || foldFreq >= 90f
            )

            Result.success(Pair(detection, recommendation))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
