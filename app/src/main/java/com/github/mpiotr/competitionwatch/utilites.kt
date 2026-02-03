package com.github.mpiotr.competitionwatch

import android.app.Application
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.os.Environment
import android.text.TextPaint
import android.util.Log
import androidx.core.content.FileProvider
import androidx.core.net.toUri
import androidx.lifecycle.application
import java.io.File
import java.io.FileOutputStream
import kotlin.time.Duration.Companion.milliseconds

fun formattedTimeSplit(now : Long, comp_start_time : Long) : String {
    val duration = (now - comp_start_time).milliseconds
    val competitionTime = duration.toComponents {
            hours, minutes, seconds, nanoseconds ->
        val centiseconds = (nanoseconds / 10e7.toFloat()).toInt()
        "%02d:%02d:%02d.%d".format(hours, minutes, seconds, centiseconds)  }
    return competitionTime
}

fun makeResultPDF(viewModel: CompetitorViewModel, application : Application, onSendPdf : (File)->Unit) {

        val pdfDoc = PdfDocument()
        var page_count = 1
        val pageinfo = PdfDocument.PageInfo.Builder(842, 595, page_count).create()
        var pdfPage = pdfDoc.startPage(pageinfo)
        val titleOffsetX = 100.0f
        val tableOffsetX = 10.0f
        val titleOffsetY = 50.0f
        val titlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        val subTitlePaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG)
        val backPaint = Paint()
        val aR = application.resources

        var x = titleOffsetX
        var y = titleOffsetY

        val ts = 10.0f
        titlePaint.textSize = 24.0f
        subTitlePaint.textSize = 18.0f
        textPaint.textSize = ts
        textPaint.typeface = Typeface.MONOSPACE
        backPaint.color = Color.LTGRAY

        val pageStop = pageinfo.pageWidth.toFloat() - 10


        val canvas = pdfPage.canvas

        canvas.drawText(aR.getString(R.string.to_results), titleOffsetX, titleOffsetY, titlePaint)

        y += titlePaint.textSize + 2.5f
        val result = viewModel.getResults()
        for (kvpair in result) {
            val sexname =
                if (kvpair.key.first == 1) aR.getString(R.string.men)
                else aR.getString(R.string.women)
            y += subTitlePaint.textSize * 1.0f
            canvas.drawText(
                "${aR.getString(R.string.group)} ${kvpair.key.second}, $sexname",
                titleOffsetX,
                y,
                subTitlePaint
            )
            y += subTitlePaint.textSize * 0.5f
            canvas.drawLine(10.0f, y, pageStop, y, textPaint)
            y += subTitlePaint.textSize + 0.5f


            x = tableOffsetX
            for ((j, c) in kvpair.value.withIndex()) {
                if ((j + 1) % 2 == 0) {
                    canvas.drawRect(10.0f, y - ts, pageStop, y + 1.2f * ts, backPaint)
                }
                canvas.drawText(c.result.toString(), x, y, textPaint)
                x += 10

                canvas.drawText(c.name, x, y, textPaint)
                x += 200

                canvas.drawText(aR.getString(R.string.race_time), x, y, textPaint); y += ts
                canvas.drawText(aR.getString(R.string.lap_time), x, y, textPaint); y -= ts
                x += 50


                val raceSplits = c.formattedSplitsRaceTime()
                val lapSplits = c.formattedSplitsLapTime()
                for ((i, s) in raceSplits.withIndex()) {
                    canvas.drawText(s, x, y, textPaint); y += ts
                    canvas.drawText(lapSplits[i], x, y, textPaint)
                    y -= ts
                    x += 75
                }
                x = tableOffsetX

                y += 2.5f * ts
                if (y > 560) {
                    pdfDoc.finishPage(pdfPage)
                    page_count++
                    PdfDocument.PageInfo.Builder(842, 595, page_count).create()
                    pdfPage = pdfDoc.startPage(
                        PdfDocument.PageInfo.Builder(842, 595, page_count).create()
                    )
                    y = titleOffsetY
                }
            }


        }
        pdfDoc.finishPage(pdfPage)

        application.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS)


        val myExternalFile =
            File(
                application.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS),
                "result.pdf"
            )
        val stream = FileOutputStream(myExternalFile)
        pdfDoc.writeTo(stream)
        pdfDoc.close()

        onSendPdf(myExternalFile)


}

fun getEmailIntent(file : File, recipients : List<String>, application : Application) : Intent {
    val rec_array = Array(recipients.size, { i -> recipients[i] })

    val emailSelectorIntent = Intent(Intent.ACTION_SENDTO)
    emailSelectorIntent.setData("mailto:".toUri())

    val intent = Intent(Intent.ACTION_SEND)
    intent.data = "mailto:".toUri() // only email apps
    intent.putExtra(Intent.EXTRA_EMAIL, rec_array)
    intent.putExtra(
        Intent.EXTRA_SUBJECT,
        application.resources.getString(R.string.to_results)
    )
    intent.putExtra(Intent.EXTRA_TEXT, "Competition results")
    intent.putExtra(
        Intent.EXTRA_STREAM, FileProvider.getUriForFile(
            application.applicationContext,
            "${application.applicationContext.packageName}.fileprovider",
            file
        )
    )
    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
    intent.selector = emailSelectorIntent

    return intent
}