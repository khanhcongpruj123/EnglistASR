package com.asr.englishasr

import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.asr.englishasr.databinding.ItemWordBinding

class ListWordAdapter (val context: Context): RecyclerView.Adapter<ListWordAdapter.WordHolder>() {

    private val list = mutableListOf<ItemWord>()
    private val listColor = listOf(
        "#FF0000",
        "#FFFF00",
        "#FFFF00",
        "#7FFF00",
        "#00FF00"
    )

    inner class WordHolder(val binding: ItemWordBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(itemWord: ItemWord) {

            binding.text.text = itemWord.word

            var index = (itemWord.score * listColor.size).toInt() - 1

            if (index < 0) index = 0
            if (index > listColor.size) index = listColor.size - 1

            val color = listColor[index]

            Log.d("AppLog", "Bind: ${itemWord.word} - ${list.size}")

            binding.text.setTextColor(Color.parseColor(color))
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WordHolder {
        return WordHolder(ItemWordBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun onBindViewHolder(holder: WordHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    fun submitList(list: List<ItemWord>) {
        this.list.clear()
        this.list.addAll(list)
        notifyDataSetChanged()
    }
}