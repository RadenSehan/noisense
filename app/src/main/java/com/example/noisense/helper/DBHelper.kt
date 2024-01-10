package com.example.noisense.helper

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.noisense.db.Recording

class DBHelper(context: Context) : SQLiteOpenHelper(context, "recordings.db", null, 2) {

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL("""
        CREATE TABLE recordings (
            audio_id VARCHAR(100) NOT NULL PRIMARY KEY,
            audio_title VARCHAR(25) NOT NULL,
            audio_timestamp DATETIME,
            audio_path VARCHAR(100) NOT NULL,
            audio_size FLOAT NOT NULL,
            audio_duration VARCHAR(9) NOT NULL,
            audio_label VARCHAR(100) NOT NULL
        )
    """)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL("DROP TABLE IF EXISTS recordings")
        onCreate(db)
    }

    fun addRecording(recording: Recording): Long {
        val db = this.writableDatabase
        val values = ContentValues().apply {
            put("audio_id", recording.audio_id)
            put("audio_title", recording.audio_title)
            put("audio_timestamp", recording.audio_timestamp)
            put("audio_path", recording.audio_path)
            put("audio_size", recording.audio_size)
            put("audio_duration", recording.audio_duration)
            put("audio_label", recording.audio_label)
        }
        val result = db.insert("recordings", null, values)
        db.close()
        return result
    }
}
