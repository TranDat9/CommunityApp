package com.example.blogapp.register

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.blogapp.R
import com.example.blogapp.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import kotlin.math.log

class SignUpActivity : AppCompatActivity() {
    private val binding:ActivitySignUpBinding by lazy {
        ActivitySignUpBinding.inflate(layoutInflater)
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage : FirebaseStorage


    private val PICK_IMAGE_REQUEST =1
    private var imageUri: Uri?=null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //initialize firebase authenticaton
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://blogapp-46ef8-default-rtdb.asia-southeast1.firebasedatabase.app")
        storage = FirebaseStorage.getInstance()




        //get data from edit text
        binding.buttonSignUp.setOnClickListener {


            val registerName = binding.edittextName.text.toString();
            val registerEmail = binding.edittexEmail.text.toString()
            val registerPassword = binding.edittextPassword.text.toString()

            if (registerName.isEmpty() || registerEmail.isEmpty() || registerPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all the details", Toast.LENGTH_LONG).show();
            } else {
                auth.createUserWithEmailAndPassword(registerEmail, registerPassword)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            auth.signOut()

                            user?.let {
                                //save user data in to firebase realtime database
                                val userReference = database.getReference("users")
                                val userId = user.uid
                                val userData = com.example.blogapp.Model.UserData(
                                    registerName,
                                    registerEmail
                                )
                                userReference.child(userId).setValue(userData)

                                //upload image to firebase storage
                                val storageReference =
                                    storage.reference.child("profile_image/$userId.jpg")

                                storageReference.putFile(imageUri!!)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            Log.d("TAG", "onCreate: image upload success")
                                            storageReference.downloadUrl.addOnCompleteListener { imageUri ->
                                                if (imageUri.isSuccessful) {
                                                    val imageUrl: String =
                                                        imageUri.result.toString()
                                                    Log.d("AAA", "File available at: $imageUrl")
                                                    // sava the image url to the real time data base
                                                    userReference.child(userId)
                                                        .child("profileImage").setValue(imageUrl)
//                                                     Glide.with(applicationContext)
//                                                     .load(imageUri)
//                                                     .apply(RequestOptions.circleCropTransform())
//                                                     .into(binding.imageProfile)
                                                }
                                            }
                                        }
                                    }
                                //end upload image
                                Toast.makeText(
                                    this,
                                    "User Register Successfully",
                                    Toast.LENGTH_LONG
                                ).show();
                                startActivity(Intent(this, SignInActivity::class.java))
                                finish()
                            }

                        } else {
                            Toast.makeText(this, "User Register Failed", Toast.LENGTH_LONG).show();
                        }
                    }
            }
        }


        binding.imageProfile.setOnClickListener{
            val intent = Intent()
            intent.type="image/*"
            intent.action= Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent,"select Image"),
                PICK_IMAGE_REQUEST
            )
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if( requestCode == PICK_IMAGE_REQUEST && resultCode== RESULT_OK && data != null && data.data!=null)
        {
            imageUri = data.data
            if (imageUri==null)
            {
                Log.d("AAA","select image")
            }
            else
            {
                Glide.with(this)
                    .load(imageUri)
                    .apply(RequestOptions.circleCropTransform())
                    .into(binding.imageProfile)
            }
        }
    }

}
