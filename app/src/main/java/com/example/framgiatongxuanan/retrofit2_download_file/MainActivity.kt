package com.example.framgiatongxuanan.retrofit2_download_file

import android.content.Context
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.example.framgiatongxuanan.viblokolin.features.androidversion.Repository
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


    }

    fun downloadFile() {
        val retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        val service = retrofit.create(ApiService::class.java!!)

        val call = service.downloadFileWithFixedUrl()

        call.enqueue(object : Callback<ResponseBody> {
            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                Log.d("onFailure", t.toString())
            }

            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                try {

                    Log.d("onResponse", "Response came from server")

                    val FileDownloaded = writeResponseBodyToDisk(response?.body()?:null)

                    Log.d("onResponse", "Image is downloaded and saved ? $FileDownloaded")

                } catch (e: Exception) {
                    Log.d("onResponse", "There is an error")
                    e.printStackTrace()
                }
            }


            fun onFailure(t: Throwable) {
                Log.d("onFailure", t.toString())
            }
        })

    }

    private fun writeResponseBodyToDisk(body: ResponseBody, fileName: String): Boolean {
        try {
            // todo change the file location/name according to your needs

            var futureStudioIconFile = File(getExternalFilesDir(null).toString() + File.separator + fileName)
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null

            try {
                val fileReader = ByteArray(4096)

                val fileSize = body.contentLength()
                var fileSizeDownloaded: Long = 0

                inputStream = body.byteStream()
                outputStream = FileOutputStream(futureStudioIconFile)

                while (true) {
                    val read = inputStream!!.read(fileReader)

                    if (read == -1) {
                        break
                    }

                    outputStream!!.write(fileReader, 0, read)

                    fileSizeDownloaded += read.toLong()

                    Log.d("writeResponseBodyToDisk", "file download: $fileSizeDownloaded of $fileSize")
                }

                outputStream!!.flush()

                return true
            } catch (e: IOException) {
                return false
            } finally {
                if (inputStream != null) {
                    inputStream!!.close()
                }

                if (outputStream != null) {
                    outputStream!!.close()
                }
            }
        } catch (e: IOException) {
            return false
        }

    }
}
