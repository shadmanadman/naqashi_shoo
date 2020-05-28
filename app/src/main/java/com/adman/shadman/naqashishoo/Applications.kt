package com.adman.shadman.naqashishoo

import android.app.Application
import android.content.Context
import io.github.inflationx.calligraphy3.CalligraphyConfig
import io.github.inflationx.calligraphy3.CalligraphyInterceptor
import io.github.inflationx.viewpump.ViewPump

class Applications : Application() {

    companion object{
        private var INSTANCE: Applications? = null
        var context: Context? = null
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        context = this
        ViewPump.init(
            ViewPump.builder()
                .addInterceptor(
                    CalligraphyInterceptor(
                        CalligraphyConfig.Builder()
                            .setDefaultFontPath("font/iransans.ttf")
                            .setFontAttrId(R.attr.fontPath)
                            .build()
                    )
                )
                .build()
        )

    }
}