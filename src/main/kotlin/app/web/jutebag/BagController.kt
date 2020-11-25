package app.web.jutebag

import app.web.jutebag.data.SaveRequest
import app.web.jutebag.data.ShoppingItem
import com.google.auth.oauth2.GoogleCredentials
import com.google.cloud.firestore.DocumentSnapshot
import com.google.cloud.firestore.Firestore
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.cloud.FirestoreClient
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.hateoas.JsonError
import io.micronaut.http.hateoas.Link
import org.slf4j.LoggerFactory
import java.lang.RuntimeException


/**
 * referenced by the firebase instance as "/bag/xyz", thus we use "/bag" as controller URL.
 * "good to know :D"
 */
@Controller("/bag")
class BagController {

    val LOG = LoggerFactory.getLogger("TestController")
    val db: Firestore

    constructor() {
        val credentials = GoogleCredentials.getApplicationDefault()
        val options = FirebaseOptions.Builder()
                .setCredentials(credentials)
                .setProjectId("jutebag")
                .build()
        FirebaseApp.initializeApp(options)
        db = FirestoreClient.getFirestore()
    }

    @Post("/saveBag")
    @Produces(MediaType.APPLICATION_JSON)
    fun saveBag(@Body req: SaveRequest) : SimpleReply {
        val bagId = getBagId(req.email)
        val coll = db.collection("bags").document(bagId).collection("items");
        coll.listDocuments().forEach { docRef ->  docRef?.delete() }
        req.items.forEach { item -> coll.document(item.name).set(item) }
        return SimpleReply(req.email, "${req.items.size} items stored for ${req.email}")
    }

    fun getBagId(userEmail : String): String {
        val docRef = db.collection("users").document(userEmail)
        var bagId: String? = docRef.get().get().get("bagId") as String?
        if (bagId == null) { // create bag and associate bag with user
            bagId = db.collection("bags").document().id;
            docRef.set(mapOf("bagId" to bagId)).get()
        }
        return bagId
    }

    @Get("/loadBag")
    fun loadBag(user: String) : List<ShoppingItem> {
        val bagId = getBagId(user)
        val coll = db.collection("bags").document(bagId).collection("items");
        return coll.listDocuments()
                .map { r -> r.get().get() }
                .filter { r -> r.exists() }
                .mapNotNull { r -> toItem(r) }
    }


    @Get("/storage")
    @Produces(MediaType.TEXT_PLAIN)
    fun test_firebase_storage(): String {
        try {
            val docRef = db.collection("users").document("moritz.maus@hm10.net")
            val userDoc = docRef.get().get();
            var bagId: String? = docRef.get().get().get("bagId") as String?
            if (bagId == null) {
                bagId = db.collection("bags").document().id;
                docRef.set(mapOf("bagId" to bagId)).get()
            }
            val coll = db.collection("bags").document(bagId).collection("items");
            val defaultProps = mapOf("qty" to 1, "category" to "any")
            val itemA = ShoppingItem(1, "Bier", 2, "Drinks")
            val itemB = ShoppingItem(2, "Chips", 12, "Snacks")
            coll.document(itemA.name).set(itemA)
            coll.document(itemB.name).set(itemB)
            val writeResult = coll.document("beer").set(defaultProps).get()
            return "Updated data in ${writeResult.updateTime}"

        } catch (e: RuntimeException) {
            return "OOPS: $e"
        }
    }

    @Get("/storedItems")
    fun read_stored_items(user: String) : List<ShoppingItem> {
        try {
            val docRef = db.collection("users").document(user)
            var bagId: String? = docRef.get().get().get("bagId") as String?
            if (bagId == null) {
                bagId = db.collection("bags").document().id;
                docRef.set(mapOf("bagId" to bagId)).get()
            }
            val coll = db.collection("bags").document(bagId).collection("items");
            return coll.listDocuments().map { r -> r.get().get() }.filter { r -> r.exists() }
                    .map { r -> toItem(r) }.filterNotNull()
        } catch (e: RuntimeException) {
            return listOf()
        }
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

    @Get("/someItems")
    fun return_some_items() : List<ShoppingItem> {
        return listOf(ShoppingItem(1, "Bier", 2, "Drinks"),
          ShoppingItem(2, "Chips", 2, "Snacks"),
        ShoppingItem(3, "Smarties", 2, "Snacks"))
    }



    @Get("/auth")
    @Produces(MediaType.TEXT_PLAIN)
    fun checkHeader(@Header("Authorization") auth: String, @Header("Cookie") cookie: String): String {
        return "Auth : $auth, Cookie = $cookie"
    }


    @Get("/version")
    @Produces(MediaType.TEXT_PLAIN)
    fun debugVersion(): String {
        return "0.1.1"
    }

    @Get("/store")
    @Produces(MediaType.TEXT_PLAIN)
    fun readData(): String {
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
                val res: java.util.List<String> = document.get("items") as java.util.List<String>
                val kotlinList = mutableListOf<String>()
                for (item in res) {
                    kotlinList.add(item)
                }
                val resClass = res!!::class
                result.append("  Clazz is: $resClass")
                result.append("  In kotlin: $kotlinList")
            }
        } catch (e: RuntimeException) {
            return "An error occurred: " + e
        }
        return result.toString();
    }

    fun addData() {
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
    fun baseDir(): List<String> {
        LOG.warn("MAIN PAGE REQUESTED")
        return listOf("nix")
    }

    @Error(status = HttpStatus.NOT_FOUND, global = true)
    fun notFound(request: HttpRequest<Any>): HttpResponse<JsonError> {
        LOG.error("PAGE NOT FOUND!!")
        val error = JsonError("Dude, something went wrong").link(Link.SELF, Link.of(request.uri))
        return HttpResponse.notFound(error);
    }


    @Get("/hello")
    @Produces(MediaType.TEXT_PLAIN)
    fun greet(you: String): String {
        return "Hello, $you"
    }

    @Get("/ready")
    @Produces(MediaType.TEXT_PLAIN)
    fun name(): String {
        return "NOT IMPLEMENTED"
    }

    @Get("/json")
    fun someJson(): List<String> {
        return listOf("a", "b", "c");
    }

}

data class SimpleReply(val user: String, val msg: String)
