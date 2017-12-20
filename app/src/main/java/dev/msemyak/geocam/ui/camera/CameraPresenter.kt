package dev.msemyak.geocam.ui.camera

import android.annotation.SuppressLint
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.*
import android.media.ImageReader
import android.os.Handler
import android.util.Size
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import dev.msemyak.geocam.utils.CompareSizesByArea
import dev.msemyak.geocam.utils.ImageSaver
import dev.msemyak.geocam.utils.Logga
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class CameraPresenter : CameraContract.Presenter {

    //general fields
    private var myView: CameraContract.View? = null
    override var cameraIsReady = false

    //camera related fields
    private lateinit var systemCameraManager: CameraManager
    private var cameraID: String? = "0"
    private var cameraDevice: CameraDevice? = null
    private var cameraCaptureSession: CameraCaptureSession? = null

    private lateinit var previewSurface: Surface
    private lateinit var textureView: TextureView
    private lateinit var captureSurface: Surface
    private var imageReader: ImageReader? = null

    private lateinit var previewCaptureRequest: CaptureRequest
    private lateinit var captureCaptureRequest: CaptureRequest

    private var sensorOrientation = 0
    private var flashSupported = false
    private lateinit var maxPhotoSize: Size
    private lateinit var previewSurfaceSize: Size

    private lateinit var fileSaveFolder: String
    private lateinit var capturedImageFilename: String

    private var backgroundHandler: Handler? = null

    // ----- camera related functions -----
    override fun launchCameraPreview(systemCameraManager: CameraManager, textureView: TextureView) {
        this.systemCameraManager = systemCameraManager
        this.textureView = textureView

        backgroundHandler = myView?.backgroundHandler

        configureCamera()
        openCamera()

    }

    private fun configureCamera() {

        for (tempCamID in systemCameraManager.cameraIdList) {

            val cameraCharacteristics = systemCameraManager.getCameraCharacteristics(tempCamID)

            val cameraDirection = cameraCharacteristics.get(CameraCharacteristics.LENS_FACING)
            if (cameraDirection != null && cameraDirection == CameraCharacteristics.LENS_FACING_BACK) {
                cameraID = tempCamID
                sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION)
                flashSupported = cameraCharacteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true

                val scalerMap = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

                val outputSizes = scalerMap.getOutputSizes(ImageFormat.JPEG)

                maxPhotoSize = Collections.max(Arrays.asList(*outputSizes), CompareSizesByArea())

                previewSurfaceSize = chooseOptimalSize(scalerMap.getOutputSizes(SurfaceTexture::class.java),
                        textureView.height, textureView.width, MAX_PREVIEW_WIDTH, MAX_PREVIEW_HEIGHT, maxPhotoSize)

                myView?.setTextureViewAspectRatio(previewSurfaceSize.height, previewSurfaceSize.width)

                imageReader = ImageReader.newInstance(maxPhotoSize.width, maxPhotoSize.height, ImageFormat.JPEG, 1)
                captureSurface = imageReader?.surface!!

                imageReader?.setOnImageAvailableListener(
                        {
                            // saving procedure
                            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
                            val filename = "gcam_$timeStamp.jpg"
                            val file = File(fileSaveFolder, filename)
                            capturedImageFilename = file.path

                            backgroundHandler?.post(ImageSaver(it.acquireNextImage(), file))

                        }, null)

            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun openCamera() {
        try {
            systemCameraManager.openCamera(cameraID, object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice?) {
                    cameraDevice = camera
                    preparePreviewCaptureRequest()
                    createCameraCaptureSession()
                    cameraIsReady = true
                }

                override fun onDisconnected(camera: CameraDevice?) {
                    cameraDevice?.close()
                }

                override fun onError(camera: CameraDevice?, error: Int) {
                    Logga("Camera error on Open Camera. Error code: $error")
                }
            }, null)
        } catch (e: CameraAccessException) {
            Logga(e.toString())
        }
    }

    private fun preparePreviewCaptureRequest() {
        try {

            previewSurface = Surface(textureView.surfaceTexture.apply { setDefaultBufferSize(previewSurfaceSize.width, previewSurfaceSize.height) })

            previewCaptureRequest = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)!!.run {
                addTarget(previewSurface)
                set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
                build()
            }
        } catch (e: CameraAccessException) {
            Logga(e.toString())
        }
    }

    private fun prepareCaptureCaptureRequest() {
        try {
            captureCaptureRequest = cameraDevice?.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)!!.run {
                addTarget(captureSurface)
                set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE)
                set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH)
                set(CaptureRequest.JPEG_ORIENTATION, (ORIENTATIONS.get(myView?.getScreenRotation()!!) + sensorOrientation + 270) % 360)
                build()
            }
        } catch (e: CameraAccessException) {
            Logga(e.toString())
        }
    }

    private fun createCameraCaptureSession() {

        cameraDevice?.createCaptureSession(Arrays.asList(previewSurface, captureSurface),
                object : CameraCaptureSession.StateCallback() {

                    override fun onConfigured(cameraCaptureSession: CameraCaptureSession) {
                        if (cameraDevice == null) return

                        this@CameraPresenter.cameraCaptureSession = cameraCaptureSession

                        try {
                            cameraCaptureSession.setRepeatingRequest(previewCaptureRequest, null, backgroundHandler)
                        } catch (e: CameraAccessException) {
                            Logga("Error creating Camera Capture Session. Details: $e")
                        }

                    }

                    override fun onConfigureFailed(session: CameraCaptureSession) {
                        Logga("Camera configuration failed at creating camera capture session")
                    }
                }, null)

    }

    override fun captureImage() {
        prepareCaptureCaptureRequest()

        try {
            cameraCaptureSession?.apply {
                stopRepeating()
                abortCaptures()
                capture(captureCaptureRequest, object : CameraCaptureSession.CaptureCallback() {
                    override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
                        myView?.openPreviewActivity(capturedImageFilename)
                    }
                }, backgroundHandler)
            }
        } catch (e: CameraAccessException) {
            Logga(e.toString())
        }
    }

    private fun closeCamera() {

        cameraCaptureSession?.close()
        cameraCaptureSession = null

        cameraDevice?.close()
        cameraDevice = null

        imageReader?.close()
        imageReader = null

        cameraIsReady = false

    }

    // ------------------------ functions --------------------------------
    override fun attachView(view: CameraContract.View) {
        this.myView = view
        fileSaveFolder = myView?.getGalleryFolder()!!
    }

    override fun detachView() {
        closeCamera()
        backgroundHandler = null
    }

    companion object {

        private val ORIENTATIONS = SparseIntArray()

        init {
            ORIENTATIONS.append(Surface.ROTATION_0, 90)
            ORIENTATIONS.append(Surface.ROTATION_90, 0)
            ORIENTATIONS.append(Surface.ROTATION_180, 270)
            ORIENTATIONS.append(Surface.ROTATION_270, 180)
        }

        private val MAX_PREVIEW_WIDTH = 1920
        private val MAX_PREVIEW_HEIGHT = 1080

        private fun chooseOptimalSize(choices: Array<Size>, textureViewWidth: Int, textureViewHeight: Int, maxWidth: Int, maxHeight: Int, aspectRatio: Size): Size {

            val bigEnough = ArrayList<Size>()

            val notBigEnough = ArrayList<Size>()
            val w = aspectRatio.width
            val h = aspectRatio.height
            choices.filter { it.width <= maxWidth && it.height <= maxHeight && it.height == it.width * h / w }
                    .forEach {
                        if (it.width >= textureViewWidth && it.height >= textureViewHeight) {
                            bigEnough.add(it)
                        } else {
                            notBigEnough.add(it)
                        }
                    }

            return when {
                bigEnough.size > 0 -> Collections.min(bigEnough, CompareSizesByArea())
                notBigEnough.size > 0 -> Collections.max(notBigEnough, CompareSizesByArea())
                else -> {
                    Logga("Couldn't find any suitable preview size")
                    choices[0]
                }
            }
        }

    }
}