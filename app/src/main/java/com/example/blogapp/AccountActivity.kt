package com.example.blogapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.databinding.ActivityAccountBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AccountActivity : AppCompatActivity() {
    private val binding : ActivityAccountBinding by lazy {
        ActivityAccountBinding.inflate(layoutInflater)
    }

    private val userReference: DatabaseReference =
        FirebaseDatabase.getInstance("https://blogapp-46ef8-default-rtdb.asia-southeast1.firebasedatabase.app").
        getReference("users")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

       // val blogItem = intent.getParcelableExtra<BlogItemModel>("blogItem")
         val userId= intent.getStringExtra("userId")

        getdataAccount(userId.toString())


    }

    private fun getdataAccount(userId: String) {
        val userReference = FirebaseDatabase.getInstance("https://blogapp-46ef8-default-rtdb.asia-southeast1.firebasedatabase.app")
            .reference.child("users").child(userId)

        userReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Lấy dữ liệu của người dùng
                val profileImageUrl = snapshot.child("profileImage").getValue(String::class.java)
                val name = snapshot.child("name").getValue(String::class.java)
                val email = snapshot.child("email").getValue(String::class.java)

                // Cập nhật giao diện với dữ liệu lấy được
                profileImageUrl?.let {
                    Glide.with(this@AccountActivity)
                        .load(it)
                        .into(binding.image)
                }
                name?.let { binding.name.text = it }
                email?.let { binding.bio.text = it }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@AccountActivity, "Error loading user data", Toast.LENGTH_LONG).show()
            }
        })
    }



}