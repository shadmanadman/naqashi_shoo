package com.adman.shadman.naqashishoo.adapter

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.adman.shadman.naqashishoo.R


import java.util.ArrayList;
import java.util.List;


class ColorPickerAdapter(
    context: Context
) :
    RecyclerView.Adapter<ColorPickerAdapter.ViewHolder>() {
    private var context: Context
    private var inflater: LayoutInflater
    private val colorPickerColors: ArrayList<Int>
    private var onColorPickerClickListener: OnColorPickerClickListener? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View = inflater.inflate(R.layout.color_picker_item_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        holder.colorPickerView.setBackgroundColor(colorPickerColors[position])
    }

    override fun getItemCount(): Int {
        return colorPickerColors.size
    }

    private fun buildColorPickerView(view: View, colorCode: Int) {
        view.setVisibility(View.VISIBLE)
        val biggerCircle = ShapeDrawable(OvalShape())
        biggerCircle.intrinsicHeight = 20
        biggerCircle.intrinsicWidth = 20
        biggerCircle.bounds = Rect(0, 0, 20, 20)
        biggerCircle.paint.color = colorCode
        val smallerCircle = ShapeDrawable(OvalShape())
        smallerCircle.intrinsicHeight = 5
        smallerCircle.intrinsicWidth = 5
        smallerCircle.bounds = Rect(0, 0, 5, 5)
        smallerCircle.paint.color = Color.WHITE
        smallerCircle.setPadding(10, 10, 10, 10)
        val drawables = arrayOf<Drawable>(smallerCircle, biggerCircle)
        val layerDrawable = LayerDrawable(drawables)
        view.setBackgroundDrawable(layerDrawable)
    }

    fun setOnColorPickerClickListener(onColorPickerClickListener: OnColorPickerClickListener?) {
        this.onColorPickerClickListener = onColorPickerClickListener
    }

    inner class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var colorPickerView: View

        init {
            colorPickerView = itemView.findViewById(R.id.color_picker_view)
            itemView.setOnClickListener{
                    if (onColorPickerClickListener != null) onColorPickerClickListener!!.onColorPickerClickListener(
                        colorPickerColors[adapterPosition]
                    )
            }
        }
    }

    interface OnColorPickerClickListener {
        fun onColorPickerClickListener(colorCode: Int)
    }

    companion object {
        fun getDefaultColors(context: Context?): ArrayList<Int> {
            val colorPickerColors: ArrayList<Int> = ArrayList()
            colorPickerColors.add(ContextCompat.getColor(context!!, R.color.blue_color_picker))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.brown_color_picker))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.green_color_picker))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.orange_color_picker))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.red_color_picker))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.black))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.red_orange_color_picker))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.sky_blue_color_picker))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.violet_color_picker))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.white))
            colorPickerColors.add(ContextCompat.getColor(context, R.color.yellow_color_picker))
            colorPickerColors.add(
                ContextCompat.getColor(
                    context,
                    R.color.yellow_green_color_picker
                )
            )
            return colorPickerColors
        }
    }

    init {
        this.context = context
        inflater = LayoutInflater.from(context)
        this.colorPickerColors = getDefaultColors(context)
    }
}