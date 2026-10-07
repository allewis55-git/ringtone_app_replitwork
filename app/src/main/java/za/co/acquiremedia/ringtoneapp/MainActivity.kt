package za.co.acquiremedia.ringtoneapp


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import za.co.acquiremedia.ringtoneapp.ui.theme.RingtoneAppTheme
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow


class MainActivity : ComponentActivity() {

    data class Ringtone(val name: String, val filename: String)

    private val stylesUrl =
        "https://www.hilton-chiropractor.co.za/nZ3PrYY7/php-admin/api/styles_request.php"

    private val ringtonesUrl =
        "https://www.hilton-chiropractor.co.za/nZ3PrYY7/php-admin/api/ringtones_request.php"

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

    private fun loadRingtones(style: String): List<Ringtone> {
        return try {
            val encodedStyle = URLEncoder.encode(style, "UTF-8")
            val urlWithParams = "$ringtonesUrl?style=$encodedStyle"
            val connection = URL(urlWithParams).openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000

            val response = connection.inputStream
                .bufferedReader()
                .use { it.readText() }

            connection.disconnect()

            val json = JSONObject(response)
            val ringtonesJson = json.getJSONArray("ringtones")

            List(ringtonesJson.length()) { index ->
                val item = ringtonesJson.getJSONObject(index)
                Ringtone(
                    name = item.getString("name"),
                    filename = item.getString("filename")
                )
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
                modifier = Modifier.fillMaxSize()
                    .padding(   start = 20.dp,
                        end = 20.dp,
                        top = 20.dp,
                        bottom = 56.dp)
                    .border(
                        width = 1.dp,
                        color = Color(0xFFffede6).copy(alpha = 0.7f)
                    ),
                containerColor = Color.White.copy(alpha = 0.65f),


             /*   containerColor = Color.Transparent,   */
                topBar = {
                    Column {
                        Spacer(modifier = Modifier.height(32.dp)) // basic edge-to-edge spacer
                        PrimaryTabRow(
                            selectedTabIndex = selectedTabIndex,
                            containerColor = Color.White.copy(alpha = 0.40f)
                          /*  containerColor = Color.Transparent   */
                        ) {
                            tabs.forEachIndexed { index, title ->
                                Tab(
                                    selected = selectedTabIndex == index,
                                    onClick = { selectedTabIndex = index },
                                    text = {
                                        Text(
                                            text = title,
                                            fontSize = 14.sp,
                                            color = Color(0xFF030304).copy(alpha = 0.6f)
                                        )
                                    }

                                  /*  text = { Text(title) }   */
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
        var expanded by remember { mutableStateOf(true) }
      /*  var expanded by remember { mutableStateOf(false) }   */
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
                fontFamily = FontFamily(
                    Font(R.font.alice_regular)
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            Text(
                text = "Choose a style from the menu",
                fontSize = 19.sp,
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
                else ->  {
                    /*  {
                    Button(onClick = { expanded = true }) {
                        Text(if (selectedLocalStyle.isEmpty()) "Choose a style" else selectedLocalStyle)
                    }  remove button */

                    if (expanded) {
                        Popup(
                            alignment = Alignment.TopCenter,
                            onDismissRequest = { expanded = false }
                        ) {
                            Surface(
                                modifier = Modifier
                                    .padding(top = 95.dp)
                                    .width(280.dp)
                                    .heightIn(max = 400.dp),
                                color = Color(0xFFF4F4F7),
                                tonalElevation = 4.dp,
                                shadowElevation = 4.dp
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

                                        HorizontalDivider(
                                            color = Color(0xFFD8D8DE),
                                            thickness = 1.dp
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
        var ringtones by remember { mutableStateOf<List<Ringtone>?>(null) }
        var selectedRingtone by remember { mutableStateOf<Ringtone?>(null) }

        var expanded by remember { mutableStateOf(true) }
      /*  var expanded by remember { mutableStateOf(false) }   */

        LaunchedEffect(selectedStyle) {
            if (selectedStyle.isNotEmpty()) {
                ringtones = withContext(Dispatchers.IO) {
                    loadRingtones(selectedStyle)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Ringtones",
                style = MaterialTheme.typography.headlineLarge,
                fontFamily = FontFamily(
                    Font(R.font.alice_regular)
                ),
                modifier = Modifier.padding(bottom = 8.dp)
            )

            if (selectedStyle.isEmpty()) {
                Text("Please select a style first.")
            } else {
                Text("Selected Style: $selectedStyle")
                
                Spacer(modifier = Modifier.height(16.dp))

                when {
                    ringtones == null -> {
                        CircularProgressIndicator()
                        Text("Loading ringtones...", modifier = Modifier.padding(top = 16.dp))
                    }
                    ringtones!!.isEmpty() -> {
                        Text("No ringtones found for this style.")
                    }
                    else -> {
                       /* OutlinedButton(
                            onClick = { expanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(selectedRingtone?.name ?: "Choose a ringtone")
                        }
                        */
                        if (expanded) {
                            Popup(
                                alignment = Alignment.TopCenter,
                                onDismissRequest = { expanded = false }
                            ) {
                                Surface(
                                    modifier = Modifier
                                        .padding(top = 95.dp)
                                        .width(280.dp)
                                        .heightIn(max = 400.dp),
                                    color = Color(0xFFF4F4F7),
                                    tonalElevation = 4.dp,
                                    shadowElevation = 4.dp
                                ) {
                                    LazyColumn {
                                        items(ringtones!!) { ringtone ->
                                            DropdownMenuItem(
                                                text = { Text(ringtone.name) },
                                                onClick = {
                                                    selectedRingtone = ringtone
                                                    expanded = false
                                                }
                                            )
                                            HorizontalDivider(
                                                color = Color(0xFFD8D8DE),
                                                thickness = 1.dp
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
                    onClick = { /* TODO: Install */ },
                    modifier = Modifier
                        .width(280.dp)
                        .heightIn(min = 68.dp)
                        .padding(bottom = 16.dp)
                        .shadow(
                            elevation = 3.dp,
                            shape = RoundedCornerShape(20.dp),
                            ambientColor = Color(0xFFFFB9A3),
                            spotColor = Color(0xFFFFB9A3)
                        ),
                    enabled = selectedRingtone != null,
                    border = BorderStroke(
                        width = 1.dp,
                        color = Color(0xFFFF8559)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = Color(0xFFFFE5DC),
                        disabledContainerColor = Color(0xFFFFE5DC).copy(alpha = 0.5f),
                        contentColor = Color(0xFF4E4E6B)
                    )
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        if (selectedRingtone != null) {
                            Text(
                                text = "Install",
                                fontSize = 16.sp,
                                color = Color(0xFFcf3700),
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = selectedRingtone!!.name,
                                fontSize = 18.sp,
                                color = Color(0xFF4E4E6B),
                                textAlign = TextAlign.Center,
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        } else {
                            Text(
                                text = "Select a ringtone",
                                fontSize = 18.sp,
                                color = Color(0xFFcf3700).copy(alpha = 0.6f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
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
                fontSize = 18.sp,
                fontFamily = FontFamily(
                    Font(R.font.alice_regular)
                ),
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
