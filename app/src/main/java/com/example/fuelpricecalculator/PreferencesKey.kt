package com.example.fuelpricecalculator

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object PreferencesKey {
    val PETROL_PRICE = floatPreferencesKey("petrol_price")
    val DIESEL_PRICE = floatPreferencesKey("diesel_price")
}

class SettingsManager(private val context: Context){
    private val Context.dataStore by preferencesDataStore(name = "settings")
    val petrolPrice: Flow<Float> = context.dataStore.data.map {it[PreferencesKey.PETROL_PRICE] ?: 1.45f}
    val dieselPrice: Flow<Float> = context.dataStore.data.map {it[PreferencesKey.DIESEL_PRICE] ?: 1.45f}

    suspend fun savePrices(petrol: Float, diesel: Float){
        context.dataStore.edit { prefs ->
            prefs[PreferencesKey.PETROL_PRICE] = petrol
            prefs[PreferencesKey.DIESEL_PRICE] = diesel
        }
    }
}