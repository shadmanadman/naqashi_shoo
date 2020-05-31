package com.adman.shadman.naqashishoo.ui.about

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.adman.shadman.naqashishoo.databinding.BottomSheetAboutBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class AboutBottomSheetFragment : BottomSheetDialogFragment() {


    fun newInstance(): AboutBottomSheetFragment =
        AboutBottomSheetFragment()

    private lateinit var binding: BottomSheetAboutBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = BottomSheetAboutBinding.inflate(layoutInflater)
        binding.appSource.setOnClickListener {
            val browse = Intent(
                Intent.ACTION_VIEW,
                Uri.parse("https://github.com/shadmanadman/naqashi_shoo")
            )
            startActivity(browse)
        }
        return binding.root
    }
}