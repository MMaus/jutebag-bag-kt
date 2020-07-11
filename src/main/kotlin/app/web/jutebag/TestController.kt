package app.web.jutebag

import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Error
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Produces
import io.micronaut.http.hateoas.JsonError
import io.micronaut.http.hateoas.Link
import org.slf4j.LoggerFactory
import java.lang.RuntimeException


/**
 * referenced by the firebase instance as "/bag/xyz", thus we use "/bag" as controller URL.
 * "good to know :D"
 */
@Controller("/bag")
class TestController {

    val LOG = LoggerFactory.getLogger("TestController")
    val db : Firestore

    constructor() {
        val credentials = GoogleCredentials.getApplicationDefault()
        val options = FirebaseOptions.Builder()
                .setCredentials(credentials)
                .setProjectId("jutebag")
                .build()
        FirebaseApp.initializeApp(options)
        db = FirestoreClient.getFirestore()
    }

    @Get("/version")
    @Produces(MediaType.TEXT_PLAIN)
    fun debugVersion() : String {
        return "0.1"
    }

    @Get("/store")
    @Produces(MediaType.TEXT_PLAIN)
    fun readData() : String {
        // tutorial content
        // asynchronously retrieve all users
        var result = StringBuilder()
        result.append("Items in bag: ")
        try {
            val query = db.collection("bags").get()
// ...
// query.get() blocks on response
// ...
// query.get() blocks on response
            val querySnapshot = query.get()
            val documents = querySnapshot.documents
            for (document in documents) {
                result.append("Document : ${document.id}")
                result.append("  Items: ${document.get("items", List::class.java)}")
            }
        } catch (e : RuntimeException) {
            return "An error occurred: " + e
        }
        return result.toString();
    }

    fun addData(){
        // tutorial content: adding data
//        val docRef = db.collection("users").document("alovelace")
//// Add document data  with id "alovelace" using a hashmap
//// Add document data  with id "alovelace" using a hashmap
//        val data: MutableMap<String, Any> = HashMap()
//        data["first"] = "Ada"
//        data["last"] = "Lovelace"
//        data["born"] = 1815
////asynchronously write data
////asynchronously write data
//        val result: ApiFuture<WriteResult> = docRef.set(data)
//// ...
//// result.get() blocks on response
//// ...
//// result.get() blocks on response
//        System.out.println("Update time : " + result.get().getUpdateTime())

        // tutorial: adding another document
//        val docRef = db.collection("users").document("aturing")
//// Add document data with an additional field ("middle")
//// Add document data with an additional field ("middle")
//        val data: MutableMap<String, Any> = HashMap()
//        data["first"] = "Alan"
//        data["middle"] = "Mathison"
//        data["last"] = "Turing"
//        data["born"] = 1912
//
//        val result: ApiFuture<WriteResult> = docRef.set(data)
//        System.out.println("Update time : " + result.get().getUpdateTime())
    }

    @Get("/")
    fun baseDir() : List<String> {
        LOG.warn("MAIN PAGE REQUESTED")
        return listOf("nix")
    }

    @Error(status = HttpStatus.NOT_FOUND, global = true)
    fun notFound(request : HttpRequest<Any>) : HttpResponse<JsonError> {
        LOG.error("PAGE NOT FOUND!!")
        val error = JsonError("Dude, something went wrong").link(Link.SELF, Link.of(request.uri))
        return HttpResponse.notFound(error);
    }


    @Get("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    fun greet(you: String) : String {
        return "Hello, $you"
    }

    @Get("/ready")
    @Produces(MediaType.TEXT_PLAIN)
    fun name() : String {
        return "NOT IMPLEMENTED"
    }

    @Get("/json")
    fun someJson() : List<String> {
        return listOf("a", "b", "c");
    }

}

