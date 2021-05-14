package com.wixreactnativeprint

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintManager
import android.webkit.MimeTypeMap
import android.webkit.URLUtil
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.annotation.RequiresApi
import androidx.print.PrintHelper
import com.facebook.react.bridge.*
import com.wix.wixreactnativeprint.HtmlPrintAdapter
import com.wix.wixreactnativeprint.LoaderPrintAdapter
import java.io.FileNotFoundException
import java.net.URL


class WixReactNativePrintModule(var reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  companion object {
    const val jobName = "PrintingJob"

    enum class ErrorCode {
      NotFound, MissingParam, UnsupportedExtension
    }

    val errorMessages = mapOf(
      ErrorCode.NotFound to "File not found",
      ErrorCode.MissingParam to "Missing param",
      ErrorCode.UnsupportedExtension to "Unsupported file extension"
    )
  }

  override fun getName(): String {
    return "WixReactNativePrint"
  }

  private var mWebView: WebView? = null

  private enum class KeyCode {
    HtmlString, Url, IsLandscape
  }

  private val optionKeys = mapOf(
    KeyCode.HtmlString to "htmlString",
    KeyCode.Url to "url",
    KeyCode.IsLandscape to "isLandscape"
  )

  private val supportedImageExtensions = arrayOf("png", "jpg", "jpeg", "gif")

  private val supportedDocumentExtensions = arrayOf("pdf")

  private val supportedExtensions = arrayOf(*supportedImageExtensions, *supportedDocumentExtensions)

  private fun getFileExtension(url: String?): String {
    return MimeTypeMap.getFileExtensionFromUrl(url) ?: ""
  }

  private fun isExtensionSupported(extension: String?, supportedArr: Array<String>): Boolean {
    return supportedArr.contains(extension!!)
  }

  private fun getBitmap(imageURL: String): Bitmap? {
    val input = URL(imageURL).openStream()
    return BitmapFactory.decodeStream(input)
  }

  private fun printImage(url: String?, isLandscape: Boolean, promise: Promise) {
    try {
      val isUrl = URLUtil.isValidUrl(url)
      if (isUrl) {
        Thread(Runnable {
          try {
            val bitmap = getBitmap(url!!)
            currentActivity?.also { context ->
              PrintHelper(context).apply {
                scaleMode = PrintHelper.SCALE_MODE_FIT
                orientation = if (isLandscape) PrintHelper.ORIENTATION_LANDSCAPE else PrintHelper.ORIENTATION_PORTRAIT
              }.also { printHelper ->
                printHelper.printBitmap(jobName, bitmap!!)
              }
            }
          } catch (e: Exception) {
            promise.reject(name, e)
          }
        }).start()
      }
    } catch (ee: FileNotFoundException) {
      promise.reject(name, errorMessages[ErrorCode.NotFound])
    } catch (e: Exception) {
      promise.reject(name, e)
    }
  }

  @RequiresApi(Build.VERSION_CODES.KITKAT)
  private fun printDocument(url: String?, isLandscape: Boolean, promise: Promise) {
    val printManager = currentActivity!!.getSystemService(Context.PRINT_SERVICE) as PrintManager
    val pda: PrintDocumentAdapter = LoaderPrintAdapter(url, name, promise)
    val printAttributes = PrintAttributes.Builder()
      .setMediaSize(if (isLandscape) PrintAttributes.MediaSize.UNKNOWN_LANDSCAPE else PrintAttributes.MediaSize.UNKNOWN_PORTRAIT)
      .build()
    printManager.print(jobName, pda, printAttributes)
  }

  @RequiresApi(Build.VERSION_CODES.KITKAT)
  private fun printHtml(html: String, promise: Promise) {
    UiThreadUtil.runOnUiThread {
      val webView = WebView(reactContext)
      webView.webViewClient = object : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
          return false
        }

        override fun onPageFinished(view: WebView, url: String) {
          val printManager = currentActivity!!.getSystemService(Context.PRINT_SERVICE) as PrintManager
          val adapter: PrintDocumentAdapter = HtmlPrintAdapter(mWebView)
          printManager.print(jobName, adapter, null)
          mWebView = null
          promise.resolve(name)
        }
      }
      webView.loadDataWithBaseURL(null, html, "text/HTML", "UTF-8", null)
      mWebView = webView
    }
  }

  @RequiresApi(Build.VERSION_CODES.KITKAT)
  @ReactMethod
  fun printHtml(options: ReadableMap, promise: Promise) {
    val html = optionKeys[KeyCode.HtmlString]?.let { options.getString(it) }

    if (html == null) {
      promise.reject(name, "${errorMessages[ErrorCode.MissingParam]}: html")
      return
    }

    try {
      printHtml(html, promise)
    } catch (e: Exception) {
      promise.reject(name, e)
    }
  }

  @RequiresApi(Build.VERSION_CODES.KITKAT)
  @ReactMethod
  fun printUrl(options: ReadableMap, promise: Promise) {
    val url = optionKeys[KeyCode.Url]?.let { options.getString(it) }
    val isLandscape = optionKeys[KeyCode.IsLandscape]?.let {
      if (options.hasKey(it)) options.getBoolean(it) else false
    } ?: false

    if (url == null) {
      promise.reject(name, "${errorMessages[ErrorCode.MissingParam]}: url")
      return
    }

    try {
      val extension = getFileExtension(url)

      if (!isExtensionSupported(extension, supportedExtensions)) {
        promise.reject(name, "${errorMessages[ErrorCode.UnsupportedExtension]}: $extension")
        return
      }

      val isImage = isExtensionSupported(extension, supportedImageExtensions)

      if (isImage) {
        printImage(url, isLandscape, promise)
      } else {
        printDocument(url, isLandscape, promise)
      }

      promise.resolve(name)
    } catch (e: Exception) {
      promise.reject(name, e)
    }
  }
}
