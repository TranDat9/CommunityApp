package com.example.blogapp.adapter

import android.graphics.ColorSpace.Model
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.Model.Comment
import com.example.blogapp.databinding.BlogItemBinding
import com.example.blogapp.databinding.ItemCommentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CommentAdapter(private val itemCmts : MutableList<Comment> ) : RecyclerView.Adapter<CommentAdapter.CommetViewHolder>()
{

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CommetViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ItemCommentBinding.inflate(inflater,parent,false)
        return CommetViewHolder(binding)
    }

    override fun getItemCount(): Int {
       return  itemCmts.size
    }

    override fun onBindViewHolder(holder: CommetViewHolder, position: Int) {
        val cmtItem =itemCmts[position]
        holder.bind(cmtItem)
    }





    inner class CommetViewHolder(private val binding: ItemCommentBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(commentModel: Comment) {
            binding.txtCmtText.text = commentModel.comment.toString()
            binding.txtCmtDate.text = commentModel.date.toString()
            binding.txtCmtname.text = commentModel.username.toString()
            Glide.with(binding.imgComtProfile.context)
                .load(commentModel.profileImage)
                .into(binding.imgComtProfile)

        }
    }

}