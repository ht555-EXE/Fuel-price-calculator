package com.example.fuelpricecalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import com.example.fuelpricecalculator.ui.theme.FuelPriceCalculatorTheme
import kotlin.getValue
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

class ChangeCarActivity : ComponentActivity() {
    private val viewModel: FuelViewModel by viewModels {
        val repository = (application as TripApplication).repository
        FuelViewModelFactory(repository)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            FuelPriceCalculatorTheme {
                ChangeCarScreen(onBack = {finish()}, viewModel = viewModel)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeCarScreen(onBack: () -> Unit, viewModel: FuelViewModel) {
    var regInput by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Add Vehicle") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Enter the vehicle registration to fetch details from the DVLA.")

            OutlinedTextField(
                value = regInput,
                onValueChange = { regInput = it.uppercase().replace(" ", "") },
                label = { Text("Registration Number") },
                placeholder = { Text("e.g. AA19AAA") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                isError = errorMessage != null
            )

            if (errorMessage != null) {
                Text(text = errorMessage!!, color = MaterialTheme.colorScheme.error)
            }

            Button(
                onClick = {
                    isLoading = true
                    errorMessage = null
                    // You already hardcoded the key in the ViewModel earlier!
                    viewModel.addNewCar(regInput)

                    // Note: In a real app, you'd observe a "Success" state
                    // before finishing, but since Room updates automatically:
                    Toast.makeText(context, "Searching...", Toast.LENGTH_SHORT).show()
                    isLoading = false
                    onBack() // Go back to Home
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                enabled = regInput.isNotBlank() && !isLoading
            ) {
                if (isLoading) CircularProgressIndicator(color = Color.White)
                else Text("Search & Add Car")
            }
        }
    }
}