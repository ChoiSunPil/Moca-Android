package com.example.parkseeun.moca_android.model.get

data class GetFollowingResponse (
    val status: Int,
    val message: String?,
    val data: ArrayList<GetFollowerResponseData>
)