package com.jauhe.drawingtable

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.MotionEvent
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.ortiz.touchview.TouchImageView
import com.jauhe.drawingtable.ui.theme.DrawingtableTheme

class MainActivity : ComponentActivity() {

    private var isLocked = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Enable intercepting Volume Down key
        setContent {
            DrawingtableTheme {
                AppContent(
                    isLocked = isLocked,
                    onBackPressedDispatcher = onBackPressedDispatcher
                )
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        return if (isLocked.value) true else super.dispatchTouchEvent(ev)
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (isLocked.value && keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            isLocked.value = false
            return true
        }
        return super.onKeyDown(keyCode, event)
    }
}

@Composable
fun AppContent(
    isLocked: MutableState<Boolean>,
    onBackPressedDispatcher: androidx.activity.OnBackPressedDispatcher
) {
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    if (selectedImageUri != null) {
        ImageViewerScreen(
            imageUri = selectedImageUri!!,
            isLocked = isLocked,
            onExit = { selectedImageUri = null }
        )
    } else {
        MenuScreen(onImageChosen = { selectedImageUri = it })
    }
}

@Composable
fun MenuScreen(onImageChosen: (Uri) -> Unit) {
    val context = LocalContext.current

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { onImageChosen(it) }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Centered Button
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(32.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = { imagePickerLauncher.launch("image/*") }) {
                Text("Choose Picture")
            }
        }

        // Links and Logos at the Bottom
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(32.dp)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "View source code on GitHub",
                color = Color.Blue,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Fuktsteen/drawingtable"))
                        context.startActivity(intent)
                    },
                textAlign = TextAlign.Center // Center the text
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Support me on Ko-Fi",
                color = Color.Blue,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://ko-fi.com/fuktsteen"))
                        context.startActivity(intent)
                    },
                textAlign = TextAlign.Center // Center the text
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                horizontalArrangement = Arrangement.Center, // Center the images
                modifier = Modifier.fillMaxWidth()
            ) {
                Image(
                    painter = painterResource(id = R.drawable.kotlin_logo),
                    contentDescription = "Kotlin Logo",
                    modifier = Modifier.size(50.dp)
                )

                Spacer(modifier = Modifier.width(24.dp))

                Image(
                    painter = painterResource(id = R.drawable.gplv3),
                    contentDescription = "GPL-3.0 license",
                    modifier = Modifier.size(50.dp)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
        }
    }
}



@Composable
fun ImageViewerScreen(
    imageUri: Uri,
    isLocked: MutableState<Boolean>,
    onExit: () -> Unit
) {
    val context = LocalContext.current
    val lockBannerVisible = isLocked.value

    Box(modifier = Modifier.fillMaxSize()) {
        // Zoomable PhotoView
        AndroidView(
            factory = { context ->
                TouchImageView(context).apply {
                    setImageURI(imageUri)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // LOCK button
        if (!isLocked.value) {
            Button(
                onClick = { isLocked.value = true },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
            ) {
                Text("LOCK")
            }
        }

        // LOCKED Banner
        if (lockBannerVisible) {
            Text(
                text = "LOCKED: VOL DOWN TO UNLOCK",
                color = Color.White,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.Black)
                    .padding(12.dp)
                    .align(Alignment.TopCenter)
            )
        }

        // Back Button (for testing exit)
        if (!isLocked.value) {
            Button(
                onClick = onExit,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
            ) {
                Text("Back")
            }
        }
    }
}
