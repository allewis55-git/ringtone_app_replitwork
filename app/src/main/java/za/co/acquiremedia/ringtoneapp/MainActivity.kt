package za.co.acquiremedia.ringtoneapp


import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
    data class Notification(val name: String, val filename: String)

    private val stylesUrl =
        "https://www.hilton-chiropractor.co.za/nZ3PrYY7/php-admin/api/styles_request.php"

    private val ringtonesUrl =
        "https://www.hilton-chiropractor.co.za/nZ3PrYY7/php-admin/api/ringtones_request.php"

    private val notificationsUrl =
        "https://www.hilton-chiropractor.co.za/nZ3PrYY7/php-admin/api/notifications_request.php"

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

    private fun loadNotifications(style: String): List<Notification> {
        return try {
            val encodedStyle = URLEncoder.encode(style, "UTF-8")
            val urlWithParams = "$notificationsUrl?style=$encodedStyle"
            val connection = URL(urlWithParams).openConnection() as HttpURLConnection

            connection.requestMethod = "GET"
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000

            val response = connection.inputStream
                .bufferedReader()
                .use { it.readText() }

            connection.disconnect()

            val json = JSONObject(response)
            val notificationsJson = json.getJSONArray("notifications")

            List(notificationsJson.length()) { index ->
                val item = notificationsJson.getJSONObject(index)
                Notification(
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
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        var ringtones by remember { mutableStateOf<List<Ringtone>?>(null) }
        var selectedRingtone by remember { mutableStateOf<Ringtone?>(null) }
        var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

        var expanded by remember { mutableStateOf(true) }

        // Cleanup MediaPlayer when leaving composition
        DisposableEffect(Unit) {
            onDispose {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            }
        }

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

                                                /*    expanded = false  */

                                                    // Handle Playback
                                                    try {
                                                        mediaPlayer?.stop()
                                                        mediaPlayer?.release()
                                                        mediaPlayer = null

                                                        val audioUrl = "https://www.hilton-chiropractor.co.za/nZ3PrYY7/php-admin/uploads/ringtones/${ringtone.filename}"
                                                        mediaPlayer = MediaPlayer().apply {
                                                            setDataSource(audioUrl)
                                                            setOnPreparedListener { start() }
                                                            setOnCompletionListener {
                                                                it.release()
                                                                if (mediaPlayer == it) mediaPlayer = null
                                                            }
                                                            setOnErrorListener { mp, _, _ ->
                                                                Toast.makeText(context, "Unable to play ringtone", Toast.LENGTH_SHORT).show()
                                                                mp.release()
                                                                if (mediaPlayer == mp) mediaPlayer = null
                                                                true
                                                            }
                                                            prepareAsync()
                                                        }
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()
                                                        Toast.makeText(context, "Unable to play ringtone", Toast.LENGTH_SHORT).show()
                                                    }
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
                    onClick = {
                        selectedRingtone?.let { ringtone ->
                            if (!Settings.System.canWrite(context)) {
                                val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(intent)
                                Toast.makeText(context, "Please allow 'Modify system settings' and try again", Toast.LENGTH_LONG).show()
                            } else {
                                scope.launch {
                                    val success = withContext(Dispatchers.IO) {
                                        installRingtone(context, ringtone)
                                    }
                                    if (success) {
                                        Toast.makeText(context, "✓ Ringtone installed", Toast.LENGTH_SHORT).show()
                                    } else {
                                        Toast.makeText(context, "Unable to install ringtone", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            }
                        }
                    },
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

    private suspend fun installRingtone(context: Context, ringtone: Ringtone): Boolean {
        return try {
            val audioUrl = "https://www.hilton-chiropractor.co.za/nZ3PrYY7/php-admin/uploads/ringtones/${ringtone.filename}"
            val contentResolver = context.contentResolver
            
            // Check if already exists to avoid duplicates
            val userFriendlyName = "${ringtone.name}.mp3"
            val projection = arrayOf(MediaStore.Audio.Media._ID)
            val selection = "${MediaStore.Audio.Media.DISPLAY_NAME} = ?"
            val selectionArgs = arrayOf<String>(userFriendlyName)
            val queryUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            
            var ringtoneUri: Uri? = null
            
            contentResolver.query(queryUri, projection, selection, selectionArgs, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                    ringtoneUri = Uri.withAppendedPath(queryUri, id.toString())
                }
            }
            
            if (ringtoneUri == null) {
                val values = ContentValues().apply {
                    put(MediaStore.Audio.Media.DISPLAY_NAME, userFriendlyName)
                    put(MediaStore.Audio.Media.TITLE, ringtone.name)
                    put(MediaStore.Audio.Media.MIME_TYPE, "audio/mpeg")
                    put(MediaStore.Audio.Media.IS_RINGTONE, true)
                    put(MediaStore.Audio.Media.IS_NOTIFICATION, false)
                    put(MediaStore.Audio.Media.IS_ALARM, false)
                    put(MediaStore.Audio.Media.IS_MUSIC, false)
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(MediaStore.Audio.Media.RELATIVE_PATH, Environment.DIRECTORY_RINGTONES)
                        put(MediaStore.Audio.Media.IS_PENDING, 1)
                    }
                }
                
                val newUri = contentResolver.insert(queryUri, values) ?: return false
                
                val connection = URL(audioUrl).openConnection() as HttpURLConnection
                connection.connect()
                
                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    contentResolver.openOutputStream(newUri)?.use { os ->
                        connection.inputStream.use { it.copyTo(os) }
                    }
                    
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        values.clear()
                        values.put(MediaStore.Audio.Media.IS_PENDING, 0)
                        contentResolver.update(newUri, values, null, null)
                    }
                    ringtoneUri = newUri
                } else {
                    contentResolver.delete(newUri, null, null)
                    return false
                }
            }
            
            if (ringtoneUri != null) {
                RingtoneManager.setActualDefaultRingtoneUri(
                    context,
                    RingtoneManager.TYPE_RINGTONE,
                    ringtoneUri
                )
                true
            } else {
                false
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    @Composable
    fun NotificationsScreen(selectedStyle: String, onBackToStyles: () -> Unit) {
        val context = LocalContext.current
        val scope = rememberCoroutineScope()
        var notifications by remember { mutableStateOf<List<Notification>?>(null) }
        var selectedNotification by remember { mutableStateOf<Notification?>(null) }
        var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

        var expanded by remember { mutableStateOf(true) }

        // Cleanup MediaPlayer when leaving composition
        DisposableEffect(Unit) {
            onDispose {
                mediaPlayer?.stop()
                mediaPlayer?.release()
                mediaPlayer = null
            }
        }

        LaunchedEffect(selectedStyle) {
            if (selectedStyle.isNotEmpty()) {
                notifications = withContext(Dispatchers.IO) {
                    loadNotifications(selectedStyle)
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

                Spacer(modifier = Modifier.height(16.dp))

                when {
                    notifications == null -> {
                        CircularProgressIndicator()
                        Text(
                            "Loading notifications...",
                            modifier = Modifier.padding(top = 16.dp)
                        )
                    }

                    notifications!!.isEmpty() -> {
                        Text("No notifications found for this style.")
                    }

                    else -> {
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
                                        items(notifications!!) { notification ->
                                            DropdownMenuItem(
                                                text = { Text(notification.name) },
                                                onClick = {
                                                    selectedNotification = notification
                                                    // Keep dropdown open as requested

                                                    // Handle Playback
                                                    try {
                                                        mediaPlayer?.stop()
                                                        mediaPlayer?.release()
                                                        mediaPlayer = null

                                                        val audioUrl =
                                                            "https://www.hilton-chiropractor.co.za/nZ3PrYY7/php-admin/uploads/notifications/${notification.filename}"

                                                        mediaPlayer = MediaPlayer().apply {
                                                            setDataSource(audioUrl)

                                                            setOnPreparedListener {
                                                                start()
                                                            }

                                                            setOnCompletionListener {
                                                                it.release()
                                                                if (mediaPlayer == it) {
                                                                    mediaPlayer = null
                                                                }
                                                            }

                                                            setOnErrorListener { mp, _, _ ->
                                                                Toast.makeText(
                                                                    context,
                                                                    "Unable to play notification",
                                                                    Toast.LENGTH_SHORT
                                                                ).show()

                                                                mp.release()

                                                                if (mediaPlayer == mp) {
                                                                    mediaPlayer = null
                                                                }

                                                                true
                                                            }

                                                            prepareAsync()
                                                        }
                                                    } catch (e: Exception) {
                                                        e.printStackTrace()

                                                        Toast.makeText(
                                                            context,
                                                            "Unable to play notification",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
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

                Button(
                    onClick = {
                        selectedNotification?.let { notification ->
                            if (!Settings.System.canWrite(context)) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_WRITE_SETTINGS
                                ).apply {
                                    data = Uri.parse("package:${context.packageName}")
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }

                                context.startActivity(intent)

                                Toast.makeText(
                                    context,
                                    "Please allow 'Modify system settings' and try again",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                scope.launch {
                                    val success = withContext(Dispatchers.IO) {
                                        installNotification(context, notification)
                                    }

                                    if (success) {
                                        Toast.makeText(
                                            context,
                                            "✓ Notification installed",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            context,
                                            "Unable to install notification",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Text("Install")
                }
            }

            TextButton(onClick = onBackToStyles) {
                Text("← Styles")
            }
        }
    }
    private suspend fun installNotification(
        context: Context,
        notification: Notification
    ): Boolean {
        return try {
            val audioUrl =
                "https://www.hilton-chiropractor.co.za/nZ3PrYY7/php-admin/uploads/notifications/${notification.filename}"

            val contentResolver = context.contentResolver

            // Keep the original file extension
            val extension = notification.filename.substringAfterLast(
                '.',
                "wav"
            ).lowercase()

            val mimeType = when (extension) {
                "mp3" -> "audio/mpeg"
                "wav" -> "audio/wav"
                else -> "audio/wav"
            }

            // Check if already exists to avoid duplicates
            val userFriendlyName = "${notification.name}.$extension"
            val projection = arrayOf(MediaStore.Audio.Media._ID)
            val selection = "${MediaStore.Audio.Media.DISPLAY_NAME} = ?"
            val selectionArgs = arrayOf(userFriendlyName)
            val queryUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

            var notificationUri: Uri? = null

            contentResolver.query(
                queryUri,
                projection,
                selection,
                selectionArgs,
                null
            )?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val id = cursor.getLong(
                        cursor.getColumnIndexOrThrow(
                            MediaStore.Audio.Media._ID
                        )
                    )

                    notificationUri = Uri.withAppendedPath(
                        queryUri,
                        id.toString()
                    )
                }
            }

            if (notificationUri == null) {
                val values = ContentValues().apply {
                    put(
                        MediaStore.Audio.Media.DISPLAY_NAME,
                        userFriendlyName
                    )
                    put(
                        MediaStore.Audio.Media.TITLE,
                        notification.name
                    )
                    put(
                        MediaStore.Audio.Media.MIME_TYPE,
                        mimeType
                    )
                    put(
                        MediaStore.Audio.Media.IS_RINGTONE,
                        false
                    )
                    put(
                        MediaStore.Audio.Media.IS_NOTIFICATION,
                        true
                    )
                    put(
                        MediaStore.Audio.Media.IS_ALARM,
                        false
                    )
                    put(
                        MediaStore.Audio.Media.IS_MUSIC,
                        false
                    )

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        put(
                            MediaStore.Audio.Media.RELATIVE_PATH,
                            Environment.DIRECTORY_NOTIFICATIONS
                        )
                        put(
                            MediaStore.Audio.Media.IS_PENDING,
                            1
                        )
                    }
                }

                val newUri = contentResolver.insert(
                    queryUri,
                    values
                ) ?: return false

                val connection =
                    URL(audioUrl).openConnection() as HttpURLConnection

                connection.connect()

                if (connection.responseCode == HttpURLConnection.HTTP_OK) {
                    contentResolver.openOutputStream(newUri)?.use { os ->
                        connection.inputStream.use { input ->
                            input.copyTo(os)
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        values.clear()
                        values.put(
                            MediaStore.Audio.Media.IS_PENDING,
                            0
                        )

                        contentResolver.update(
                            newUri,
                            values,
                            null,
                            null
                        )
                    }

                    notificationUri = newUri
                } else {
                    contentResolver.delete(
                        newUri,
                        null,
                        null
                    )
                    return false
                }
            }

            if (notificationUri != null) {
                RingtoneManager.setActualDefaultRingtoneUri(
                    context,
                    RingtoneManager.TYPE_NOTIFICATION,
                    notificationUri
                )

                true
            } else {
                false
            }

        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }



}
