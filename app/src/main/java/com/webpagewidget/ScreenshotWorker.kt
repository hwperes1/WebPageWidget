package com.webpagewidget

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.os.Handler
import android.os.Looper
import android.view.View
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.work.Worker
import androidx.work.WorkerParameters
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

class ScreenshotWorker(private val ctx: Context, params: WorkerParameters) : Worker(ctx, params) {

    override fun doWork(): Result {
        val prefs = ctx.getSharedPreferences(MainActivity.PREFS_NAME, Context.MODE_PRIVATE)
        val url = prefs.getString(MainActivity.KEY_URL, null)
            ?: return Result.failure()

        val latch = CountDownLatch(1)
        var captureSuccess = false

        Handler(Looper.getMainLooper()).post {
            try {
                val widthPx = 1080
                val heightPx = 810

                val webView = WebView(ctx)
                webView.settings.apply {
                    javaScriptEnabled = true
                    domStorageEnabled = true
                    loadWithOverviewMode = true
                    useWideViewPort = true
                    setSupportZoom(false)
                    userAgentString = "Mozilla/5.0 (Linux; Android 14; Pixel 8) " +
                            "AppleWebKit/537.36 (KHTML, like Gecko) " +
                            "Chrome/120.0.0.0 Mobile Safari/537.36"
                }
                webView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
                webView.isHorizontalScrollBarEnabled = false
                webView.isVerticalScrollBarEnabled = false

                val wSpec = View.MeasureSpec.makeMeasureSpec(widthPx, View.MeasureSpec.EXACTLY)
                val hSpec = View.MeasureSpec.makeMeasureSpec(heightPx, View.MeasureSpec.EXACTLY)
                webView.measure(wSpec, hSpec)
                webView.layout(0, 0, widthPx, heightPx)

                webView.webViewClient = object : WebViewClient() {
                    override fun onPageFinished(view: WebView, loadedUrl: String) {
                        // Wait for JS/images to render before capturing
                        Handler(Looper.getMainLooper()).postDelayed({
                            try {
                                view.measure(wSpec, hSpec)
                                view.layout(0, 0, widthPx, heightPx)

                                val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
                                val canvas = Canvas(bitmap)
                                view.draw(canvas)

                                val file = File(ctx.filesDir, "screenshot.png")
                                FileOutputStream(file).use { out ->
                                    bitmap.compress(Bitmap.CompressFormat.PNG, 85, out)
                                }
                                bitmap.recycle()
                                captureSuccess = true
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                latch.countDown()
                            }
                        }, RENDER_DELAY_MS)
                    }
                }

                webView.loadUrl(url)
            } catch (e: Exception) {
                e.printStackTrace()
                latch.countDown()
            }
        }

        val completed = latch.await(TIMEOUT_SECONDS, TimeUnit.SECONDS)

        if (captureSuccess && completed) {
            PageWidget.updateAllWidgets(ctx)
            return Result.success()
        }

        return Result.retry()
    }

    companion object {
        private const val RENDER_DELAY_MS = 3000L
        private const val TIMEOUT_SECONDS = 45L
    }
}
