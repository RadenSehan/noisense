package com.example.noisense.db

data class Recording(
    val audio_id: String,
    val audio_title: String,
    val audio_timestamp: String,
    val audio_path: String,
    val audio_size: Float,
    val audio_duration: String,
    val audio_label: String)


