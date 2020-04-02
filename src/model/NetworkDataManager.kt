package model

import datamodel.DATABASE_LOCAL
import datamodel.URLS
import views.ImgToBase64String
import java.awt.image.BufferedImage
import java.io.BufferedReader
import java.io.ByteArrayInputStream
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.sql.ResultSet
import java.util.*
import javax.imageio.ImageIO


class NetworkDataManager {

    fun getAvatar () {
        val url = URL("${URLS.chat_avatar}996137")

        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        if (connection.responseCode != HttpURLConnection.HTTP_OK) return

        val bufferedReader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))

        if (bufferedReader.read() < 100) return

        val bufferedImage: BufferedImage = ImageIO.read(url)
        val base64Avatar = ImgToBase64String.convert(bufferedImage, "jpg")

        val db = DatabaseManagerLocal()
        var query = "SELECT COUNT(uid) AS isset FROM ${DATABASE_LOCAL.tables.sip_user} LIMIT 1"
        val rs: ResultSet = db.select(query);
        if (rs.getInt("isset") == 0) {
            rs.close()
            query = "INSERT INTO ${DATABASE_LOCAL.tables.sip_user} (avatar) VALUES('${base64Avatar}')"
            db.insert(query)
            db.close()
        } else {
            query = "UPDATE ${DATABASE_LOCAL.tables.sip_user} SET avatar = '${base64Avatar}'"
            db.updaate(query)
            db.close()
        }
    }
}