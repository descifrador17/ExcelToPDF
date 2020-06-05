package com.descifrador.exceltopdf

import android.annotation.SuppressLint
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import com.itextpdf.text.*
import com.itextpdf.text.pdf.PdfWriter
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


class BackgroundThread(val filename : String,
                       val progressTextView : TextView,
                       val progressBar : ProgressBar,
                       val showHeader : Boolean) : Thread() {

    val threadhandler = Handler(Looper.getMainLooper())

    override fun run() {
        threadhandler.post(Runnable {
            progressTextView.visibility = View.VISIBLE
        })
        readExcelData("/storage/emulated/0/${filename}")
    }

    @SuppressLint("SimpleDateFormat", "SetTextI18n")
    private fun readExcelData(filePath : String){
        val inputFile = File(filePath)

        try {
            val inputStream: InputStream = FileInputStream(inputFile)
            val workbook = XSSFWorkbook(inputStream)
            val sheet: XSSFSheet = workbook.getSheetAt(0)

            val totalRows = sheet.physicalNumberOfRows

            var headings = ArrayList<String>()

            val row = sheet.getRow(0)
            val cellIter = row.cellIterator()
            while(cellIter.hasNext()){
                val myCell = cellIter.next() as XSSFCell
                headings.add(myCell.toString())
            }

            var completedata = ""
            val rowIter = sheet.rowIterator()
            rowIter.next()
            var rowno = 1
            while (rowIter.hasNext()) {
                var data = ""
                val currentRow = rowIter.next() as XSSFRow
                val cellIterator = currentRow.cellIterator()
                var colno = 0
                while (cellIterator.hasNext()) {
                    val currentCell = cellIterator.next() as XSSFCell
                    data += headings[colno] + " : " + currentCell.toString() + "\n"
                    colno++
                }
                completedata += data

                threadhandler.post(Runnable {
                    progressTextView.text = "$rowno/${totalRows - 1}"
                })
                savePDF(data, rowno)
                rowno++
            }
            progressBar.visibility = View.INVISIBLE
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

            val fontsize_14 : Font = FontFactory.getFont(FontFactory.HELVETICA,14f)
            val fontsize_16 : Font = FontFactory.getFont(FontFactory.HELVETICA,16f)

            mDoc.addAuthor("Descifrador")
            mDoc.pageSize = PageSize.A4

            if(showHeader){
                val header : Paragraph = Paragraph("Details Row $rowno")
                header.alignment = Paragraph.ALIGN_RIGHT
                mDoc.add(header)
            }

            mDoc.add(Paragraph(data,fontsize_14))
            mDoc.close()
        }
        catch (e : Exception){
            Log.e("Background",e.toString())
        }

    }

}