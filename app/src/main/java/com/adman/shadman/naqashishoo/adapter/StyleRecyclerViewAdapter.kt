package com.adman.shadman.naqashishoo.adapter

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.adman.shadman.naqashishoo.R
import com.adman.shadman.naqashishoo.ui.main_activity.OnListFragmentInteractionListener
import com.bumptech.glide.Glide

class StyleRecyclerViewAdapter(
    private val styles: List<String>,
    private val context: Context,
    private val mListener: OnListFragmentInteractionListener?
) : RecyclerView.Adapter<StyleRecyclerViewAdapter.StyleItemViewHolder>() {

    private val mOnClickListener: View.OnClickListener

    init {
        mOnClickListener = View.OnClickListener { v ->
            val item = v.tag as String
            // Notify the active callbacks interface (the activity, if the fragment is attached to
            // one) that an item has been selected.
            mListener?.onListFragmentInteraction(item)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StyleItemViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_image, parent, false)
        return StyleItemViewHolder(view)
    }

    override fun onBindViewHolder(holder: StyleItemViewHolder, position: Int) {
        val imagePath = styles[position]

        if (position == 0) {
            holder.imageView.layoutParams.width = 150
            holder.imageView.layoutParams.height = 150
            Glide.with(context)
                .load(R.drawable.ic_gallery)
                .into(holder.imageView)
        } else if (position == 1) {
            holder.imageView.layoutParams.width = 150
            holder.imageView.layoutParams.height = 150
            Glide.with(context)
                .load(R.drawable.ic_camera)
                .into(holder.imageView)
        } else {
            holder.imageView.layoutParams.width = LinearLayout.LayoutParams.MATCH_PARENT
            holder.imageView.layoutParams.height = LinearLayout.LayoutParams.MATCH_PARENT
            Glide.with(context)
                .load(Uri.parse("file:///android_asset/thumbnails/$imagePath"))
                .centerInside()
                .into(holder.imageView)
        }

        with(holder.mView) {
            tag = imagePath
            setOnClickListener(mOnClickListener)
        }
    }

    override fun getItemCount(): Int = styles.size

    inner class StyleItemViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        var imageView: ImageView = mView.findViewById(R.id.image_view)
    }
}
