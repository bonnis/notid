package com.example.notid.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.DialogFragment
import com.example.notid.R
import com.example.notid.Util
import com.example.notid.activity.EditActivity
import com.example.notid.model.Note
import com.example.notid.model.UserPermission
import com.example.notid.response.MessageResponse
import kotlinx.android.synthetic.main.dialog_layout.*
import kotlinx.android.synthetic.main.find_layout.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.lang.IllegalStateException

class CreationDialogFragment : DialogFragment() {
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            val inflater = requireActivity().layoutInflater;
            val user_id = Util().getPref(it.applicationContext).getInt("user_id", 0)
            builder.setView(inflater.inflate(R.layout.dialog_layout, null))
                .setPositiveButton("Submit") { dialog, id ->
                    (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                    val notecode = dialog.findViewById<EditText>(R.id.notecodeText).text.toString()
                    val title = dialog.findViewById<EditText>(R.id.titleText).text.toString()
                    val privacy = dialog.findViewById<Switch>(R.id.privateSwitch).isChecked
                    val up = MutableList<UserPermission>(init = {UserPermission(id = user_id, readOnly = false)}, size = 1)
                    val note = Note(name = title, isPrivate = privacy, noteCode = notecode, permission = (up))
                    Util().retrofitRequest(it.applicationContext).createNote(note).enqueue(object : Callback<MessageResponse>{
                        override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                            Util().networkError(it.applicationContext)
                        }

                        override fun onResponse(
                            call: Call<MessageResponse>,
                            response: Response<MessageResponse>
                        ) {
                            if (response.isSuccessful){
                                val intent = Intent(it.applicationContext, EditActivity::class.java).apply {
                                    putExtra("com.example.notid.NOTECODE", notecode)
                                }
                                it.startActivity(intent)
                                dialog.dismiss()
                            }else{
                                Util().toastMaker(it.applicationContext, "Error")
                                (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                            }
                        }
                    })

                }
                .setNegativeButton("Cancel") { dialog, _->
                    dialog.cancel()
                }
            val ret = builder.create()
            ret.setOnShowListener(){dialog->
                (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled=false
                var notecodeText = (dialog as AlertDialog).findViewById<EditText>(R.id.notecodeText)
                var notecodeErrorMessage = dialog.findViewById<TextView>(R.id.notecodeErrorMessage)
                var notecodeProgressBar = dialog.findViewById<ProgressBar>(R.id.notecodeProgressBar)
                notecodeText.doOnTextChanged { text, start, count, after ->
                    notecodeProgressBar.visibility = View.VISIBLE
                    (dialog as AlertDialog).getButton(AlertDialog.BUTTON_POSITIVE).isEnabled=false
                    if (text.toString().isNullOrEmpty()) {
                        notecodeErrorMessage.text = "Notecode cannot be empty"
                        notecodeErrorMessage.visibility = View.VISIBLE
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                        notecodeProgressBar.visibility = View.INVISIBLE

                    }
                    else if (text.toString().contains(' ')) {
                        notecodeErrorMessage.text = "No space allowed!"
                        notecodeErrorMessage.visibility = View.VISIBLE
                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                        notecodeProgressBar.visibility = View.INVISIBLE
                    } else {
                        Util().retrofitRequest(it.applicationContext).checkNote(text.toString())
                            .enqueue(object : Callback<MessageResponse> {
                                override fun onFailure(call: Call<MessageResponse>, t: Throwable) {
                                    Toast.makeText(
                                        it.applicationContext,
                                        "Network error, check internet connection",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                                    notecodeProgressBar.visibility = View.INVISIBLE
                                }

                                override fun onResponse(
                                    call: Call<MessageResponse>,
                                    response: Response<MessageResponse>
                                ) {
                                    if (response.isSuccessful) {
                                        notecodeErrorMessage.text = "Notecode already used, pick another!"
                                        notecodeErrorMessage.visibility = View.VISIBLE
                                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = false
                                        notecodeProgressBar.visibility = View.INVISIBLE
                                    } else {
                                        notecodeErrorMessage.visibility = View.INVISIBLE
                                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).isEnabled = true
                                        notecodeProgressBar.visibility = View.INVISIBLE
                                    }
                                }
                            })
                    }
                }
            }

            return ret
        }?:throw IllegalStateException("Activity cannot be null")
    }
}