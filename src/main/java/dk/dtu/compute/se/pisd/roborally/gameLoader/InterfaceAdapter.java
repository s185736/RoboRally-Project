/*
 *  This file is part of the initial project provided for the
 *  course "Project in Software Development (02362)" held at
 *  DTU Compute at the Technical University of Denmark.
 *
 *  Copyright (C) 2019, 2020: Ekkart Kindler, ekki@dtu.dk
 *
 *  This software is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; version 2 of the License.
 *
 *  This project is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this project; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 */
package dk.dtu.compute.se.pisd.roborally.gameLoader;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * A generic type adapter for Gson, which deals with structures, where
 * a statically typed element can have dynamic sub-types. The solution is a
 * simple adaptation of <a href="https://github.com/mperdikeas/json-polymorphism"
 * >https://github.com/mperdikeas/json-polymorphism</a>, which can be used
 * in a generic way. The type parameter <code>E</code> refers to the top of
 * the class hierarchy resp. to the static type, which is dynamically sub-typed
 * in the structure. Note that this solution does not work if instances of
 * E itself need to be serialized (typically E would be abstract).
 *
 * @author Menelaos Perdikeas, https://github.com/mperdikeas
 * @author Ekkart Kindler, ekki@dtu.dk
 *
 * @param <A> The top of the class hierarchy
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