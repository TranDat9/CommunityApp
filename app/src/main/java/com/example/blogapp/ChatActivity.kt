package com.example.blogapp

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.blogapp.Model.Message
import com.example.blogapp.adapter.MessagesAdapter
import com.example.blogapp.databinding.ActivityChatBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storageMetadata
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date

const val REQUEST_CODE = 200
class ChatActivity : AppCompatActivity() {

    var binding: ActivityChatBinding? = null
    var adapter: MessagesAdapter? = null
    var messages: ArrayList<Message>? = null
    var senderRoom: String? = null
    var receiverRoom: String? = null
    var database : FirebaseDatabase? = null
    var storage : FirebaseStorage? = null
    var dialog : ProgressDialog? = null
    var senderUid: String? = null
    var receiverUid: String? = null

    private lateinit var amplitudes: ArrayList<Float>

    private lateinit var recorder: MediaRecorder
    private var dirPath = ""
    private var fileName = ""


    private var permissions = arrayOf(Manifest.permission.RECORD_AUDIO)
    private var permissionGranted = false




    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding!!.root)
        setSupportActionBar(binding!!.toolbar)
        dialog = ProgressDialog(this)
        database = FirebaseDatabase.getInstance("https://blogapp-46ef8-default-rtdb.asia-southeast1.firebasedatabase.app")
        storage = FirebaseStorage.getInstance()
        dialog!!.setMessage("Uploading image...")
        dialog!!.setCancelable(false)
        messages = ArrayList()

        binding!!.linearChat.visibility = View.VISIBLE
        binding!!.linearRecord.visibility = View.GONE


        permissionGranted = ActivityCompat.checkSelfPermission(this , permissions[0]) == PackageManager.PERMISSION_GRANTED

        val name = intent.getStringExtra("name")
        val profile = intent.getStringExtra("image")
        binding!!.name.text = name
        Glide.with(this@ChatActivity).load(profile)

            .into(binding!!.image)



        binding!!.imageView2.setOnClickListener {
            finish()
        }

        receiverUid = intent.getStringExtra("uid")
        senderUid = FirebaseAuth.getInstance().uid
        database!!.reference.child("Presence").child(receiverUid!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    if(snapshot.exists()){
                        val status = snapshot.getValue(String :: class.java)
                        if ( status == "offline"){
                            binding!!.status.visibility = View.GONE
                        }else{
                            binding!!.status.setText(status)
                            binding!!.status.visibility = View.VISIBLE
                        }
                    }
                }

                override fun onCancelled(p0: DatabaseError) {

                }
            })

        senderRoom = senderUid + receiverUid
        receiverRoom = receiverUid + senderUid
        adapter = MessagesAdapter(this@ChatActivity, messages, senderRoom!!, receiverRoom!!)

        binding!!.recyclerView.layoutManager = LinearLayoutManager(this@ChatActivity)
        binding!!.recyclerView.adapter = adapter

        database!!.reference.child("chats")
            .child(senderRoom!!)
            .child("messages")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    messages!!.clear()
                    for(snapshot1 in snapshot.children){
                        val message: Message? = snapshot1.getValue(Message ::class.java)
                        message!!.messageId = snapshot1.key
                        messages!!.add(message)
                        adapter!!.notifyDataSetChanged()
                    }

                }

                override fun onCancelled(p0: DatabaseError) {
                    TODO("Not yet implemented")
                }

            }
            )



        val handler = Handler()
        binding!!.messageBox.addTextChangedListener(object: TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, affter: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun afterTextChanged(s: Editable?) {
                database!!.reference.child("Presence")
                    .child(senderUid!!)
                    .setValue("typing...")
                handler.removeCallbacksAndMessages(null)
                handler.postDelayed(userStoppedTyping,1000)
            }
            var userStoppedTyping = Runnable {
                database!!.reference.child("Presence")
                    .child(senderUid!!)
                    .setValue("Online")

            }

        })
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding!!.attach.setOnClickListener{
            val intent = Intent()
            intent.type="image/*"
            intent.action= Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent,"select Image"),
                25
            )
        }
        binding!!.sendBtn.setOnClickListener {

            val messageTxt: String = binding!!.messageBox.text.toString()
            if (messageTxt.isNotEmpty()) {
            val date = Date()
            val message = Message(messageTxt, senderUid, date.time)

            binding!!.messageBox.setText("")
            val randomKey = database!!.reference.push().key
            val lastMsgObj = HashMap<String, Any>()
            lastMsgObj["lastMsg"] = message.message!!
            lastMsgObj["lastMsgTime"] = date.time

            database!!.reference.child("chats").child(senderRoom!!)
                .updateChildren(lastMsgObj)
            database!!.reference.child("chats").child(receiverRoom!!)
                .updateChildren(lastMsgObj)

            database!!.reference.child("chats").child(senderRoom!!)
                .child("messages")
                .child(randomKey!!)
                .setValue(message).addOnSuccessListener {
                    database!!.reference.child("chats")
                        .child(receiverRoom!!)
                        .child("messages")
                        .child(randomKey)
                        .setValue(message)
                        .addOnSuccessListener {

                        }
                }
             addToChatList(senderUid!!, receiverUid!!)
            }else {
                uploadAudio()
            }

        }


        binding!!.btnClose.setOnClickListener {

            binding!!.linearImagesend.visibility = View.GONE
        }

        binding!!.btnMic.setOnClickListener {
            startRecording()
            binding!!.linearChat.visibility = View.GONE
            binding!!.linearRecord.visibility = View.VISIBLE

        }

        binding!!.btnDelete.setOnClickListener {
            stopRecorder()
            binding!!.waveformView.clear()
            binding!!.linearChat.visibility = View.VISIBLE
            binding!!.linearRecord.visibility = View.GONE

        }

    }



    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 25 && resultCode == RESULT_OK && data != null && data.data!=null)
        {

              var selectedImage = data.data

            if (selectedImage==null)
            {
                Log.d("Select","select image")
            }else
            {
                binding!!.linearImagesend.visibility = View.VISIBLE
                Glide.with(this)
                    .load(selectedImage)
                    .into(binding!!.imagesend)
                // up
                val calender = Calendar.getInstance()
                var refence = storage!!.reference.child("chats")
                    .child(calender.timeInMillis.toString()+"")
                dialog!!.show()
                refence.putFile(selectedImage!!)
                    .addOnCompleteListener { task ->
                        dialog!!.dismiss()
                        if (task.isSuccessful) {
                            refence.downloadUrl.addOnSuccessListener { uri ->
                                val filePath = uri.toString()
                                val messageTxt: String = binding!!.messageBox.text.toString()
                                val date = Date()
                                val message = Message(messageTxt, senderUid, date.time)
                                message.message = "photo"
                                message.imageurl = filePath
                                binding!!.messageBox.setText("")
                                val randomkey = database!!.reference.push().key
                                val lastMsgOjb = HashMap<String, Any>()
                                lastMsgOjb["lastMsg"] = message.message!!
                                lastMsgOjb["lastMsgTime"] = date.time
                                database!!.reference.child("chats")
                                    .child(senderRoom!!)
                                    .updateChildren(lastMsgOjb)

                                database!!.reference.child("chats")
                                    .child(receiverRoom!!)
                                    .updateChildren(lastMsgOjb)

                                database!!.reference.child("chats")
                                    .child(senderRoom!!)
                                    .child("messages")
                                    .child(randomkey!!)
                                    .setValue(message).addOnSuccessListener {
                                        adapter!!.notifyDataSetChanged()
                                    }

                                database!!.reference.child("chats")
                                    .child(receiverRoom!!)
                                    .child("messages")
                                    .child(randomkey)
                                    .setValue(message)
                                    .addOnSuccessListener {
                                        adapter!!.notifyDataSetChanged()
                                    }
                            }
                        }
                    }
            }
        }
    }


    fun addToChatList(currentUserUid: String, otherUserUid: String) {
        val dbRef = FirebaseDatabase.getInstance("https://blogapp-46ef8-default-rtdb.asia-southeast1.firebasedatabase.app")
            .reference
        val currentUserRef = dbRef.child("users")
            .child(currentUserUid).child("chatWith")
            .child(otherUserUid)
        currentUserRef.setValue(true)

        val otherUserRef = dbRef.child("users")
            .child(otherUserUid)
            .child("chatWith")
            .child(currentUserUid)
        otherUserRef.setValue(true)
    }


    override fun onResume() {
        super.onResume()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("Presence")
            .child(currentId!!)
            .setValue("Online")
    }

    override fun onPause() {
        super.onPause()
        val currentId = FirebaseAuth.getInstance().uid
        database!!.reference.child("Presence")
            .child(currentId!!)
            .setValue("Offline")
    }

    private fun stopRecorder() {
        recorder.apply {
            stop()
            release()
        }
        handler.removeCallbacks(updateWaveform)
    }

    private val handler = Handler()
    private val updateWaveform = object : Runnable {
        override fun run() {
            if (permissionGranted) {
                binding!!.waveformView.addAmplitude(recorder.maxAmplitude.toFloat())
                handler.postDelayed(this, 10) // Cập nhật mỗi 100ms
            }
        }
    }

    private fun startRecording() {
        if (!permissionGranted) {
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE)
            return
        }
        recorder = MediaRecorder()
        dirPath = "${externalCacheDir?.absolutePath}/"
        val simpleDateFormat = SimpleDateFormat("yyy.MM.DD_hh.mm.ss")
        val date = simpleDateFormat.format(Date())
        fileName = "audio_record_$date"
        recorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile("$dirPath$fileName.mp3")
            try {
                prepare()
            } catch (e: Exception) {
                Log.e("RecorderError", "Recording failed", e)
                e.printStackTrace()
            }
            start()
        }
        handler.post(updateWaveform)
    }

    private fun uploadFileToFirebase(file: File) {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference.child("audio_records/${file.name}")

        // Tạo URI từ file
        val uri = Uri.fromFile(file)
        val metadata = storageMetadata {
            contentType = "audio/mpeg"
        }

        val uploadTask = storageRef.putFile(uri, metadata)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val audioUrl = uri.toString()

            }
        }.addOnFailureListener { exception ->

        }
    }


    private fun uploadAudio() {
        val storage = FirebaseStorage.getInstance()
        val storageRef = storage.reference.child("audio_records/$fileName.mp3")

        val fileUri = Uri.fromFile(File("$dirPath$fileName.mp3"))
        //val uploadTask = storageRef.putFile(fileUri)
        val metadata = storageMetadata {
            contentType = "audio/mpeg"
        }
        val uploadTask = storageRef.putFile(fileUri, metadata)
        uploadTask.addOnSuccessListener { taskSnapshot ->
            storageRef.downloadUrl.addOnSuccessListener { uri ->
                val audioUrl = uri.toString()

                // Lưu URL của tệp âm thanh vào Firebase Realtime Database hoặc Firestore
                val messageTxt: String = binding!!.messageBox.text.toString()
                val date = Date()
                val message = Message(messageTxt, senderUid, date.time)
                message.message = "audio"
                message.imageurl = audioUrl

                val randomKey = database!!.reference.push().key
                database!!.reference.child("chats").child(senderRoom!!)
                    .child("messages")
                    .child(randomKey!!)
                    .setValue(message).addOnSuccessListener {
                        database!!.reference.child("chats")
                            .child(receiverRoom!!)
                            .child("messages")
                            .child(randomKey)
                            .setValue(message)
                    }

                binding!!.waveformView.clear()
                stopRecorder()
            }
        }.addOnFailureListener {
            // Xử lý lỗi nếu có
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == REQUEST_CODE)
            permissionGranted = grantResults[0] == PackageManager.PERMISSION_GRANTED
    }


}