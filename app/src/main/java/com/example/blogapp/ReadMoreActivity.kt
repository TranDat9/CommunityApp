package com.example.blogapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.Glide.RequestOptionsFactory
import com.bumptech.glide.request.RequestOptions
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.Model.Comment
import com.example.blogapp.Model.UserData
import com.example.blogapp.adapter.BlogAdapter
import com.example.blogapp.adapter.CommentAdapter
import com.example.blogapp.databinding.ActivityAddArticleBinding
import com.example.blogapp.databinding.ActivityReadMoreBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.SimpleDateFormat
import java.util.Date

class ReadMoreActivity : AppCompatActivity() {
    private val binding : ActivityReadMoreBinding by lazy {
        ActivityReadMoreBinding.inflate(layoutInflater)
    }


    private val databaseReference: DatabaseReference =
        FirebaseDatabase.getInstance("https://blogapp-46ef8-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("blogs")



    private val userReference: DatabaseReference =
        FirebaseDatabase.getInstance("https://blogapp-46ef8-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users")
    private val auth = FirebaseAuth.getInstance()
    private val cmtItems = mutableListOf<Comment>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        val blogItem = intent.getParcelableExtra<BlogItemModel>("blogItem")
        val postId = intent.getStringExtra("postId")

   if (blogItem!=null)
   {
       binding.txtRMheading.text = blogItem.heading.toString()
       binding.txtRMpost.text = blogItem.post.toString()
      if (blogItem.profileImage !=null)
      {
          Glide.with(this@ReadMoreActivity)
              .load(blogItem.profileImage)
              .into(binding.imgRMProfile)
      }
   }

        //initialize the recycle view and adapter
        val recyclerView = binding.rcvCmt
        val cmtAdapter = CommentAdapter(cmtItems)
        recyclerView.adapter = cmtAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // fetch data from data base
       val CmtReference: DatabaseReference =databaseReference.child(postId.toString()).child("comments")
        CmtReference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                cmtItems.clear()
                for(snapshot in snapshot.children)
                {
                    val cmtItem = snapshot.getValue(Comment::class.java)
                    if(cmtItem !=null)
                    {
                        cmtItems.add(cmtItem)
                    }
                }
                //revers blog
                cmtItems.reverse()
                // notify data change
                cmtAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ReadMoreActivity," load failed",Toast.LENGTH_LONG).show()
            }
        })

        binding.backButton.setOnClickListener {
            finish()
        }
        binding.btnCancelCmt.setOnClickListener {
           binding.txtAddCmt.setText("")

        }
        binding.btnAddCmt.setOnClickListener {
             val contentCmt :String = binding.txtAddCmt.text.toString().trim()
            val user: FirebaseUser?=auth.currentUser

            if(user!=null) {
            val userID:String = user.uid
            userReference.child(userID).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userData = snapshot.getValue(UserData::class.java)
                    if(userData !=null)
                    {
                        val userNameFromDB = userData.name
                        val userImageURLfromDB = userData.profileImage
                        val currentDate = SimpleDateFormat("yyy-MM-dd").format(Date())
                        //create a cmt item
                        val cmtItem = Comment(
                            contentCmt,
                            currentDate,
                            userImageURLfromDB,
                            userNameFromDB,
                            userID

                        )
                        //generate a unique key for the blog post
                        val key = CmtReference.push().key
                        if(key !=null)
                        {
                            cmtItem.commentId =key
                            val blogReference =CmtReference.child(key)
                            blogReference.setValue(cmtItem).addOnCompleteListener {
                                if(it.isSuccessful)
                                {
                                    binding.txtAddCmt.setText("")
                                }else{
                                    Toast.makeText(this@ReadMoreActivity,"failed add cmt",Toast.LENGTH_LONG).show()
                                }
                            }
                        }

                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
               })
            }
        }

    }
}