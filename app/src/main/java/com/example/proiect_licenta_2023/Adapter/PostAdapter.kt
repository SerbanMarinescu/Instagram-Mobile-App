package com.example.proiect_licenta_2023.Adapter

import android.content.Context
import android.content.Intent
import android.media.Image
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.proiect_licenta_2023.CommentsActivity
import com.example.proiect_licenta_2023.Fragments.PostDetailsFragment
import com.example.proiect_licenta_2023.Fragments.ProfileFragment
import com.example.proiect_licenta_2023.MainActivity
import com.example.proiect_licenta_2023.Model.Post
import com.example.proiect_licenta_2023.Model.User
import com.example.proiect_licenta_2023.R
import com.example.proiect_licenta_2023.ShowUsersActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import de.hdodenhof.circleimageview.CircleImageView

class PostAdapter(private val mContext: Context, 
                  private val mPost: List<Post>):RecyclerView.Adapter<PostAdapter.ViewHolder>() {

    private var firebaseUser:FirebaseUser?=null
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view=LayoutInflater.from(mContext).inflate(R.layout.posts_layout,parent,false)
        
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        firebaseUser=FirebaseAuth.getInstance().currentUser
        val post=mPost[position]
        
        Picasso.get().load(post.getPostImage()).into(holder.postImage)

        if(post.getDescription().equals("")){
            holder.description.visibility=View.GONE
        }
        else {
            holder.description.visibility=View.VISIBLE
            holder.description.text=post.getDescription()
        }
        
        publisherInfo(holder.profileImage, holder.userName, holder.publisher, post.getPublisher())
        isLikes(post.getPostId(),holder.likeButton)
        numberOfLikes(holder.likes, post.getPostId())
        numberOfComments(holder.comments, post.getPostId())
        checkSavedStatus(post.getPostId(),holder.saveButton)

        holder.postImage.setOnClickListener{
            val editor=mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
            editor.putString("postId", post.getPostId())
            editor.apply()
            (mContext as FragmentActivity).supportFragmentManager
                .beginTransaction().replace(R.id.fragment_container, PostDetailsFragment()).commit()
        }

        holder.publisher.setOnClickListener{
            val editor=mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
            editor.putString("profileId", post.getPublisher())
            editor.apply()
            (mContext as FragmentActivity).supportFragmentManager
                .beginTransaction().replace(R.id.fragment_container, ProfileFragment()).commit()
        }

        holder.profileImage.setOnClickListener{
            val editor=mContext.getSharedPreferences("PREFS",Context.MODE_PRIVATE).edit()
            editor.putString("profileId", post.getPublisher())
            editor.apply()
            (mContext as FragmentActivity).supportFragmentManager
                .beginTransaction().replace(R.id.fragment_container, ProfileFragment()).commit()
        }

        holder.likes.setOnClickListener {
            val intent=Intent(mContext, ShowUsersActivity::class.java)
            intent.putExtra("id",post.getPostId())
            intent.putExtra("title","likes")
            mContext.startActivity(intent)
        }

        holder.likeButton.setOnClickListener{
            if(holder.likeButton.tag == "Like"){
                FirebaseDatabase.getInstance().reference.child("Likes").child(post.getPostId())
                    .child(firebaseUser!!.uid).setValue(true)

                addNotification(post.getPublisher(),post.getPostId())
            }
            else {
                FirebaseDatabase.getInstance().reference.child("Likes").child(post.getPostId())
                    .child(firebaseUser!!.uid).removeValue()

                val intent= Intent(mContext,MainActivity::class.java)
                mContext.startActivity(intent)

            }
        }

        holder.commentButton.setOnClickListener{
            val intentComment= Intent(mContext,CommentsActivity::class.java)
            intentComment.putExtra("postId",post.getPostId())
            intentComment.putExtra("publisherId",post.getPublisher())
            mContext.startActivity(intentComment)
        }

        holder.comments.setOnClickListener{
            val intentComment= Intent(mContext,CommentsActivity::class.java)
            intentComment.putExtra("postId",post.getPostId())
            intentComment.putExtra("publisherId",post.getPublisher())
            mContext.startActivity(intentComment)
        }

        holder.saveButton.setOnClickListener{
            if(holder.saveButton.tag=="Save"){
                FirebaseDatabase.getInstance().reference.child("Saves")
                    .child(firebaseUser!!.uid).child(post.getPostId()).setValue(true)
            }
            else{
                FirebaseDatabase.getInstance().reference.child("Saves")
                    .child(firebaseUser!!.uid).child(post.getPostId()).removeValue()
            }
        }
    }


    override fun getItemCount(): Int {
        return mPost.size
    }
    
    
       inner class ViewHolder(@NonNull itemView: View):RecyclerView.ViewHolder(itemView){
           var profileImage:CircleImageView
           var postImage: ImageView
           var likeButton:ImageView
           var commentButton:ImageView
           var saveButton:ImageView
           var userName:TextView
           var likes:TextView
           var publisher:TextView
           var description:TextView
           var comments:TextView
           
           init {
               profileImage=itemView.findViewById(R.id.user_profile_image_post)
               postImage=itemView.findViewById(R.id.post_image_home)
               likeButton=itemView.findViewById(R.id.post_image_like_btn)
               commentButton=itemView.findViewById(R.id.post_image_comment_btn)
               saveButton=itemView.findViewById(R.id.post_save_comment_btn)
               userName=itemView.findViewById(R.id.user_name_post)
               likes=itemView.findViewById(R.id.likes)
               publisher=itemView.findViewById(R.id.publisher)
               description=itemView.findViewById(R.id.description)
               comments=itemView.findViewById(R.id.comments)
           }
       }

    private fun publisherInfo(profileImage: CircleImageView, userName: TextView, publisher: TextView, publisherID: String) {

        val userRef=FirebaseDatabase.getInstance().reference.child("Users").child(publisherID)

        userRef.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    val user=snapshot.getValue<User>(User::class.java)
                    Picasso.get().load(user!!.getImage()).placeholder(R.drawable.profile).into(profileImage)
                    userName.setText(user.getUsername())
                    publisher.setText(user.getFullname())

                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun isLikes(postId: String, likeButton: ImageView) {

        val firebaseUser=FirebaseAuth.getInstance().currentUser
        val likesRef=FirebaseDatabase.getInstance().reference.child("Likes").child(postId)

        likesRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.child(firebaseUser!!.uid).exists()){
                    likeButton.setImageResource(R.drawable.heart_clicked)
                    likeButton.tag = "Liked"
                }
                else {
                    likeButton.setImageResource(R.drawable.heart_not_clicked)
                    likeButton.tag = "Like"
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun numberOfLikes(likes: TextView, postId: String) {
        val likesRef=FirebaseDatabase.getInstance().reference.child("Likes").child(postId)

        likesRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    likes.text=snapshot.childrenCount.toString()+ " Likes"
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun numberOfComments(comments: TextView, postId: String) {
        val likesRef=FirebaseDatabase.getInstance().reference.child("Comments").child(postId)

        likesRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    comments.text="View all "+snapshot.childrenCount.toString()+ " comments"
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun checkSavedStatus(postid:String, imageView:ImageView){
       val saveRef= FirebaseDatabase.getInstance().reference.child("Saves").child(firebaseUser!!.uid)

        saveRef.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.child(postid).exists()){
                    imageView.setImageResource(R.drawable.save_large_icon)
                    imageView.tag="Saved"
                }
                else{
                    imageView.setImageResource(R.drawable.save_unfilled_large_icon)
                    imageView.tag="Save"
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }


    private fun addNotification(userId:String, postId:String){
        val notificationRef=FirebaseDatabase.getInstance().reference.child("Notifications").child(userId)

        val notificationMap=HashMap<String, Any>()
        notificationMap["userid"]=firebaseUser!!.uid
        notificationMap["text"]="liked your post"
        notificationMap["postid"]=postId
        notificationMap["ispost"]=true

        notificationRef.push().setValue(notificationMap)
    }
    
}