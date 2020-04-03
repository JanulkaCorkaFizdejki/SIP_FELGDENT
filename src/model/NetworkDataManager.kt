package model

import com.sun.org.apache.xpath.internal.operations.Bool
import datamodel.DATABASE_LOCAL
import datamodel.HTTP_DATA_STATUS
import datamodel.SQLITEPopularQuery
import datamodel.URLS
import org.json.JSONObject
import views.ImgToBase64String
import java.awt.image.BufferedImage
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.sql.ResultSet
import java.util.*
import javax.imageio.ImageIO


class NetworkDataManager {
    private val timeout: Int = 10000


    fun loginFelg (login: String, password: String) : HTTP_DATA_STATUS {

        try {

            val params: LinkedHashMap<String, String> = LinkedHashMap()

            params["login"] = login
            params["password"] = password

            val postData = StringBuilder()

            for ((key, value) in params) {
                if (postData.isNotEmpty()) postData.append('&')
                postData.append(URLEncoder.encode(key, "UTF-8"))
                postData.append('=')
                postData.append(URLEncoder.encode(value, "UTF-8"))
            }
            val httpDataBytes = postData.toString().toByteArray(charset("UTF-8"))
            val url = URL(URLS.appapi_auth)

            val connection = url!!.openConnection() as HttpURLConnection
            connection.requestMethod = "POST"
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
            connection.setRequestProperty("Content-Length", httpDataBytes.size.toString())
            connection.connectTimeout = timeout
            connection.doOutput = true
            connection.outputStream.write(httpDataBytes)

            if (connection.responseCode != HttpURLConnection.HTTP_OK) return HTTP_DATA_STATUS.SERVER_ERROR

            val bufferedReader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
            val stringBuilder = StringBuilder()

            var str: String?
            while (null != bufferedReader.readLine().also { str = it }) {
                stringBuilder.append(str)
            }

            val jsonObject = JSONObject(stringBuilder.toString())

            if (jsonObject.getInt("status") == 0) return HTTP_DATA_STATUS.LOGIN_PASSWORD_ERROR

            val lang: String = if (jsonObject.getString("lang").isNotEmpty()) jsonObject.getString("lang").toUpperCase() else System.getProperty("user.language").toUpperCase()

            val databaseManagerLocal = DatabaseManagerLocal()
            var query: String = SQLITEPopularQuery.select_count_sip_user;
            val rs: ResultSet = databaseManagerLocal.select(query)

            if (rs.getInt("isset") == 0) {
                query = "INSERT INTO ${DATABASE_LOCAL.tables.sip_user} (uid, appuid, user_name, token, avatar, lang, password) " +
                        "VALUES ('${jsonObject.getString("uid")}', '$login', 'User', '${jsonObject.getString("token")}', 0, '$lang', '$password')"
                databaseManagerLocal.insert(query)
            } else {
                query = "UPDATE ${DATABASE_LOCAL.tables.sip_user} SET" +
                        "uid = '${jsonObject.getString("uid")}', appuid = '$login', token = '${jsonObject.getString("token")}', lang = '$lang'"
            }
            databaseManagerLocal.close()
            getAvatar(jsonObject.getString("uid"), true)
            getUserName(jsonObject.getString("uid"), jsonObject.getString("token"))


            return HTTP_DATA_STATUS.SERVER_DATA_OK
        } catch (e: IOException) {
            return  HTTP_DATA_STATUS.SERVER_ERROR
        }

    }

    fun getAvatar (uid: String) {
        val url = URL("${URLS.chat_avatar}$uid")

        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        if (connection.responseCode != HttpURLConnection.HTTP_OK) return

        val bufferedReader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))

        if (bufferedReader.read() < 100) return

        val bufferedImage: BufferedImage = ImageIO.read(url)
        val base64Avatar = ImgToBase64String.convert(bufferedImage, "jpg")

        val db = DatabaseManagerLocal()
        var query = "SELECT COUNT(uid) AS isset FROM ${DATABASE_LOCAL.tables.sip_user} LIMIT 1"
        val rs: ResultSet = db.select(query)

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

    private fun getAvatar (uid: String,  sipUserIsset: Boolean) {


        val url = URL("${URLS.chat_avatar}$uid")

        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"

        if (connection.responseCode != HttpURLConnection.HTTP_OK) return

        val bufferedReader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))

        if (bufferedReader.read() < 100) return

        val bufferedImage: BufferedImage = ImageIO.read(url)
        val base64Avatar = ImgToBase64String.convert(bufferedImage, "jpg")

        val db = DatabaseManagerLocal()
        if (sipUserIsset) {
            val query = "UPDATE ${DATABASE_LOCAL.tables.sip_user} SET avatar = '${base64Avatar}'"
            db.updaate(query)
        } else {
            val query = "INSERT INTO ${DATABASE_LOCAL.tables.sip_user} (avatar) VALUES('${base64Avatar}')"
            db.insert(query)
        }

        db.close()
    }



    fun getUserName (uid: String, token: String) {

        val url = URL("${URLS.doctor_name}?uid=${uid}&token=${token}")

        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"


        if (connection.responseCode != HttpURLConnection.HTTP_OK) return

        val bufferedReader = BufferedReader(InputStreamReader(connection.inputStream, "UTF-8"))
        val stringBuilder = StringBuilder()

        var str: String?
        while (null != bufferedReader.readLine().also { str = it }) {
            stringBuilder.append(str)
        }

        val jsonObject =  JSONObject (stringBuilder.toString())
        val userName = jsonObject.getJSONObject("info").getString("name") + " " + jsonObject.getJSONObject("info").getString("lastname")

        val db = DatabaseManagerLocal()
        val query = "UPDATE ${DATABASE_LOCAL.tables.sip_user} SET user_name = '$userName'"
        db.insert(query)
        db.close()

    }
}