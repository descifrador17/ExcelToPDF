package com.descifrador.exceltopdf

import android.Manifest
import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.graphics.fonts.FontFamily
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.itextpdf.text.PageSize.A4
import com.itextpdf.text.Paragraph
import com.itextpdf.text.Phrase
import com.itextpdf.text.pdf.PdfWriter
import kotlinx.android.synthetic.main.activity_main.*
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class MainActivity : AppCompatActivity() {

    lateinit var buttonSDCard : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        buttonSDCard = findViewById(R.id.btnViewSDCard)

        checkFilePermission()

        buttonSDCard.setOnClickListener{
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        Toast.makeText(this,"Inside Function",Toast.LENGTH_SHORT).show()
        when(requestCode){
            100 ->run {
                if(resultCode == RESULT_OK){
                    val path : String = data!!.data!!.path!!
                    Toast.makeText(this,path,Toast.LENGTH_SHORT).show()
                    val filename = path.substringAfter(":")
                    textView.text = filename
                    readExcelData("/storage/emulated/0/${filename}")
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    @SuppressLint("SimpleDateFormat")
    private fun readExcelData(filePath : String){
        val inputFile = File(filePath)
        Log.e("Main",inputFile.toString())

        try {
            val inputStream: InputStream = FileInputStream(inputFile)
            val workbook = XSSFWorkbook(inputStream)
            val sheet: XSSFSheet = workbook.getSheetAt(0)

            var headings = ArrayList<String>()

            //reading headings
            val row = sheet.getRow(0)
            val cellIter = row.cellIterator()
            while(cellIter.hasNext()){
                val myCell = cellIter.next() as XSSFCell
                headings.add(myCell.toString())
            }

            Log.e("Main",headings.toString())

            //reading data

            var completedata = ""
            val rowIter = sheet.rowIterator()
            rowIter.next()
            var rowno = 1
            while (rowIter.hasNext()) {
                var data = ""
                val currentRow = rowIter.next() as XSSFRow
                val cellIterator = currentRow.cellIterator()
                var colno = 0
                while(cellIterator.hasNext()){
                    val currentCell = cellIterator.next() as XSSFCell
                    data += headings[colno]+ " : " + currentCell.toString() + "\n"
                    colno++
                }
                completedata+=data
                savePDF(data,rowno)
                rowno++
            }
            textView.text = completedata
        }
        catch (e : Exception){
            Log.e("Main",e.toString())
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun savePDF(data : String, rowno:Int){
        val mDoc = com.itextpdf.text.Document()
        val mFileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())+"$rowno"
        val mFilePath : String = "${Environment.getExternalStorageDirectory()}/$mFileName.pdf"

        try{
            PdfWriter.getInstance(mDoc, FileOutputStream(mFilePath))
            mDoc.open()
            mDoc.addAuthor("Utkarsh")
            mDoc.pageSize = A4
            mDoc.add(Paragraph(data))
            mDoc.close()
            Toast.makeText(this,"Done",Toast.LENGTH_SHORT).show()
        }
        catch (e : Exception){
            Toast.makeText(this,e.toString(),Toast.LENGTH_SHORT).show()
        }

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

