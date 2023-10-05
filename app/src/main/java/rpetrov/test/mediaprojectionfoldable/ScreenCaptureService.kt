package rpetrov.test.mediaprojectionfoldable

import android.app.Notification
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.content.res.Configuration
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.PixelFormat
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.ImageReader
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.util.Log
import java.io.File
import java.io.FileOutputStream

class ScreenCaptureService : Service() {

    private var mediaProjection: MediaProjection? = null
    private var virtualDisplay: VirtualDisplay? = null
    private var imageReader: ImageReader? = null
    private var mHandler: Handler? = null

    private var mStoreDir: String? = null

    private var imageProduced = 0


    private var width = 0
    private var height = 0

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.e("ScreenCaptureService", "onConfigurationChanged")
    }

    override fun onCreate() {
        super.onCreate()
        // create store dir
        val externalFilesDir = getExternalFilesDir(null)
        if (externalFilesDir != null) {
            mStoreDir = externalFilesDir.absolutePath + "/screenshots/"
            val storeDirectory = File(mStoreDir)
            if (!storeDirectory.exists()) {
                val success = storeDirectory.mkdirs()
                if (!success) {
                    Log.e("ScreenCaptureService", "failed to create file storage directory.")
                    stopSelf()
                }
            }
        } else {
            Log.e("ScreenCaptureService", "failed to create file storage directory, getExternalFilesDir is null.")
            stopSelf()
        }

    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {

        val notification: Pair<Int, Notification> = NotificationUtils.getNotification(this)
        startForeground(
            notification.first, notification.second, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION
        )

        val mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager

        width = intent.getIntExtra("W", 0)
        height = intent.getIntExtra("H", 0)

        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2)
        imageReader!!.setOnImageAvailableListener({
            try {
                it.acquireLatestImage().use { image ->
                    if (image != null) {
                        val width = image.width
                        val height = image.height
                        val planes = image.planes
                        val buffer = planes[0].buffer
                        val pixelStride = planes[0].pixelStride
                        val rowStride = planes[0].rowStride
                        val rowPadding = rowStride - pixelStride * width

                        // create bitmap
                        var bitmap =
                            Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888)
                        bitmap.copyPixelsFromBuffer(buffer)

                        // write bitmap to a file
                        val fos = FileOutputStream(mStoreDir + "/myscreen_" + imageProduced + ".png")
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos)
                        imageProduced++
                        Log.e("ScreenCaptureService", "captured image: " + imageProduced)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, mHandler)

        mediaProjection = mediaProjectionManager.getMediaProjection(
            intent.getIntExtra("RESULT_CODE", 0),
            intent.getParcelableExtra("DATA")!!
        )


        virtualDisplay = mediaProjection!!.createVirtualDisplay(
            "ScreenCapture",
            width,
            height,
            Resources.getSystem().displayMetrics.densityDpi,
            DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY or DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC,
            imageReader!!.surface,
            null, null
        )


        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaProjection?.stop()
        virtualDisplay?.release()
        imageReader?.setOnImageAvailableListener(null, null)
        mediaProjection = null
        virtualDisplay = null
        imageReader = null
        mHandler?.looper?.quitSafely()
        mHandler = null
    }
}