package com.example.proiect_licenta_2023.Fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.proiect_licenta_2023.Adapter.PostAdapter
import com.example.proiect_licenta_2023.Model.Post
import com.example.proiect_licenta_2023.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [HomeFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class HomeFragment : Fragment() {

    private var recyclerView: RecyclerView?=null
    private var postAdapter:PostAdapter?=null
    private var postList:MutableList<Post>?=null
    private var followingList:MutableList<Post>?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        recyclerView=view.findViewById(R.id.recycle_view_home)

        val linearLayoutManager=LinearLayoutManager(context)
        linearLayoutManager.reverseLayout=true
        linearLayoutManager.stackFromEnd=true

        recyclerView?.layoutManager=linearLayoutManager

        postList=ArrayList()
        postAdapter=context?.let{ PostAdapter(it,postList as ArrayList<Post>)}

        recyclerView?.adapter=postAdapter

        checkFollowing()

        return view
    }

    private fun checkFollowing() {
        followingList=ArrayList()

        val followingRef= FirebaseDatabase.getInstance().reference
            .child("Follow")
                .child(FirebaseAuth.getInstance().currentUser!!.uid)
                .child("Following")

        followingRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    (followingList as ArrayList<String>).clear()

                    for(p in snapshot.children){
                        p.key?.let { (followingList as ArrayList<String>).add(it) }
                    }

                    retrieveAllPosts()
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun retrieveAllPosts() {
        val postsRef= FirebaseDatabase.getInstance().reference.child("Posts")

        postsRef.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                postList?.clear()

                for(p in snapshot.children){

                    val post=p.getValue(Post::class.java)

                    for (userId in (followingList as ArrayList<String>)){
                        if(post!!.getPublisher() == userId){
                            postList!!.add(post)
                        }

                        postAdapter!!.notifyDataSetChanged()
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })

    }


}