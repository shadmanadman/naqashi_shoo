package com.adman.shadman.naqashishoo.ui.image_editore.tools

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.adman.shadman.naqashishoo.R


class EditingToolsAdapter(private val mOnItemSelected: OnItemSelected, private val mContext: Context) :
    RecyclerView.Adapter<EditingToolsAdapter.ViewHolder>() {
    private val mToolList: MutableList<ToolModel> =
        ArrayList()

    interface OnItemSelected {
        fun onToolSelected(toolType: ToolType?)
    }

    class ToolModel(
        val mToolName: String,
        val mToolIcon: Int,
         toolType: ToolType
    ) {
        val mToolType: ToolType
        init {
            mToolType = toolType
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        val view: View = LayoutInflater.from(parent.context)
            .inflate(R.layout.row_editing_tools, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int
    ) {
        val item = mToolList[position]
        holder.txtTool.text = item.mToolName
        holder.imgToolIcon.setImageResource(item.mToolIcon)
    }

    override fun getItemCount(): Int {
        return mToolList.size
    }

    inner class ViewHolder(itemView: View) :
        RecyclerView.ViewHolder(itemView) {
        var imgToolIcon: ImageView
        var txtTool: TextView

        init {
            imgToolIcon = itemView.findViewById(R.id.imgToolIcon)
            txtTool = itemView.findViewById(R.id.txtTool)
            itemView.setOnClickListener{
                    mOnItemSelected.onToolSelected(mToolList[layoutPosition].mToolType)

            }
        }
    }

    init {
        mToolList.add(ToolModel(mContext.getString(R.string.label_brush), R.drawable.ic_brush, ToolType.BRUSH))
        mToolList.add(ToolModel(mContext.getString(R.string.label_text), R.drawable.ic_text, ToolType.TEXT))
        mToolList.add(
            ToolModel(
                mContext.getString(R.string.label_eraser),
                R.drawable.ic_eraser,
                ToolType.ERASER
            )
        )
        mToolList.add(
            ToolModel(
                mContext.getString(R.string.label_filter),
                R.drawable.ic_photo_filter,
                ToolType.FILTER
            )
        )
        mToolList.add(
            ToolModel(
                mContext.getString(R.string.label_emoji),
                R.drawable.ic_insert_emoticon,
                ToolType.EMOJI
            )
        )
        mToolList.add(
            ToolModel(
                mContext.getString(R.string.label_sticker),
                R.drawable.ic_sticker,
                ToolType.STICKER
            )
        )
    }
}