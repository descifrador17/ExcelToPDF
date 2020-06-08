package com.descifrador.exceltopdf.model

import com.itextpdf.kernel.geom.PageSize

class DocumentSettings(){

    var pageSize: PageSize = PageSize.A4
    var heading : String = ""
    var headingSize : Float = 16.0f
    var documentFontSize : Float = 14.0f
    var fileName : String = ""

    fun getPageSize(pos : Int) : PageSize{
        when(pos) {
            0 -> return PageSize.A0
            1 -> return PageSize.A1
            2 -> return PageSize.A2
            3 -> return PageSize.A3
            4 -> return PageSize.A4
            5 -> return PageSize.A5
            6 -> return PageSize.A6
            7 -> return PageSize.A7
            8 -> return PageSize.A8
            9 -> return PageSize.A9
            10 -> return PageSize.A10
            11 -> return PageSize.B0
            12-> return PageSize.B1
            13 -> return PageSize.B2
            14 -> return PageSize.B3
            15 -> return PageSize.B4
            16 -> return PageSize.B5
            17 -> return PageSize.B6
            18 -> return PageSize.B7
            19 -> return PageSize.B8
            20 -> return PageSize.B9
            21 -> return PageSize.B10
            else -> return PageSize.A4
        }
    }
}