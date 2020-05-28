package com.asr.englishasr

import android.Manifest
import android.content.pm.PackageManager
import android.media.AudioRecord
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.asr.englishasr.databinding.ActivityMainBinding
import com.asr.englishasr.recorder.Recorder
import com.asr.englishasr.remote.ApiService
import com.asr.englishasr.remote.ResultApi
import com.asr.englishasr.remote.RetrofitModule
import com.beloo.widget.chipslayoutmanager.ChipsLayoutManager
import com.obsez.android.lib.filechooser.ChooserDialog
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import java.io.File
import java.lang.IllegalStateException


class MainActivity : AppCompatActivity() {

    private var mediaPlayer: MediaPlayer? = null
    private val PERMISSION = arrayOf(Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE)
    private val recorder by lazy { Recorder(this) }
    private lateinit var binding : ActivityMainBinding
    private val STAT_STOP = 2
    private val STAT_START = 1
    private val STAT_PROCESS = 3
    private var statButtonRecord = STAT_START
    private val adapter by lazy { ListWordAdapter(this@MainActivity) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)

        setContentView(binding.root)

        if (allPermissionIsGranted()) {

        } else {
            ActivityCompat.requestPermissions(this, PERMISSION, 10)
        }

        statButtonRecord = STAT_START
        binding.btnStart.text = "Start"


        binding.btnStart.setOnClickListener {

            if (statButtonRecord == STAT_START) {
                val refs = binding.edtRefs.text.toString()
                if (refs.isNullOrEmpty()) Toast.makeText(this, "Enter your word!", Toast.LENGTH_LONG).show()
                else {
                    Toast.makeText(this, "Recorder start!", Toast.LENGTH_LONG).show()
                    recorder.start()
                    statButtonRecord = STAT_STOP
                    binding.btnStart.text = "Stop"
                }
            } else if (statButtonRecord == STAT_STOP) {
                lifecycleScope.launch(Dispatchers.IO) {

                    withContext(Dispatchers.Main) { Toast.makeText(this@MainActivity, "Recorder stop!", Toast.LENGTH_LONG).show() }
                    recorder.stop()

                    val api = RetrofitModule.getInstance()?.create(ApiService::class.java)
                    if (api != null) {

                        statButtonRecord = STAT_PROCESS
                        withContext(Dispatchers.Main) { binding.btnStart.text = "Process..." }

                        val file = File(this@MainActivity.cacheDir, "final.wav")

                        val requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file)

                        val body = MultipartBody.Part.createFormData("audio-file", file.name, requestFile)
                        val token = RequestBody.create(MediaType.parse("text/plain"), "gcxpHQmLeVwLWobE6apU1lgAg49YTMa0")
                        val refs = RequestBody.create(MediaType.parse("text/plain"), binding.edtRefs.text.toString())

                        api?.getPronunciation(
                            token,
                            body,
                            refs
                        )?.enqueue(object : retrofit2.Callback<ResultApi> {
                            override fun onFailure(call: Call<ResultApi>, t: Throwable) {
                                Log.d("AppLog", "Error: ${t.message}")
                                statButtonRecord = STAT_START
                                binding.btnStart.text = "Start"
                            }

                            override fun onResponse(call: Call<ResultApi>, response: Response<ResultApi>) {
                                Log.d("AppLog", "Success: ${response.body()}")
                                statButtonRecord = STAT_START
                                binding.btnStart.text = "Start"

                                //process data
                                val result = response.body()
                                result?.let {
                                    val list = it.result

                                    val listWord = mutableListOf<ItemWord>()
                                    list.forEach {
                                        it.letters.forEach {
                                            val letter = it.letter
                                            val score = it.score_normalize
                                            listWord.add(ItemWord(letter, score))
                                        }
                                        listWord.add(ItemWord(" ", 0.toDouble()))
                                    }
                                    Log.d("AppLog", "List: ${listWord}")
                                    adapter.submitList(listWord)
                                }
                            }
                        })
                    } else {
                        statButtonRecord = STAT_START
                        withContext(Dispatchers.Main) { binding.btnStart.text = "Start" }
                    }
                }
            }
        }

        binding.btnPlay.setOnClickListener {
            try {
                mediaPlayer?.release()
                mediaPlayer = MediaPlayer()
                mediaPlayer?.setDataSource(recorder.getAudioFile().absolutePath)
                Log.d("AppLog", recorder.getAudioFile().length().toString())
                mediaPlayer?.setOnPreparedListener {
                    mediaPlayer?.start()
                }
                mediaPlayer?.setOnCompletionListener {
                    mediaPlayer?.release()
                }
                mediaPlayer?.prepareAsync()
            } catch (ex: IllegalStateException) {

            }
        }

        binding.listWord.apply {
            adapter = this@MainActivity.adapter
            layoutManager = LinearLayoutManager(this@MainActivity).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }

//                ChipsLayoutManager.newBuilder(this@MainActivity)
//                    //set gravity resolver where you can determine gravity for item in position.
//                    //This method have priority over previous one
//                    .setScrollingEnabled(false)
//                    .setGravityResolver { Gravity.NO_GRAVITY }
//                    .setOrientation(ChipsLayoutManager.HORIZONTAL)
//                    //row strategy for views in completed row, could be
//                    //STRATEGY_DEFAULT, STRATEGY_FILL_VIEW, STRATEGY_FILL_SPACE or STRATEGY_CENTER
//                    .setRowStrategy(ChipsLayoutManager.STRATEGY_DEFAULT)
//                    .setChildGravity(Gravity.LEFT)
//                    .build()
        }
    }

    private fun showSelectFileDialog(l: (path: String, file: File) -> Unit) {
        ChooserDialog(this)
            .build()
            .withChosenListener { s, file ->
                l(s, file)
            }
            .show()
    }

    private fun allPermissionIsGranted(): Boolean {
        return PERMISSION.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 10) {
            if (!grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                finish()
            }
        }
    }

}
