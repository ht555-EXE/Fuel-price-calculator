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

class FuelPriceWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params){
    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO){
            try{
                val url = URL("https://fuel.motorfuelgroup.com/fuel_prices_data.json")
                val connection = url.openConnection() as HttpURLConnection
                connection.setRequestProperty("User-Agent", "Mozilla/5.0")
                val content = connection.inputStream.bufferedReader().use {it.readText()}
                val response = Gson().fromJson(content, FuelResponse::class.java)
                val petrol = response.stations.mapNotNull {it.prices["E10"]}.average()
                val diesel = response.stations.mapNotNull {it.prices["B7"]}.average()
                val settings = (applicationContext as TripApplication).settingsManager
                settings.savePrices(petrol.toFloat(), diesel.toFloat())
                Result.success()
            } catch (e: Exception){
                Result.retry()
            }
        }
    }
}