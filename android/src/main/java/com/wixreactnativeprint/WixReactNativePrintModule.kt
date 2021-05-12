package com.wixreactnativeprint

import android.content.Context
import android.os.Build
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import com.facebook.react.bridge.*
import com.wix.wixreactnativeprint.HtmlPrintAdapter
import com.wix.wixreactnativeprint.LoaderPrintAdapter

class WixReactNativePrintModule(var reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {

  override fun getName(): String {
    return "WixReactNativePrint"
  }

  val jobName = "Document"

  private companion object {
    const val htmlKey = "html"
    const val filePathKey = "filePath"
    const val isLandscapeKey = "isLandscape"
  }

  var mWebView: WebView? = null

  @RequiresApi(Build.VERSION_CODES.KITKAT)
  @ReactMethod
  fun print(options: ReadableMap, promise: Promise) {
    val html = if (options.hasKey(htmlKey)) options.getString(htmlKey) else null
    val filePath =
      if (options.hasKey(filePathKey)) options.getString(filePathKey) else null
    val isLandscape =
      if (options.hasKey(isLandscapeKey)) options.getBoolean(isLandscapeKey) else false
    if (html == null && filePath == null || html != null && filePath != null) {
      promise.reject(
        name,
        "Must provide either `html` or `filePath`.  Both are either missing or passed together"
      )
      return
    }
    if (html != null) {
      try {
        UiThreadUtil.runOnUiThread { // Create a WebView object specifically for printing
          val webView = WebView(reactContext)
          webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
              view: WebView,
              url: String
            ): Boolean {
              return false
            }

            @RequiresApi(Build.VERSION_CODES.KITKAT)
            override fun onPageFinished(
              view: WebView,
              url: String
            ) {
              // Get the print manager.
              val printManager =
                currentActivity!!.getSystemService(
                  Context.PRINT_SERVICE
                ) as PrintManager
              // Create a wrapper PrintDocumentAdapter to clean up when done.
              val adapter: PrintDocumentAdapter = HtmlPrintAdapter(mWebView)
              // Pass in the ViewView's document adapter.
              printManager.print(jobName, adapter, null)
              mWebView = null
              promise.resolve(jobName)
            }
          }
          webView.loadDataWithBaseURL(null, html, "text/HTML", "UTF-8", null)

          // Keep a reference to WebView object until you pass the PrintDocumentAdapter
          // to the PrintManager
          mWebView = webView
        }
      } catch (e: Exception) {
        promise.reject("print_error", e)
      }
    } else {
      try {
        val printManager =
          currentActivity!!.getSystemService(Context.PRINT_SERVICE) as PrintManager
        val pda: PrintDocumentAdapter = LoaderPrintAdapter(filePath, promise, name)
        val printAttributes = PrintAttributes.Builder()
          .setMediaSize(if (isLandscape) PrintAttributes.MediaSize.UNKNOWN_LANDSCAPE else PrintAttributes.MediaSize.UNKNOWN_PORTRAIT)
          .build()
        printManager.print(jobName, pda, printAttributes)
        promise.resolve(jobName)
      } catch (e: Exception) {
        promise.reject(name, e)
      }
    }
  }
}
