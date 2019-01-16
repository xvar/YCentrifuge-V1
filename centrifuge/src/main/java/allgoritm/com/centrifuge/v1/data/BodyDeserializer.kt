package allgoritm.com.centrifuge.v1.data

import com.google.gson.JsonDeserializationContext
import com.google.gson.JsonDeserializer
import com.google.gson.JsonElement
import org.json.JSONObject
import java.lang.reflect.Type

class BodyDeserializer : JsonDeserializer<Body> {

    override fun deserialize(json: JsonElement, typeOfT: Type?, context: JsonDeserializationContext): Body {
        val bodyObj = json.asJsonObject
        return Body(JSONObject(bodyObj.toString()))
    }

}
