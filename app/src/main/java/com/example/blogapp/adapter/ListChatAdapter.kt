package com.example.blogapp.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.Model.UserData
import com.example.blogapp.databinding.BlogItemBinding
import com.example.blogapp.databinding.ItemListChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class ListChatAdapter(private val items: MutableList<UserData>) :
   RecyclerView.Adapter<ListChatAdapter.ListChatViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListChatViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemListChatBinding.inflate(inflater,parent,false)
        return ListChatViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: ListChatViewHolder, position: Int) {
        val listchatItem =items[position]
        holder.bind(listchatItem)
    }

    inner class ListChatViewHolder(private val binding: ItemListChatBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(listItemModel: UserData) {
            binding.name.text = listItemModel.name
            Glide.with(binding.image.context)
                .load(listItemModel.profileImage)
                .into(binding.image)
        }

    }



}