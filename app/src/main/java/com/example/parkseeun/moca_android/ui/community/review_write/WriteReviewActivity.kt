package com.example.parkseeun.moca_android.ui.community.review_write

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.View
import com.example.parkseeun.moca_android.R
import kotlinx.android.synthetic.main.activity_community_writereview.*
import org.jetbrains.anko.startActivity
import android.widget.RatingBar
import android.widget.TextView
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.example.parkseeun.moca_android.model.post.PostReviewWriteResponse
import com.example.parkseeun.moca_android.network.ApplicationController
import com.example.parkseeun.moca_android.network.NetworkService
import com.example.parkseeun.moca_android.ui.community.feed.FeedActivity
import com.example.parkseeun.moca_android.ui.community.review_write.adapter.PhotoAdapter
import com.example.parkseeun.moca_android.ui.community.review_write.data.PhotoData
import com.example.parkseeun.moca_android.ui.community.review_write.data.ReviewImageData
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.backgroundResource
import org.jetbrains.anko.sdk27.coroutines.textChangedListener
import org.jetbrains.anko.textColorResource
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream

class WriteReviewActivity : AppCompatActivity() {
    //    var PICK_IMAGE_MULTIPLE = 1
//    var imageEncoded: String? = null
//    var imagesEncodedList = ArrayList<String>()
    private val My_READ_STORAGE_REQUEST_CODE = 1004
    private val REQUEST_CODE_SELECT_IMAGE = 2004
    private val REQ_CODE_SELECT_IMAGE = 100
    lateinit var data: Uri
    var REQUEST_CODE: Int = 1007
    private var mImage: MultipartBody.Part? = null
    var btn_num = 0
    private var cafe_id: Int? = null
    private var currentProgress: Int = 0
    //통신
    private val networkService by lazy {
        ApplicationController.instance.networkService
    }
    lateinit var postReviewWriteResponse: Call<PostReviewWriteResponse>
    var images = ArrayList<MultipartBody.Part>()
    lateinit var photoItems: ArrayList<PhotoData>
    lateinit var totalItems: ArrayList<PhotoData>
    lateinit var PhotoAdapter: PhotoAdapter
    lateinit var requestManager: RequestManager
    lateinit var reviewImageItems: ArrayList<ReviewImageData>  //서버로 보낼 이미지 스트링 배열
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_community_writereview)
        reviewImageItems = ArrayList()

        requestManager = Glide.with(this)
        photoItems = ArrayList()

        var displayMetrics = applicationContext.resources.displayMetrics //화면 해상도를 구함
        //var width = displayMetrics.widthPixels
        //var height = displayMetrics.heightPixels
        Log.v("onCreate", "아아")
        setUnderlineColor()
        SetOnClickListener()
        ratingBar()

        var wherefrom : Boolean = false
        var cafe_id_default = intent.getIntExtra("cafe_id_default",0)
        var cafename = intent.getStringExtra("cafename")
        var cafeaddress = intent.getStringExtra("cafeaddress")
        Log.v("디테일에서 넘어온거",cafe_id_default.toString()+" "+cafename+" "+cafeaddress)
        if(cafename!=null){
            cafe_id=cafe_id_default
            Log.v("디테일에서 넘어온 거 넣음 ",cafe_id.toString())
            et_addreview_cafename.text=cafename
            et_addreview_cafeaddress.text=cafeaddress
            et_addreview_cafename.isEnabled = false
            et_addreview_cafename.isClickable = false
            wherefrom = true
        }
    }

    private fun setUnderlineColor() {
        //카페이름
        et_addreview_cafename.textChangedListener {
            onTextChanged { s, start, before, count ->
                if (et_addreview_cafename.text.toString().isNotEmpty())
                    v_write_review_cafename.backgroundResource = R.color.point_pink
                else v_write_review_cafename.backgroundResource = R.color.dark_gray
            }
        }
        //카페주소
        et_addreview_cafeaddress.textChangedListener {
            onTextChanged { s, start, before, count ->
                if (et_addreview_cafeaddress.text.toString().isNotEmpty())
                    v_write_review_cafeaddress.backgroundResource = R.color.point_pink
                else v_write_review_cafeaddress.backgroundResource = R.color.dark_gray
            }
        }
        //레이팅
        if (ratingBarCustom.rating >= 0) v_write_review_rating.backgroundResource = R.color.point_pink

        //한줄 설명
        et_addreview_oneline.textChangedListener {
            onTextChanged { s, start, before, count ->
                if (et_addreview_oneline.text.toString().isNotEmpty())
                    v_write_review_oneline.backgroundResource = R.color.point_pink
                else v_write_review_oneline.backgroundResource = R.color.dark_gray
            }
        }
        //상세 설명
        et_addreview_multiline.textChangedListener {
            onTextChanged { s, start, before, count ->
                if (et_addreview_multiline.text.toString().isNotEmpty())
                    v_write_review_multiline.backgroundResource = R.color.point_pink
                else v_write_review_multiline.backgroundResource = R.color.dark_gray
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        var cafe_id_default = data!!.getIntExtra("cafe_id_default",0)
        Log.v("cafe_id_default =",cafe_id_default.toString())
        if (resultCode == RESULT_OK) {
            when (requestCode) {
                1007 -> {
                    et_addreview_cafename.text = data!!.getStringExtra("cafe_name")
                    cafe_id = data!!.getIntExtra("cafe_id", 0)
                    et_addreview_cafeaddress.text = data!!.getStringExtra("cafe_address")
                }
            }
        }
        if (requestCode == REQ_CODE_SELECT_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                try {
                    //if(ApplicationController.getInstance().is)
                    this.data = data!!.data //그이미지의Uri를 가져옴
                    Log.v("이미지1", this.data.toString())

                    val options = BitmapFactory.Options()

                    var input: InputStream? = null // here, you need to get your context.
                    try {
                        input = contentResolver.openInputStream(this.data)
                        Log.v("이미지", this.data.toString())
                    } catch (e: FileNotFoundException) {
                        e.printStackTrace()
                    }

                    val bitmap = BitmapFactory.decodeStream(input, null, options) // InputStream 으로부터 Bitmap 을 만들어 준다.
                    val baos = ByteArrayOutputStream()
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 20, baos)
                    val photoBody = RequestBody.create(MediaType.parse("image/jpg"), baos.toByteArray())
                    val photo = File(this.data.toString()) // 가져온 파일의 이름을 알아내려고 사용합니다
                    //RequestBody photoBody = RequestBody.create(MediaType.parse("image/jpg"), baos.toByteArray());
                    // MultipartBody.Part 실제 파일의 이름을 보내기 위해 사용!!
                    images.add(
                        MultipartBody.Part.createFormData(
                            "image",
                            photo.name,
                            photoBody
                        )
                    )//여기의 image는 키값의 이름하고 같아야함
                    Log.v("images size", images.size.toString())
                    //body = MultipartBody.Part.createFormData("image", photo.getName(), profile_pic);

                    photoItems.add(PhotoData(data.data))
                    reviewImageItems.add(ReviewImageData(data.data.toString()))
                    PhotoAdapter = PhotoAdapter(photoItems, requestManager,images, this.findViewById(R.id.rl_all_addreview))
                    rv_photo_review.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                    rv_photo_review.adapter = PhotoAdapter

                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
    }

    fun changeImage() {
        var intent = Intent(Intent.ACTION_PICK)
        intent.type = android.provider.MediaStore.Images.Media.CONTENT_TYPE
        intent.data = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        startActivityForResult(intent, REQ_CODE_SELECT_IMAGE)
    }

    fun ratingBar() {
        val ratingBarCustom = findViewById<View>(R.id.ratingBarCustom) as RatingBar
        ratingBarCustom.onRatingBarChangeListener = object : RatingBar.OnRatingBarChangeListener {
            var ratingBarCustomInfo = findViewById<View>(R.id.ratingBarCustomInfo) as TextView
            override fun onRatingChanged(ratingBar: RatingBar, ratingValue: Float, fromUser: Boolean) {
                currentProgress = ratingBarCustom.progress
                ratingBarCustomInfo.text = "Current start number is $ratingValue, Progress number is $currentProgress"
            }
        }

    }

    fun SetOnClickListener() {
        et_addreview_cafename.setOnClickListener {
            val intent = Intent(this, ReviewSearchLocationActivity::class.java)
            startActivityForResult(intent, REQUEST_CODE)
        }
//
//        if(photoItems.size >0) {
//            btn_cancel_photo.setOnClickListener {
//                val idx: Int = rv_photo_review.getChildAdapterPosition(it)
//                photoItems.removeAt(idx)
//                Log.v("포토아이템 사이즈", photoItems.size.toString())
//                PhotoAdapter.notifyDataSetChanged()
//            }
//        }

        img_addreview_image.setOnClickListener {
            requestReadExternalStoragePermission()
        }

//        iv_cancel_addreview.setOnClickListener { finish() }
        img_addreview_complete.setOnClickListener {
            val input_cafeid = cafe_id
            val input_title = et_addreview_oneline.text.toString()//한줄 설명
            val input_content = et_addreview_multiline.text.toString() //상세 설명
            val input_rating = currentProgress// 별점
            var flag: Boolean = false
            Log.v(
                "postReviewWriteResponse",
                cafe_id.toString() + "  " + input_rating.toString() + " " + input_title + " " + input_content + " " + images.toString()
            )

            if (input_title.isNotEmpty() && input_content.isNotEmpty() && input_cafeid != null && photoItems.size > 0) { //Multipart 형식은 String을 RequestBody 타입으로 바꿔줘야 합니다
                flag = true
                changeButtonColor(flag)
                Log.v("postReviewWriteResponse", "다 널 아니다")
                postReviewWriteResponse(input_cafeid, input_content, input_rating, input_title)

            } else {
                flag = false
                changeButtonColor(flag)
                toast("모든 항목을 채워주세요")
            }
        }
    }

    private fun requestReadExternalStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {

            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    My_READ_STORAGE_REQUEST_CODE
                )
            }
        } else {

            changeImage()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == My_READ_STORAGE_REQUEST_CODE) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                changeImage()
            } else {

            }
        }
    }

    private fun changeButtonColor(flag: Boolean) {
        if (flag == true) {
            img_addreview_complete.setBackgroundResource(R.color.point_pink)
            txt_complete_addreview.textColorResource = R.color.white
        } else {
            img_addreview_complete.setBackgroundResource(R.color.light_gray)
            txt_complete_addreview.textColorResource = R.color.dark_gray
        }
    }


    private fun postReviewWriteResponse(input_cafeid: Int, input_content: String, input_rating: Int, input_title: String) {
        var content = RequestBody.create(MediaType.parse("text/plain"), input_content)
        var title = RequestBody.create(MediaType.parse("text/plain"), input_title)
        postReviewWriteResponse =
                networkService.postReviewWriteResponse(
                    "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiZmlyc3QiLCJpc3MiOiJEb0lUU09QVCJ9.0wvtXq58-W8xkndwb_3GYiJJEbq8zNEXzm6fnHA6xRM",
                    input_cafeid, title, content, input_rating, images
                )
        postReviewWriteResponse.enqueue(object : Callback<PostReviewWriteResponse> {

            override fun onFailure(call: Call<PostReviewWriteResponse>?, t: Throwable?) {
                Log.v("fail..",t.toString())
            }

            override fun onResponse(
                call: Call<PostReviewWriteResponse>?,
                response: Response<PostReviewWriteResponse>?
            ) {
                if (response!!.isSuccessful)
                    if (response.body()!!.status == 201) {
                        toast("수고해써 소희얌")
                        finish()
                        Log.v("success..",images.toString())
                    } else {
                        Log.v("WriteReviewResponse",response.body()!!.message)
                    }
            }
        })
        Log.v("postReviewWriteResponse","여기는 리스폰스 밖..")
    }
}
// val postReviewWriteResponse = networkService.postReviewWriteResponse(
//     "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJ1c2VyX2lkIjoiZmlyc3QiLCJpc3MiOiJEb0lUU09QVCJ9.0wvtXq58-W8xkndwb_3GYiJJEbq8zNEXzm6fnHA6xRM",
//     PostReviewWriteData(1,)
// )

//   override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
//        try {
//            // When an Image is picked
//            if (requestCode == PICK_IMAGE_MULTIPLE && resultCode == Activity.RESULT_OK && null != data) {
//                // Get the Image from data
//
//                val filePathColumn = arrayOf(MediaStore.Images.Media.DATA)
////                imagesEncodedList = ArrayList<String>()
//                if (data.data != null) {
//
//                    val mImageUri = data.data
//
//                    // Get the cursor
//                    val cursor = contentResolver.query(
//                        mImageUri!!,
//                        filePathColumn, null, null, null
//                    )
//                    // Move to first row
//                    cursor!!.moveToFirst()
//
//                    val columnIndex = cursor.getColumnIndex(filePathColumn[0])
//                    imageEncoded = cursor.getString(columnIndex)
//                    cursor.close()
//
//                }
//                else {
////                    imagesEncodedList = ArrayList<String>()
//                    if (data.clipData != null) {

//                        val mClipData = data.clipData
//                        val mArrayUri = ArrayList<Uri>()
//                        for (i in 0 until mClipData!!.itemCount) {
//
//                            val item = mClipData.getItemAt(i)
//                            val uri = item.uri
//                            mArrayUri.add(uri)
//                            // Get the cursor
//                            val cursor = contentResolver.query(uri, filePathColumn, null, null, null)
//                            // Move to first row
//                            cursor!!.moveToFirst()
//
//                            val columnIndex = cursor.getColumnIndex(filePathColumn[0])
//                            imageEncoded = cursor.getString(columnIndex)
//                            imagesEncodedList.add(imageEncoded!!)
//                            cursor.close()
//
//                        }
//                        Log.v("LOG_TAG", "Selected Images" + mArrayUri.size)
//                    }
//                }
//            } else {
//                Toast.makeText(
//                    this, "You haven't picked Image",
//                    Toast.LENGTH_LONG
//                ).show()
//            }
//        } catch (e: Exception) {
//            Toast.makeText(this, "Something went wrong", Toast.LENGTH_LONG)
//                .show()
//        }
//
//        super.onActivityResult(requestCode, resultCode, data)
//    }
//  }

