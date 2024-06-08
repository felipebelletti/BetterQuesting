package betterquesting.api.utils;

import betterquesting.api.api.QuestingAPI;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.stream.JsonWriter;
import net.minecraft.nbt.*;
import org.apache.logging.log4j.Level;

import java.io.IOException;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

public class NBTConverter {
    /**
     * Convert NBT tags to a JSON object
     */
    private static void NBTtoJSON_Base(Tag value, boolean format, JsonWriter out) throws IOException {
        if (value == null || value.getId() == 0) out.beginObject().endObject();
        else if (value instanceof NumericTag) out.value(NBTConverter.getNumber(value));
        else if (value instanceof StringTag) out.value(((StringTag) value).getAsString());
        else if (value instanceof ByteArrayTag) {
            out.beginArray();
            for (byte b : ((ByteArrayTag) value).getAsByteArray()) {
                out.value(b);
            }
            out.endArray();
        } else if (value instanceof IntArrayTag) {
            out.beginArray();
            for (int b : ((IntArrayTag) value).getAsIntArray()) {
                out.value(b);
            }
            out.endArray();
        } else if (value instanceof LongArrayTag) {
            out.beginArray();
            for (long l : ((LongArrayTag) value).getAsLongArray()) {
                out.value(l);
            }
            out.endArray();
        } else if (value instanceof ListTag) {
            ListTag tagList = (ListTag) value;
            if (format) {
                out.beginObject();
                for (int i = 0; i < tagList.size(); i++) {
                    Tag tag = tagList.get(i);
                    out.name(i + ":" + tag.getId());
                    NBTtoJSON_Base(tag, true, out);
                }
                out.endObject();
            } else {
                out.beginArray();
                for (Tag tag : tagList) {
                    NBTtoJSON_Base(tag, false, out);
                }
                out.endArray();
            }
        } else if (value instanceof CompoundTag) {
            NBTtoJSON_Compound((CompoundTag) value, out, format);
        } else {
            // idk man what is this
            out.beginObject().endObject();
        }
    }

    public static void NBTtoJSON_Compound(CompoundTag parent, JsonWriter out, boolean format) throws IOException {
        out.beginObject();

        if (parent != null)
            for (String key : parent.getAllKeys()) {
                Tag tag = parent.get(key);

                if (format) {
                    out.name(key + ":" + tag.getId());
                    NBTtoJSON_Base(tag, true, out);
                } else {
                    out.name(key);
                    NBTtoJSON_Base(tag, false, out);
                }
            }
        out.endObject();
    }

    /**
     * Convert NBT tags to a JSON object
     */
    private static JsonElement NBTtoJSON_Base(Tag tag, boolean format) {
        if (tag == null) {
            return new JsonObject();
        }

        if (tag.getId() >= 1 && tag.getId() <= 6) {
            return new JsonPrimitive(getNumber(tag));
        }
        if (tag instanceof StringTag) {
            return new JsonPrimitive(((StringTag) tag).getAsString());
        } else if (tag instanceof CompoundTag) {
            return NBTtoJSON_Compound((CompoundTag) tag, new JsonObject(), format);
        } else if (tag instanceof ListTag) {
            if (format) {
                JsonObject jAry = new JsonObject();

                ListTag tagList = (ListTag) tag;

                for (int i = 0; i < tagList.size(); i++) {
                    jAry.add(i + ":" + tagList.get(i).getId(), NBTtoJSON_Base(tagList.get(i), true));
                }

                return jAry;
            } else {
                JsonArray jAry = new JsonArray();

                ListTag tagList = (ListTag) tag;

                for (Tag t : tagList) {
                    jAry.add(NBTtoJSON_Base(t, false));
                }

                return jAry;
            }
        } else if (tag instanceof ByteArrayTag) {
            JsonArray jAry = new JsonArray();

            for (byte b : ((ByteArrayTag) tag).getAsByteArray()) {
                jAry.add(new JsonPrimitive(b));
            }

            return jAry;
        } else if (tag instanceof IntArrayTag) {
            JsonArray jAry = new JsonArray();

            for (int i : ((IntArrayTag) tag).getAsIntArray()) {
                jAry.add(new JsonPrimitive(i));
            }

            return jAry;
        } else if (tag instanceof LongArrayTag) {
            JsonArray jAry = new JsonArray();

            for (long l : ((LongArrayTag) tag).getAsLongArray()) {
                jAry.add(new JsonPrimitive(l));
            }

            return jAry;
        } else {
            return new JsonObject(); // No valid types found. We'll just return this to prevent a NPE
        }
    }

    // The fact that this is necessary is so dumb
    @SuppressWarnings("WeakerAccess")
    public static long[] readLongArray(LongArrayTag tag) {
        if (tag == null) return new long[0];

        String[] entry = tag.toString().replaceAll("[\\[\\]L;]", "").split(","); // Cut off square braces and "L;" before splitting elements
        final long[] ary = new long[entry.length];
        for (int i = 0; i < entry.length; i++) {
            try {
                ary[i] = Long.parseLong(entry[i]);
            } catch (Exception ignored) {
            }
        }

        return ary;
    }

    public static JsonObject NBTtoJSON_Compound(CompoundTag parent, JsonObject jObj, boolean format) {
        if (parent == null) {
            return jObj;
        }

        Set<String> keySet = new TreeSet<>(parent.getAllKeys());
        for (String key : keySet) {
            Tag tag = parent.get(key);

            if (format) {
                jObj.add(key + ":" + tag.getId(), NBTtoJSON_Base(tag, true));
            } else {
                jObj.add(key, NBTtoJSON_Base(tag, false));
            }
        }

        return jObj;
    }

    /**
     * Convert JsonObject to a CompoundTag
     */
    public static CompoundTag JSONtoNBT_Object(JsonObject jObj, CompoundTag tags, boolean format) {
        if (jObj == null) {
            return tags;
        }

        for (Entry<String, JsonElement> entry : jObj.entrySet()) {
            String key = entry.getKey();

            if (!format) {
                tags.put(key, JSONtoNBT_Element(entry.getValue(), (byte) 0, false));
            } else {
                String[] s = key.split(":");
                byte id = 0;

                try {
                    id = Byte.parseByte(s[s.length - 1]);
                    key = key.substring(0, key.lastIndexOf(":" + id));
                } catch (Exception e) {
                    if (tags.contains(key)) {
                        QuestingAPI.getLogger().log(Level.WARN, "JSON/NBT formatting conflict on key '" + key + "'. Skipping...");
                        continue;
                    }
                }

                tags.put(key, JSONtoNBT_Element(entry.getValue(), id, true));
            }
        }

        return tags;
    }

    /**
     * Tries to interpret the tagID from the JsonElement's contents
     */
    private static Tag JSONtoNBT_Element(JsonElement jObj, byte id, boolean format) {
        if (jObj == null) {
            return StringTag.valueOf("");
        }

        byte tagID = id <= 0 ? fallbackTagID(jObj) : id;

        try {
            if (tagID == 1 && (id <= 0 || jObj.getAsJsonPrimitive().isBoolean())) // Edge case for BQ2 legacy files
            {
                return ByteTag.valueOf(jObj.getAsBoolean() ? (byte) 1 : (byte) 0);
            } else if (tagID >= 1 && tagID <= 6) {
                return instanceNumber(jObj.getAsNumber(), tagID);
            } else if (tagID == 8) {
                return StringTag.valueOf(jObj.getAsString());
            } else if (tagID == 10) {
                return JSONtoNBT_Object(jObj.getAsJsonObject(), new CompoundTag(), format);
            } else if (tagID == 7) // Byte array
            {
                JsonArray jAry = jObj.getAsJsonArray();

                byte[] bAry = new byte[jAry.size()];

                for (int i = 0; i < jAry.size(); i++) {
                    bAry[i] = jAry.get(i).getAsByte();
                }

                return new ByteArrayTag(bAry);
            } else if (tagID == 11) {
                JsonArray jAry = jObj.getAsJsonArray();

                int[] iAry = new int[jAry.size()];

                for (int i = 0; i < jAry.size(); i++) {
                    iAry[i] = jAry.get(i).getAsInt();
                }

                return new IntArrayTag(iAry);
            } else if (tagID == 12) {
                JsonArray jAry = jObj.getAsJsonArray();

                long[] lAry = new long[jAry.size()];

                for (int i = 0; i < jAry.size(); i++) {
                    lAry[i] = jAry.get(i).getAsLong();
                }

                return new LongArrayTag(lAry);
            } else if (tagID == 9) {
                ListTag tList = new ListTag();

                if (jObj.isJsonArray()) {
                    JsonArray jAry = jObj.getAsJsonArray();

                    for (int i = 0; i < jAry.size(); i++) {
                        JsonElement jElm = jAry.get(i);
                        tList.add(JSONtoNBT_Element(jElm, (byte) 0, format));
                    }
                } else if (jObj.isJsonObject()) {
                    JsonObject jAry = jObj.getAsJsonObject();

                    for (Entry<String, JsonElement> entry : jAry.entrySet()) {
                        try {
                            String[] s = entry.getKey().split(":");
                            byte id2 = Byte.parseByte(s[s.length - 1]);
                            //String key = entry.getKey().substring(0, entry.getKey().lastIndexOf(":" + id));
                            tList.add(JSONtoNBT_Element(entry.getValue(), id2, format));
                        } catch (Exception e) {
                            tList.add(JSONtoNBT_Element(entry.getValue(), (byte) 0, format));
                        }
                    }
                }

                return tList;
            }
        } catch (Exception e) {
            QuestingAPI.getLogger().log(Level.ERROR, "An error occured while parsing JsonElement to Tag (" + tagID + "):", e);
        }

        QuestingAPI.getLogger().log(Level.WARN, "Unknown NBT representation for " + jObj.toString() + " (ID: " + tagID + ")");
        return StringTag.valueOf("");
    }

    @SuppressWarnings("WeakerAccess")
    public static Number getNumber(Tag tag) {
        if (tag instanceof ByteTag) {
            return ((ByteTag) tag).getAsByte();
        } else if (tag instanceof ShortTag) {
            return ((ShortTag) tag).getAsShort();
        } else if (tag instanceof IntTag) {
            return ((IntTag) tag).getAsInt();
        } else if (tag instanceof FloatTag) {
            return ((FloatTag) tag).getAsFloat();
        } else if (tag instanceof DoubleTag) {
            return ((DoubleTag) tag).getAsDouble();
        } else if (tag instanceof LongTag) {
            return ((LongTag) tag).getAsLong();
        } else {
            return 0;
        }
    }

    @SuppressWarnings("WeakerAccess")
    public static Tag instanceNumber(Number num, byte type) {
        switch (type) {
            case 1:
                return ByteTag.valueOf(num.byteValue());
            case 2:
                return ShortTag.valueOf(num.shortValue());
            case 3:
                return IntTag.valueOf(num.intValue());
            case 4:
                return LongTag.valueOf(num.longValue());
            case 5:
                return FloatTag.valueOf(num.floatValue());
            default:
                return DoubleTag.valueOf(num.doubleValue());
        }
    }

    private static byte fallbackTagID(JsonElement jObj) {
        byte tagID = 0;

        if (jObj.isJsonPrimitive()) {
            JsonPrimitive prim = jObj.getAsJsonPrimitive();

            if (prim.isNumber()) {
                if (prim.getAsString().contains(".")) // Just in case we'll choose the largest possible container supporting this number type (Long or Double)
                {
                    tagID = 6;
                } else {
                    tagID = 4;
                }
            } else if (prim.isBoolean()) {
                tagID = 1;
            } else {
                tagID = 8; // Non-number primitive. Assume string
            }
        } else if (jObj.isJsonArray()) {
            JsonArray array = jObj.getAsJsonArray();

            for (JsonElement entry : array) {
                if (entry.isJsonPrimitive() && tagID == 0) // Note: TagLists can only support Integers, Bytes and Compounds (Strings can be stored but require special handling)
                {
                    try {
                        for (JsonElement element : array) {
                            // Make sure all entries can be bytes
                            if (element.getAsLong() != element.getAsByte()) // In case casting works but overflows
                            {
                                throw new ClassCastException();
                            }
                        }
                        tagID = 7; // Can be used as byte
                    } catch (Exception e1) {
                        try {
                            for (JsonElement element : array) {
                                // Make sure all entries can be integers
                                if (element.getAsLong() != element.getAsInt()) // In case casting works but overflows
                                {
                                    throw new ClassCastException();
                                }
                            }
                            tagID = 11;
                        } catch (Exception e2) {
                            tagID = 9; // Is primitive however requires TagList interpretation
                        }
                    }
                } else if (!entry.isJsonPrimitive()) {
                    break;
                }
            }

            tagID = 9; // No data to judge format. Assuming tag list
        } else {
            tagID = 10;
        }

        return tagID;
    }
}
