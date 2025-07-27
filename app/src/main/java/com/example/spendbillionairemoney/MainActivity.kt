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
import java.io.File

class MainActivity : ComponentActivity() {
    // State to hold custom billionaires loaded from SharedPreferences
    val customBillionaireState = mutableStateOf(emptyList<Billionaire>())

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        // Clear custom billionaires on app launch
//        val sharedPrefs = getSharedPreferences("custom_billionaires", MODE_PRIVATE)
//        sharedPrefs.edit().remove("billionaire_list").apply()

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
        Billionaire("Bill Gates", 120_000_000_000, R.drawable.bill_gates),
        Billionaire("Elon Musk", 40_000_000_000, R.drawable.elon_musk),
        Billionaire("Jeff Bezos", 100_000_000_000, R.drawable.jeff_bezos)
    )
    val billionaires = listOf(Billionaire("Add New", 0, R.drawable.ic_add)) +
        customBillionaires +
        defaultBillionaires

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(billionaires) { billionaire ->
                if (billionaire.name == "Add New") {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onAddNewClicked() },
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_add),
                                contentDescription = "Add New Billionaire",
                                modifier = Modifier.size(48.dp)
                            )
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onBillionaireSelected(billionaire) },
                        elevation = CardDefaults.cardElevation(4.dp)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            val imageFile = billionaire.imagePath?.let { File(it) }
                            if (imageFile != null && imageFile.exists()) {
                                AsyncImage(
                                    model = imageFile,
                                    contentDescription = billionaire.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp),
                                    placeholder = painterResource(id = R.drawable.placeholder),
                                    error = painterResource(id = R.drawable.placeholder)
                                )
                            } else {
                                Image(
                                    painter = painterResource(id = billionaire.imageResId ?: R.drawable.placeholder),
                                    contentDescription = billionaire.name,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(120.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = billionaire.name, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = "$${billionaire.netWorth / 1_000_000_000} Billion",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }
        }

        Button(
            onClick = {
                context.startActivity(Intent(context, ProfileActivity::class.java))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
        ) {
            Text("View Profile")
        }
        Button(
            onClick = {
                context.startActivity(Intent(context, HistoryActivity::class.java))
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        ) {
            Text("View History")
        }
    }
}