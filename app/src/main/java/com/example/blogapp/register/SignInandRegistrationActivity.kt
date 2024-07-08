package com.example.blogapp.register

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.service.autofill.UserData
import android.util.Log
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.blogapp.MainActivity
import com.example.blogapp.R
import com.example.blogapp.databinding.ActivitySignInandRegistrationBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class SignInandRegistrationActivity : AppCompatActivity() {
    private val binding:ActivitySignInandRegistrationBinding by lazy {
        ActivitySignInandRegistrationBinding.inflate(layoutInflater)
    }
    private lateinit var auth:FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage :FirebaseStorage
    private val PICK_IMAGE_REQUEST =1
    private var imageUri:Uri?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
           //initialize firebase authenticaton
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://blogapp-46ef8-default-rtdb.asia-southeast1.firebasedatabase.app")
        storage = FirebaseStorage.getInstance()
        val action =intent.getStringExtra("action")
        if (action=="login")
        {
           binding.loginEmailAddress.visibility= View.VISIBLE;
            binding.loginPassword.visibility= View.VISIBLE;
            binding.loginbutton.visibility= View.VISIBLE

            binding.resgisterButton.visibility = View.INVISIBLE;
            binding.registerNewHere.visibility= View.INVISIBLE;
            binding.registerEmail.visibility = View.GONE
            binding.registerPassword.visibility=View.GONE
            binding.registerName.visibility=View.GONE

            binding.loginbutton.setOnClickListener {
                val loginEmail : String = binding.loginEmailAddress.text.toString()
                val loginPassword : String = binding.loginPassword.text.toString()
                if(loginEmail.isEmpty() && loginPassword.isEmpty())
                {
                    Toast.makeText(this,"Fill details",Toast.LENGTH_LONG).show()
                }
                else{
                    auth.signInWithEmailAndPassword(loginEmail,loginPassword)
                        .addOnCompleteListener {task ->
                                if(task.isSuccessful)
                                {
                                    Toast.makeText(this,"Login Success",Toast.LENGTH_LONG).show()
                                    startActivity(Intent(this,MainActivity::class.java))
                                    finish()
                                }else{
                                    Toast.makeText(this,"Login Failed",Toast.LENGTH_LONG).show()

                                }
                        }

                }

            }

        }else if(action=="register")
        {

            binding.loginbutton.visibility= View.INVISIBLE
            binding.resgisterButton.setOnClickListener{
                //get data from edit text
                val registerName = binding.registerName.text.toString();
                val registerEmail = binding.registerEmail.text.toString()
                val registerPassword = binding.registerPassword.text.toString()

                if(registerName.isEmpty() || registerEmail.isEmpty()||registerPassword.isEmpty())
                {
                    Toast.makeText(this , "Please fill all the details",Toast.LENGTH_LONG).show();
                }else
                {
                    auth.createUserWithEmailAndPassword(registerEmail,registerPassword)
                        .addOnCompleteListener{ task ->
                            if(task.isSuccessful)
                            {
                                val user =auth.currentUser
                               auth.signOut()

                                user?.let {
                                    //save user data in to firebase realtime database
                                 val userReference = database.getReference("users")
                                    val userId = user.uid
                                    val userData = com.example.blogapp.Model.UserData(
                                        registerName,
                                        registerEmail,
                                        "https://firebasestorage.googleapis.com/v0/b/blogapp-46ef8.appspot.com/o/profile_image%2Fk%20(3).png?alt=media&token=5cb7bdae-4437-4913-8e3a-8f744f18714b"
                                    )
                                    userReference.child(userId).setValue(userData)

                                    //upload image to firebase storage
                                    val storageReference =storage.reference.child("profile_image/$userId.jpg")

                                    storageReference.putFile(imageUri!!)
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful)
                                            {
                                                Log.d("TAG","onCreate: image upload success")
                                                storageReference.downloadUrl.addOnCompleteListener { imageUri ->
                                                    if(imageUri.isSuccessful)
                                                    {
                                                        val imageUrl:String =imageUri.result.toString()
                                                        // sava the image url to the real time data base
                                                        userReference.child(userId).child("profileImage").setValue(imageUrl)
                                                       /* Glide.with(applicationContext)
                                                            .load(imageUri)
                                                            .apply(RequestOptions.circleCropTransform())
                                                            .into(binding.registerImageUser)*/
                                                    }
                                                }
                                            }
                                    }
                                    //end upload image
                                    Toast.makeText(this , "User Register Successfully",Toast.LENGTH_LONG).show();
                                    startActivity(Intent(this,WelcomeActivity::class.java))
                                    finish()
                            }

                            }else{
                                Toast.makeText(this , "User Register Failed",Toast.LENGTH_LONG).show();
                            }


                        }
                }
            }

        }
        binding.registerImageUser.setOnClickListener{
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
           Glide.with(this)
               .load(imageUri)
               .apply(RequestOptions.circleCropTransform())
               .into(binding.registerImageUser)
       }
    }


}