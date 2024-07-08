package com.example.blogapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.LinearLayout
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.adapter.BlogAdapter
import com.example.blogapp.databinding.ActivityMainBinding
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging

class MainActivity : AppCompatActivity() {
    private val  binding : ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }

      private  lateinit var databaseReference: DatabaseReference
      private val blogItems = mutableListOf<BlogItemModel>()
    private lateinit var auth:FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        //
        binding.profileImage.setOnClickListener {
            startActivity(Intent(this,ProfileActivity::class.java))
        }

        //go to save acticity
        binding.saveArticleButton.setOnClickListener {
            startActivity(Intent(this,SaveArticlesActivity::class.java))
        }

        auth = FirebaseAuth.getInstance()
        databaseReference =FirebaseDatabase
                            .getInstance("https://blogapp-46ef8-default-rtdb.asia-southeast1.firebasedatabase.app")
                            .reference.child("blogs")

        val userId =auth.currentUser?.uid

        // set profile image
        if(userId!=null)
        {
            loadUserProfileImage(userId);
        }
      // set blogs post in to recycle view

        //initialize the recycle view and adapter
        val recyclerView = binding.blogRecycleview
        val blogAdapter = BlogAdapter(blogItems)
        recyclerView.adapter = blogAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)
        // fetch data from data base

        databaseReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                blogItems.clear()
                for(snapshot in snapshot.children)
                {
                    val blogItem = snapshot.getValue(BlogItemModel::class.java)
                    if(blogItem !=null)
                    {
                        blogItems.add(blogItem)
                    }
                }
                //revers blog
                blogItems.reverse()
                // notify data change
                blogAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@MainActivity,"Blog Data load failed",Toast.LENGTH_LONG).show()
            }
        })

        binding.floatingAddArticleButton.setOnClickListener {
            startActivity(Intent(this,AddArticleActivity::class.java))

        }
        getFCMToken()
    }

    private fun getFCMToken() {

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.i("token1", token)

            }






        })
    }

    private fun loadUserProfileImage(userId: String) {
        val userReference = FirebaseDatabase.getInstance("https://blogapp-46ef8-default-rtdb.asia-southeast1.firebasedatabase.app").reference.child("users").child(userId)
        userReference.child("profileImage").addValueEventListener(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val profileImageUrl = snapshot.getValue(String::class.java)
                if(profileImageUrl!=null)
                {
                    Glide.with(this@MainActivity)
                        .load(profileImageUrl)
                        .into(binding.profileImage)
                }
            }

            override fun onCancelled(error: DatabaseError) {
               Toast.makeText(this@MainActivity,"Error loading profile image",Toast.LENGTH_LONG).show()
            }
        })


    }
}