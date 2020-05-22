package com.example.notid.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.notid.R
import com.example.notid.Util
import com.example.notid.adapter.NoteRecylerViewAdapter
import com.example.notid.decorator.PaddingDecorator
import com.example.notid.model.Note
import kotlinx.android.synthetic.main.fragment_note.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * A simple [Fragment] subclass.
 */
class NoteFragment : Fragment() {

    var navController: NavController? = null
    var noteAdapter:NoteRecylerViewAdapter = NoteRecylerViewAdapter()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_note, container, false)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = Navigation.findNavController(view)
        initRecView()
        fetchNoteData()
    }

    override fun onResume() {
        super.onResume()
        fetchNoteData()
    }



    private fun initRecView(){
        recycler_note.apply {
            val paddingDecorator = PaddingDecorator(10)
            addItemDecoration(paddingDecorator)
            layoutManager=LinearLayoutManager(activity)
        }

    }

    private fun fetchNoteData(){
        Util().retrofitRequest(context!!).getNote().enqueue(object: Callback<List<Note>>{
            override fun onFailure(call: Call<List<Note>>, t: Throwable) {
                Toast.makeText(context!!, "Network Error", Toast.LENGTH_SHORT).show()
            }

            override fun onResponse(call: Call<List<Note>>, response: Response<List<Note>>) {
                if (response.isSuccessful){
                    //Toast.makeText(context!!, "Done loading",Toast.LENGTH_LONG).show()
                    recycler_note.adapter = NoteRecylerViewAdapter()
                    (recycler_note.adapter as NoteRecylerViewAdapter).submitList(response.body()!!)
                }else{
                    Toast.makeText(context!!,"Error : "+response.code(),Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}
