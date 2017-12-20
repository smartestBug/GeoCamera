package dev.msemyak.geocam.ui.camera

import android.hardware.camera2.CameraManager
import android.os.Handler
import android.view.TextureView

interface CameraContract {
    interface View {
        fun getGalleryFolder(): String
        fun openPreviewActivity(imagePath: String)
        fun getScreenRotation(): Int
        fun setTextureViewAspectRatio(width: Int, height: Int)
        var backgroundHandler: Handler?
    }

    interface Presenter {
        fun attachView(view: CameraContract.View)
        fun detachView()
        fun launchCameraPreview(systemCameraManager: CameraManager, textureView: TextureView)
        fun captureImage()
        var cameraIsReady: Boolean
    }
}