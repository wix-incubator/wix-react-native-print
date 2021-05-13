package com.wix.wixreactnativeprint

import android.os.Build
import android.os.Bundle
import android.os.CancellationSignal
import android.os.ParcelFileDescriptor
import android.print.PageRange
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.webkit.WebView
import androidx.annotation.RequiresApi

@RequiresApi(Build.VERSION_CODES.KITKAT)
class HtmlPrintAdapter(mWebView: WebView?): PrintDocumentAdapter() {
    private val mWrappedInstance =
        mWebView!!.createPrintDocumentAdapter()

    override fun onStart() {
        mWrappedInstance.onStart()
    }

    override fun onLayout(
        oldAttributes: PrintAttributes,
        newAttributes: PrintAttributes,
        cancellationSignal: CancellationSignal,
        callback: LayoutResultCallback,
        extras: Bundle
    ) {
        mWrappedInstance.onLayout(
            oldAttributes, newAttributes, cancellationSignal,
            callback, extras
        )
    }

    override fun onWrite(
        pages: Array<PageRange>,
        destination: ParcelFileDescriptor,
        cancellationSignal: CancellationSignal,
        callback: WriteResultCallback
    ) {
        mWrappedInstance.onWrite(
            pages,
            destination,
            cancellationSignal,
            callback
        )
    }

    override fun onFinish() {
        mWrappedInstance.onFinish()
    }
}
