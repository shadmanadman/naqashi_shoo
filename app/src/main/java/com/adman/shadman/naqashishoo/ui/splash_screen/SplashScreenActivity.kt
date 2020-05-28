package com.adman.shadman.naqashishoo.ui.splash_screen

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import com.adman.shadman.naqashishoo.ui.main_activity.MainActivity
import com.adman.shadman.naqashishoo.databinding.ActivitySplashScreenBinding
import io.github.inflationx.viewpump.ViewPumpContextWrapper

class SplashScreenActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashScreenBinding
    private val _delayForSplashScreen: Long = 3000
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MainActivity().makeStatusBarColorTransparent(this)
        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setupHandler()
        setContentView(binding.root)
    }

    private fun setupHandler() {
        Handler().postDelayed({
            MainActivity().start(this)
            finish()
        }, _delayForSplashScreen)
    }

    override protected fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(ViewPumpContextWrapper.wrap(newBase!!))
    }
}
