package com.example.blogapp.register

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.blogapp.MainActivity
import com.example.blogapp.R
import com.example.blogapp.databinding.ActivitySignInBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class SignInActivity : AppCompatActivity() {
    private val binding : ActivitySignInBinding by lazy {
        ActivitySignInBinding.inflate(layoutInflater)
    }

    private lateinit var auth: FirebaseAuth
    private lateinit var database: FirebaseDatabase
    private lateinit var storage : FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        //initialize firebase authenticaton
        auth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance("https://blogapp-46ef8-default-rtdb.asia-southeast1.firebasedatabase.app")
        storage = FirebaseStorage.getInstance()

        binding.textcreateNewAccount.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
            finish()
        }

        binding.buttonSignIn.setOnClickListener {
            val email = binding.edittextEmal.text.toString()
            val password = binding.edittextPassword.text.toString()

            if (email.isEmpty() || password.isEmpty())
            {
                Toast.makeText(this , "Please Enter Email or Password",Toast.LENGTH_SHORT).show()
            }
            else{
                auth.signInWithEmailAndPassword(email,password)
                    .addOnCompleteListener {
                        if (it.isSuccessful){
                            Toast.makeText(this , "Login Successful",Toast.LENGTH_SHORT).show()
                            Toast.makeText(this,"Login Success",Toast.LENGTH_LONG).show()
                            startActivity(Intent(this, MainActivity::class.java))
                            finish()
                        }
                        else{
                            Toast.makeText(this , "Login Faile",Toast.LENGTH_SHORT).show()
                        }
                    }
            }
        }

    }


    override fun onStart() {
        super.onStart()
        val currentUser: FirebaseUser? = auth.currentUser
        if(currentUser!=null)
        {
            startActivity(Intent(this,MainActivity::class.java))
            finish()
        }

    }
}