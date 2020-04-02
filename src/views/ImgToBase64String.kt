package views

import java.awt.image.RenderedImage
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.UncheckedIOException
import java.util.*
import javax.imageio.ImageIO

object ImgToBase64String {
    fun convert(img: RenderedImage?, formatName: String?): String? {
        val os = ByteArrayOutputStream()
        return try {
            ImageIO.write(img, formatName, os)
            Base64.getEncoder().encodeToString(os.toByteArray())
        } catch (ioe: IOException) {
            throw UncheckedIOException(ioe)
        }
    }
}