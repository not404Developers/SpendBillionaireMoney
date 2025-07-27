package com.example.spendbillionairemoney

import android.os.Bundle
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.addCallback
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import com.example.spendbillionairemoney.model.Item
import com.example.spendbillionairemoney.SummaryItem
import com.google.gson.reflect.TypeToken
import com.google.gson.Gson

data class ItemState(
    val item: Item,
    val quantity: MutableState<Int> = mutableStateOf(0)
)

class GameActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        var showExitDialog by mutableStateOf(false)

        val name = intent.getStringExtra("name")
        val netWorth = intent.getLongExtra("netWorth", 0)
        val imageResId = intent.getIntExtra("imageResId", 0)
        val imagePath = intent.getStringExtra("imagePath")

        val itemStates = mutableStateListOf(
            ItemState(Item("Castle in Europe", 120_000_000, R.drawable.castle_in_europe)),
            ItemState(Item("Feed 100,000 Families", 35_000_000, R.drawable.feed_1_00_000_families)),
            ItemState(Item("Yacht", 300_000_000, R.drawable.yacht)),
            ItemState(Item("Fighter Jet", 150_000_000, R.drawable.fighter_jet)),
            ItemState(Item("Diamond Ring", 3_000_000, R.drawable.diamond_ring)),
            ItemState(Item("Skyscraper", 850_000_000, R.drawable.skyscraper)),
            ItemState(Item("Gaming PC Setup", 25_000, R.drawable.gaming_pcs)),
            ItemState(Item("Plant 1 Million Trees", 10_000_000, R.drawable.plant_1_million_trees)),
            ItemState(Item("Rolls Royce Phantom", 450_000, R.drawable.rolls_royce_phantom)),
            ItemState(Item("Satellite Launch", 70_000_000, R.drawable.satellite)),
            ItemState(Item("Monster Truck", 250_000, R.drawable.monster_truck)),
            ItemState(Item("Support 1,000 Orphans", 8_000_000, R.drawable.support_1_000_orphans)),
            ItemState(Item("Helicopter", 18_000_000, R.drawable.helicopter)),
            ItemState(Item("Luxury Villa", 80_000_000, R.drawable.luxury_villa)),
            ItemState(Item("Bugatti Chiron", 3_000_000, R.drawable.bugatti_chiron)),
            ItemState(Item("F1 Race Car", 20_000_000, R.drawable.f1_race_car)),
            ItemState(Item("Buy a Football Club", 600_000_000, R.drawable.buy_a_football_club)),
            ItemState(Item("Ice Cream Truck", 60_000, R.drawable.ice_cream_truck)),
            ItemState(Item("Banana Farm", 50_000, R.drawable.bananas)),
            ItemState(Item("Apartment Building", 30_000_000, R.drawable.apartment_building)),
            ItemState(Item("Desert Palace", 200_000_000, R.drawable.desert_palace)),
            ItemState(Item("Lamborghini Aventador", 500_000, R.drawable.lamborghini_aventador)),
            ItemState(Item("Buy McDonald's Franchise", 2_500_000, R.drawable.buy_a_mcdonald_s_franchise)),
            ItemState(Item("Private Zoo", 25_000_000, R.drawable.private_zoo)),
            ItemState(Item("USB Flash Drives (1,000)", 1_000, R.drawable.usb_flash_drives)),
            ItemState(Item("Harley Davidson", 70_000, R.drawable.harley_davidson)),
            ItemState(Item("Private Jet", 50_000_000, R.drawable.private_jet)),
            ItemState(Item("SpaceX Falcon 9 Ride", 90_000_000, R.drawable.spacex_falcon_9_ride)),
            ItemState(Item("VR Headset", 3_000, R.drawable.vr_headset)),
            ItemState(Item("Concorde Jet", 200_000_000, R.drawable.concorde)),
            ItemState(Item("Hot Air Balloon", 45_000, R.drawable.hot_air_balloon)),
            ItemState(Item("Golf Course", 150_000_000, R.drawable.golf_course)),
            ItemState(Item("Mountain Cabin", 8_000_000, R.drawable.mountain_cabin)),
            ItemState(Item("Jet Ski", 120_000, R.drawable.jet_ski)),
            ItemState(Item("Private Library", 2_000_000, R.drawable.private_library)),
            ItemState(Item("Boeing 747", 400_000_000, R.drawable.boeing_747)),
            ItemState(Item("NASA Space Suit", 12_000_000, R.drawable.nasa_space_suit)),
            ItemState(Item("Beach House", 60_000_000, R.drawable.beach_house)),
            ItemState(Item("iPhone 15 Pro Max", 1_500, R.drawable.iphone_15_pro_max)),
            ItemState(Item("Bulletproof SUV", 500_000, R.drawable.bulletproof_suv)),
            ItemState(Item("Jet (Mid-Range)", 35_000_000, R.drawable.jet)),
            ItemState(Item("Rolex Watch", 75_000, R.drawable.rolex_watch)),
            ItemState(Item("Airship", 250_000_000, R.drawable.airship)),
        )

        setContent {
            val dialogState = showExitDialog
            var remainingMoney by remember { mutableStateOf(netWorth) }
            var elapsedTime by remember { mutableStateOf(0) }
            val imageUri = remember { intent.getStringExtra("imageUri") }

            // Independent timer
            LaunchedEffect(Unit) {
                while (true) {
                    delay(1000L)
                    elapsedTime++
                }
            }

            // Navigation trigger
            LaunchedEffect(remainingMoney) {
                if (remainingMoney <= 0) {
                    val intent = Intent(this@GameActivity, SummaryActivity::class.java).apply {
                        putExtra("elapsedTime", elapsedTime)
                        putExtra("billionaireName", name)
                        putExtra("billionaireImageResId", imageResId)
                        putExtra("imagePath", imagePath)
                        putParcelableArrayListExtra(
                            "summaryItems",
                            arrayListOf<SummaryItem>().apply {
                                itemStates.forEach {
                                    add(SummaryItem(it.item.name, it.item.price, it.item.imageResId, it.quantity.value, it.item.imageUri))
                                }
                            }
                        )
                    }

                    val sharedPrefs = getSharedPreferences("game_history", MODE_PRIVATE)
                    val historyJson = sharedPrefs.getString("history_list", "[]")
                    val type = object : TypeToken<MutableList<HistoryEntry>>() {}.type
                    val historyList: MutableList<HistoryEntry> = Gson().fromJson(historyJson, type)

                    val newEntry = HistoryEntry(
                        billionaireName = name ?: "Unknown",
                        timeTaken = elapsedTime,
                        billionaireImage = imageResId,
                        billionaireImageUri = imageUri,
                        billionaireImagePath = imagePath,
                        timestamp = System.currentTimeMillis(),
                        items = itemStates
                            .filter { it.quantity.value > 0 }
                            .map {
                                SummaryItem(it.item.name, it.item.price, it.item.imageResId, it.quantity.value, it.item.imageUri)
                            }
                    )

                    historyList.add(0, newEntry)
                    sharedPrefs.edit().putString("history_list", Gson().toJson(historyList)).apply()

                    startActivity(intent)
                    finish()
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Enhanced billionaire image handling: check for local imagePath, then imageUri, then fallback
                when {
                    !imagePath.isNullOrEmpty() -> {
                        val imageFile = java.io.File(imagePath)
                        if (imageFile.exists()) {
                            AsyncImage(
                                model = imageFile,
                                contentDescription = name,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(160.dp)
                                    .padding(bottom = 16.dp)
                            )
                        }
                    }
                    !imageUri.isNullOrEmpty() -> {
                        AsyncImage(
                            model = imageUri,
                            contentDescription = name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .padding(bottom = 16.dp)
                        )
                    }
                    else -> {
                        Image(
                            painter = painterResource(id = imageResId),
                            contentDescription = name,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .padding(bottom = 16.dp)
                        )
                    }
                }

                Text(
                    text = "Money Left: $${remainingMoney}",
                    style = MaterialTheme.typography.headlineSmall
                )

                Text(
                    text = "Time: ${elapsedTime}s",
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
                )

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(itemStates) { itemState ->
                        val item = itemState.item
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Image(
                                    painter = painterResource(id = item.imageResId),
                                    contentDescription = item.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(100.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(text = item.name, style = MaterialTheme.typography.titleMedium)
                                Text(text = "Price: $${item.price}", style = MaterialTheme.typography.bodyMedium)
                                Text(text = "Quantity: ${itemState.quantity.value}", style = MaterialTheme.typography.bodySmall)

                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Button(
                                        onClick = {
                                            if (remainingMoney >= item.price) {
                                                remainingMoney -= item.price
                                                itemState.quantity.value += 1
                                            }
                                        }
                                    ) {
                                        Text("Buy")
                                    }
                                    Button(
                                        onClick = {
                                            if (itemState.quantity.value > 0) {
                                                remainingMoney += item.price
                                                itemState.quantity.value -= 1
                                            }
                                        }
                                    ) {
                                        Text("Sell")
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (dialogState) {
                AlertDialog(
                    onDismissRequest = { showExitDialog = false },
                    title = { Text("Exit Game") },
                    text = { Text("Are you sure you want to exit? All progress will be lost.") },
                    confirmButton = {
                        TextButton(onClick = {
                            showExitDialog = false
                            finish()
                        }) {
                            Text("Exit")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showExitDialog = false }) {
                            Text("Cancel")
                        }
                    }
                )
            }
        }

        onBackPressedDispatcher.addCallback(this) {
            showExitDialog = true
        }
    }
}