package dev.msemyak.geocam.ui.camera

import android.content.Context
import android.content.Intent
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.HandlerThread
import android.support.v7.app.AppCompatActivity
import android.view.TextureView
import dev.msemyak.geocam.AppBoss
import dev.msemyak.geocam.R
import dev.msemyak.geocam.ui.preview.PreviewActivity
import dev.msemyak.geocam.utils.Logga
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.File

class CameraActivity : AppCompatActivity(), CameraContract.View {

//    @Inject lateinit var myPresenter: CameraContract.Presenter
    lateinit var myPresenter: CameraContract.Presenter
    private lateinit var cameraManager: CameraManager

    private var backgroundThread: HandlerThread? = null
    override var backgroundHandler: Handler? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

//        AppBoss.cameraComponent?.inject(this)
        myPresenter = AppBoss.appComponent.getCameraPresenter()

        startBackgroundThread()

        myPresenter.attachView(this)

        cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager

        IV_shutter_button.setOnClickListener {
            if (myPresenter.cameraIsReady) {
                myPresenter.captureImage()
            }
        }
    }

    override fun getGalleryFolder(): String {
        val systemGalleryFolder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).toString()
        val imageSavingFolder = File(systemGalleryFolder + File.separator + getString(R.string.gallery_dir))
        return if (imageSavingFolder.exists()) imageSavingFolder.toString()
        else {
            if (imageSavingFolder.mkdir()) imageSavingFolder.toString()
            else systemGalleryFolder
        }
    }

    override fun openPreviewActivity(imagePath: String) {
        startActivity(Intent(this, PreviewActivity::class.java).putExtra("filename", imagePath))
    }

    private fun startBackgroundThread() {
        backgroundThread = HandlerThread("CameraBackground").also { it.start() }
        backgroundHandler = Handler(backgroundThread?.looper)
    }

    private fun stopBackgroundThread() {
        backgroundThread?.quitSafely()
        try {
            backgroundThread?.join()
            backgroundThread = null
            backgroundHandler = null
        } catch (e: InterruptedException) {
            Logga(e.toString())
        }
    }

    override fun onResume() {
        super.onResume()
        startBackgroundThread()

        if (TXV_camera_preview.isAvailable) {
            myPresenter.launchCameraPreview(cameraManager, TXV_camera_preview)
        } else {
            TXV_camera_preview.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
                override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture?, width: Int, height: Int) {}
                override fun onSurfaceTextureUpdated(surface: SurfaceTexture?) {}
                override fun onSurfaceTextureDestroyed(surface: SurfaceTexture?): Boolean = false
                override fun onSurfaceTextureAvailable(surface: SurfaceTexture?, width: Int, height: Int) {
                    myPresenter.launchCameraPreview(cameraManager, TXV_camera_preview)
                }
            }
        }
    }

    override fun getScreenRotation(): Int {
        return windowManager.defaultDisplay.rotation
    }

    override fun setTextureViewAspectRatio(width: Int, height: Int) {
        TXV_camera_preview.setAspectRatio(width, height)
    }

    override fun onPause() {
        stopBackgroundThread()
        myPresenter.detachView()
        super.onPause()
    }

}
