package com.descifrador.exceltopdf

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkFilePermission()

        btnViewSDCard.setOnClickListener{
            progressTextView.visibility = View.GONE
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            val uri = Uri.parse(Environment.getExternalStorageDirectory().path + resources.getString(R.string.app_name))
            intent.setDataAndType(uri, "*/*")
            try {
                startActivityForResult(Intent.createChooser(intent, "Open folder"), 100)
            } catch (ex: ActivityNotFoundException) {
                Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Toast.makeText(this,"File Uploaded...Click on Convert File",Toast.LENGTH_SHORT).show()

        when(requestCode){
            100 ->run {
                if(resultCode == RESULT_OK){
                    val path : String = data!!.data!!.path!!
                    val filename = path.substringAfter(":")
                    fileNameView.visibility = View.VISIBLE
                    fileNameView.text = "File Name : $filename"
                    convertView.setOnClickListener{
                        progressBar.visibility = View.VISIBLE
                        progressTextView.text = "Progress"
                        val thread = BackgroundThread(filename,progressTextView,progressBar)
                        thread.start()
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }


    private fun checkFilePermission(){
        var permissioncheck = this.checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
        permissioncheck += this.checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
        if(permissioncheck!=0){
            this.requestPermissions(arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE),1000)
        }
        else{
            Log.e("Main","No check required")
        }
    }
}

