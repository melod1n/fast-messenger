package dev.meloda.fast.messageshistory.recorder

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File

class VoiceRecorder(
    private val context: Context,
    private val scope: CoroutineScope
) {

    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    private var tickerJob: Job? = null
    private var startTimeMs: Long = 0L

    private val _isRecording = MutableStateFlow(false)
    val isRecording: StateFlow<Boolean> = _isRecording.asStateFlow()

    private val _durationSec = MutableStateFlow(0)
    val durationSec: StateFlow<Int> = _durationSec.asStateFlow()

    fun start(): Boolean {
        if (_isRecording.value) return true

        val directory = File(context.cacheDir, "voice_messages").apply { mkdirs() }
        var success = false

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val oggFile = File(directory, "voice_${System.currentTimeMillis()}.ogg")
            val oggRecorder = createRecorder()
            try {
                oggRecorder.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.OGG)
                    setAudioEncoder(MediaRecorder.AudioEncoder.OPUS)
                    setAudioSamplingRate(48_000)
                    setAudioEncodingBitRate(64_000)
                    setAudioChannels(1)
                    setOutputFile(oggFile.absolutePath)
                    prepare()
                    start()
                }
                recorder = oggRecorder
                outputFile = oggFile
                success = true
            } catch (e: Exception) {
                Log.w("VoiceRecorder", "OGG/OPUS encoding failed, falling back to MP4/AAC", e)
                runCatching { oggRecorder.release() }
                oggFile.delete()
            }
        }

        if (!success) {
            val mp4File = File(directory, "voice_${System.currentTimeMillis()}.m4a")
            val mp4Recorder = createRecorder()
            try {
                mp4Recorder.apply {
                    setAudioSource(MediaRecorder.AudioSource.MIC)
                    setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                    setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                    setAudioSamplingRate(44_100)
                    setAudioEncodingBitRate(64_000)
                    setAudioChannels(1)
                    setOutputFile(mp4File.absolutePath)
                    prepare()
                    start()
                }
                recorder = mp4Recorder
                outputFile = mp4File
                success = true
            } catch (e: Exception) {
                Log.e("VoiceRecorder", "Failed to start MediaRecorder", e)
                runCatching { mp4Recorder.release() }
                mp4File.delete()
                recorder = null
                outputFile = null
                return false
            }
        }

        startTimeMs = System.currentTimeMillis()
        _durationSec.value = 0
        _isRecording.value = true
        startTicker()
        return true
    }

    fun stop(): File? {
        if (!_isRecording.value) return null

        val file = outputFile
        val recordDuration = System.currentTimeMillis() - startTimeMs

        return try {
            if (recordDuration < 600L) {
                cancel()
                null
            } else {
                recorder?.stop()
                file?.takeIf { it.exists() && it.length() > 0 }
            }
        } catch (e: Exception) {
            Log.e("VoiceRecorder", "Error stopping MediaRecorder", e)
            file?.delete()
            null
        } finally {
            releaseInternal()
        }
    }

    fun cancel() {
        if (!_isRecording.value) return

        runCatching { recorder?.stop() }
        outputFile?.delete()
        releaseInternal()
    }

    fun release() {
        if (_isRecording.value) {
            runCatching { recorder?.stop() }
            outputFile?.delete()
        }
        runCatching { recorder?.release() }
        recorder = null
        outputFile = null
        tickerJob?.cancel()
        tickerJob = null
        _isRecording.value = false
        _durationSec.value = 0
    }

    private fun releaseInternal() {
        runCatching { recorder?.release() }
        recorder = null
        outputFile = null
        tickerJob?.cancel()
        tickerJob = null
        _isRecording.value = false
    }

    private fun startTicker() {
        tickerJob?.cancel()
        tickerJob = scope.launch {
            while (isActive && _isRecording.value) {
                delay(1_000L)
                _durationSec.value += 1

                if (_durationSec.value >= MAX_DURATION_SEC) break
            }
        }
    }

    private fun createRecorder(): MediaRecorder =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

    companion object {
        private const val MAX_DURATION_SEC = 300
    }
}
