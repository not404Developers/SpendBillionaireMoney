package com.example.spendbillionairemoney

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.spendbillionairemoney.ui.theme.SpendBillionaireMoneyTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import java.io.File

class ProfileActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            val sharedPrefs = remember { context.getSharedPreferences("game_history", MODE_PRIVATE) }

            // Load history data with error handling
            val historyList = remember {
                try {
                    val historyJson = sharedPrefs.getString("history_list", "[]") ?: "[]"
                    val type = object : TypeToken<List<HistoryEntry>>() {}.type
                    Gson().fromJson<List<HistoryEntry>>(historyJson, type) ?: emptyList()
                } catch (e: Exception) {
                    Log.e("ProfileActivity", "Error loading history", e)
                    Toast.makeText(context, "Error loading history", Toast.LENGTH_SHORT).show()
                    emptyList()
                }
            }

            SpendBillionaireMoneyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Profile Header
                        Text(
                            text = "Your Profile",
                            style = MaterialTheme.typography.headlineMedium,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )

                        // Statistics Section
                        val totalSpent = historyList.sumOf { entry ->
                            entry.items?.sumOf { it.price * it.quantity } ?: 0L
                        }

                        val mostPurchasedItem = historyList
                            .flatMap { it.items ?: emptyList() }
                            .groupBy { it.name }
                            .maxByOrNull { it.value.sumOf { item -> item.quantity } }
                            ?.let {
                                val totalQuantity = it.value.sumOf { item -> item.quantity }
                                Triple(it.key, totalQuantity, it.value.firstOrNull()?.imageResId)
                            } ?: Triple("None", 0, null)

                        val favoriteBillionaire = historyList
                            .groupingBy { it.billionaireName ?: "Unknown" }
                            .eachCount()
                            .maxByOrNull { it.value }
                            ?.let {
                                val entry = historyList.firstOrNull { item -> item.billionaireName == it.key }
                                Triple(it.key, it.value, entry?.billionaireImageUri ?: entry?.billionaireImagePath)
                            } ?: Triple("None", 0, null)

                        // Total Spent Card
                        StatCard(
                            title = "Total Money Spent",
                            value = "$${"%,d".format(totalSpent)}"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Most Purchased Item Card
                        StatCard(
                            title = "Most Purchased Item",
                            value = "${mostPurchasedItem.first} (${mostPurchasedItem.second} times)",
                            imageResId = mostPurchasedItem.third,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Favorite Billionaire Card
                        StatCard(
                            title = "Favorite Billionaire",
                            value = "${favoriteBillionaire.first} (${favoriteBillionaire.second} times)",
                            imagePath = favoriteBillionaire.third,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Delete Data Button
                        var showDeleteDialog by remember { mutableStateOf(false) }

                        Button(
                            onClick = { showDeleteDialog = true },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.errorContainer,
                                contentColor = MaterialTheme.colorScheme.error
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Delete All Data")
                        }

                        // Delete Confirmation Dialog
                        if (showDeleteDialog) {
                            AlertDialog(
                                onDismissRequest = { showDeleteDialog = false },
                                title = { Text("Confirm Deletion") },
                                text = { Text("This will permanently delete all your game data including history and custom billionaires.") },
                                confirmButton = {
                                    TextButton(
                                        onClick = {
                                            try {
                                                context.getSharedPreferences("game_history", MODE_PRIVATE)
                                                    .edit().clear().apply()
                                                context.getSharedPreferences("custom_billionaires", MODE_PRIVATE)
                                                    .edit().clear().apply()
                                                Toast.makeText(context, "All data deleted", Toast.LENGTH_SHORT).show()
                                            } catch (e: Exception) {
                                                Log.e("ProfileActivity", "Error deleting data", e)
                                                Toast.makeText(context, "Error deleting data", Toast.LENGTH_SHORT).show()
                                            }
                                            showDeleteDialog = false
                                        }
                                    ) {
                                        Text("Delete", color = MaterialTheme.colorScheme.error)
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDeleteDialog = false }) {
                                        Text("Cancel")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    imageResId: Int? = null,
    imagePath: String? = null,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(modifier = Modifier.height(8.dp))

            when {
                !imagePath.isNullOrEmpty() && File(imagePath).exists() -> {
                    AsyncImage(
                        model = File(imagePath),
                        contentDescription = title,
                        modifier = Modifier.size(100.dp),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.placeholder),
                        error = painterResource(id = R.drawable.placeholder)
                    )
                }
                imageResId != null -> {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = title,
                        modifier = Modifier.size(100.dp),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}