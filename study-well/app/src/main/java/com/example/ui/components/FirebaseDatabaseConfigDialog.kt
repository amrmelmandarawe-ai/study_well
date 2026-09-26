package com.example.ui.components

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.AppDatabase
import com.example.data.firebase.CloudStats
import com.example.data.firebase.FirebaseDatabaseService
import com.example.data.firebase.SyncSummary
import com.example.ui.theme.SleekBlue400
import com.example.ui.theme.SleekBlue600
import com.example.ui.theme.SleekGold400
import com.example.ui.theme.SleekNavy800
import com.example.ui.theme.SleekNavy900
import com.example.ui.theme.SleekNavy950
import com.example.ui.theme.sleekTextFieldColors
import kotlinx.coroutines.launch

@Composable
fun FirebaseDatabaseConfigDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val database = remember { AppDatabase.getDatabase(context) }

    var apiKey by remember { mutableStateOf("") }
    var projectId by remember { mutableStateOf("") }
    var appId by remember { mutableStateOf("") }
    var dbUrl by remember { mutableStateOf("") }

    var isConnected by remember { mutableStateOf(false) }
    var isTesting by remember { mutableStateOf(false) }
    var isPullingCloud by remember { mutableStateOf(false) }
    var isPushingCloud by remember { mutableStateOf(false) }

    var testResultMessage by remember { mutableStateOf<String?>(null) }
    var testResultSuccess by remember { mutableStateOf<Boolean?>(null) }
    var cloudStats by remember { mutableStateOf<CloudStats?>(null) }
    var syncStatusBanner by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        val config = FirebaseDatabaseService.getFirebaseConfig(context)
        apiKey = config.apiKey
        projectId = config.projectId
        appId = config.appId
        dbUrl = config.databaseUrl
        isConnected = FirebaseDatabaseService.isFirebaseAvailable(context)

        if (isConnected) {
            scope.launch {
                cloudStats = FirebaseDatabaseService.getCloudStatistics(context)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier.testTag("firebase_config_dialog"),
        containerColor = SleekNavy800,
        shape = RoundedCornerShape(24.dp),
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    shape = CircleShape,
                    color = SleekBlue600.copy(alpha = 0.25f),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Storage,
                            contentDescription = null,
                            tint = SleekBlue400,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Upgraded Firebase Cloud Database",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Cloud Firestore Real-Time Synchronizer",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF94A3B8)
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(top = 4.dp)
            ) {
                // Connection Status Badge
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = if (isConnected) Color(0xFF0F2E22) else Color(0xFF2E1515),
                    border = BorderStroke(
                        1.dp,
                        if (isConnected) Color(0xFF10B981).copy(alpha = 0.5f) else Color(0xFFEF4444).copy(alpha = 0.5f)
                    ),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isConnected) Icons.Default.CloudDone else Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = if (isConnected) Color(0xFF34D399) else Color(0xFFF87171),
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = if (isConnected) "Active Upgraded Firebase Cloud" else "Local Persistent SQLite Mode",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = if (isConnected)
                                    "Connected to project: $projectId. Offline persistence enabled with zero UI lag."
                                else
                                    "Save credentials below to connect to live Cloud Firestore.",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    }
                }

                // Cloud Stats Summary
                cloudStats?.let { stats ->
                    if (stats.connected) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Card(
                            shape = RoundedCornerShape(14.dp),
                            colors = CardDefaults.cardColors(containerColor = SleekNavy950),
                            border = BorderStroke(1.dp, Color(0xFF334155)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = "Cloud Records in Firestore",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SleekGold400
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    CloudStatItem("Materials", stats.materialsCount.toString())
                                    CloudStatItem("Users", stats.usersCount.toString())
                                    CloudStatItem("Quizzes", stats.quizAttemptsCount.toString())
                                    CloudStatItem("Activities", stats.activitiesCount.toString())
                                }
                            }
                        }
                    }
                }

                // Sync status banner if any operation just ran
                syncStatusBanner?.let { banner ->
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = SleekBlue600.copy(alpha = 0.2f),
                        border = BorderStroke(1.dp, SleekBlue400.copy(alpha = 0.5f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = banner,
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.White,
                            modifier = Modifier.padding(10.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Cloud Sync Action Buttons
                Text(
                    text = "Cloud Synchronization Actions",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = SleekGold400
                )
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Pull from Cloud Button
                    Button(
                        onClick = {
                            scope.launch {
                                isPullingCloud = true
                                syncStatusBanner = "Downloading latest materials, users, and quizzes from Cloud..."
                                val summary = FirebaseDatabaseService.pullAllFromFirestore(context, database)
                                isPullingCloud = false
                                syncStatusBanner = summary.message
                                cloudStats = FirebaseDatabaseService.getCloudStatistics(context)
                                Toast.makeText(context, summary.message, Toast.LENGTH_LONG).show()
                            }
                        },
                        enabled = !isPullingCloud && !isPushingCloud,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = SleekBlue600),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("pull_from_cloud_button")
                    ) {
                        if (isPullingCloud) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = Color.White
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pulling...", fontSize = 12.sp)
                        } else {
                            Icon(Icons.Default.CloudDownload, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pull Cloud", fontSize = 12.sp)
                        }
                    }

                    // Push to Cloud Button
                    Button(
                        onClick = {
                            scope.launch {
                                isPushingCloud = true
                                syncStatusBanner = "Uploading local database records to Cloud Firestore..."
                                val summary = FirebaseDatabaseService.pushAllToFirestore(context, database)
                                isPushingCloud = false
                                syncStatusBanner = summary.message
                                cloudStats = FirebaseDatabaseService.getCloudStatistics(context)
                                Toast.makeText(context, summary.message, Toast.LENGTH_LONG).show()
                            }
                        },
                        enabled = !isPullingCloud && !isPushingCloud,
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B)),
                        border = BorderStroke(1.dp, SleekBlue400),
                        modifier = Modifier
                            .weight(1f)
                            .testTag("push_to_cloud_button")
                    ) {
                        if (isPushingCloud) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = SleekBlue400
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Uploading...", fontSize = 12.sp)
                        } else {
                            Icon(Icons.Default.CloudUpload, contentDescription = null, modifier = Modifier.size(16.dp), tint = SleekBlue400)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Backup Cloud", fontSize = 12.sp, color = SleekBlue400)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Firebase Credentials & Project",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = SleekGold400
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = projectId,
                    onValueChange = { projectId = it },
                    label = { Text("Project ID") },
                    placeholder = { Text("studywell-igcse-firebase") },
                    singleLine = true,
                    colors = sleekTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("firebase_project_id_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    placeholder = { Text("AIzaSy...") },
                    singleLine = true,
                    colors = sleekTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("firebase_api_key_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = appId,
                    onValueChange = { appId = it },
                    label = { Text("App ID") },
                    placeholder = { Text("1:1000000000000:android:...") },
                    singleLine = true,
                    colors = sleekTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("firebase_app_id_input")
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = dbUrl,
                    onValueChange = { dbUrl = it },
                    label = { Text("Realtime / Firestore Database URL (Optional)") },
                    placeholder = { Text("https://studywell-igcse-firebase.firebaseio.com") },
                    singleLine = true,
                    colors = sleekTextFieldColors(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("firebase_db_url_input")
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Test Connection Button
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            isTesting = true
                            testResultMessage = null
                            testResultSuccess = null

                            FirebaseDatabaseService.saveFirebaseConfig(context, apiKey, projectId, appId, dbUrl)
                            val (success, msg) = FirebaseDatabaseService.testFirebaseConnection(context)
                            isTesting = false
                            testResultSuccess = success
                            testResultMessage = msg
                            isConnected = FirebaseDatabaseService.isFirebaseAvailable(context)
                            if (isConnected) {
                                cloudStats = FirebaseDatabaseService.getCloudStatistics(context)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("test_firebase_connection_button"),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isTesting
                ) {
                    if (isTesting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            strokeWidth = 2.dp,
                            color = SleekBlue400
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verifying Cloud Connection...")
                    } else {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Test Live Cloud Connection")
                    }
                }

                testResultMessage?.let { msg ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(10.dp),
                        color = if (testResultSuccess == true)
                            SleekBlue600.copy(alpha = 0.15f)
                        else
                            MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f),
                        border = BorderStroke(
                            1.dp,
                            if (testResultSuccess == true) SleekBlue400 else MaterialTheme.colorScheme.error
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (testResultSuccess == true) Icons.Default.CheckCircle else Icons.Default.Warning,
                                contentDescription = null,
                                tint = if (testResultSuccess == true) SleekBlue400 else MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = msg,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val saved = FirebaseDatabaseService.saveFirebaseConfig(context, apiKey, projectId, appId, dbUrl)
                    if (saved) {
                        Toast.makeText(context, "Upgraded Firebase Cloud connected!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Firebase settings saved.", Toast.LENGTH_SHORT).show()
                    }
                    onDismiss()
                },
                modifier = Modifier.testTag("save_firebase_config_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = SleekBlue600)
            ) {
                Text("Save & Connect", fontWeight = FontWeight.Bold, color = Color.White)
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("close_firebase_config_button")
            ) {
                Text("Close", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    )
}

@Composable
private fun CloudStatItem(label: String, count: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = count,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Color(0xFF94A3B8),
            fontSize = 11.sp
        )
    }
}
