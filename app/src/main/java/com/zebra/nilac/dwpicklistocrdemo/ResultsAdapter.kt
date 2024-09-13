package com.zebra.nilac.dwpicklistocrdemo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.zebra.nilac.dwpicklistocrdemo.databinding.ResultRowBinding
import java.text.SimpleDateFormat
import java.util.Locale

class ResultsAdapter : RecyclerView.Adapter<ResultsAdapter.ViewHolder>() {

    private val dateOutputFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    private lateinit var mInflater: LayoutInflater
    private lateinit var mContext: Context

    private var mResultsList: MutableList<OutputResult> = ArrayList()

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ViewHolder {
        mContext = parent.context
        mInflater = LayoutInflater.from(mContext)

        val view = ResultRowBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val result = mResultsList[position]

        holder.mBinder.value.text = result.value
        holder.mBinder.date.text = dateOutputFormatter.format(result.date)

        if (result.image != null) {
            holder.mBinder.capturedImage.apply {
                visibility = View.VISIBLE
                setImageBitmap(result.image)
            }
        } else {
            View.GONE
        }
    }

    override fun getItemCount(): Int {
        return mResultsList.size
    }

    fun notifyAdapter(item: OutputResult) {
        mResultsList.add(0, item)
        notifyItemInserted(0)
    }

    class ViewHolder internal constructor(binding: ResultRowBinding) :
        RecyclerView.ViewHolder(binding.root) {

        var mBinder = binding
    }
}