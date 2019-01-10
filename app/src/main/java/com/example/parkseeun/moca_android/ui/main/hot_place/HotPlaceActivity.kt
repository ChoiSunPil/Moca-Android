package com.example.parkseeun.moca_android.ui.main.hot_place

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import com.example.parkseeun.moca_android.R
import com.example.parkseeun.moca_android.model.get.GetHotPlaceListData
import com.example.parkseeun.moca_android.model.get.GetHotPlaceListResponse
import com.example.parkseeun.moca_android.network.ApplicationController
import com.example.parkseeun.moca_android.util.User
import kotlinx.android.synthetic.main.activity_hot_place.*
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HotPlaceActivity : AppCompatActivity() {

    lateinit var hotPlaceAdapter: HotPlaceAdapter
    var hot_place_id = -1

    // 통신
    private val networkService  = ApplicationController.instance.networkService
    private lateinit var getHotPlaceListResponse : Call<GetHotPlaceListResponse> // 인기 카페 리스트
    private var getHotPlaceListData = ArrayList<GetHotPlaceListData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hot_place)

        setUpRecyclerView()

        ib_hot_place_back.setOnClickListener {
            finish()
        }

        var hot_place_id = this.intent.getIntExtra("hot_place_id", hot_place_id)

        getHotPlaceList(hot_place_id)
    }

    // 핫플레이스 통신
    private fun getHotPlaceList(hot_place_id : Int) {
        getHotPlaceListResponse = networkService.getHotPlaceListResponse(User.token, hot_place_id)
        getHotPlaceListResponse.enqueue(object : Callback<GetHotPlaceListResponse> {
            override fun onFailure(call: Call<GetHotPlaceListResponse>, t: Throwable) {
                toast(t.message.toString())
            }

            override fun onResponse(call: Call<GetHotPlaceListResponse>, response: Response<GetHotPlaceListResponse>) {
                if (response.isSuccessful) {
                    if (response.body()!!.status == 200) {
                        getHotPlaceListData = response.body()!!.data

                        setUpRecyclerView()
                    }
                }
            }

        })
    }

    private fun setUpRecyclerView() {
        hotPlaceAdapter = HotPlaceAdapter(this, getHotPlaceListData)
        rv_hot_place.adapter = hotPlaceAdapter
        rv_hot_place.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
    }

}
