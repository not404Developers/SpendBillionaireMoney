package com.example.spendbillionairemoney

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.spendbillionairemoney.ui.theme.SpendBillionaireMoneyTheme
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class HistoryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPrefs = getSharedPreferences("game_history", MODE_PRIVATE)
        val historyJson = sharedPrefs.getString("history_list", "[]") ?: "[]"
        val type = object : TypeToken<List<HistoryEntry>>() {}.type
        val history: List<HistoryEntry> = try {
            Gson().fromJson(historyJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        setContent {
            SpendBillionaireMoneyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp)
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(12.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    startActivity(Intent(this@HistoryActivity, MainActivity::class.java))
                                    finish()
                                }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_home),
                                    contentDescription = "Home",
                                    modifier = Modifier.size(64.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "History",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        // Top Scores section
                        val topScores = history
                            .groupBy { it.billionaireName ?: "Unknown" }
                            .mapValues { entry -> entry.value.minByOrNull { it.timeTaken } }
                            .values
                            .filterNotNull()
                            .sortedBy { it.timeTaken }

                        if (history.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No history yet", style = MaterialTheme.typography.bodyLarge)
                            }
                        } else {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {
                                if (topScores.isNotEmpty()) {
                                    item {
                                        Card(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 16.dp),
                                            elevation = CardDefaults.cardElevation(4.dp),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Text(
                                                    text = "Top Scores",
                                                    style = MaterialTheme.typography.titleLarge,
                                                    modifier = Modifier.padding(bottom = 8.dp)
                                                )
                                                topScores.forEach { topEntry ->
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(vertical = 4.dp),
                                                        horizontalArrangement = Arrangement.SpaceBetween
                                                    ) {
                                                        Text(
                                                            text = topEntry.billionaireName ?: "Unknown",
                                                            style = MaterialTheme.typography.bodyLarge
                                                        )
                                                        Text(
                                                            text = "${topEntry.timeTaken} sec",
                                                            style = MaterialTheme.typography.bodyLarge
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                items(history) { entry ->
                                    HistoryCard(entry = entry)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HistoryCard(entry: HistoryEntry) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Larger centered billionaire image
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                ) {
                    val imageModel: Any = when {
                        !entry.billionaireImagePath.isNullOrEmpty() && File(entry.billionaireImagePath).exists() -> File(entry.billionaireImagePath)
                        entry.billionaireImage != 0 -> entry.billionaireImage
                        else -> R.drawable.placeholder
                    }

                    AsyncImage(
                        model = imageModel,
                        contentDescription = entry.billionaireName,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        placeholder = painterResource(id = R.drawable.placeholder),
                        error = painterResource(id = R.drawable.placeholder)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Centered billionaire name
                Text(
                    text = entry.billionaireName ?: "Unknown",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Money and time row with divider
                val totalSpent = entry.items?.sumOf { it.price * it.quantity } ?: 0
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Spent: ${formatPrice(totalSpent)}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Box(
                        modifier = Modifier
                            .height(20.dp)
                            .width(1.dp)
                            .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    )
                    Text(
                        text = "Time: ${entry.timeTaken} sec",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Items List
            var expanded by remember { mutableStateOf(false) }
            val itemsToShow = entry.items?.let { items ->
                if (expanded) items else items.take(2)
            } ?: emptyList()

            itemsToShow.filter { it.quantity > 0 }.forEach { item ->
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    // Item Image and Name
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(48.dp)) {
                            when {
                                !item.imageUri.isNullOrEmpty() && File(item.imageUri).exists() -> {
                                    AsyncImage(
                                        model = File(item.imageUri),
                                        contentDescription = item.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        placeholder = painterResource(id = R.drawable.placeholder),
                                        error = painterResource(id = R.drawable.placeholder)
                                    )
                                }
                                !item.imageUri.isNullOrEmpty() -> {
                                    AsyncImage(
                                        model = Uri.parse(item.imageUri),
                                        contentDescription = item.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        placeholder = painterResource(id = R.drawable.placeholder),
                                        error = painterResource(id = R.drawable.placeholder)
                                    )
                                }
                                item.imageResId != 0 -> {
                                    Image(
                                        painter = painterResource(id = item.imageResId),
                                        contentDescription = item.name,
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                                else -> {
                                    Image(
                                        painter = painterResource(id = R.drawable.placeholder),
                                        contentDescription = "Placeholder",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                }
                            }
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("${item.name} x${item.quantity}")
                    }

                    // Item Total Price
                    Text(formatPrice(item.price * item.quantity))
                }
            }

            // Show More/Less Button if needed
            if ((entry.items?.size ?: 0) > 2) {
                TextButton(
                    onClick = { expanded = !expanded },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text(if (expanded) "Show Less" else "Show More")
                }
            }
        }
    }
}

fun formatPrice(amount: Int): String {
    return when {
        amount >= 1_000_000_000 -> "$${"%.1f".format(amount / 1_000_000_000.0)}b"
        amount >= 1_000_000 -> "$${"%.1f".format(amount / 1_000_000.0)}m"
        else -> "$${"%,d".format(amount)}"
    }
}