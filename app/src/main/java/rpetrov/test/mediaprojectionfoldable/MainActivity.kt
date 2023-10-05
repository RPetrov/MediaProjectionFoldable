package rpetrov.test.mediaprojectionfoldable

import android.content.Intent
import android.media.projection.MediaProjectionManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.window.layout.WindowMetricsCalculator

class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        findViewById<Button>(R.id.startButton).setOnClickListener {
            val mediaProjectionManager = getSystemService(MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
            startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(),
                REQUEST_CODE
            )
        }
        findViewById<Button>(R.id.stopButton).setOnClickListener {
            stopService(Intent(this, ScreenCaptureService::class.java))
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK) {

                val wmc = WindowMetricsCalculator.getOrCreate()
                val bounds = wmc.computeCurrentWindowMetrics(this).bounds

                val intent = Intent(this, ScreenCaptureService::class.java)
                intent.putExtra("RESULT_CODE", resultCode)
                intent.putExtra("DATA", data)
                intent.putExtra("W", bounds.width())
                intent.putExtra("H", bounds.height())
                startService(intent)
            }
        }
    }
}