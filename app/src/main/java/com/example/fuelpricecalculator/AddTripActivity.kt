package com.example.fuelpricecalculator

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.PressInteraction
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.fuelpricecalculator.ui.theme.FuelPriceCalculatorTheme
import com.google.android.libraries.places.widget.PlaceAutocomplete
import com.google.android.libraries.places.widget.PlaceAutocompleteActivity
import kotlin.getValue

class AddTripActivity : ComponentActivity() {
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
    var origin by remember { mutableStateOf("") }
    var destination by remember { mutableStateOf("") }
    var originPlaceId by remember { mutableStateOf<String?>(null) }
    var destinationPlaceId by remember { mutableStateOf<String?>(null) }
    val currentCarLicense by viewModel.currentCarLicense.observeAsState()

    val context = LocalContext.current

    val startLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == PlaceAutocompleteActivity.RESULT_OK && result.data != null) {
            val prediction = PlaceAutocomplete.getPredictionFromIntent(result.data!!)
            origin = prediction?.getFullText(null).toString()
            originPlaceId = prediction?.placeId
        }
    }

    val endLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == PlaceAutocompleteActivity.RESULT_OK && result.data != null) {
            val prediction = PlaceAutocomplete.getPredictionFromIntent(result.data!!)
            destination = prediction?.getFullText(null).toString()
            destinationPlaceId = prediction?.placeId
        }
    }

    val startInteractionSource = remember{ MutableInteractionSource() }
    val endInteractionSource = remember{ MutableInteractionSource() }

    LaunchedEffect(startInteractionSource){
        startInteractionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release){
                val intent = PlaceAutocomplete.IntentBuilder().setCountries(listOf("UK")).build(context as Activity)
                startLauncher.launch(intent)
            }
        }
    }

    LaunchedEffect(endInteractionSource){
        endInteractionSource.interactions.collect { interaction ->
            if (interaction is PressInteraction.Release){
                val intent = PlaceAutocomplete.IntentBuilder().setCountries(listOf("UK")).build(context as Activity)
                endLauncher.launch(intent)
            }
        }
    }
    //TODO: make current car more efficient
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Add a Trip") },
                navigationIcon = {
                    IconButton(onClick = onBack){
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
            if (currentCarLicense == null) {
                Text(
                    text = "No car selected. Please add/select a car first.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            Text("Journey Start", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = origin,
                onValueChange = {},
                label = {Text("Journey Start")},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                interactionSource = startInteractionSource

            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            Text("Journey End", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = destination,
                onValueChange = {destination = it},
                label = {Text("Journey End")},
                modifier = Modifier.fillMaxWidth(),
                readOnly = true,
                interactionSource = endInteractionSource
            )
            HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
            Button(
                onClick = {
                    val oId = originPlaceId
                    val dId = destinationPlaceId
                    if (oId != null && dId != null && currentCarLicense != null) {
                        viewModel.addNewTrip(
                            originPlaceId = oId,
                            destinationPlaceId = dId,
                            origin = origin,
                            destination = destination
                        )
                        onBack()
                    }
                },
                enabled = originPlaceId != null && destinationPlaceId != null && currentCarLicense != null,
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) {Text("Add Journey")}
        }
    }
}