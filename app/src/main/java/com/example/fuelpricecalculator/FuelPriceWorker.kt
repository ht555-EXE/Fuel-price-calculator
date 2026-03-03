package com.example.fuelpricecalculator

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import androidx.work.ListenableWorker.Result
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.net.HttpURLConnection
import java.net.URL

/**
 * A background worker responsible for periodically fetching and updating [PreferencesKey]
 * with the most up-to-date fuel price data
 *
 * @param context the [Context] provided by the WorkManager to access application resources
 * @param params the [WorkerParameters] containing execution details and constraints
 */
class FuelPriceWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params){
    /**
     * Executes background [FuelPriceWorker] task.
     *
     * @return [Result.success] if prices were updated, or [Result.retry] if an error occurred
     */
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO){
            try{
                //connects to MFG dataset
                val url = URL("https://fuel.motorfuelgroup.com/fuel_prices_data.json")
                val connection = url.openConnection() as HttpURLConnection
                connection.setRequestProperty("User-Agent", "Mozilla/5.0")
                //gets values from set
                val content = connection.inputStream.bufferedReader().use {it.readText()}
                val response = Gson().fromJson(content, FuelResponse::class.java)
                val petrol = response.stations.mapNotNull {it.prices["E10"]}.average()
                val diesel = response.stations.mapNotNull {it.prices["B7"]}.average()
                val lastUpdate = response.lastUpdated
                val settings = (applicationContext as TripApplication).settingsManager
                //saves prices and timestamp to PreferencesKey
                settings.savePrices(petrol, diesel, lastUpdate)
                Result.success()
            } catch (e: Exception){
                Result.retry()
            }
        }
    }
}