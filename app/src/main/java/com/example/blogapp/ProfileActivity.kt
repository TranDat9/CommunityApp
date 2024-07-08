package com.example.blogapp

import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.adapter.ViewPagerAdapter
import com.example.blogapp.databinding.ActivityProfileBinding
import com.example.blogapp.fragment.MyPostFragment
import com.example.blogapp.fragment.MyReelsFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileActivity : AppCompatActivity() {
    private  val binding: ActivityProfileBinding by lazy {
        ActivityProfileBinding.inflate(layoutInflater)
    }
    private lateinit var viewPagerAdapter: ViewPagerAdapter
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        viewPagerAdapter = ViewPagerAdapter(this.supportFragmentManager)
        viewPagerAdapter.addFragments(MyPostFragment(),"My Post")
        viewPagerAdapter.addFragments(MyReelsFragment(),"My Reels")

        binding.viewPager.adapter = viewPagerAdapter
        binding.tabLayout.setupWithViewPager(binding.viewPager)

        auth = FirebaseAuth.getInstance()

        val userId =auth.currentUser?.uid

        if(userId!=null)
        {
            loadUserProfile(userId);
        }


    }

    override fun onStart() {
        super.onStart()

    }


    private fun loadUserProfile(userId: String) {
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
                    Glide.with(this@ProfileActivity)
                        .load(it)
                        .into(binding.image)
                }
                name?.let { binding.name.text = it }
                email?.let { binding.bio.text = it }
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProfileActivity, "Error loading user data", Toast.LENGTH_LONG).show()
            }
        })
    }

}