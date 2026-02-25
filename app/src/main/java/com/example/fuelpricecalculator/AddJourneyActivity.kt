package com.example.fuelpricecalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.fuelpricecalculator.ui.theme.FuelPriceCalculatorTheme
import kotlin.getValue

class AddJourneyActivity : ComponentActivity() {
    private val viewModel: FuelViewModel by viewModels {
        val repository = (application as TripApplication).repository
        FuelViewModelFactory(repository)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            FuelPriceCalculatorTheme {
                AddJourneyScreen(onBack = {finish()}, viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddJourneyScreen(onBack: () -> Unit, viewModel: FuelViewModel){
    var journeyStart by remember { mutableStateOf("") }
    var journeyEnd by remember { mutableStateOf("") }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Add a New Journey") }) }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).padding(16.dp)) {
            Text("Journey Start", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = journeyStart,
                onValueChange = {journeyStart = it},
                label = {Text("Journey Start")},
                modifier = Modifier.fillMaxWidth()
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            Text("Journey End", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = journeyEnd,
                onValueChange = {journeyEnd = it},
                label = {Text("Journey End")},
                modifier = Modifier.fillMaxWidth()
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            Button(
                onClick = {},
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {Text("Add Journey")}
        }
    }
}