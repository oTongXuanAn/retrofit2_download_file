package com.example.framgiatongxuanan.retrofit2_download_file

import android.Manifest
import android.content.pm.PackageManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import android.os.AsyncTask
import android.support.v4.app.ActivityCompat
import android.os.Build
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        downloadFile()
        btnDownLoad.setOnClickListener {
            if (isStoragePermissionGranted()) {
                downloadFile()
            } else {
                downloadFile()
            }
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            downloadFile()
        }
    }

    fun downloadFile() {
        val retrofit = Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        val service = retrofit.create(ApiService::class.java!!)

        val call = service.downloadFileWithFixedUrl()

        call.enqueue(object : Callback<ResponseBody> {

            override fun onResponse(call: Call<ResponseBody>?, response: Response<ResponseBody>?) {
                try {
                    if (response?.isSuccessful == true) {
                        val execute = object : AsyncTask<Void, Void, Void>() {
                            override fun doInBackground(vararg voids: Void): Void? {
                                val writtenToDisk = writeResponseBodyToDisk(response.body(), "retrofit-2.0.0-beta2-javadoc")
                                Log.d("download", "file download was a success? $writtenToDisk")
                                return null
                            }
                        }.execute()
                    }

                    Log.d("onResponse", "Response came from server")

                } catch (e: Exception) {
                    Log.d("onResponse", "There is an error")
                    e.printStackTrace()
                }
            }

            override fun onFailure(call: Call<ResponseBody>?, t: Throwable?) {
                Log.d("onFailure", t.toString())
            }
        })

    }

    private fun writeResponseBodyToDisk(body: ResponseBody?, fileName: String): Boolean {
        try {
            // todo change the file location/name according to your needs

            var futureStudioIconFile = File(getExternalFilesDir(null).toString() + File.separator + fileName)
            Log.e("futureStudioIconFile", futureStudioIconFile.path)
            var inputStream: InputStream? = null
            var outputStream: OutputStream? = null

            try {
                val fileReader = ByteArray(4096)

                val fileSize = body?.contentLength()
                var fileSizeDownloaded: Long = 0

                inputStream = body?.byteStream()
                outputStream = FileOutputStream(futureStudioIconFile)

                while (true) {
                    val read = inputStream!!.read(fileReader)
                    //Nó được sử dụng để trả về một ký tự trong mẫu ASCII. Nó trả về -1 vào cuối tập tin.
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

    fun isStoragePermissionGranted(): Boolean {
        return if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                true
            } else {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 1)
                false
            }
        } else {
            true
        }
    }
}
