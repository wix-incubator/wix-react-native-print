package com.wix.wixreactnativeprint

import android.os.*
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.webkit.URLUtil
import androidx.annotation.RequiresApi
import com.facebook.react.bridge.Promise
import com.wixreactnativeprint.WixReactNativePrintModule.Companion.errorMessages
import com.wixreactnativeprint.WixReactNativePrintModule.Companion.ErrorCode
import com.wixreactnativeprint.WixReactNativePrintModule.Companion.jobName
import java.io.*
import java.net.URL


@RequiresApi(Build.VERSION_CODES.KITKAT)
class LoaderPrintAdapter(
  private val url: String?,
  private val promise: Promise,
  private val name: String
) : PrintDocumentAdapter() {

  override fun onWrite(
    pages: Array<PageRange>,
    destination: ParcelFileDescriptor,
    cancellationSignal: CancellationSignal,
    callback: WriteResultCallback
  ) {
    try {
      val isUrl = URLUtil.isValidUrl(url)
      if (isUrl) {
        Thread(Runnable {
          try {
            val input = URL(url).openStream()
            loadAndClose(destination, callback, input)
          } catch (e: Exception) {
            e.printStackTrace()
          }
        }).start()
      } else {
        val input: InputStream = FileInputStream(url!!)
        loadAndClose(destination, callback, input)
      }
    } catch (ee: FileNotFoundException) {
      promise.reject(name, errorMessages[ErrorCode.NotFound])
    } catch (e: Exception) {
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
