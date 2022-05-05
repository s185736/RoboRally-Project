package dk.dtu.compute.se.pisd.roborally.model;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * ...
 *
 * @author
 * Converting to JSON file from FieldAction.
 */
public class InterfaceAdapter<A> implements JsonSerializer<A>, JsonDeserializer<A> {

    /**
     * @param json
     * @param typeOfT
     * @param context
     * @return
     * @throws JsonParseException
     */
    public A deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject object = json.getAsJsonObject();
        JsonPrimitive primitive = (JsonPrimitive) object.get("CLASS_DIRECTORY");
        String className = primitive.getAsString();
        Class<?> classs = null;
        try {
            classs = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return context.deserialize(object.get("DETAIL"), classs);
    }

    /**
     * @param a
     * @param type
     * @param jsonSerializationContext
     * @return
     */
    @Override
    public JsonElement serialize(A a, Type type, JsonSerializationContext jsonSerializationContext) {
        return null;
    }
}