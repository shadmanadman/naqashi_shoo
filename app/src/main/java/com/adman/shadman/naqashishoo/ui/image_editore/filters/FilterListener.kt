package com.adman.shadman.naqashishoo.ui.image_editore.filters

import ja.burhanrashid52.photoeditor.PhotoFilter


interface FilterListener {
    fun onFilterSelected(photoFilter: PhotoFilter?)
}