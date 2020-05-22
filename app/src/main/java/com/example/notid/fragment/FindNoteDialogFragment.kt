package com.example.notid.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import com.example.notid.R
import com.example.notid.Util
import com.example.notid.activity.EditActivity
import com.example.notid.response.MessageResponse
import kotlinx.android.synthetic.main.find_layout.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.IllegalStateException

class FindNoteDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater

            builder.setView(inflater.inflate(R.layout.find_layout,null))
                .setPositiveButton("Open") {dialogI, id ->
                    val  btn = (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE)
                    btn.isEnabled=false
                    var findProgressBar = (dialog as AlertDialog).findViewById<ProgressBar>(R.id.findProgressBar)
                    var notecodeInput = (dialog as AlertDialog).findViewById<EditText>(R.id.notecodeInput)
                    findProgressBar.visibility=View.VISIBLE
                    Util().retrofitRequest(it.applicationContext).checkNote(notecode = notecodeInput.text.toString()).enqueue( object : Callback<MessageResponse>{
                        override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                            Util().networkError(it.applicationContext)
                        }

                        override fun onResponse(
                            call: Call<MessageResponse>,
                            response: Response<MessageResponse>
                        ) {
                            if (response.isSuccessful){
                                val intent = Intent(it.applicationContext, EditActivity::class.java).apply {
                                    putExtra("com.example.notid.NOTECODE", notecodeInput.text.toString())
                                }
                                it.startActivity(intent)
                                dialogI.dismiss()
                            } else {
                                Util().toastMaker(it.applicationContext, "Note not found")
                                btn.isEnabled=true
                                findProgressBar.visibility=View.INVISIBLE
                            }
                        }
                    })
                }
                .setNegativeButton("Cancel"){dialog, id ->
                    dialog.cancel()
                }
            val ret = builder.create()
            ret.setOnShowListener(object : DialogInterface.OnShowListener {
                override fun onShow(dialog: DialogInterface?) {
                    (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled=false
                    dialog.findViewById<EditText>(R.id.notecodeInput).doOnTextChanged { text, start, count, after ->
                        var findProgressBar = dialog.findViewById<ProgressBar>(R.id.findProgressBar)
                        findProgressBar.visibility=View.VISIBLE
                        if (text.isNullOrEmpty()){
                            Log.d("texts", "aku kepanggil")
                            Util().toastMaker(dialog.context, "Notecode must no be empty")
                            (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled=false
                            findProgressBar.visibility=View.INVISIBLE
                        } else{
                            findProgressBar.visibility=View.INVISIBLE
                            dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled=true
                        }
                    }
                }
            })
            return ret
        } ?: throw IllegalStateException("Activity can't be null")
    }




}