package app.web.jutebag

import app.web.jutebag.data.SaveRequest
import app.web.jutebag.data.ShoppingItem
import com.google.cloud.firestore.DocumentSnapshot
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import org.slf4j.LoggerFactory
import java.lang.RuntimeException


/**
 * referenced by the firebase instance as "/bag/xyz", thus we use "/bag" as controller URL.
 * "good to know :D"
 */
@Controller("/dev")
class DevController {

    val LOG = LoggerFactory.getLogger("TestController")


    @Post("/saveBag")
    @Produces(MediaType.APPLICATION_JSON)
    fun saveBag(@Body req: SaveRequest) : String {
        LOG.info("Received save request: $req")
        return "alles ok"
    }


    fun toItem(r: DocumentSnapshot): ShoppingItem? {
        try {
            return ShoppingItem(r.getLong("id")!!,
                    r.getString("name")!!,
                    r.getLong("qty") ?: 1,
                    r.getString("category")!!,
                    r.getBoolean("stored") ?: false)
        } catch (ex: RuntimeException) {
            return null;
        }
    }




    @Get("/auth")
    @Produces(MediaType.TEXT_PLAIN)
    fun checkHeader(@Header("Authorization") auth: String, @Header("Cookie") cookie: String): String {
        return "Auth : $auth, Cookie = $cookie"
    }


    @Get("/version")
    @Produces(MediaType.TEXT_PLAIN)
    fun debugVersion(): String {
        return "0.1"
    }


}

