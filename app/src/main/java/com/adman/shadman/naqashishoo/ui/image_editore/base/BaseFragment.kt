package com.adman.shadman.naqashishoo.ui.image_editore.base

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.Nullable
import androidx.fragment.app.Fragment


abstract class BaseFragment : Fragment() {
    protected abstract val layoutId: Int

    @Nullable
    override fun onCreateView(
        inflater: LayoutInflater,
        @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View {
        require(layoutId != 0) { "Invalid layout id" }
        return inflater.inflate(layoutId, container, false)
    }
}