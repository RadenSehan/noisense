package com.example.noisense.recorder

import java.io.File

interface AudioRecorder {
    fun start(outputFile: File)
    fun stop(inputAudioTitle:String, inputAudioLabel: String)

    fun onClickInsert(audioFile: File?, inputAudioTitle: String)
}