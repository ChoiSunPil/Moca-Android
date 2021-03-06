package com.example.parkseeun.moca_android.ui.communitySearch

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.RelativeLayout
import android.widget.TextView
import com.bumptech.glide.Glide
import com.example.parkseeun.moca_android.R
import com.example.parkseeun.moca_android.model.get.SearchUserData
import com.example.parkseeun.moca_android.ui.community.feed.other_user.OtherUserActivity
import de.hdodenhof.circleimageview.CircleImageView
import org.jetbrains.anko.find
import org.jetbrains.anko.startActivity

class ComSearUserAdapter(val context: Context, val dataList: ArrayList<SearchUserData>) :
    RecyclerView.Adapter<ComSearUserAdapter.Holder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        // 뷰 인플레이트
        val view: View = LayoutInflater.from(context).inflate(R.layout.rv_act_comm_search_user, parent, false)

        return Holder(view)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        // 뷰 바인딩
        holder.item.setOnClickListener {
            context!!.startActivity<OtherUserActivity>()
        }

//        Glide.with(context).load(dataList[position].).into(holder.photo) // 이게 맞을깡

        holder.name.text = dataList[position].user_name
        holder.state.text = dataList[position].user_status_comment

        if (!dataList[position].follow_is) {
            holder.follow.setBackgroundResource(R.drawable.community_followinglist_follow)
            holder.follow.setOnClickListener {
                    holder.follow.setBackgroundResource(R.drawable.community_following_following)

            }
        } else {
            holder.follow.setBackgroundResource(R.drawable.community_following_following)
            holder.follow.setOnClickListener {
                holder.follow.setBackgroundResource(R.drawable.community_followinglist_follow)
            }
        }


    }

    // View Holder
    inner class Holder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val item : RelativeLayout = itemView.findViewById(R.id.rl_rv_frag_comm_sear_user) as RelativeLayout
        val photo: CircleImageView = itemView.findViewById(R.id.iv_comm_search_profile) as CircleImageView
        val name: TextView = itemView.findViewById(R.id.tv_rv_act_comm_sear_user_name) as TextView
        val state: TextView = itemView.findViewById(R.id.tv_act_comm_sear_user_state) as TextView
        val follow: ImageButton = itemView.findViewById(R.id.ib_act_comm_sear_user_follow) as ImageButton
    }
}