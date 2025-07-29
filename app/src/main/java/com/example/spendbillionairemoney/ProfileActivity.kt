package com.example.spendbillionairemoney

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.graphics.Shape
import androidx.compose.foundation.shape.CircleShape
import java.io.File

class ProfileActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val context = LocalContext.current
            val sharedPrefs = remember { context.getSharedPreferences("game_history", MODE_PRIVATE) }

            // Load history data with error handling
            var historyList by remember {
                mutableStateOf(
                    try {
                        val historyJson = sharedPrefs.getString("history_list", "[]") ?: "[]"
                        val type = object : TypeToken<List<HistoryEntry>>() {}.type
                        Gson().fromJson<List<HistoryEntry>>(historyJson, type) ?: emptyList()
                    } catch (e: Exception) {
                        Log.e("ProfileActivity", "Error loading history", e)
                        Toast.makeText(context, "Error loading history", Toast.LENGTH_SHORT).show()
                        emptyList()
                    }
                )
            }

            SpendBillionaireMoneyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier
                            .padding(16.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Home Button and Profile Header in a Row (HistoryActivity style)
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(12.dp)
                        ) {
                            IconButton(
                                onClick = { (context as? ComponentActivity)?.finish() },
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_home),
                                    contentDescription = "Go Home",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Your Profile",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

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
                                val resName = entry?.billionaireImagePath
                                val imageResId = resName?.let { res ->
                                    context.resources.getIdentifier(res, "drawable", context.packageName)
                                }?.takeIf { it != 0 }
                                val imagePath = entry?.billionaireImageUri ?: entry?.billionaireImagePath
                                Triple(it.key, it.value, Pair(imageResId, imagePath))
                            } ?: Triple("None", 0, Pair(null, null))

                        val imageModel: Any? = when {
                            !favoriteBillionaire.third.second.isNullOrEmpty() && File(favoriteBillionaire.third.second!!).exists() ->
                                File(favoriteBillionaire.third.second!!)
                            favoriteBillionaire.third.first != null -> favoriteBillionaire.third.first
                            else -> R.drawable.placeholder
                        }

                        // Total Spent Card
                        StatCard(
                            title = "Total Money Spent",
                            value = "$${"%,d".format(totalSpent)}"
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Most Purchased Item Card
                        StatCard(
                            title = "Most Purchased Item",
                            value = mostPurchasedItem.first,
                            subtitle = "${mostPurchasedItem.second} times",
//                            imageResId = mostPurchasedItem.third,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Favorite Billionaire Card
                        StatCard(
                            title = "Favorite Billionaire",
                            value = favoriteBillionaire.first,
                            subtitle = "${favoriteBillionaire.second} times",
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
                                                historyList = emptyList()
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
    subtitle: String? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(top = 4.dp)
            )
            if (subtitle != null) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

        }
    }
}