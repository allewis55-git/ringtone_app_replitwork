package za.co.acquiremedia.ringtoneapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
                StylesScreen()
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
            throw e
        }
    }

    @Composable
    fun StylesScreen() {

        var styles by remember { mutableStateOf<List<String>?>(null) }

        LaunchedEffect(Unit) {
            styles = withContext(Dispatchers.IO) {
                loadStyles()
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            when {
                styles == null -> {
                    CircularProgressIndicator()

                    Text(
                        text = "Loading styles...",
                        modifier = Modifier.padding(top = 16.dp)
                    )
                }

                styles!!.isEmpty() -> {
                    Text("Could not load styles.")
                }

                else -> {
                    Text("Styles received:")

                    styles!!.forEach { style ->
                        Text(
                            text = style,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }
            }
        }
    }
}