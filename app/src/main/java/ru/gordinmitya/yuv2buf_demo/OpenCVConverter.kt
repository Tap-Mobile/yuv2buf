package ru.gordinmitya.yuv2buf_demo

import android.graphics.Bitmap
import androidx.camera.core.ImageProxy
import org.opencv.android.Utils
import org.opencv.core.Core
import org.opencv.core.CvType
import org.opencv.core.Mat
import org.opencv.imgproc.Imgproc
import ru.gordinmitya.yuv2buf.Yuv
import java.nio.ByteBuffer


class OpenCVConverter() : ImageConverter {
    override fun getName(): String = "OpenCV"

    private var reuseBuffer: ByteBuffer? = null

    override fun convert(image: ImageProxy): Bitmap {
        val converted = Yuv.toBuffer(image, reuseBuffer)
        reuseBuffer = converted.buffer

        val format = when (converted.type) {
            Yuv.Type.YUV_420 -> Imgproc.COLOR_YUV2RGBA_I420
            Yuv.Type.YUV_NV21 -> Imgproc.COLOR_YUV2RGBA_NV21
        }
        val yuvMat =
            Mat(image.height + image.height / 2, image.width, CvType.CV_8UC1, converted.buffer)
        val rgbMat = Mat(image.height, image.width, CvType.CV_8UC4)
        Imgproc.cvtColor(yuvMat, rgbMat, format)

        if (image.imageInfo.rotationDegrees != 0) {
            Core.rotate(rgbMat, rgbMat, image.imageInfo.rotationDegrees / 90 - 1)
        }

        val bitmap = Bitmap.createBitmap(rgbMat.cols(), rgbMat.rows(), Bitmap.Config.ARGB_8888)
        Utils.matToBitmap(rgbMat, bitmap)

        // don't forget to call image.close() here
        // but as long as we have many converters
        // we'll do it in CompositeConverter
        // image.close()

        return bitmap
    }
}