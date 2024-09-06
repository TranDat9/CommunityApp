package com.example.blogapp

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.blogapp.Model.UserData
import com.example.blogapp.adapter.BlogAdapter
import com.example.blogapp.adapter.ListChatAdapter
import com.example.blogapp.databinding.ActivityListChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
class ListChatActivity : AppCompatActivity() {

    private val databaseReference: DatabaseReference = FirebaseDatabase
        .getInstance("https://your-database-url").reference
    private val currentUser= FirebaseAuth.getInstance().currentUser

    val binding: ActivityListChatBinding by lazy {
        ActivityListChatBinding.inflate(layoutInflater)
    }
    val dbRef = FirebaseDatabase.getInstance("https://blogapp-46ef8-default-rtdb.asia-southeast1.firebasedatabase.app").reference
    private val userDataList = mutableListOf<UserData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        Log.d("ListChatActivity2", "Chat")
        //initialize the recycle view and adapter
        val recyclerView = binding.recyclerView
        val listchatAdapter = ListChatAdapter(userDataList)
        recyclerView.adapter = listchatAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val userReference = FirebaseDatabase.getInstance("https://blogapp-46ef8-default-rtdb.asia-southeast1.firebasedatabase.app")
            .reference.child("users")
            .child(userId!!)

        userReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("name").getValue(String::class.java)
                val profile = snapshot.child("profileImage").getValue(String::class.java)

                // Cập nhật tên và ảnh vào binding
                binding!!.name.text = name
                Glide.with(this@ListChatActivity).load(profile).into(binding!!.image)
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ListChatActivity, "Failed to load user data", Toast.LENGTH_SHORT).show()
            }
        })


        currentUser?.uid?.let { currentUserUid ->
            getChatList(currentUserUid) { chatList ->
                Log.d("ListChatActivity", "Chat List: $chatList")
                getUserDataList(chatList) { users ->
                    userDataList.clear()
                    userDataList.addAll(users)
                    listchatAdapter.notifyDataSetChanged()
                    Log.d("ListUser","user :$userDataList")
                    // Assuming you have a RecyclerView setup in your layout
                    binding.recyclerView.adapter = listchatAdapter
                }
            }
        }
    }

    fun getChatList(currentUserUid: String, onComplete: (List<String>) -> Unit) {
        val chatListRef = dbRef.child("users").child(currentUserUid).child("chatWith")
        chatListRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val chatList = mutableListOf<String>()
                snapshot.children.forEach {
                    chatList.add(it.key ?: "")
                }
                onComplete(chatList)
            }
            override fun onCancelled(error: DatabaseError) {
                // Handle the error
                Log.e("ListChatActivity", "Database error: ${error.message}")
            }
        })
    }

    fun getUserDataList(chatList: List<String>, onComplete: (List<UserData>) -> Unit) {
        val usersRef = dbRef.child("users")
        val usersData = mutableListOf<UserData>()
        chatList.forEach { userId ->
            usersRef.child(userId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userData = snapshot.getValue(UserData::class.java)
                    userData?.let { usersData.add(it) }
                    if (usersData.size == chatList.size) {
                        onComplete(usersData)
                    }
                }
                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }
}
