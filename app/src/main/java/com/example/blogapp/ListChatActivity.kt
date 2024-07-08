package com.example.blogapp

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.blogapp.databinding.ActivityListChatBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ListChatActivity : AppCompatActivity() {

    val binding: ActivityListChatBinding by lazy {
        ActivityListChatBinding.inflate(layoutInflater)
    }
    val dbRef = FirebaseDatabase.getInstance("https://blogapp-46ef8-default-rtdb.asia-southeast1.firebasedatabase.app").reference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)




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

            }
        })
    }

}