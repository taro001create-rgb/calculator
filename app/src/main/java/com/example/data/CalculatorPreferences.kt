package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.domain.AngleUnit
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray
import org.json.JSONObject

private val Context.calculatorDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "calculator_preferences"
)

data class CalculationRecord(
    val id: Long,
    val expression: String,
    val rawResult: String,
    val formattedResult: String,
    val timestampMillis: Long,
)

interface CalculatorStorage {
    val isDarkThemeFlow: Flow<Boolean?>
    val isScientificModeFlow: Flow<Boolean>
    val angleUnitFlow: Flow<AngleUnit>
    val historyFlow: Flow<List<CalculationRecord>>
    suspend fun setDarkTheme(isDark: Boolean)
    suspend fun setScientificMode(isScientific: Boolean)
    suspend fun setAngleUnit(unit: AngleUnit)
    suspend fun addHistoryRecord(record: CalculationRecord)
    suspend fun clearHistory()
}

class CalculatorPreferencesRepository(private val context: Context) : CalculatorStorage {

    private object Keys {
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val IS_SCIENTIFIC_MODE = booleanPreferencesKey("is_scientific_mode")
        val ANGLE_UNIT = stringPreferencesKey("angle_unit")
        val HISTORY_JSON = stringPreferencesKey("history_json")
    }

    override val isDarkThemeFlow: Flow<Boolean?> = context.calculatorDataStore.data.map { prefs ->
        prefs[Keys.IS_DARK_THEME]
    }

    override val isScientificModeFlow: Flow<Boolean> = context.calculatorDataStore.data.map { prefs ->
        prefs[Keys.IS_SCIENTIFIC_MODE] ?: false
    }

    override val angleUnitFlow: Flow<AngleUnit> = context.calculatorDataStore.data.map { prefs ->
        when (prefs[Keys.ANGLE_UNIT]) {
            AngleUnit.RAD.name -> AngleUnit.RAD
            else -> AngleUnit.DEG
        }
    }

    override val historyFlow: Flow<List<CalculationRecord>> = context.calculatorDataStore.data.map { prefs ->
        decodeHistory(prefs[Keys.HISTORY_JSON].orEmpty())
    }

    override suspend fun setDarkTheme(isDark: Boolean) {
        context.calculatorDataStore.edit { prefs ->
            prefs[Keys.IS_DARK_THEME] = isDark
        }
    }

    override suspend fun setScientificMode(isScientific: Boolean) {
        context.calculatorDataStore.edit { prefs ->
            prefs[Keys.IS_SCIENTIFIC_MODE] = isScientific
        }
    }

    override suspend fun setAngleUnit(unit: AngleUnit) {
        context.calculatorDataStore.edit { prefs ->
            prefs[Keys.ANGLE_UNIT] = unit.name
        }
    }

    override suspend fun addHistoryRecord(record: CalculationRecord) {
        context.calculatorDataStore.edit { prefs ->
            val current = decodeHistory(prefs[Keys.HISTORY_JSON].orEmpty())
            val updated = (listOf(record) + current).take(MAX_HISTORY_ITEMS)
            prefs[Keys.HISTORY_JSON] = encodeHistory(updated)
        }
    }

    override suspend fun clearHistory() {
        context.calculatorDataStore.edit { prefs ->
            prefs.remove(Keys.HISTORY_JSON)
        }
    }

    private fun encodeHistory(records: List<CalculationRecord>): String {
        val array = JSONArray()
        records.forEach { item ->
            val obj = JSONObject().apply {
                put("id", item.id)
                put("expression", item.expression)
                put("rawResult", item.rawResult)
                put("formattedResult", item.formattedResult)
                put("timestampMillis", item.timestampMillis)
            }
            array.put(obj)
        }
        return array.toString()
    }

    private fun decodeHistory(json: String): List<CalculationRecord> {
        if (json.isBlank()) return emptyList()
        return try {
            val array = JSONArray(json)
            buildList {
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    add(
                        CalculationRecord(
                            id = obj.getLong("id"),
                            expression = obj.getString("expression"),
                            rawResult = obj.getString("rawResult"),
                            formattedResult = obj.getString("formattedResult"),
                            timestampMillis = obj.getLong("timestampMillis"),
                        )
                    )
                }
            }
        } catch (_: Exception) {
            emptyList()
        }
    }

    private companion object {
        const val MAX_HISTORY_ITEMS = 50
    }
}
