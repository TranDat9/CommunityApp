package com.example.blogapp.adapter

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogapp.AccountActivity
import com.example.blogapp.ChatActivity
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.ProfileActivity
import com.example.blogapp.ReadMoreActivity
import com.example.blogapp.databinding.BlogItemBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*


class BlogAdapter(private val items : MutableList<BlogItemModel>):
    RecyclerView.Adapter<BlogAdapter.BlogViewHolder>() {

     private val databaseReference:DatabaseReference = FirebaseDatabase
         .getInstance("https://blogapp-46ef8-default-rtdb.asia-southeast1.firebasedatabase.app").reference
    private val currentUser= FirebaseAuth.getInstance().currentUser




    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BlogAdapter.BlogViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = BlogItemBinding.inflate(inflater,parent,false)
        return BlogViewHolder(binding)
    }
    override fun onBindViewHolder(holder: BlogAdapter.BlogViewHolder, position: Int) {
        val blogItem =items[position]
        holder.bind(blogItem)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    inner class BlogViewHolder(private val binding:BlogItemBinding):
        RecyclerView.ViewHolder(binding.root) {
        fun bind(blogItemModel: BlogItemModel) {
            val postId = blogItemModel.postId
            val userId = blogItemModel.userId
            val context = binding.root.context
           binding.heading.text = blogItemModel.heading.toString()
            Glide.with(binding.profile.context)
                .load(blogItemModel.profileImage)
                .into(binding.profile)
            binding.username.text = blogItemModel.username.toString()
            binding.date.text = blogItemModel.date.toString()
            binding.post.text = blogItemModel.post.toString()
            binding.likecount.text = blogItemModel.likeCount.toString()


            //check if the current user has liked the post and update image like
            val postLikeReference :DatabaseReference = databaseReference.child("blogs").child(postId).child("likes")
             val currentUserLiked = currentUser?.uid?.let { uid ->
                 postLikeReference.child(uid).addListenerForSingleValueEvent(object : ValueEventListener{
                     override fun onDataChange(snapshot: DataSnapshot) {
                         if(snapshot.exists())
                         {
                         binding.likebutton.setImageResource(com.example.blogapp.R.drawable.heart_liked)
                         }else{
                             binding.likebutton.setImageResource(com.example.blogapp.R.drawable.heart)
                         }
                     }

                     override fun onCancelled(error: DatabaseError) {

                     }
                 })
             }

            binding.addBlogbutton.setOnClickListener {
                val context =binding.root.context
                val intent =Intent(context,ReadMoreActivity::class.java)
                intent.putExtra("postId",postId)
                intent.putExtra("blogItem",blogItemModel)
                context.startActivity(intent)

            }
            //handles like button clicks
            binding.likebutton.setOnClickListener {
               if(currentUser!=null)
               {
                   handleLikeButtonClickd(postId,blogItemModel,binding)
               }
                else{
                   Toast.makeText(context,"You must login First", Toast.LENGTH_LONG).show()
               }
            }

            binding.profile.setOnClickListener {
                val context = binding.root.context
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

                if (currentUserId == userId) {
                    val intent = Intent(context, ProfileActivity::class.java)
                    context.startActivity(intent)
                } else {
                    val intent = Intent(context, ChatActivity::class.java)
                    intent.putExtra("uid", userId)
                    intent.putExtra("name", blogItemModel.username.toString())
                    intent.putExtra("image", blogItemModel.profileImage.toString())
                    context.startActivity(intent)
                }
            }



            //handles save button clicks
            binding.postsaveButton.setOnClickListener {
                if(currentUser!=null)
                {
                    handleSaveButtonClickd(postId,blogItemModel,binding)
                }
                else{
                    Toast.makeText(context,"You must login First", Toast.LENGTH_LONG).show()
                }
            }

            val userReference = databaseReference.child("users").child(currentUser?.uid?:"")
            val postSaveReference = userReference.child("saveBlogPosts").child(postId)
            postSaveReference.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists())
                    {
                        //if blog already save
                        binding.postsaveButton.setImageResource(com.example.blogapp.R.drawable.unsave_article_ref)
                    }else {
                        binding.postsaveButton.setImageResource(com.example.blogapp.R.drawable.save_article_fill_red)
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }
    }


    private fun handleLikeButtonClickd(postId: String, blogItemModel: BlogItemModel,binding: BlogItemBinding) {
          val userReference = databaseReference.child("users").child(currentUser!!.uid)
        val postLikeReference = databaseReference.child("blogs").child(postId).child("likes")
        //User has already liked the post , so unlike it
        postLikeReference.child(currentUser.uid)
            .addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    userReference.child("likes").child(postId).removeValue()
                        .addOnSuccessListener {
                            postLikeReference.child(currentUser.uid).removeValue()
                            blogItemModel.likedBy?.remove(currentUser.uid)
                            updateLikeButton(binding,false)

                            //decrement the likes in data base
                            val newLikeCount = blogItemModel.likeCount-1
                            blogItemModel.likeCount = newLikeCount
                            databaseReference.child("blogs").child(postId).child("likeCount").setValue(newLikeCount)
                            notifyDataSetChanged()
                        }
                        .addOnFailureListener {e->
                            Log.e("LikedClick","Failed to unlike the blog $e", )

                        }
                }
                else{
                    //user not liked the post , so like it
                    userReference.child("likes").child(postId).setValue(true)
                        .addOnSuccessListener {
                            postLikeReference.child(currentUser.uid).setValue(true)
                            blogItemModel.likedBy?.add(currentUser.uid)
                            updateLikeButton(binding,true)

                            //increment the likes in data base
                            val newLikeCount = blogItemModel.likeCount+1
                            blogItemModel.likeCount = newLikeCount
                            databaseReference.child("blogs").child(postId).child("likeCount").setValue(newLikeCount)
                            notifyDataSetChanged()
                        }
                        .addOnFailureListener {e->
                            Log.e("LikedClick","Failed to like the blog $e", )

                        }

                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun updateLikeButton(binding:BlogItemBinding,liked: Boolean) {
        if(liked )
        {
            binding.likebutton.setImageResource(com.example.blogapp.R.drawable.heart)
        }else{
            binding.likebutton.setImageResource(com.example.blogapp.R.drawable.heart_liked)
        }
    }


    private fun handleSaveButtonClickd(
        postId: String,
        blogItemModel: BlogItemModel,
        binding: BlogItemBinding
    ) {
          val userReference =databaseReference.child("users").child(currentUser!!.uid)
        userReference.child("saveBlogPosts").child(postId).addListenerForSingleValueEvent(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists())
                {
                    //tHE BLOG is currently save , so unsave it
                    userReference.child("saveBlogPosts").child(postId).child(postId).removeValue()
                        .addOnSuccessListener {
                            //update    the ui
                            val clickedBlogItem = items.find { it.postId== postId }
                            clickedBlogItem?.isSaved = false
                            notifyDataSetChanged()

                            val context = binding.root.context
                            Toast.makeText(context,"blog un saved ",Toast.LENGTH_LONG).show()

                        }.addOnFailureListener {
                            val context = binding.root.context
                            Toast.makeText(context,"Failed blog un saved ",Toast.LENGTH_LONG).show()
                        }
                    binding.postsaveButton.setImageResource(com.example.blogapp.R.drawable.unsave_article_ref)
                }else{
                    //tHE BLOG is not save , so save it
                    userReference.child("saveBlogPosts").child(postId).child(postId).setValue(true)
                        .addOnSuccessListener {
                            //update    the ui
                            val clickedBlogItem = items.find { it.postId== postId }
                            clickedBlogItem?.isSaved = true
                            notifyDataSetChanged()

                            val context = binding.root.context
                            Toast.makeText(context,"blog saved ",Toast.LENGTH_LONG).show()

                        }.addOnFailureListener {
                            val context = binding.root.context
                            Toast.makeText(context,"Failed blog  save ",Toast.LENGTH_LONG).show()
                        }
                    binding.postsaveButton.setImageResource(com.example.blogapp.R.drawable.save_article_fill_red)
                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    fun updateData(saveBlogsArticles:List<BlogItemModel>) {
          items.clear()
        items.addAll(saveBlogsArticles)
        notifyDataSetChanged()
    }
}