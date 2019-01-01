package com.example.parkseeun.moca_android.ui.community.feed

import android.content.Context
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieComposition
import com.airbnb.lottie.LottieDrawable
import com.bumptech.glide.Glide
import com.example.parkseeun.moca_android.R
import de.hdodenhof.circleimageview.CircleImageView

class ReviewRecyclerViewAdapter(val context : Context, val dataList : ArrayList<ReviewData>) : RecyclerView.Adapter<ReviewRecyclerViewAdapter.Holder>() {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val view : View = LayoutInflater.from(context).inflate(R.layout.rv_item_review, parent, false)
        return Holder(view)
    }

    override fun getItemCount(): Int = dataList.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        Glide.with(context).load(dataList[position].profileImg).into(holder.profile)
        holder.name.text = dataList[position].name
        holder.rating.rating = dataList[position].rating.toFloat()
        if(dataList[position].heartFlag==1)
            holder.heart.setBackgroundResource(R.drawable.common_heart_fill)
        else
            holder.heart.setBackgroundResource(R.drawable.common_heart_blank)
        holder.heart.setOnClickListener {
            // 임시 애니메이션
            holder.heartLt.imageAssetsFolder = "/assets"
            holder.heartLt.setAnimation("heart.json")
            holder.heartLt.playAnimation()
        }
        Glide.with(context).load(dataList[position].pic).into(holder.pic)
        holder.heartNum.text = dataList[position].heartNum.toString()
        holder.commentNum.text = dataList[position].commentNum.toString()
        holder.cafeName.text = dataList[position].cafeName
        holder.time.text = dataList[position].time
        holder.cafeLocation.text = dataList[position].cafeLocation
        holder.comment.text = dataList[position].comment
    }

    // View Holder
    inner class Holder(itemView : View) : RecyclerView.ViewHolder(itemView) {
        val profile : CircleImageView = itemView.findViewById(R.id.review_item_profile_circle) as CircleImageView
        val name : TextView = itemView.findViewById(R.id.review_item_name_tv) as TextView
        val rating : RatingBar = itemView.findViewById(R.id.review_item_rating_rating) as RatingBar
        val heart : ImageView = itemView.findViewById(R.id.review_item_heart_iv) as ImageView
        val heartLt : LottieAnimationView = itemView.findViewById(R.id.review_item_heart_lt) as LottieAnimationView
        val pic : ImageView = itemView.findViewById(R.id.review_item_pic_iv) as ImageView
        val heartNum : TextView = itemView.findViewById(R.id.review_item_heart2Num_iv) as TextView
        val commentNum : TextView = itemView.findViewById(R.id.review_item_comment2_tv) as TextView
        val cafeName : TextView = itemView.findViewById(R.id.review_item_cafename_tv) as TextView
        val time : TextView = itemView.findViewById(R.id.review_item_time_tv) as TextView
        val cafeLocation : TextView = itemView.findViewById(R.id.review_item_cafelocation_tv) as TextView
        val comment : TextView = itemView.findViewById(R.id.review_item_comment_tv) as TextView
    }
}