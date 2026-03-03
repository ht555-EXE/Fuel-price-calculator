package com.example.fuelpricecalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import com.example.fuelpricecalculator.ui.theme.FuelPriceCalculatorTheme
import kotlin.getValue
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.room.util.TableInfo
import androidx.compose.foundation.layout.Row
import androidx.compose.ui.Alignment
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.foundation.layout.fillMaxWidth

/**
 * Entry point for settings functionality.
 *
 * Initializes [FuelViewModel] through [FuelViewModelFactory],
 * using [FuelRepository] from [TripApplication], rendering [SettingsScreen]
 *
 */
class SettingsActivity : ComponentActivity (){
    private val viewModel: FuelViewModel by viewModels {
        val repository = (application as TripApplication).repository
        FuelViewModelFactory(repository)
    }

    /**
     * Renders [SettingsScreen].
     *
     * @param savedInstanceState previous saved state of UI or null if this is a fresh start.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FuelPriceCalculatorTheme {
                SettingsScreen(onBack = {finish()}, viewModel = viewModel)
            }
        }
    }
}

/**
 * Function for displaying settings screen UI.
 * Allows users to change settings and view most recent fuel prices,
 * pulling information from [viewModel] via [PreferencesKey].
 *
 * @param onBack A callback invoked when the user exits the screen, via back button or after new
 * trip is added
 * @param viewModel view model instance passed for adding vehicles to persistent storage.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: FuelViewModel) {
    val petrolPrice by viewModel.petrolPrice.observeAsState(0.0)
    val dieselPrice by viewModel.dieselPrice.observeAsState(0.0)
    val lastUpdate by viewModel.lastUpdate.observeAsState("Never")
    val isMetric by viewModel.useMetric.observeAsState(false)
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            //text section for fuel prices
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Current Fuel Prices",
                    style = MaterialTheme.typography.titleLarge
                )
                Text(
                    text = "Current Petrol Price - ${String.format("%.1f", petrolPrice)}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Current Diesel Price - ${String.format("%.1f", dieselPrice)}",
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "(last updated $lastUpdate)",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            Column(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                //settings toggle menus for settings
                Text("Localization Settings", style = MaterialTheme.typography.titleLarge)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Distance Units", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            text = if (isMetric) "Using Kilometers (km)" else "Using Miles (mi)",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    Switch(
                        checked = isMetric,
                        onCheckedChange = { viewModel.toggleUnits(it) }
                    )
                }
            }
        }
    }
}