package com.adman.shadman.naqashishoo.ui.details_transform

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.adman.shadman.naqashishoo.databinding.ActivityMainBinding
import com.adman.shadman.naqashishoo.databinding.BottomSheetStyleTransformDetailsBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


private const val ARG_PARAM1 = "param1"

class StyleTransformDetailsBottomSheet :BottomSheetDialogFragment(){

    companion object{
        fun newInstance(details:String):StyleTransformDetailsBottomSheet=StyleTransformDetailsBottomSheet().apply {
            arguments=Bundle().apply {
                putString(ARG_PARAM1,details)
            }
        }
        private lateinit var details:String
        private lateinit var mContext: Context
        private lateinit var binding: BottomSheetStyleTransformDetailsBinding
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mContext=context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments.let {
            details=it!!.getString(ARG_PARAM1,"")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=BottomSheetStyleTransformDetailsBinding.inflate(layoutInflater)
        binding.styleTransformDetail.text= details
        return binding.root
    }
}