package com.descifrador.exceltopdf

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import com.descifrador.exceltopdf.model.DocumentSettings
import com.itextpdf.kernel.geom.PageSize
import kotlinx.android.synthetic.main.activity_settings.*

class Settings : AppCompatActivity() , AdapterView.OnItemSelectedListener{

    private var documentSettings : DocumentSettings = DocumentSettings()

    private var selectedPageSize : Int = 4

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        title = "Settings"

        selectedPageSize = intent.getIntExtra("page_position",4)

        pageSizeList()

        saveSettingButton.setOnClickListener {
            saveButtonAction()
        }

        headingSizeEditText.hint = document_settings.headingSize.toString()

        fontSizeEditText.hint = document_settings.documentFontSize.toString()

    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        documentSettings.pageSize = document_settings.pageSize
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        selectedPageSize = position
    }


    private fun pageSizeList(){
        val pagesizeAdapter = ArrayAdapter.createFromResource(this,R.array.pageSizes,android.R.layout.simple_spinner_item)
        pagesizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        pageSizesSpinner.adapter = pagesizeAdapter
        pageSizesSpinner.onItemSelectedListener = this
        pageSizesSpinner.setSelection(selectedPageSize)

    }
    private fun saveButtonAction(){
        if(headingEditView.text.toString() != ""){
            documentSettings.heading = headingEditView.text.toString()
        }
        if(headingSizeEditText.text.toString() != ""){
            documentSettings.headingSize = headingSizeEditText.text.toString().toFloat()
        }
        if(fontSizeEditText.text.toString() != ""){
            documentSettings.documentFontSize = fontSizeEditText.text.toString().toFloat()
        }

        documentSettings.pageSize = documentSettings.getPageSize(selectedPageSize)


        val sharedPreferences = applicationContext.getSharedPreferences("settings_document",
            Context.MODE_PRIVATE)

        val editor : SharedPreferences.Editor = sharedPreferences.edit()
        editor.putFloat("document_font_size",documentSettings.documentFontSize)
        editor.putFloat("heading_font_size",documentSettings.headingSize)
        editor.putInt("page_size",selectedPageSize)
        editor.putString("page_heading",documentSettings.heading)
        editor.apply()


        val intent = Intent(this@Settings,MainActivity::class.java)
        this.startActivity(intent)

    }
}