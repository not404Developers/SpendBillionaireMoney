package com.example.spendbillionairemoney

import com.example.spendbillionairemoney.model.CustomBillionaire
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

import android.net.Uri
import android.content.Intent
import coil.compose.AsyncImage
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts


import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.spendbillionairemoney.ui.theme.SpendBillionaireMoneyTheme
import java.io.File

class CreateBillionaireActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SpendBillionaireMoneyTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var name by remember { mutableStateOf("") }
                    var netWorth by remember { mutableStateOf("") }

                    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

                    val imagePickerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri: Uri? ->
                        if (uri != null) {
                            selectedImageUri = uri
                        }
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.primaryContainer)
                                .padding(12.dp)
                        ) {
                            IconButton(
                                onClick = { finish() }
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.ic_home),
                                    contentDescription = "Go Home",
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Create Billionaire",
                                style = MaterialTheme.typography.headlineMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text("Create Custom Billionaire", style = MaterialTheme.typography.titleLarge)

                                TextField(
                                    value = name,
                                    onValueChange = { name = it },
                                    label = { Text("Name") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                )

                                TextField(
                                    value = netWorth,
                                    onValueChange = { netWorth = it },
                                    label = { Text("Net Worth (e.g. 1000000000)") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 8.dp)
                                )

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(150.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (selectedImageUri != null) {
                                        AsyncImage(
                                            model = selectedImageUri,
                                            contentDescription = "Selected Image",
                                            modifier = Modifier.fillMaxSize()
                                        )
                                    } else {
                                        Text("No Image Selected", style = MaterialTheme.typography.bodyMedium)
                                    }
                                }

                                Button(
                                    onClick = {
                                        imagePickerLauncher.launch("image/*")
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                ) {
                                    Text("Choose Photo")
                                }

                                Button(
                                    onClick = {
                                        if (name.isNotBlank() && netWorth.isNotBlank() && selectedImageUri != null) {
                                            try {
                                                val netWorthValue = netWorth.toLongOrNull() ?: run {
                                                    Toast.makeText(
                                                        this@CreateBillionaireActivity,
                                                        "Invalid net worth entered",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    return@Button
                                                }

                                                val imagePath = selectedImageUri?.let { uri ->
                                                    try {
                                                        // Create a unique filename
                                                        val filename = "billionaire_${System.currentTimeMillis()}.jpg"

                                                        // Get input stream from URI
                                                        contentResolver.openInputStream(uri)?.use { inputStream ->
                                                            // Create file in app's internal storage
                                                            val file = File(filesDir, filename)
                                                            file.outputStream().use { output ->
                                                                inputStream.copyTo(output)
                                                            }
                                                            file.absolutePath
                                                        }
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                        null
                                                    }
                                                } ?: ""

                                                val newBillionaire = CustomBillionaire(
                                                    name = name.trim(),
                                                    netWorth = netWorthValue,
                                                    imagePath = imagePath
                                                )

                                                val prefs = getSharedPreferences("custom_billionaires", MODE_PRIVATE)
                                                val json = prefs.getString("billionaire_list", "[]") ?: "[]"
                                                val type = object : TypeToken<MutableList<CustomBillionaire>>() {}.type
                                                val billionaireList: MutableList<CustomBillionaire> =
                                                    try {
                                                        Gson().fromJson(json, type) ?: mutableListOf()
                                                    } catch (e: Exception) {
                                                        mutableListOf()
                                                    }

                                                billionaireList.add(0, newBillionaire)
                                                prefs.edit()
                                                    .putString("billionaire_list", Gson().toJson(billionaireList))
                                                    .apply()

                                                Toast.makeText(
                                                    this@CreateBillionaireActivity,
                                                    "Billionaire created!",
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                                finish()

                                            } catch (e: Exception) {
                                                Toast.makeText(
                                                    this@CreateBillionaireActivity,
                                                    "Error: ${e.localizedMessage}",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                                e.printStackTrace()
                                            }
                                        } else {
                                            Toast.makeText(
                                                this@CreateBillionaireActivity,
                                                "Please enter name, net worth, and select an image",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                        }
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 8.dp)
                                ) {
                                    Text("Save")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}