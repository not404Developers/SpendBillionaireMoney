package com.example.spendbillionairemoney

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import java.io.File

class MainActivity : ComponentActivity() {
    // State to hold custom billionaires loaded from SharedPreferences
    val customBillionaireState = mutableStateOf(emptyList<Billionaire>())

    override fun onCreate(savedInstanceState: Bundle?) {

        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)

        setContent {
            SpendBillionaireMoneyTheme {
                val customBillionaires = customBillionaireState.value
                Surface(modifier = Modifier.fillMaxSize()) {
                    BillionaireSelectionScreen(
                        customBillionaires = customBillionaires,
                        onBillionaireSelected = { billionaire ->
                            val intent = Intent(this, GameActivity::class.java).apply {
                                putExtra("name", billionaire.name)
                                putExtra("netWorth", billionaire.netWorth)
                                putExtra("imageResId", billionaire.imageResId ?: 0)
                                putExtra("imagePath", billionaire.imagePath)
                            }
                            startActivity(intent)
                        },
                        onAddNewClicked = {
                            val intent = Intent(this, CreateBillionaireActivity::class.java)
                            startActivity(intent)
                        }
                    )
                }
            }
        }
        // Load custom billionaires initially
        loadCustomBillionaires()
    }

    override fun onResume() {
        super.onResume()
        loadCustomBillionaires()
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
}

@Composable
fun BillionaireSelectionScreen(
    customBillionaires: List<Billionaire>,
    onBillionaireSelected: (Billionaire) -> Unit,
    onAddNewClicked: () -> Unit
) {
    val context = LocalContext.current
    val defaultBillionaires = listOf(
        Billionaire("Bill Gates", 180_000_000_000, R.drawable.bill_gates),
        Billionaire("Elon Musk", 380_000_000_000, R.drawable.elon_musk),
        Billionaire("Jeff Bezos", 230_000_000_000, R.drawable.jeff_bezos)
    )
    val billionaires = listOf(Billionaire("Add New", 0, R.drawable.ic_add)) +
            customBillionaires +
            defaultBillionaires

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
                items(billionaires) { billionaire ->
                    if (billionaire.name == "Add New") {
                        AddNewCard(onClick = onAddNewClicked)
                    } else {
                        BillionaireCard(
                            billionaire = billionaire,
                            onClick = { onBillionaireSelected(billionaire) }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AddNewCard(onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(0.9f)
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
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

@Composable
private fun BillionaireCard(billionaire: Billionaire, onClick: () -> Unit) {
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
}