package com.example.spendbillionairemoney

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.spendbillionairemoney.ui.theme.SpendBillionaireMoneyTheme
import java.io.File

class SummaryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val elapsedTime = intent.getIntExtra("elapsedTime", 0)
        val billionaireName = intent.getStringExtra("billionaireName") ?: "Unknown"
        val billionaireImageResId = intent.getIntExtra("billionaireImageResId", 0)
        val billionaireImagePath = intent.getStringExtra("imagePath")
        val summaryItems = intent.getParcelableArrayListExtra<SummaryItem>("summaryItems")
            ?.filter { it.quantity > 0 } ?: emptyList()

        setContent {
            SpendBillionaireMoneyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Button(
                            onClick = {
                                startActivity(Intent(this@SummaryActivity, MainActivity::class.java))
                                finish()
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Return to Home")
                        }

                        Text(
                            text = "You spent all the money in $elapsedTime seconds!",
                            style = MaterialTheme.typography.titleLarge
                        )

                        // Billionaire Image Section
                        val imageModel: Any = remember(billionaireImagePath, billionaireImageResId) {
                            when {
                                !billionaireImagePath.isNullOrEmpty() && File(billionaireImagePath).exists() -> {
                                    Log.d("SummaryActivity", "Loading image from path: $billionaireImagePath")
                                    File(billionaireImagePath)
                                }
                                billionaireImageResId != 0 -> {
                                    Log.d("SummaryActivity", "Loading image from resource ID: $billionaireImageResId")
                                    billionaireImageResId
                                }
                                else -> {
                                    Log.d("SummaryActivity", "Using placeholder image")
                                    R.drawable.placeholder
                                }
                            }
                        }

                        AsyncImage(
                            model = imageModel,
                            contentDescription = billionaireName,
                            modifier = Modifier
                                .height(160.dp)
                                .fillMaxWidth(),
                            contentScale = ContentScale.Fit,
                            placeholder = painterResource(id = R.drawable.placeholder),
                            error = painterResource(id = R.drawable.placeholder)
                        )

                        // Items Bought Section
                        Text(
                            text = "Items Purchased:",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 16.dp)
                        )

                        LazyColumn(modifier = Modifier.padding(top = 8.dp)) {
                            items(summaryItems) { item ->
                                PurchaseItemRow(item = item)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PurchaseItemRow(item: SummaryItem) {
    Card(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Item Image
            AsyncImage(
                model = when {
                    item.imageResId != 0 -> item.imageResId
                    else -> R.drawable.placeholder
                },
                contentDescription = item.name,
                modifier = Modifier.size(64.dp),
                contentScale = ContentScale.Crop
            )

            // Item Details
            Column(modifier = Modifier.weight(1f).padding(start = 8.dp)) {
                Text(text = item.name, style = MaterialTheme.typography.bodyLarge)
                Text(text = "Qty: ${item.quantity}", style = MaterialTheme.typography.bodySmall)
            }

            // Item Total
            Text(
                text = "$${item.price * item.quantity}",
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}