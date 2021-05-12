package com.wix.wixreactnativeprint

import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.webkit.URLUtil
import androidx.annotation.RequiresApi
import com.facebook.react.bridge.Promise
import java.io.*
import java.net.URL


@RequiresApi(Build.VERSION_CODES.KITKAT)
class LoaderPrintAdapter(private val filePath: String?, private val promise: Promise, private val name: String) : PrintDocumentAdapter() {

    val jobName = "Document"

    override fun onWrite(
        pages: Array<PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal,
        callback: WriteResultCallback
    ) {
        try {
            val isUrl = URLUtil.isValidUrl(filePath)
            if (isUrl) {
                Thread(Runnable {
                    try {
                        val input =
                            URL(filePath).openStream()
                        loadAndClose(destination, callback, input)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }).start()
            } else {
                val input: InputStream = FileInputStream(filePath)
                loadAndClose(destination, callback, input)
            }
        } catch (ee: FileNotFoundException) {
            promise.reject(name, "File not found")
        } catch (e: Exception) {
            // Catch exception
            promise.reject(name, e)
        }
    }

    override fun onLayout(
        oldAttributes: PrintAttributes,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal,
        callback: LayoutResultCallback,
        extras: Bundle
    ) {
        if (cancellationSignal.isCanceled) {
            callback.onLayoutCancelled()
            return
        }
        val pdi = PrintDocumentInfo.Builder(jobName)
            .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT).build()
        callback.onLayoutFinished(pdi, true)
    }



    @Throws(IOException::class)
    private fun loadAndClose(
        destination: ParcelFileDescriptor,
        callback: WriteResultCallback,
        input: InputStream
    ) {
        var output: OutputStream? = null
        output = FileOutputStream(destination.fileDescriptor)
        val buf = ByteArray(1024)
        var bytesRead: Int
        while (input.read(buf).also { bytesRead = it } > 0) {
            output.write(buf, 0, bytesRead)
        }
        callback.onWriteFinished(arrayOf(PageRange.ALL_PAGES))
        try {
            input.close()
            output.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}
