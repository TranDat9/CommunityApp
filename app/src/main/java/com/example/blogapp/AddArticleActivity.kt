package com.example.blogapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.blogapp.Model.BlogItemModel
import com.example.blogapp.Model.UserData
import com.example.blogapp.databinding.ActivityAddArticleBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import java.text.SimpleDateFormat
import java.util.*


class AddArticleActivity : AppCompatActivity() {
    private val binding : ActivityAddArticleBinding by lazy {
        ActivityAddArticleBinding.inflate(layoutInflater)
    }
  private val databaseReference: DatabaseReference =
      FirebaseDatabase.getInstance("https://blogapp-46ef8-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("blogs")
    private val userReference: DatabaseReference =
        FirebaseDatabase.getInstance("https://blogapp-46ef8-default-rtdb.asia-southeast1.firebasedatabase.app").getReference("users")
    private val auth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.imageButton.setOnClickListener {
            finish()
        }
        binding.addBlogbutton.setOnClickListener {
            val title :String = binding.blogTiltle.editText?.text.toString().trim()
            val description :String = binding.blogDescription.editText?.text.toString().trim()
            if(title.isEmpty() || description.isEmpty() )
            {
                Toast.makeText(this,"Fill all the fields",Toast.LENGTH_LONG).show()
            }
            //get current user
            val user:FirebaseUser?=auth.currentUser
            if(user!=null)
            {
                val userID:String = user.uid
                val userName :String= user.displayName?:"Anymous"
                val userUmageURL = user.photoUrl?:""

                // fetch user name and  profile from database
                userReference.child(userID).addListenerForSingleValueEvent(object :ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                       val userData = snapshot.getValue(UserData::class.java)
                        if(userData !=null)
                        {
                            val userNameFromDB = userData.name
                            val userImageURLfromDB = userData.profileImage

                            val currentDate = SimpleDateFormat("yyy-MM-dd").format(Date())
                            //create a blogitemmodel
                            val blogItem = BlogItemModel(
                                title,
                                userNameFromDB,
                                currentDate,
                                description,
                                0,
                                userID,
                                userImageURLfromDB
                            )
                            //generate a unique key for the blog post
                            val key = databaseReference.push().key
                            if(key !=null)
                            {
                                blogItem.postId =key
                                   val blogReference =databaseReference.child(key)
                                blogReference.setValue(blogItem).addOnCompleteListener {
                                     if(it.isSuccessful)
                                     {
                                         finish()
                                     }else{
                                         Toast.makeText(this@AddArticleActivity,"failed add blog",Toast.LENGTH_LONG).show()
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