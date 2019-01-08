package com.example.parkseeun.moca_android.ui.loginJoin

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.example.parkseeun.moca_android.R
import com.example.parkseeun.moca_android.model.post.PostJoinData
import com.example.parkseeun.moca_android.model.post.PostJoinResponse
import com.example.parkseeun.moca_android.model.post.PostLoginData
import com.example.parkseeun.moca_android.model.post.PostLoginResponse
import com.example.parkseeun.moca_android.network.ApplicationController
import com.example.parkseeun.moca_android.ui.main.HomeActivity2
import com.example.parkseeun.moca_android.util.User
import kotlinx.android.synthetic.main.activity_join.*
import kotlinx.android.synthetic.main.activity_login.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.jetbrains.anko.imageURI
import org.jetbrains.anko.toast
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileNotFoundException
import java.io.InputStream
import java.util.regex.Pattern

class JoinActivity : AppCompatActivity() {

    //앨범에서 사진 가져오기
    private val REQ_CODE_SELECT_IMAGE = 100
    var data: Uri? = null
    var path: String = ""
    lateinit var requestManager: RequestManager
    private var image : MultipartBody.Part? = null

    // 통신
    val networkService  = ApplicationController.instance.networkService
    lateinit var postProjResponse : Call<PostJoinResponse>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_join)

        requestManager = Glide.with(this)

        // 프로필 사진 선택 버튼
        ib_join_selectProfile.setOnClickListener {
            changeImage()
        }

        // 회원가입 버튼 (통신)
        btn_join.setOnClickListener {
            if (checkValidation()) {
                if(data!=null)
                    path = data!!.path

                // RequestBody 생성
                val user_id = RequestBody.create(MediaType.parse("text/plain"), et_join_id.text.toString())
                val user_password = RequestBody.create(MediaType.parse("text/plain"), et_join_pw.text.toString())
                val user_name = RequestBody.create(MediaType.parse("text/plain"), et_join_name.text.toString())
                val user_phone = RequestBody.create(MediaType.parse("text/plain"), et_join_phone.text.toString())

                postProjResponse = networkService.postJoin(user_id, user_password, user_name, user_phone, image)
                postProjResponse.enqueue(object : Callback<PostJoinResponse> {
                    override fun onFailure(call: Call<PostJoinResponse>?, t: Throwable?) {
                        toast(t.toString())
                    }

                    override fun onResponse(call: Call<PostJoinResponse>?, response: Response<PostJoinResponse>?) {
                        if(response!!.isSuccessful)
                            if(response!!.body()!!.status==201) {
                                finish()
                            }else{
                                toast(response!!.body()!!.message)
                            }
                    }
                })
            }
        }

        // 취소 버튼
        btn_join_cancel.setOnClickListener {
            finish()
        }
    }

    // 유효성 검사
    private fun checkValidation(): Boolean {
        if (et_join_id.text.toString() != "" && et_join_name.text.toString() != "" && et_join_phone.text.toString() != "" && et_join_pw.text.toString() != "" && et_join_pwCheck.text.toString() != "") {
            if (et_join_id.text.length < 4 || et_join_id.text.length > 10) {
                toast("아이디는 4~10글자로 입력해주세요.")


                return false
            } else if(!Pattern.compile("^[a-zA-Z0-9]*S").matcher(et_join_id.text.toString()).matches()){
                toast("아이디는 영어와 숫자만으로 입력해주세요.")

                return false
            }
            if (et_join_name.text.length < 2 || et_join_name.text.length > 8) {
                toast("이름은 2~8글자로 입력해주세요.")

                return false
            } else if(!Pattern.compile("^([가-힇][a-z][A-Z])$").matcher(et_join_name.text.toString()).matches()){
                toast("이름은 한글과 영어만으로 입력해주세요.")

                return false
            }
            if (et_join_pwCheck.text.toString() == et_join_pw.text.toString()) {
                if (et_join_pw.text.length > 15 || et_join_pw.text.length < 6) {
                    toast("비밀번호는 6~15글자 이내로 입력해주세요.")

                    return false
                }
            } else if(!Pattern.compile("^([a-z][A-Z][0-9])$").matcher(et_join_pwCheck.text.toString()).matches()){
                toast("비밀번호는 영어와 숫자만으로 입력해주세요.")

                return false
            } else {
                toast("비밀번호가 다릅니다.")

                return false
            }
        }
        else {
            toast("모두 입력해주세요.")

            return false
        }

        return false
    }

    // 프로필 사진 선택
    fun changeImage() {
        var intent = Intent(Intent.ACTION_PICK)
        intent.type = android.provider.MediaStore.Images.Media.CONTENT_TYPE
        intent.data = android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI
        startActivityForResult(intent, REQ_CODE_SELECT_IMAGE)
    }

    //사진 관련해서 return 값 처리
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
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

                    ///RequestBody photoBody = RequestBody.create(MediaType.parse("image/jpg"), baos.toByteArray());
                    // MultipartBody.Part 실제 파일의 이름을 보내기 위해 사용!!

                    image = MultipartBody.Part.createFormData("user_img", photo.name, photoBody)
                    //이거 서버에 보내줄때 필요 image = MultipartBody.Part.createFormData("photo", photo.name, photoBody) //여기의 photo는 키값의 이름하고 같아야함

                    requestManager
                        .load(data.data)
                        //.centerCrop()
                        .into(civ_join_profile)


                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        }
    }
}
