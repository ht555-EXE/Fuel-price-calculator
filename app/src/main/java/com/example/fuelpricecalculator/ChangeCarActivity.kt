package com.example.fuelpricecalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import com.example.fuelpricecalculator.ui.theme.FuelPriceCalculatorTheme
import kotlin.getValue
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Color

/**
 * Entry point for change car functionality.
 *
 * Initializes [FuelViewModel] through [FuelViewModelFactory],
 * using [FuelRepository] from [TripApplication], rendering [ChangeCarScreen]
 *
 */
class ChangeCarActivity : ComponentActivity() {
    private val fuelViewModel: FuelViewModel by viewModels {
        val repository = (application as TripApplication).repository
        FuelViewModelFactory(repository)
    }

    /**
     * Renders [ChangeCarScreen] and displays snack bars when observed from [fuelViewModel]
     *
     * @param savedInstanceState previous saved state of UI or null if this is a fresh start.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FuelPriceCalculatorTheme {
                val snackbarHostState = remember { SnackbarHostState() }

                //observer for snackbar messages through view model
                LaunchedEffect(Unit) {
                    fuelViewModel.uiEvent.collect { event ->
                        if (event is UIEvent.ShowSnackBar) {
                            snackbarHostState.showSnackbar(event.message)
                        }
                    }
                }
                ChangeCarScreen(
                    onBack = { finish() },
                    viewModel = fuelViewModel,
                    snackbarHostState = snackbarHostState
                )
            }
        }
    }
}

/**
 * Function for displaying change car screen UI.
 * Allows users to add a new vehicle based on a license plate,
 * pulling information from [DvlaApiService] through [FuelViewModel].
 * Allows users to select a vehicle they have previously added.
 *
 * @param onBack A callback invoked when the user exits the screen, via back button or after new
 * trip is added
 * @param viewModel view model instance passed for adding vehicles to persistent storage.
 * @param snackbarHostState manages the display state of snackbar messages
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeCarScreen(onBack: () -> Unit, viewModel: FuelViewModel,
                    snackbarHostState: SnackbarHostState) {
    // Collect the list of cars from the database
    val cars by viewModel.allCars.observeAsState(initial = emptyList())
    var regInput by remember { mutableStateOf("") }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("Change Car") },
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
            //field for adding a new car via license plate
            Text("Add New Vehicle", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = regInput,
                onValueChange = { regInput = it.uppercase() },
                label = { Text("Registration Number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Button(
                onClick = { viewModel.addNewCar(regInput){
                          regInput = ""}},
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                enabled = regInput != ""
            ) { Text("Search & Add") }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
            //saved vehicle section
            Text("Saved Vehicles", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            //message displayed for lack of any vehicles
            if (cars.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No Cars saved",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            } else {
                //Lazy list for current vehicles
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(cars) { car ->
                        val currentCar by rememberUpdatedState(car)
                        //logic for swipe to delete
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.deleteCar(currentCar)
                                    true
                                } else {
                                    false
                                }
                            }
                        )
                        SwipeToDismissBox(
                            state = dismissState,
                            enableDismissFromStartToEnd = false,
                            backgroundContent = {
                                val color =
                                    if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
                                        MaterialTheme.colorScheme.errorContainer
                                    } else Color.Transparent
                                Box(
                                    Modifier.fillMaxSize()
                                        .background(color, CardDefaults.shape)
                                        .padding(horizontal = 16.dp),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        ) {
                            Card(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    viewModel.selectCar(car.license)
                                    onBack()
                                }
                            ) {
                                Row(
                                    modifier = Modifier.padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(text = car.license, fontWeight = FontWeight.Bold)
                                        Text(text = "${car.make} ${car.colour}")
                                    }
                                    SuggestionChip(
                                        onClick = {},
                                        label = { Text(car.fuelType.name) })
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}