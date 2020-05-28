package com.asr.englishasr.recorder

import android.content.Context
import android.media.AudioRecord
import android.util.Log
import android.widget.Toast
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import kotlin.math.round

class Recorder(val context: Context) {

    private val sampleRate = 16000L
    private val bufferSize = round(sampleRate * 0.4F).toInt()
    private lateinit var buffer : ByteArray
    private val audioRecord: AudioRecord
    private var isRecording = false
    private var recordThread : RecordThread? = null
    private var tmp = File(context.cacheDir, "tmp.bin")
    private var finalTmp = File(context.cacheDir, "final.wav")
    private var tmpOutput: FileOutputStream? = null

    init {
        audioRecord = AudioRecord(6, sampleRate.toInt(), 16, 2, bufferSize)
    }

    fun start() {
        try {
            if (tmp.exists()) tmp.delete()
            tmp.createNewFile()

            recordThread = RecordThread(-1)
            recordThread?.start()
        } catch (ex: IOException) {
            Toast.makeText(context, "${ex.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun shutdown() {
        audioRecord.release()
        isRecording = false
    }

    private fun saveAudio() {
        try {
            if (finalTmp.exists()) finalTmp.delete()
            finalTmp.createNewFile()

            val fis = tmp.inputStream()
            val fos = finalTmp.outputStream()

            val totalAudioLen = fis.channel.size()
            val totalDataLen = totalAudioLen + 36
            val byteRate: Long = 16 * sampleRate * 1 / 8.toLong()

            writeWaveFileHeader(
                fos,
                totalAudioLen,
                totalDataLen,
                sampleRate,
                1,
                byteRate
            )

            val data = ByteArray(bufferSize)

            while (fis.read(data) != -1) {
                fos.write(data)
            }

            fis.close()
            fos.close()
        } catch (ex: IOException) {
            Toast.makeText(context, "${ex.message}", Toast.LENGTH_LONG).show()
        }
    }

    fun getAudioFile(): File {
        return finalTmp
    }

    private fun writeWaveFileHeader(
        out: FileOutputStream, totalAudioLen: Long,
        totalDataLen: Long, longSampleRate: Long, channels: Int,
        byteRate: Long
    ) {
        val header = ByteArray(44)
        header[0] = 'R'.toByte() // RIFF/WAVE header
        header[1] = 'I'.toByte()
        header[2] = 'F'.toByte()
        header[3] = 'F'.toByte()
        header[4] = (totalDataLen and 0xff).toByte()
        header[5] = (totalDataLen shr 8 and 0xff).toByte()
        header[6] = (totalDataLen shr 16 and 0xff).toByte()
        header[7] = (totalDataLen shr 24 and 0xff).toByte()
        header[8] = 'W'.toByte()
        header[9] = 'A'.toByte()
        header[10] = 'V'.toByte()
        header[11] = 'E'.toByte()
        header[12] = 'f'.toByte() // 'fmt ' chunk
        header[13] = 'm'.toByte()
        header[14] = 't'.toByte()
        header[15] = ' '.toByte()
        header[16] = 16 // 4 bytes: size of 'fmt ' chunk
        header[17] = 0
        header[18] = 0
        header[19] = 0
        header[20] = 1 // format = 1
        header[21] = 0
        header[22] = channels.toByte()
        header[23] = 0
        header[24] = (longSampleRate and 0xff).toByte()
        header[25] = (longSampleRate shr 8 and 0xff).toByte()
        header[26] = (longSampleRate shr 16 and 0xff).toByte()
        header[27] = (longSampleRate shr 24 and 0xff).toByte()
        header[28] = (byteRate and 0xff).toByte()
        header[29] = (byteRate shr 8 and 0xff).toByte()
        header[30] = (byteRate shr 16 and 0xff).toByte()
        header[31] = (byteRate shr 24 and 0xff).toByte()
        header[32] = (2 * 16 / 8).toByte() // block align
        header[33] = 0
        header[34] = 16 // bits per sample
        header[35] = 0
        header[36] = 'd'.toByte()
        header[37] = 'a'.toByte()
        header[38] = 't'.toByte()
        header[39] = 'a'.toByte()
        header[40] = (totalAudioLen and 0xff).toByte()
        header[41] = (totalAudioLen shr 8 and 0xff).toByte()
        header[42] = (totalAudioLen shr 16 and 0xff).toByte()
        header[43] = (totalAudioLen shr 24 and 0xff).toByte()
        out.write(header, 0, 44)
    }

    fun isRecording(): Boolean {
        return isRecording
    }

    fun stop(): Boolean {
        if (tmpOutput != null) {
            try {
                tmpOutput?.close()
                tmpOutput = null
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        val result: Boolean = this.stopRecognizerThread()
        if (result) {
            saveAudio()
        }
        return result
    }

    private fun stopRecognizerThread(): Boolean {
        return if (null == this.recordThread) {
            false
        } else {
            try {
                this.recordThread?.interrupt()
                this.recordThread?.join()
            } catch (var2: InterruptedException) {
                Thread.currentThread().interrupt()
            }
            this.recordThread = null
            true
        }
    }

    inner class RecordThread(val timeout: Int) : Thread() {

        private var remainingSamples = 0
        private var timeoutSamples = 0
        private val NO_TIMEOUT = -1

        init {

            if (timeout != -1) {
                timeoutSamples = (timeout * this@Recorder.sampleRate / 1000).toInt()
            } else {
                timeoutSamples = -1
            }

            remainingSamples = timeoutSamples
        }


        override fun run() {

            try {
                tmpOutput = tmp.outputStream()

                audioRecord.startRecording()
                isRecording = true


                buffer = ByteArray(bufferSize)
                while (!interrupted() && (timeoutSamples == -1 || remainingSamples > 0)) {

                    val nread = audioRecord.read(buffer, 0, buffer.size)
                    tmpOutput?.write(buffer)
                    Log.d("AppLog", "tmp: ${tmp.length()}")

                    if (timeoutSamples != -1) {
                        remainingSamples -= nread
                    }
                }
            } catch (ex: FileNotFoundException) {
                this@Recorder.stop()
                Toast.makeText(context, "${ex.message}", Toast.LENGTH_LONG).show()
            } catch (ex: IllegalStateException) {
                Toast.makeText(context, "${ex.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}