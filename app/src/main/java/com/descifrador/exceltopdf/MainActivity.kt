package com.descifrador.exceltopdf

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.descifrador.exceltopdf.model.DocumentSettings
import kotlinx.android.synthetic.main.activity_main.*

var document_settings = DocumentSettings()

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences : SharedPreferences;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        checkFilePermission()

        getDocSharedPreferences()

        setDetails()

        btnViewSDCard.setOnClickListener{

            progressTextView.visibility = View.INVISIBLE
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

    //Override Functions
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.settings,menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == R.id.settings){
            val intent = Intent(this@MainActivity,Settings::class.java)
            intent.putExtra("page_position",sharedPreferences.getInt("page_size",4))
            this.startActivity(intent)
            return true;
        }
        else{
            return super.onOptionsItemSelected(item);
        }
    }

    //Helper Functions
    private fun getDocSharedPreferences(){
        sharedPreferences = applicationContext.getSharedPreferences("settings_document",
            Context.MODE_PRIVATE)

        document_settings.pageSize = document_settings.getPageSize(sharedPreferences.getInt("page_size",4))
        document_settings.documentFontSize = sharedPreferences.getFloat("document_font_size",14.0f)
        document_settings.headingSize = sharedPreferences.getFloat("heading_font_size",16.0f)
        document_settings.heading = sharedPreferences.getString("page_heading","").toString()
    }


    @SuppressLint("SetTextI18n")
    private fun setDetails(){
        val pageName : String = getPageName(sharedPreferences.getInt("page_size",4))
        pageSizeTextView.text = "Page Size : $pageName"
        headingTextView.text = "Heading : ${document_settings.heading}"
        headingSizeTextView.text = "Heading Size : ${document_settings.headingSize}"
        documentFontSizeTextView.text = "Document Font Size : ${document_settings.documentFontSize}"
    }

    private fun getPageName(value : Int) : String{
        return if(value<10) "A${value%11}" else "B${value%11}"
    }

    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Toast.makeText(this,"File Uploaded...Click on Convert File",Toast.LENGTH_SHORT).show()

        when(requestCode){
            100 ->run {
                if(resultCode == RESULT_OK){
                    val path : String = data!!.data!!.path!!
                    document_settings.fileName = path.substringAfter(":")
                    fileNameView.visibility = View.VISIBLE
                    fileNameView.text = "File Name : ${document_settings.fileName}"
                    convertView.setOnClickListener{
                        progressBar.visibility = View.VISIBLE
                        progressTextView.text = "Progress"

                        val thread = BackgroundThread(document_settings.fileName,progressTextView,progressBar)
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

