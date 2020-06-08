package com.descifrador.exceltopdf

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import com.itextpdf.kernel.geom.PageSize
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Cell
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Table
import com.itextpdf.layout.property.TextAlignment
import org.apache.poi.xssf.usermodel.XSSFCell
import org.apache.poi.xssf.usermodel.XSSFRow
import org.apache.poi.xssf.usermodel.XSSFSheet
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.File
import java.io.FileInputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class BackgroundThread(
    private val filename : String,
    private val progressTextView : TextView,
    private val progressBar : ProgressBar) : Thread() {

    private val threadHandler = Handler(Looper.getMainLooper())

    override fun run() {

        threadHandler.post{
            progressTextView.visibility = View.VISIBLE
        }
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
            val headings = ArrayList<String>()

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
                    data += headings[colno] + ":" + currentCell.toString() + ";"
                    colno++
                }
                completedata += data

                threadHandler.post{
                    progressTextView.text = "$rowno/${totalRows - 1}"
                }
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

        val mFileName = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(System.currentTimeMillis())+"$rowno"
        val mFilePath : String = "${Environment.getExternalStorageDirectory()}/$mFileName.pdf"

        try{

            val writer : PdfWriter = PdfWriter(mFilePath)
            val pdfDoc = PdfDocument(writer)
            val mDoc = Document(pdfDoc, PageSize(document_settings.pageSize))

            mDoc.setFontSize(document_settings.documentFontSize)

            val header = Paragraph(document_settings.heading)
            header.setTextAlignment(TextAlignment.CENTER).setBold().setFontSize(document_settings.headingSize)
            mDoc.add(header)

            val pointColumnWidths = floatArrayOf(((document_settings.pageSize.width)/2.0f),((document_settings.pageSize.width)/2.0f) )
            val table = Table(pointColumnWidths)

            val completeData = data.split(";").toTypedArray()
            for(i in 0 until completeData.size-1){
                val oneRow = completeData[i]
                val heading = oneRow.substringBefore(":").trim()
                val detail = oneRow.substringAfter(":").trim()
                table.addCell(Cell().add(heading).setBold())
                table.addCell(Cell().add(detail))
            }

            mDoc.add(table)
            mDoc.close()
        }
        catch (e : Exception){
            Log.e("Background",e.toString())
        }

    }

}