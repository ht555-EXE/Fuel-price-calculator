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

class ChangeCarActivity : ComponentActivity() {
    private val fuelViewModel: FuelViewModel by viewModels {
        val repository = (application as TripApplication).repository
        FuelViewModelFactory(repository)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FuelPriceCalculatorTheme {
                val snackbarHostState = remember { SnackbarHostState() }

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeCarScreen(onBack: () -> Unit, viewModel: FuelViewModel, snackbarHostState: SnackbarHostState) {
    // Collect the list of cars from the database
    val cars by viewModel.allCars.observeAsState(initial = emptyList())
    var regInput by remember { mutableStateOf("") }
    val context = LocalContext.current

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

            Text("Add New Vehicle", style = MaterialTheme.typography.titleMedium)
            OutlinedTextField(
                value = regInput,
                onValueChange = { regInput = it.uppercase() },
                label = { Text("Registration Number") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Button(
                onClick = { viewModel.addNewCar(regInput) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
            ) { Text("Search & Add") }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

            Text("Saved Vehicles", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
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
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(cars) { car ->
                        val currentCar by rememberUpdatedState(car)
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