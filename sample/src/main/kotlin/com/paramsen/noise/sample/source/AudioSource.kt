package com.paramsen.noise.sample.source

import android.annotation.SuppressLint
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.nio.FloatBuffer

private const val RATE_HZ = 44100
private const val SAMPLE_SIZE = 4096

/**
 * Coroutine Flow that while subscribed to emits
 * audio frames of size 4096 and 768 [~10fps, ~60fps].
 *
 * @author Pär Amsen 06/2017
 */
fun createAudioSource(scope: CoroutineScope): Flow<FloatArray> {
    /**
     * All subscribers must unsubscribe in order for Flow to cancel the microphone stream. The
     * stream is started automatically when subscribed to, the same mic stream is used for all subs.
     */
    return createFlow().shareIn(scope, SharingStarted.WhileSubscribed(200L))
}

/**
 * The returned Flow publish frames of two sizes; 4096 and 768. Roughly 10fps / 60fps.
 * Filter is used to distinguish the two types. Ideally this should be handled in two separate
 * Flows, but AudioRecord makes that utterly complex.
 */
@SuppressLint("MissingPermission")
private fun createFlow(): Flow<FloatArray> = callbackFlow {
    val src = MediaRecorder.AudioSource.MIC
    val cfg = AudioFormat.CHANNEL_IN_MONO
    val format = AudioFormat.ENCODING_PCM_16BIT
    val size = AudioRecord.getMinBufferSize(RATE_HZ, cfg, format)

    check(size > 0) {
        "AudioSource / Could not allocate audio buffer on this device (emulator? no mic?)"
    }

    val recorder = AudioRecord(src, RATE_HZ, cfg, format, size)

    recorder.startRecording()

    val readerJob = launch {
        val buf = ShortArray(512)
        val out = FloatBuffer.allocate(SAMPLE_SIZE)
        var read = 0

        while (isActive) {
            read += recorder.read(buf, read, buf.size - read)

            if (read == buf.size) {
                out.appendFloats(buf)

                if (!out.hasRemaining()) {
                    trySend(out.array().copyOf())
                    out.clear()
                }

                read = 0
            }
        }
    }

    awaitClose {
        readerJob.cancel()
        recorder.stop()
        recorder.release()
    }
}

private fun FloatBuffer.appendFloats(arr: ShortArray) {
    for (element in arr) {
        put(element.toFloat())
    }
}