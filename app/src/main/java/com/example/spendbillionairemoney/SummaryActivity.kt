package com.example.spendbillionairemoney

import com.google.android.gms.ads.LoadAdError

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.CircleShape
import coil.compose.AsyncImage
import com.example.spendbillionairemoney.ui.theme.SpendBillionaireMoneyTheme
import java.io.File
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.FullScreenContentCallback

class SummaryActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val elapsedTime = intent.getIntExtra("elapsedTime", 0)
        val billionaireName = intent.getStringExtra("billionaireName") ?: "Unknown"
        val billionaireImageResId = intent.getIntExtra("billionaireImageResId", 0)
        val billionaireImagePath = intent.getStringExtra("imagePath")
        val summaryItems = intent.getParcelableArrayListExtra<SummaryItem>("summaryItems")
            ?.filter { it.quantity > 0 } ?: emptyList()

        MobileAds.initialize(this) {}

        val adRequest = AdRequest.Builder().build()
        InterstitialAd.load(
            this,
            "ca-app-pub-2318663517302041/5691655191",
            adRequest,
            object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    ad.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            // Proceed to show the content after ad is dismissed
                            showSummaryContent(elapsedTime, billionaireName, billionaireImageResId, billionaireImagePath, summaryItems)
                        }
                    }
                    ad.show(this@SummaryActivity)
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.d("SummaryActivity", "Ad failed to load: $adError")
                    showSummaryContent(elapsedTime, billionaireName, billionaireImageResId, billionaireImagePath, summaryItems)
                }
            }
        )
    }

    private fun showSummaryContent(
        elapsedTime: Int,
        billionaireName: String,
        billionaireImageResId: Int,
        billionaireImagePath: String?,
        summaryItems: List<SummaryItem>
    ) {
        setContent {
            SpendBillionaireMoneyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(12.dp)
                        ) {
                            IconButton(
                                onClick = {
                                    startActivity(Intent(this@SummaryActivity, MainActivity::class.java))
                                    finish()
                                }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_home),
                                    contentDescription = "Return Home",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Congratulations",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

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
                                .size(140.dp)
                                .clip(CircleShape)
                                .align(Alignment.CenterHorizontally),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.placeholder),
                            error = painterResource(id = R.drawable.placeholder)
                        )

                        Text(
                            text = billionaireName,
                            style = MaterialTheme.typography.headlineSmall,
                            modifier = Modifier
                                .padding(vertical = 8.dp)
                                .align(Alignment.CenterHorizontally)
                        )

                        Text(
                            text = "You spent all the money",
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier
                                .padding(bottom = 8.dp)
                                .align(Alignment.CenterHorizontally)
                        )

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min)
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "Spent: ${formatPrice(summaryItems.sumOf { it.price.toLong() * it.quantity.toLong() })}")
                            }

                            Divider(
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(1.dp)
                            )

                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .fillMaxHeight(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(text = "Time: ${elapsedTime}s")
                            }
                        }

                        Text(
                            text = "Items Purchased:",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(top = 8.dp, bottom = 8.dp)
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
    Card(modifier = Modifier.padding(vertical = 6.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
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
            Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(text = "Qty: ${item.quantity}", style = MaterialTheme.typography.bodySmall)
            }

            // Item Total
            Text(
                text = formatPrice(item.price.toLong() * item.quantity.toLong()),
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

fun formatPrice(value: Long): String {
    return when {
        value >= 1_000_000_000L -> "$${value / 1_000_000_000}b"
        value >= 1_000_000L -> "$${value / 1_000_000}m"
        else -> "$$value"
    }
}