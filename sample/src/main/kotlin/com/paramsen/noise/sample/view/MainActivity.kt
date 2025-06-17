package com.paramsen.noise.sample.view

import android.Manifest.permission.RECORD_AUDIO
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_GRANTED
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.menu.ActionMenuItemView
import androidx.appcompat.widget.ActionMenuView
import androidx.core.net.toUri
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.descendants
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.paramsen.noise.Noise
import com.paramsen.noise.sample.BuildConfig
import com.paramsen.noise.sample.R
import com.paramsen.noise.sample.databinding.ActivityMainBinding
import com.paramsen.noise.sample.databinding.ViewInfoBinding
import com.paramsen.noise.sample.source.createAudioSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.plus

class MainActivity : AppCompatActivity() {

    @Suppress("PropertyName", "UNNECESSARY_NOT_NULL_ASSERTION")
    val TAG: String = javaClass.simpleName!!

    val p0 = Profiler("p0")
    val p1 = Profiler("p1")
    val p2 = Profiler("p2")
    val p3 = Profiler("p3")

    private val audioRequestLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) {
            start()
        }
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        this.binding = binding

        val root = binding.root
        setContentView(root)

        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val systemInsets = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(
                systemInsets.left,
                systemInsets.top,
                systemInsets.right,
                systemInsets.bottom
            )
            insets
        }

        val infoBinding = ViewInfoBinding.bind(binding.info)
        infoBinding.version.text = "v${BuildConfig.VERSION_NAME}"
        infoBinding.github.setOnClickListener { browser("https://github.com/paramsen/noise") }
        infoBinding.me.setOnClickListener { browser("https://paramsen.github.io") }
        infoBinding.close.setOnClickListener { binding.info.onClose() }

        if (requestAudio()) {
            start()
        }

        root.postOnAnimation {
            scheduleAbout()
        }
    }

    private fun start() {
        val binding = binding
        val audioView = binding.audioView
        val fftHeatMapView = binding.fftHeatMapView
        val fftBandView = binding.fftBandView

        lifecycleScope.launch(Dispatchers.Default) {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                val src = createAudioSource(this)
                    .catch {
                        Log.e("Noise", it.message.orEmpty())
                    }

                val noise = Noise.real(4096)

                //AudioView
                src
                    .onEach {
                        p0.next()
                    }
                    .onEach {
                        audioView.onWindow(it)
                    }.launchIn(this)

                //FFTView
                val fftFlow = src
                    .onEach {
                        p1.next()
                    }
                    .map {
                        for (i in it.indices) {
                            it[i] *= 2.0f
                        }
                        return@map it
                    }
                    .map { noise.fft(it, FloatArray(4096 + 2)) }
                    .onEach {
                        p3.next()
                    }
                    .shareIn(this@launch, started = SharingStarted.WhileSubscribed(), replay = 0)

                fftFlow.onEach { fft ->
                    fftHeatMapView.onFFT(fft)
                }.launchIn(this + Dispatchers.Default.limitedParallelism(1))

                fftFlow.onEach { fft ->
                    fftBandView.onFFT(fft)
                }.launchIn(this + Dispatchers.Default.limitedParallelism(1))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.tip.schedule()
    }

    /**
     * Output windows of 4096 len, ~10/sec for 44.1khz, accumulates for FFT
     */
    private fun accumulate(o: Flow<FloatArray>): Flow<FloatArray> {
        val size = 4096

        val buf = FloatArray(size * 2)
        val empty = floatArrayOf()
        var c = 0

        return o.map { window ->
            System.arraycopy(window, 0, buf, c, window.size)
            c += window.size

            if (c >= size) {
                val out = FloatArray(size)
                System.arraycopy(buf, 0, out, 0, size)

                if (c > size) {
                    System.arraycopy(buf, c % size, buf, 0, c % size)
                }

                c = 0

                return@map out
            }

            return@map empty
        }.filter { fft -> fft.size == size } //filter only the emissions of complete 4096 windows
    }

    private fun accumulate1(o: Flow<FloatArray>): Flow<FloatArray> {
        return o.windowed(6)
            .map { window ->
                val out = FloatArray(4096)
                var c = 0
                for (each in window) {
                    if (c + each.size >= 4096)
                        break

                    System.arraycopy(each, 0, out, c, each.size)
                    c += each.size - 1
                }
                out
            }
    }

    private fun requestAudio(): Boolean {
        return if (hasAudioPermission()) {
            true
        } else {
            audioRequestLauncher.launch(RECORD_AUDIO)
            false
        }
    }

    private fun hasAudioPermission(): Boolean {
        return checkSelfPermission(RECORD_AUDIO) == PERMISSION_GRANTED
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        binding.info.onShow()
        return true
    }

    @SuppressLint("RestrictedApi")
    private fun scheduleAbout() {
        val binding = binding
        val contentView = binding.root.rootView as ViewGroup
        val actionMenuView = contentView.descendants.firstOrNull { it is ActionMenuView } as? ActionMenuView ?: return
        val itemView = actionMenuView.getChildAt(0) as? ActionMenuItemView ?: return

        binding.container.postDelayed({
            if (!binding.info.showed) {
                try {
                    val anim = AnimationUtils.loadAnimation(this, R.anim.nudge).apply {
                        repeatCount = 3
                        repeatMode = Animation.REVERSE
                        duration = 200
                        interpolator = AccelerateDecelerateInterpolator()
                        onTerminate { scheduleAbout() }
                    }

                    itemView.startAnimation(anim)
                } catch (e: Exception) {
                    Log.e(TAG, "Could not animate nudge", e)
                }
            }
        }, 3000)
    }

    private fun browser(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, url.toUri()))
    }
}
