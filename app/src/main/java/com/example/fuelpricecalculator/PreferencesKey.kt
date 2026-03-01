package com.example.fuelpricecalculator

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

object PreferencesKey {
    val PETROL_PRICE = doublePreferencesKey("petrol_price")
    val DIESEL_PRICE = doublePreferencesKey("diesel_price")
    val LAST_UPDATE = stringPreferencesKey("last_update")
    val USE_METRIC = booleanPreferencesKey("use_metric")
}

class SettingsManager(private val context: Context){
    private val Context.dataStore by preferencesDataStore(name = "settings")
    val petrolPrice: Flow<Double> = context.dataStore.data.map {it[PreferencesKey.PETROL_PRICE] ?: 1.45}
    val dieselPrice: Flow<Double> = context.dataStore.data.map {it[PreferencesKey.DIESEL_PRICE] ?: 1.45}
    val lastUpdate: Flow<String> = context.dataStore.data.map {it[PreferencesKey.LAST_UPDATE] ?: "Never"}
    val useMetric: Flow<Boolean> = context.dataStore.data.map { it[PreferencesKey.USE_METRIC] ?: false}

    suspend fun savePrices(petrol: Double, diesel: Double, lastUpdate: String){
        context.dataStore.edit { prefs ->
            prefs[PreferencesKey.PETROL_PRICE] = petrol
            prefs[PreferencesKey.DIESEL_PRICE] = diesel
            prefs[PreferencesKey.LAST_UPDATE] = lastUpdate
        }
    }
    suspend fun saveUnitSystem(isMetric: Boolean){
        context.dataStore.edit { it[PreferencesKey.USE_METRIC] = isMetric }
    }
}