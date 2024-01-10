package com.example.noisense.recorder

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import com.example.noisense.db.Recording
import com.example.noisense.helper.DBHelper
import com.example.noisense.service.ApiClient
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class AndroidAudioRecorder(private val context: Context) : AudioRecorder {
    private var recorder: MediaRecorder? = null
    private lateinit var currentOutputFile: File

    private fun createRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else MediaRecorder()
    }

    override fun start(outputFile: File) {
        currentOutputFile = outputFile
        createRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(FileOutputStream(outputFile).fd)

            prepare()
            start()

            recorder = this
        }

    }

    val audioId = UUID.randomUUID().toString()
    override fun stop(inputAudioTitle:String, inputAudioLabel:String) {
        recorder?.stop()
        recorder?.reset()


        val audioTitle = inputAudioTitle
        val audioTimestamp = getCurrentTimestamp()
        val audioPath = currentOutputFile.absolutePath
        val audioSize = getFileSize(audioPath)
        val audioDuration = getDuration(audioPath)
        val audioLabel = inputAudioLabel

        val recording = Recording(
            audio_id = audioId,
            audio_title = audioTitle,
            audio_timestamp = audioTimestamp,
            audio_path = audioPath,
            audio_size = audioSize,
            audio_duration = audioDuration,
            audio_label = audioLabel
        )

        val insertResult = DBHelper(context).addRecording(recording)

        if (insertResult == -1L) {
            Log.e("AndroidAudioRecorder", "Failed to insert recording into SQLite.")
        } else {
            Log.i("AndroidAudioRecorder", "Successfully inserted recording with ID $audioId.")
        }

        val call = ApiClient.apiService.addRecording(
            recording.audio_id,
            recording.audio_title,
            recording.audio_timestamp,
            recording.audio_path,
            recording.audio_size,
            recording.audio_duration,
            recording.audio_label
        )

        call.enqueue(object: Callback<Map<String, String>> {
            override fun onResponse(
                call: Call<Map<String, String>>,
                response: Response<Map<String, String>>
            ) {
                if (response.isSuccessful) {
                    Log.i("API_CALL", "Data successfully sent to server ${recording}")
                } else {
                    Log.e("API_CALL", "Failed to send data: ${response.errorBody()}")
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                Log.e("API_CALL", "Error calling API", t)
            }
        })

        recorder = null
    }

    override fun onClickInsert(audioFile: File?, inputAudioTitle: String) {
        if (audioFile != null) {
            Log.e("FILE", audioFile.name)
        }
        val audio = audioFile

        val file = File("path_to_your_audio_file.mp3") // Replace with the path to your audio file
        val audioRequestBody = RequestBody.create(MediaType.parse("audio/*"), audio)
        val audioPart = MultipartBody.Part.createFormData("audio", audioId + ".mp3", audioRequestBody)

        val fileId = RequestBody.create(MediaType.parse("text/plain"), UUID.randomUUID().toString())
        val audioId = RequestBody.create(MediaType.parse("text/plain"), audioId)
        val path = RequestBody.create(MediaType.parse("text/plain"), "your_path")

        val call = ApiClient.apiService.uploadAudio(fileId, audioId, path, audioPart)
        call.enqueue(object: Callback<Map<String, String>> {
            override fun onResponse(
                call: Call<Map<String, String>>,
                response: Response<Map<String, String>>
            ) {
                if (response.isSuccessful) {
                    Log.i("API_CALL", "File successfully sent to server db")
                } else {
                    Log.e("API_CALL", "Failed to send data: ${response.errorBody()}")
                }
            }

            override fun onFailure(call: Call<Map<String, String>>, t: Throwable) {
                Log.e("API_CALL", "Error calling API", t)
            }
        })
    }


}
private fun getFileSize(filePath: String): Float {
    val file = File(filePath)
    return file.length() / 1024f // mengembalikan ukuran dalam KB
}

private fun getDuration(filePath: String): String {
    val retriever = MediaMetadataRetriever()
    retriever.setDataSource(filePath)
    val time = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
    val timeInMillis = time!!.toLong()
    val hours = (timeInMillis / (1000 * 60 * 60)).toInt()
    val minutes = (timeInMillis / (1000 * 60) % 60).toInt()
    val seconds = (timeInMillis / 1000 % 60).toInt()
    return String.format("%02d:%02d:%02d", hours, minutes, seconds)
}

private fun getCurrentTimestamp(): String {
    val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    return sdf.format(Date())
}



