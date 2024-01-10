package com.example.noisense

//import androidx.appcompat.app.AlertDialog
import android.Manifest
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults.buttonColors
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Color.Companion.Red
import androidx.compose.ui.graphics.Color.Companion.White
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.core.app.ActivityCompat
import com.example.noisense.playback.AndroidAudioPlayer
import com.example.noisense.recorder.AndroidAudioRecorder
import com.example.noisense.ui.theme.AudioRecorderAppTheme
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.sql.Time
import java.util.Timer
import kotlin.concurrent.timer
import kotlin.time.ExperimentalTime
import kotlin.time.seconds
import kotlin.time.Duration

class MainActivity : ComponentActivity() {
    private val recorder by lazy { AndroidAudioRecorder(applicationContext) }
    private val player by lazy { AndroidAudioPlayer(applicationContext) }
    private var audioFile: File? = null

    @OptIn(ExperimentalMaterialApi::class, ExperimentalTime::class)
    fun formatTime(elapsedTime: Long): String {
        val minutes = (elapsedTime / 60000).toString().padStart(2, '0')
        val seconds = ((elapsedTime % 60000) / 1000).toString().padStart(2, '0')
        val milliseconds = ((elapsedTime % 1000) / 10).toString().padStart(3, '0')
        return "$minutes:$seconds:$milliseconds"
    }

//    fun formatElapsedTime(elapsedTime: Duration): String {
//        val hours = (elapsedTime.inSeconds / 3600).toInt()
//        val minutes = ((elapsedTime.inSeconds % 3600) / 60).toInt()
//        val seconds = (elapsedTime.inSeconds % 60).toInt()
//        return String.format("%02d:%02d:%02d", hours, minutes, seconds)
//    }
    @OptIn(ExperimentalTime::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 0)

        setContent {
            var showDialog by remember { mutableStateOf(false) }
            var elapsedTime by remember { mutableStateOf(0L) }
            var isRecording by remember { mutableStateOf(false) }
            var isPlaying by remember { mutableStateOf(false) }
            var isError by remember { mutableStateOf(false) }
            var timerState by remember { mutableStateOf<Timer?>(null) }

            AudioRecorderAppTheme {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(top = 650.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val context = LocalContext.current
                    if (!isRecording) {
                        Button(
                            onClick = {
                                val startTime = System.currentTimeMillis() - elapsedTime
                                timerState = timer(period = 10) {
                                    elapsedTime = System.currentTimeMillis() - startTime
                                }
                                isRecording=true
                                File(cacheDir, "audio.mp3").also {
                                    recorder.start(it)
                                    audioFile = it
                                    Toast.makeText(
                                        context,
                                        "Start Recording",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            },
                            colors = buttonColors(backgroundColor = Red),
                            modifier = Modifier.size(70.dp),
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        Spacer(modifier = Modifier.padding(20.dp))
                    }else {
                        Button(
                            onClick = {
//                                recorder.stop()
//                                Toast.makeText(context,"Stop Recording", Toast.LENGTH_SHORT).show()
                                showDialog = true
                            },
                            colors = buttonColors(backgroundColor = Red),
                            modifier = Modifier.size(70.dp),
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Default.Stop,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                        if (showDialog) {
                            var inputTitle by remember { mutableStateOf("") }
                            var text by remember { mutableStateOf(Size.Zero) }
                            var isExpanded by remember { mutableStateOf(false) }
                            var selectedItem by remember { mutableStateOf("") }

                            val list = listOf("Mesin", "Manusia", "Hewan")
                            val icon = if (isExpanded){
                                Icons.Filled.KeyboardArrowUp
                            }else{
                                Icons.Filled.KeyboardArrowDown
                            }

                            AlertDialog(
                                onDismissRequest = {
                                    showDialog = false
                                },

                                title = {
                                    Text(
                                        text = "Insert Information",
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.Black,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .fillMaxHeight()
                                    )
                                },
                                text = {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp)
                                    ) {
                                        // Form Input Pertama
                                        OutlinedTextField(
                                            value = inputTitle,
                                            onValueChange = { inputTitle = it },
                                            label = { Text("Input Title") },
                                            singleLine = true,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 12.dp)
                                        )

                                        //Dropdown Menu Label
                                        OutlinedTextField(
                                            value = selectedItem,
                                            onValueChange = { selectedItem = it },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .onGloballyPositioned { coordinates ->
                                                    text = coordinates.size.toSize()
                                                },
                                            label = { Text("SelectLabel") },
                                            trailingIcon = {
                                                Icon(icon, "", Modifier.clickable { isExpanded = !isExpanded })
                                            }
                                        )
                                        DropdownMenu(
                                            expanded = isExpanded,
                                            onDismissRequest = { isExpanded = false },
                                            modifier = Modifier
                                                .width(with(LocalDensity.current){text.width.toDp()})
                                        ) {
                                            list.forEach { label ->
                                                DropdownMenuItem(onClick = {
                                                    selectedItem = label
                                                    isExpanded = false
                                                }) {
                                                    Text(text = label)
                                                }
                                            }
                                        }
                                    }
                                },

                                confirmButton = {
                                    Button(onClick = {
                                        isRecording=false
                                        if (inputTitle.isEmpty() && selectedItem.isEmpty()) {
                                            isError = true
                                            Toast.makeText(context,"Berikan nama file!", Toast.LENGTH_SHORT).show()
                                        } else {
                                            isError = false
                                            // Proses inputText jika diperlukan
                                            recorder.stop(inputTitle, selectedItem)
                                            recorder.onClickInsert(audioFile, inputTitle)
                                            showDialog = false
                                            Toast.makeText(
                                                context,
                                                "Success Inserted",
                                                Toast.LENGTH_SHORT
                                            ).show()
                                            timerState?.cancel()
                                            elapsedTime = 0L
                                        }
                                    }) {
                                        Text("Konfirmasi")
                                    }
                                },
                                dismissButton = {
                                    Button(onClick = {
                                        showDialog = false
                                    }) {
                                        Text("Batal")
                                    }
                                }
                            )
                        }
                        Spacer(modifier = Modifier.padding(20.dp))
                    }

                    if (!isPlaying) {
                        Button(
                            onClick = {
                                isPlaying=true
                                player.playFile(audioFile ?: return@Button)
                            },
                            colors = buttonColors(backgroundColor = Red),
                            modifier = Modifier.size(70.dp),
                            shape = CircleShape
                        ) {
                            Icon(
                                imageVector = Icons.Default.PlayArrow,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }else {
                        Button(
                            onClick = {
                                player.stop()
                                isPlaying=false
                            },
                            colors = buttonColors(backgroundColor = Red),
                            modifier = Modifier.size(70.dp),
                            shape = CircleShape
                        ) {
                            Icon(imageVector = Icons.Default.Stop,
                                contentDescription = null,
                                tint = White,
                                modifier = Modifier.size(40.dp))
                        }
                    }
                }
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ){
                    Image(
                        painter = painterResource(id = R.drawable.noisense),
                        contentDescription = null,
                        modifier = Modifier.size(500.dp)
                    )
                    Text(
                        text = formatTime(elapsedTime),
                        fontSize = 30.sp
                    )
                }
            }
        }
    }
}
