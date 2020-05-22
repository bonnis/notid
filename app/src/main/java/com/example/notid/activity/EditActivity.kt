package com.example.notid.activity

import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.widget.addTextChangedListener
import androidx.core.widget.doBeforeTextChanged
import androidx.core.widget.doOnTextChanged
import com.example.notid.R
import com.example.notid.Util
import com.example.notid.model.Note
import com.example.notid.request.DeltaRequest
import com.example.notid.response.MessageResponse
import com.quill.android.delta.Delta
import com.quill.android.delta.json.DeltaDto
import com.quill.android.delta.json.DeltaJson
import kotlinx.android.synthetic.main.activity_edit.*
import kotlinx.android.synthetic.main.loading_overlay.*
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.math.BigInteger
import java.util.*
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock
import kotlin.math.log

class EditActivity : AppCompatActivity() {

    lateinit var notecode : String
    lateinit var noteid : Number
    lateinit var sharedPreferences: SharedPreferences
    var userid: Int = 0
    private lateinit var l : ReentrantLock
    lateinit var token : String
    lateinit var client : OkHttpClient
    var version : BigInteger = BigInteger("1")
    @Volatile var ack : Boolean = true
    @Volatile lateinit var localBranch: Delta
    @Volatile var mergeRequest : Delta = Delta()
    @Volatile var queuedRequest : Delta = Delta()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        l = ReentrantLock()
        sharedPreferences = Util().getPref(applicationContext)
        token = sharedPreferences.getString("login_token","")!!
        userid = sharedPreferences.getInt("user_id",0)
        setContentView(R.layout.activity_edit)
        progress_overlay.visibility=View.VISIBLE
        editToolbar.setNavigationOnClickListener { finish() }
        client = OkHttpClient()
        notecode = intent.getStringExtra("com.example.notid.NOTECODE")
        Util().retrofitRequest(applicationContext).getNote(notecode=notecode).enqueue(object : Callback<Note>{
            override fun onFailure(call: Call<Note>, t: Throwable) {
                Util().networkError(applicationContext)
                progress_overlay.visibility=View.INVISIBLE
                finish()
            }

            override fun onResponse(call: Call<Note>, response: Response<Note>) {
                if(response.isSuccessful) {
                    val serverText=response.body()?.text ?: ""
                    localBranch = Delta().insert(serverText)
                    noteid = response.body()!!.id!!
                    noteText.setText(serverText)
                    progress_overlay.visibility=View.INVISIBLE
                    noteTitle.text=response.body()!!.name
                    version = response.body()!!.version!!
                    initWebsockets()
                } else {
                    Util().toastMaker(applicationContext,response.message())
                    progress_overlay.visibility=View.INVISIBLE
                    finish()
                }
            }
        })

    }

    private fun sendMergeRequest(){
        Util().retrofitRequest(applicationContext).mergeRequest(noteid, DeltaRequest(DeltaJson.toJson(mergeRequest)!!)).enqueue( object : Callback<MessageResponse>{
            override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                Util().networkError(applicationContext)
            }

            override fun onResponse(
                call: Call<MessageResponse>,
                response: Response<MessageResponse>
            ) {
                Log.d("balasanResponse", "berhasil")
            }
        })

    }

    fun tost(context: Context,text: String){
        runOnUiThread{
            Util().toastMaker(context,text)
        }
    }


    private fun initWebsockets(){
        var req = Request.Builder().url("ws://192.168.137.1:8001/ws/delta/$noteid").header("Authorization",
            "Bearer $token"
        ).build()
        var OTlistener = OTWebsocketListener()
        var WS = client.newWebSocket(req, OTlistener)

        noteText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {

            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

            }

            private var timer : Timer = Timer()
            private val delay : Number = 1000
            override fun onTextChanged(text: CharSequence?, start: Int, count: Int, after: Int) {
                timer.cancel()
                timer = Timer()
                timer.schedule(object : TimerTask() {
                    override fun run() {
                        l.lock()
                        try {
                            if (localBranch.diff(Delta().insert(text.toString())).ops.isNotEmpty()) {
                                if (ack) {
                                    Log.d("stateAC", "ACK In")
                                    ack = false
                                    mergeRequest = localBranch.diff(Delta().insert(text.toString()))
                                    Log.d("balasanM", mergeRequest.ops[0].insert.toString())
                                    sendMergeRequest()
                                }
                            }
                        } finally {
                            l.unlock()
                        }
                    }
                }, delay.toLong())
            }
        })

        client.dispatcher().executorService().shutdown()
    }

   inner class OTWebsocketListener(): WebSocketListener(){

        override fun onOpen(webSocket: WebSocket, response: okhttp3.Response) {
            super.onOpen(webSocket, response)
            Log.d("balasan","mantap")
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: okhttp3.Response?) {
            super.onFailure(webSocket, t, response)
            throw  t
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosing(webSocket, code, reason)
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            super.onMessage(webSocket, text)
            //Log.d("balasanString", text)
            var msg = JSONObject(text)
            if (msg.has("delta")){
                //Log.d("balasanDelta",msg.get("delta").toString())
                var upV = msg.getString("version").toBigInteger()
                if(msg.getJSONObject("delta").get("ops")!=null && ( upV > version))
                {
                    l.lock()
                    try {
                        version = upV
                        var cursor = noteText.selectionStart
                        Log.d("stateAC", "getDelta")

                        val currentDelta = Delta().insert(noteText.text.toString())

                        Log.d("stateAC", localBranch.ops.toString())
                        Log.d("stateAC", currentDelta.ops.toString())
                        Log.d(
                            "stateACs",
                            localBranch.compose(mergeRequest).ops.toString()
                        )
                        var t = localBranch.compose(mergeRequest)

                        queuedRequest = t.diff(currentDelta)
                        Log.d(
                            "stateACs",
                            queuedRequest.ops.toString()
                        )
                        val upstream =
                            DeltaJson.fromJson(msg.getJSONObject("delta").toString()) ?: Delta()
                        var transQ = upstream.transform(queuedRequest)
                        val cChange = mergeRequest.transformPosition(cursor)
                        localBranch = localBranch.compose(upstream)
                        var localtext = localBranch.compose(transQ)
                        queuedRequest = transQ
                        Log.d(
                            "stateACs",
                            queuedRequest.ops.toString()
                        )
                        Log.d("balasan+", upstream.transformPosition(cursor).toString())
                        Log.d("balasan-", cChange.toString())
                        //                        cursor += (upstream.transformPosition(cursor) - cChange)

                        runOnUiThread {
                            //Log.d("balasanUbahtext", localBranch.ops[0]?.insert.toString()
                            noteText.setText(
                                localtext.ops[0]?.insert?.toString() ?: noteText.text
                            )
                            //noteText.setSelection(cursor)
                        }
                        if (msg.getJSONArray("ack").toString()
                                .contains("\"" + userid.toString() + "\"")
                        ) {
                            Log.d("stateAC", text)
                            if (queuedRequest.ops.isEmpty()) {
                                Log.d("stateAC", "ACK Out")
                                Log.d("stateAC", "-------")
                                ack = true
                            } else {
                                Log.d("stateAC", "ACK In (cont)")
                                mergeRequest = queuedRequest
                                queuedRequest = Delta()
                                Log.d("stateACq", mergeRequest.ops.toString())
                                Log.d("stateAC", "(cont)")
                                sendMergeRequest()
                            }
                        }
                    } finally {
                        l.unlock()
                    }

                }
                else{
                    Log.d("balasanDelta", "aneh")
                }
            } else {
                Log.d("BalasanDelta", text)
            }

        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            super.onMessage(webSocket, bytes)
            Log.d("balasanByte", bytes.toString())

        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            super.onClosed(webSocket, code, reason)
        }
    }


}
