package com.example.blogapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ImageButton
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.adapter.BlogAdapter
import com.example.blogapp.databinding.ActivitySaveArticlesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class SaveArticlesActivity : AppCompatActivity() {
    private val binding: ActivitySaveArticlesBinding by lazy {
        ActivitySaveArticlesBinding.inflate(layoutInflater)
    }
    private val saveBlogsArticles = mutableListOf<BlogItemModel>()
    private lateinit var blogAdapter: BlogAdapter
    private val auth =FirebaseAuth.getInstance()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        //initialize blog adapter
        blogAdapter = BlogAdapter(saveBlogsArticles.filter { it.isSaved }.toMutableList())
        val recyclerView= binding.saveArticlesRecyclerView
        recyclerView.adapter= blogAdapter
        recyclerView.layoutManager= LinearLayoutManager(this)


        val userId = auth.currentUser?.uid
        if(userId!=null)
        {
            val userReference = FirebaseDatabase.getInstance("https://blogapp-46ef8-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users").child(userId).child("saveBlogPosts")
            userReference.addListenerForSingleValueEvent(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    for(postSnapshot in snapshot.children)
                    {
                        val postId = postSnapshot.key
                        val isSaved:Boolean = postSnapshot.value as Boolean
                        if(postId!=null && isSaved)
                        {
                            //fetch the corresponding blog item on postId using coroutine
                            CoroutineScope(Dispatchers.IO).launch {
                                val blogitem = fetchBlogItem(postId)
                                if(blogitem!=null)
                                {
                                    saveBlogsArticles.add(blogitem)
                                    launch (Dispatchers.Main ){
                                        blogAdapter.updateData(saveBlogsArticles)

                                    }

                                }
                            }


                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
        }

        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish()
        }
    }

    private suspend fun fetchBlogItem(postId: String): BlogItemModel? {
        val blogReference =FirebaseDatabase.getInstance("https://blogapp-46ef8-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("users")
        return try {
            val dataSnapshot = blogReference.child(postId).get().await()
            val blogData =dataSnapshot.getValue(BlogItemModel::class.java)
            blogData
        }
        catch (e:Exception)
        {
            null
        }

    }
}