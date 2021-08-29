package com.audioplayer.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.audioplayer.R
import com.audioplayer.`interface`.OnClickAudio
import com.audioplayer.modal.AudioData
import kotlinx.android.synthetic.main.row_audio.view.*


class AudioListAdapter(
    val context: Context,
    val list: ArrayList<AudioData>,
    val listener: OnClickAudio
) :
    RecyclerView.Adapter<AudioListAdapter.Holder>() {

    //val listener: OnClickListener = context as OnClickListener

    // var listener = context as OnProductItemClick
    companion object {
        var btnListner: OnClickAudio? = null
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.row_audio, parent, false)
        return Holder(v)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        btnListner = listener

        holder.tvTitle.text = list[position].title
        holder.rlPlay.setOnClickListener {
            if (btnListner != null) {
                btnListner?.onClickAudio(position)
            }
        }


    }


    inner class Holder(v: View) : RecyclerView.ViewHolder(v) {
        val tvTitle: TextView = v.tvTitle
        val rlPlay: RelativeLayout = v.rlPlay


    }

}