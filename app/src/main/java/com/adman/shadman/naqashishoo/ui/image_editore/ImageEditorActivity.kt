package com.adman.shadman.naqashishoo.ui.image_editore

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import com.adman.shadman.naqashishoo.R
import com.adman.shadman.naqashishoo.databinding.ActivityImageEditorBinding
import com.adman.shadman.naqashishoo.ui.main_activity.MainActivity
import ja.burhanrashid52.photoeditor.PhotoEditor


class ImageEditorActivity : AppCompatActivity() {

    private lateinit var binding: ActivityImageEditorBinding

    fun start(context: Context) {
        val starter = Intent(context, ImageEditorActivity::class.java)
        starter.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(starter)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityImageEditorBinding.inflate(layoutInflater)
        setupEditor()
        setContentView(binding.root)
    }

    private fun setupEditor(){

        val photoEditor = PhotoEditor.Builder(this, binding.photoEditorView)
            .setPinchTextScalable(true)
            .build()
    }
}
