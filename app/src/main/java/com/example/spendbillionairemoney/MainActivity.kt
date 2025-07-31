package com.example.spendbillionairemoney

import android.util.Log
import android.os.Handler
import android.os.Looper
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.google.android.gms.ads.LoadAdError
import android.widget.Toast
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.spendbillionairemoney.model.Billionaire
import com.example.spendbillionairemoney.model.CustomBillionaire
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.example.spendbillionairemoney.ui.theme.SpendBillionaireMoneyTheme
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.zIndex
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import java.io.File

class MainActivity : ComponentActivity() {
    // State to hold custom billionaires loaded from SharedPreferences
    val customBillionaireState = mutableStateOf(emptyList<Billionaire>())

    var rewardedAd: RewardedAd? = null
    var rewardedAdPremium: RewardedAd? = null
    var isAdLoading by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        MobileAds.initialize(this)
        loadRewardedAdPremium()

        setContent {
            SpendBillionaireMoneyTheme {
                val customBillionaires = customBillionaireState.value
                Surface(modifier = Modifier.fillMaxSize()) {
                    BillionaireSelectionScreen(
                        customBillionaires = customBillionaires,
                        onBillionaireSelected = { billionaire ->
                            if (billionaire.isPremium) {
                                showRewardedAd(billionaire)
                            } else {
                                startGameActivity(billionaire)
                            }
                        },
                        isAdLoading = isAdLoading
                    )
                }
            }
        }
        loadCustomBillionaires()
    }

    override fun onResume() {
        super.onResume()
        loadCustomBillionaires()
        loadRewardedAd()
    }

    private fun startGameActivity(billionaire: Billionaire) {
        val intent = Intent(this, GameActivity::class.java).apply {
            putExtra("name", billionaire.name)
            putExtra("netWorth", billionaire.netWorth)
            putExtra("imageResId", billionaire.imageResId ?: 0)
            putExtra("imagePath", billionaire.imagePath)
        }
        startActivity(intent)
    }

    private fun showRewardedAd(billionaire: Billionaire) {
        if (rewardedAdPremium != null) {
            rewardedAdPremium?.show(this) {
                startGameActivity(billionaire)
            }
        } else {
            Toast.makeText(this, "Loading ad, please try again in a moment", Toast.LENGTH_SHORT).show()
            loadRewardedAdPremium()
        }
    }

    private fun loadCustomBillionaires() {
        val sharedPrefs = getSharedPreferences("custom_billionaires", MODE_PRIVATE)
        val json = sharedPrefs.getString("billionaire_list", "[]")
        val type = object : TypeToken<List<CustomBillionaire>>() {}.type
        val customList: List<CustomBillionaire> = Gson().fromJson(json, type)

        customBillionaireState.value = customList.map {
            Billionaire(it.name, it.netWorth, imagePath = it.imagePath)
        }
    }

    private fun loadRewardedAd() {
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            this,
            "ca-app-pub-2318663517302041/5382547909",
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d("RewardAd", "Ad successfully loaded")
                    rewardedAd = ad
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("RewardAd", "Ad failed to load: ${adError.message}")
                    rewardedAd = null
                }
            }
        )
    }

    private fun loadRewardedAdPremium() {
        if (isAdLoading) return

        isAdLoading = true
        val adRequest = AdRequest.Builder().build()
        RewardedAd.load(
            this,
            "ca-app-pub-2318663517302041/3009952854",
            adRequest,
            object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d("RewardAdPremium", "Premium Ad successfully loaded")
                    rewardedAdPremium = ad
                    isAdLoading = false
                }
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("RewardAdPremium", "Premium Ad failed to load: ${adError.message}")
                    rewardedAdPremium = null
                    isAdLoading = false
                    // Retry loading ad after short delay
                    Handler(Looper.getMainLooper()).postDelayed({
                        loadRewardedAdPremium()
                    }, 5000)
                }
            }
        )
    }
}

@Composable
fun BillionaireSelectionScreen(
    customBillionaires: List<Billionaire>,
    onBillionaireSelected: (Billionaire) -> Unit,
    isAdLoading: Boolean
) {
    val defaultBillionaires = listOf(
        Billionaire("Bill Gates", 180_000_000_000, R.drawable.bill_gates),
        Billionaire("Elon Musk", 380_000_000_000, R.drawable.elon_musk),
        Billionaire("Mukesh Ambani", 90_000_000_000, R.drawable.mukesh_ambani, isPremium = true),
        Billionaire("Jeff Bezos", 230_000_000_000, R.drawable.jeff_bezos),
        Billionaire("Sergrey Brin", 230_000_000_000, R.drawable.sergrey_brin),
        Billionaire("Steve Ballmer", 152_000_000_000, R.drawable.steve_ballmer, isPremium = true, isHard = true),
        Billionaire("Mark Zuckerberg", 172_000_000_000, R.drawable.mark_zuckerberg),
        Billionaire("Lary Page", 150_000_000_000, R.drawable.larry_page),
        Billionaire("Warren Buffet", 150_000_000_000, R.drawable.warren_buffet, isPremium = true),
        Billionaire("Gautam Adani", 70_000_000_000, R.drawable.gautam_adani, isPremium = true),
        Billionaire("Bernard Arnault", 155_000_000_000, R.drawable.bernard_arnault),
        Billionaire("Jack Ma", 34_000_000_000, R.drawable.jack_ma, isHard = true),
        Billionaire("Michael Bloomberg", 96_000_000_000, R.drawable.michael_bloomberg, isPremium = true),
    )
    val billionaires = customBillionaires + defaultBillionaires
    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // App title with navigation buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(
                    onClick = {
                        context.startActivity(Intent(context, HistoryActivity::class.java))
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_history),
                        contentDescription = "History",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Text(
                    text = "Spend Billionaire's Money",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(horizontal = 8.dp)
                )

                IconButton(
                    onClick = {
                        context.startActivity(Intent(context, ProfileActivity::class.java))
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_profile),
                        contentDescription = "Profile",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Text(
                text = "Select a Billionaire",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f),
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .align(Alignment.CenterHorizontally)
            )

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp)
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .aspectRatio(0.9f)
                                .clickable {
                                    context.startActivity(Intent(context, CreateBillionaireActivity::class.java))
                                },
                            elevation = CardDefaults.cardElevation(8.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Box(modifier = Modifier.fillMaxSize()) {
                                Column(
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                        .padding(16.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_add),
                                        contentDescription = "Add New Billionaire",
                                        modifier = Modifier.size(48.dp),
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "Create Custom",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                }
                            }
                        }
                    }
                }
                items(billionaires) { billionaire ->
                    BillionaireCard(
                        billionaire = billionaire,
                        onClick = { onBillionaireSelected(billionaire) },
                        isLoading = billionaire.isPremium && isAdLoading
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun BillionaireCard(
    billionaire: Billionaire,
    onClick: () -> Unit,
    isLoading: Boolean
) {
    Box {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(0.9f)
                .clickable(onClick = onClick),
            elevation = CardDefaults.cardElevation(8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier.padding(12.dp)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .clip(MaterialTheme.shapes.medium),
                    contentAlignment = Alignment.Center
                ) {
                    val imageFile = billionaire.imagePath?.let { File(it) }
                    if (imageFile != null && imageFile.exists()) {
                        AsyncImage(
                            model = imageFile,
                            contentDescription = billionaire.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop,
                            placeholder = painterResource(id = R.drawable.placeholder),
                            error = painterResource(id = R.drawable.placeholder)
                        )
                    } else {
                        Image(
                            painter = painterResource(id = billionaire.imageResId ?: R.drawable.placeholder),
                            contentDescription = billionaire.name,
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = billionaire.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )

                Text(
                    text = "$${"%,d".format(billionaire.netWorth / 1_000_000_000)} Billion",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Only show overlay if premium
        if (billionaire.isPremium) {
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .background(androidx.compose.ui.graphics.Color(0x66000000))
                    .zIndex(1f),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = androidx.compose.ui.graphics.Color.White)
                } else {
                    Text(
                        "Watch Ad",
                        color = androidx.compose.ui.graphics.Color.White,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }

        // "Premium" tag in top-left corner
        if (billionaire.isPremium) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(6.dp)
                    .background(
                        color = androidx.compose.ui.graphics.Color(0xFF388E3C),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .zIndex(2f)
            ) {
                Text(
                    "Premium",
                    color = androidx.compose.ui.graphics.Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }

        // Hard tag
        if (billionaire.isHard) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(6.dp)
                    .background(
                        color = androidx.compose.ui.graphics.Color(0xFFD32F2F),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .zIndex(2f)
            ) {
                Text(
                    "Hard",
                    color = androidx.compose.ui.graphics.Color.White,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}