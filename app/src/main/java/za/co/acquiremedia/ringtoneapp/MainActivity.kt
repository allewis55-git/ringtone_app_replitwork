package za.co.acquiremedia.ringtoneapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import za.co.acquiremedia.ringtoneapp.ui.theme.RingtoneAppTheme

class MainActivity : ComponentActivity() {

    private val stylesUrl =
        "https://www.hilton-chiropractor.co.za/nZ3PrYY7/php-admin/api/styles_request.php"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            RingtoneAppTheme {
                MainAppScreen()
            }
        }
    }

    private fun loadStyles(): List<String> {
        return try {
            val connection =
                URL(stylesUrl).openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000

            val response = connection.inputStream
                .bufferedReader()
                .use { it.readText() }

            connection.disconnect()

            val json = JSONObject(response)
            val stylesJson = json.getJSONArray("styles")

            List(stylesJson.length()) { index ->
                stylesJson.getString(index)
            }

        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun MainAppScreen() {
        var selectedTabIndex by remember { mutableIntStateOf(0) }
        var selectedStyle by remember { mutableStateOf("") }
        
        val tabs = listOf("Styles", "Ringtones", "Notifications")

        Box(modifier = Modifier.fillMaxSize()) {
            // 1. Background Image Layer
            Image(
                painter = painterResource(id = R.drawable.piano_sheetmusic_background),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )

            // 2. Translucent Gloss Layer
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.2f))
            )

            // 3. App Content Layer
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                containerColor = Color.Transparent,
                topBar = {
                    Column {
                        Spacer(modifier = Modifier.height(32.dp)) // basic edge-to-edge spacer
                        PrimaryTabRow(
                            selectedTabIndex = selectedTabIndex,
                            containerColor = Color.Transparent
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTabIndex == index,
                                    onClick = { selectedTabIndex = index },
                                    text = { Text(title) }
                                )
                            }
                        }
                    }
                }
            ) { innerPadding ->
                Column(
                    modifier = Modifier
                        .padding(innerPadding)
                        .fillMaxSize()
                ) {
                    when (selectedTabIndex) {
                        0 -> StylesScreen(
                            onStyleSelected = { style ->
                                selectedStyle = style
                                selectedTabIndex = 1 // Auto-navigate to Ringtones
                            }
                        )
                        1 -> RingtonesScreen(
                            selectedStyle = selectedStyle,
                            onBackToStyles = { selectedTabIndex = 0 }
                        )
                        2 -> NotificationsScreen(
                            selectedStyle = selectedStyle,
                            onBackToStyles = { selectedTabIndex = 0 }
                        )
                    }
                }
            }
        }
    }

    @Composable
    fun StylesScreen(onStyleSelected: (String) -> Unit) {
        var styles by remember { mutableStateOf<List<String>?>(null) }
        var expanded by remember { mutableStateOf(false) }
        var selectedLocalStyle by remember { mutableStateOf("") }

        LaunchedEffect(Unit) {
            styles = withContext(Dispatchers.IO) {
                loadStyles()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Styles",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Choose a style from the menu",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            when {
                styles == null -> {
                    CircularProgressIndicator()
                    Text("Loading styles...", modifier = Modifier.padding(top = 16.dp))
                }
                styles!!.isEmpty() -> {
                    Text("Could not load styles.")
                }
                else -> {
                    Button(onClick = { expanded = true }) {
                        Text(if (selectedLocalStyle.isEmpty()) "Choose a style" else selectedLocalStyle)
                    }

                    if (expanded) {
                        Popup(
                            alignment = Alignment.TopCenter,
                            onDismissRequest = { expanded = false }
                        ) {
                            Surface(
                                modifier = Modifier
                                    .width(280.dp)
                                    .heightIn(max = 400.dp),
                                tonalElevation = 4.dp,
                                shadowElevation = 8.dp
                            ) {
                                LazyColumn {
                                    items(styles!!) { style ->
                                        DropdownMenuItem(
                                            text = { Text(style) },
                                            onClick = {
                                                selectedLocalStyle = style
                                                expanded = false
                                                onStyleSelected(style)
                                            }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            OutlinedButton(
                onClick = { /* TODO: Restore functionality */ },
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text("Restore your original media")
            }
        }
    }

    @Composable
    fun RingtonesScreen(selectedStyle: String, onBackToStyles: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Ringtones",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (selectedStyle.isEmpty()) {
                Text("Please select a style first.")
            } else {
                Text("Selected Style: $selectedStyle")
                
                // Placeholder for Ringtone Selector
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Ringtone list placeholder")
                    }
                }

                Button(
                    onClick = { /* TODO: Play */ },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Text("Play")
                }

                Button(
                    onClick = { /* TODO: Install */ },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Text("Install")
                }
            }

            TextButton(onClick = onBackToStyles) {
                Text("← Styles")
            }
        }
    }

    @Composable
    fun NotificationsScreen(selectedStyle: String, onBackToStyles: () -> Unit) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Notifications",
                style = MaterialTheme.typography.headlineLarge,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (selectedStyle.isEmpty()) {
                Text("Please select a style first.")
            } else {
                Text("Selected Style: $selectedStyle")

                // Placeholder for Notification Selector
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .padding(vertical = 16.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Notification list placeholder")
                    }
                }

                Button(
                    onClick = { /* TODO: Play */ },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                ) {
                    Text("Play")
                }

                Button(
                    onClick = { /* TODO: Install */ },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)
                ) {
                    Text("Install")
                }
            }

            TextButton(onClick = onBackToStyles) {
                Text("← Styles")
            }
        }
    }
}
