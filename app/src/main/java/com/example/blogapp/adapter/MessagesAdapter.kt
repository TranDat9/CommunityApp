package com.example.blogapp.adapter

import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.view.menu.MenuView.ItemView
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.blogapp.Model.Message
import com.example.blogapp.R
import com.example.blogapp.databinding.DeleteLayoutBinding
import com.example.blogapp.databinding.ReceiveMsgBinding
import com.example.blogapp.databinding.SendMsgBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import java.net.URL

class MessagesAdapter(
    var context: Context,
    messages: ArrayList<Message>?,
    senderRoom: String,
    receiverRoom: String

):RecyclerView.Adapter<RecyclerView.ViewHolder?>() {
    lateinit var messages: ArrayList<Message>
    val ITEM_SEND = 1
    val ITEM_RECEIVE = 2
    val senderRoom: String
    var receiverRoom: String
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private lateinit var mediaplayer:MediaPlayer
    private var delay = 1000L
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if(viewType == ITEM_SEND)
        {
            val view = LayoutInflater.from(context).inflate(R.layout.send_msg,parent,false)
            SendMsgHolder(view)
        }else{
            val view = LayoutInflater.from(context).inflate(R.layout.receive_msg,parent,false)
            ReceiveMsgHolder(view)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val messages = messages[position]
        return if(FirebaseAuth.getInstance().uid == messages.senderId){
            ITEM_SEND
        }else{
            ITEM_RECEIVE
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = messages[position]
        if(holder.javaClass == SendMsgHolder :: class.java)
        {
            val viewHolder = holder as SendMsgHolder
            if(message.message.equals("photo"))
            {
                viewHolder.binding.image.visibility = View.VISIBLE
                viewHolder.binding.message.visibility = View.GONE
                viewHolder.binding.mLinear.visibility = View.GONE
                Glide.with(context)
                    .load(message.imageurl)
                    .placeholder(R.drawable.man)
                    .into(viewHolder.binding.image)
            }else  if (message.message.equals("audio")) {
                viewHolder.binding.image.visibility = View.GONE
                viewHolder.binding.message.visibility = View.GONE
                viewHolder.binding.mLinear.visibility = View.GONE
                viewHolder.binding.mLinearAudio.visibility = View.VISIBLE

                // Lấy độ dài file audio và chuẩn bị MediaPlayer
                val url = message.imageurl
                GlobalScope.launch(Dispatchers.IO) {
                    val mediaPlayer = MediaPlayer()
                    try {
                        mediaPlayer.setDataSource(url)
                        mediaPlayer.prepare()
                        val duration = mediaPlayer.duration // Độ dài tính bằng milliseconds

                        withContext(Dispatchers.Main) {
                            // Chuyển đổi độ dài sang phút và giây
                            val minutes = (duration / 1000) / 60
                            val seconds = (duration / 1000) % 60
                            viewHolder.binding.txtAudio.text = String.format("%02d:%02d", minutes, seconds)

                            // Cập nhật MediaPlayer và giao diện người dùng
                            mediaplayer = mediaPlayer
                            viewHolder.binding.btnPlayAudio.setOnClickListener {
                                if (!mediaplayer.isPlaying) {
                                    mediaplayer.start()
                                    viewHolder.binding.btnPlayAudio.background = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_pause, context.theme)
                                    handler.postDelayed(runnable, delay)
                                } else {
                                    mediaplayer.pause()
                                    viewHolder.binding.btnPlayAudio.background = ResourcesCompat.getDrawable(context.resources, R.drawable.ic_play, context.theme)
                                    handler.removeCallbacks(runnable)
                                }
                            }
                        }
                    } catch (e: IOException) {
                        e.printStackTrace()
                        withContext(Dispatchers.Main) {
                            viewHolder.binding.txtAudio.text = "Error loading audio"
                        }
                    }
                }

                handler = Handler(Looper.getMainLooper())
                runnable = Runnable {
                    handler.postDelayed(runnable, delay)
                }
            }
            else {
                viewHolder.binding.message.text = message.message
            }
                viewHolder.itemView.setOnLongClickListener {
                    val view = LayoutInflater.from(context)
                        .inflate(R.layout.delete_layout,null)
                    val binding: DeleteLayoutBinding = DeleteLayoutBinding.bind(view)

                    val dialog = AlertDialog.Builder(context)
                        .setTitle("Delete Message")
                        .setView(binding.root)
                        .create()
                    binding.everyone.setOnClickListener {
                        message.message = "This message is removed"
                        message.messageId?.let { it1 ->
                            FirebaseDatabase.getInstance("https://blogapp-46ef8-default-rtdb.asia-southeast1.firebasedatabase.app")
                                .reference.child("chats")
                                .child(senderRoom)
                                .child("messages")
                                .child(it1).setValue(message)
                        }
                        message.messageId.let { it1 ->
                            FirebaseDatabase.getInstance("https://blogapp-46ef8-default-rtdb.asia-southeast1.firebasedatabase.app")
                                .reference.child("chats")
                                .child(receiverRoom)
                                .child("messages")
                                .child( it1!! ).setValue(message)
                        }

                        dialog.dismiss()
                    }
                    binding.delete.setOnClickListener {
                        message.messageId.let { it1 ->
                            FirebaseDatabase.getInstance("https://blogapp-46ef8-default-rtdb.asia-southeast1.firebasedatabase.app")
                                .reference.child("chats")
                                .child(senderRoom)
                                .child("message")
                                .child(it1!!).setValue(null)
                        }
                        dialog.dismiss()

                    }

                    binding.cancel.setOnClickListener {
                        dialog.dismiss()
                    }
                    dialog.show()
                    false
                }


        }
        else
        {
            val viewHolder = holder as ReceiveMsgHolder
            if(message.message.equals("photo"))
            {
                viewHolder.binding.image.visibility = View.VISIBLE
                viewHolder.binding.message.visibility = View.GONE
                viewHolder.binding.mLinear.visibility = View.GONE
                Glide.with(context)
                    .load(message.imageurl)
                    .placeholder(R.drawable.man)
                    .into(viewHolder.binding.image)
            }else if(message.message.equals("audio")){

                viewHolder.binding.image.visibility = View.GONE
                viewHolder.binding.message.visibility = View.GONE
                viewHolder.binding.mLinear.visibility = View.GONE
                viewHolder.binding.mLinearAudio.visibility = View.VISIBLE



            }else {
                viewHolder.binding.message.text = message.message
            }
            viewHolder.itemView.setOnLongClickListener {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.delete_layout,null)
                val binding: DeleteLayoutBinding = DeleteLayoutBinding.bind(view)

                val dialog = AlertDialog.Builder(context)
                    .setTitle("Delete Message")
                    .setView(binding.root)
                    .create()
                binding.everyone.setOnClickListener {
                    message.message = "This message is removed"
                    message.messageId?.let { it1 ->
                        FirebaseDatabase.getInstance("https://blogapp-46ef8-default-rtdb.asia-southeast1.firebasedatabase.app")
                            .reference.child("chats")
                            .child(receiverRoom)
                            .child("messages")
                            .child(it1).setValue(message)
                    }
                    message.messageId.let { it1 ->
                        FirebaseDatabase.getInstance("https://blogapp-46ef8-default-rtdb.asia-southeast1.firebasedatabase.app")
                            .reference.child("chats")
                            .child(senderRoom)
                            .child("messages")
                            .child( it1!! ).setValue(message)
                    }

                    dialog.dismiss()
                }
                binding.delete.setOnClickListener {
                    message.messageId.let { it1 ->
                        FirebaseDatabase.getInstance("https://blogapp-46ef8-default-rtdb.asia-southeast1.firebasedatabase.app")
                            .reference.child("chats")
                            .child(receiverRoom)
                            .child("message")
                            .child(it1!!).setValue(null)
                    }
                    dialog.dismiss()

                }

                binding.cancel.setOnClickListener {
                    dialog.dismiss()
                }
                dialog.show()
                false
            }

        }





    }

    override fun getItemCount(): Int {
        return messages.size
    }





    inner class SendMsgHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
       var binding: SendMsgBinding = SendMsgBinding.bind(itemView)
    }

    inner class ReceiveMsgHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
      var  binding: ReceiveMsgBinding = ReceiveMsgBinding.bind(itemView)
    }
    init {
        if(messages != null)
        {
            this.messages = messages
        }
        this.senderRoom = senderRoom
        this.receiverRoom = receiverRoom
    }


}