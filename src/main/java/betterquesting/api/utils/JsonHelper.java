package betterquesting.api.utils;

import betterquesting.api.api.QuestingAPI;
import betterquesting.api.placeholders.ItemPlaceholder;
import betterquesting.api.placeholders.PlaceholderConverter;
import betterquesting.api2.utils.BQThreadedIO;
import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nonnull;
import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.concurrent.Future;
import java.util.HashSet;
import java.util.Set;

/**
 * Used to read JSON data with pre-made checks for null entries and casting.
 * Missing entries will return a default/blank value instead of null without
 * editing the parent JSON.<br>
 * In the event the requested item, fluid or entity is missing, a place holder will be substituted
 */
public class JsonHelper {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static JsonArray GetArray(@Nonnull JsonObject json, @Nonnull String id) {
        if (json.get(id) instanceof JsonArray) {
            return json.get(id).getAsJsonArray();
        } else {
            return new JsonArray();
        }
    }

    public static JsonObject GetObject(@Nonnull JsonObject json, @Nonnull String id) {
        if (json.get(id) instanceof JsonObject) {
            return json.get(id).getAsJsonObject();
        } else {
            return new JsonObject();
        }
    }

    public static String GetString(@Nonnull JsonObject json, @Nonnull String id, String def) {
        if (json.get(id) instanceof JsonPrimitive && json.get(id).getAsJsonPrimitive().isString()) {
            return json.get(id).getAsString();
        } else {
            return def;
        }
    }

    public static Number GetNumber(@Nonnull JsonObject json, @Nonnull String id, Number def) {
        if (json.get(id) instanceof JsonPrimitive) {
            try {
                return json.get(id).getAsNumber();
            } catch (Exception e) {
                return def;
            }
        } else {
            return def;
        }
    }

    public static boolean GetBoolean(@Nonnull JsonObject json, @Nonnull String id, boolean def) {
        if (json.get(id) instanceof JsonPrimitive) {
            try {
                return json.get(id).getAsBoolean();
            } catch (Exception e) {
                return def;
            }
        } else {
            return def;
        }
    }

    @SuppressWarnings("unchecked")
    public static ArrayList<JsonElement> GetUnderlyingArray(JsonArray array) {
        try {
            Field field = JsonArray.class.getDeclaredField("elements");
            field.setAccessible(true);

            return (ArrayList<JsonElement>) field.get(array);
        } catch (Exception e) {
            QuestingAPI.getLogger().log(org.apache.logging.log4j.Level.ERROR, "Unable to retrieve underlying JsonArray:", e);
        }

        return null;
    }

    public static void ClearCompoundTag(@Nonnull CompoundTag tag) {
        ArrayList<String> list = new ArrayList<>(tag.getAllKeys());
        list.forEach(tag::remove);
    }

    public static JsonObject ReadFromFile(File file) {
        if (file == null || !file.exists()) return new JsonObject();

        Future<JsonObject> task = BQThreadedIO.INSTANCE.enqueue(() -> {
            // NOTE: These are now split due to an edge case in the previous implementation where resource leaking can occur should the outer constructor fail
            try (FileInputStream fis = new FileInputStream(file); InputStreamReader fr = new InputStreamReader(fis, StandardCharsets.UTF_8)) {
                JsonObject json = GSON.fromJson(fr, JsonObject.class);
                fr.close();
                return json;
            } catch (Exception e) {
                QuestingAPI.getLogger().log(org.apache.logging.log4j.Level.ERROR, "An error occured while loading JSON from file:", e);

                int i = 0;
                File bkup = new File(file.getParent(), "malformed_" + file.getName() + i + ".json");

                while (bkup.exists()) {
                    bkup = new File(file.getParent(), "malformed_" + file.getName() + (++i) + ".json");
                }

                QuestingAPI.getLogger().log(org.apache.logging.log4j.Level.ERROR, "Creating backup at: " + bkup.getAbsolutePath());
                CopyPaste(file, bkup);

                return new JsonObject(); // Just a safety measure against NPEs
            }
        });

        try {
            return task.get(); // Wait for other scheduled file ops to finish
        } catch (Exception e) {
            QuestingAPI.getLogger().error("Unable to read from file " + file, e);
            return new JsonObject();
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static Future<Void> WriteToFile(File file, JsonObject jObj) {
        final File tmp = new File(file.getAbsolutePath() + ".tmp");

        return BQThreadedIO.DISK_IO.enqueue(() -> {
            try {
                if (tmp.exists()) {
                    tmp.delete();
                } else if (tmp.getParentFile() != null) {
                    tmp.getParentFile().mkdirs();
                }

                tmp.createNewFile();
            } catch (Exception e) {
                QuestingAPI.getLogger().error("An error occured while saving JSON to file (Directory setup):", e);
                return null;
            }

            // NOTE: These are now split due to an edge case in the previous implementation where resource leaking can occur should the outer constructor fail
            try (FileOutputStream fos = new FileOutputStream(tmp);
                 OutputStreamWriter fw = new OutputStreamWriter(fos, StandardCharsets.UTF_8);
                 Writer buffer = new BufferedWriter(fw);
                 JsonWriter json = new JsonWriter(buffer)) {
                json.setIndent("  "); // two space indents
                GSON.toJson(jObj, json);
            } catch (Exception e) {
                QuestingAPI.getLogger().error("An error occurred while saving JSON to file (File write):", e);
                return null;
            }

            // NOTE: These are now split due to an edge case in the previous implementation where resource leaking can occur should the outer constructor fail
            try (FileInputStream fis = new FileInputStream(tmp); InputStreamReader fr = new InputStreamReader(fis, StandardCharsets.UTF_8)) {
                // Readback what we wrote to validate it
                GSON.fromJson(fr, JsonObject.class);
            } catch (Exception e) {
                QuestingAPI.getLogger().error("An error occured while saving JSON to file (Validation check):", e);
                return null;
            }

            try {
                if (file.exists()) file.delete();
                tmp.renameTo(file);
            } catch (Exception e) {
                QuestingAPI.getLogger().error("An error occured while saving JSON to file (Temp copy):", e);
            }
            return null;
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static Future<Void> WriteToFile(File file, IOConsumer<JsonWriter> jObj) {
        final File tmp = new File(file.getAbsolutePath() + ".tmp");

        return BQThreadedIO.DISK_IO.enqueue(() -> {
            try {
                if (tmp.exists())
                    tmp.delete();
                else if (tmp.getParentFile() != null)
                    tmp.getParentFile().mkdirs();

                tmp.createNewFile();
            } catch (Exception e) {
                QuestingAPI.getLogger().error("An error occured while saving JSON to file (Directory setup):", e);
                return null;
            }

            // NOTE: These are now split due to an edge case in the previous implementation where resource leaking can occur should the outer constructor fail
            try (FileOutputStream fos = new FileOutputStream(tmp); OutputStreamWriter fw = new OutputStreamWriter(fos, StandardCharsets.UTF_8)) {
                jObj.accept(new JsonWriter(fw));
            } catch (Exception e) {
                QuestingAPI.getLogger().error("An error occured while saving JSON to file (File write):", e);
                return null;
            }

            try {
                Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException ignored) {
                try {
                    Files.move(tmp.toPath(), file.toPath(), StandardCopyOption.REPLACE_EXISTING);
                } catch (Exception e) {
                    QuestingAPI.getLogger().error("An error occured while saving JSON to file (Temp copy):", e);
                }
            } catch (Exception e) {
                QuestingAPI.getLogger().error("An error occured while saving JSON to file (Temp copy):", e);
            }
            return null;
        });
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static void CopyPaste(File fileIn, File fileOut) {
        if (!fileIn.exists()) return;

        try {
            if (fileOut.getParentFile() != null) fileOut.getParentFile().mkdirs();
            Files.copy(fileIn.toPath(), fileOut.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            QuestingAPI.getLogger().log(org.apache.logging.log4j.Level.ERROR, "Failed copy paste", e);
        }
    }

    public static String makeFileNameSafe(String s) {
        // Define illegal file characters manually
        char[] illegalFileCharacters = { '/', '\n', '\r', '\t', '\0', '\f', '?', '*', '\\', '<', '>', '|', '\"', ':' };

        for (char c : illegalFileCharacters) {
            s = s.replace(c, '_');
        }

        return s;
    }

    public static boolean isItem(CompoundTag json) {
        if (json != null && json.contains("id") && json.contains("Count", CompoundTag.TAG_ANY_NUMERIC) && json.contains("Damage", CompoundTag.TAG_ANY_NUMERIC)) {
            if (json.contains("id", CompoundTag.TAG_STRING)) {
                return ForgeRegistries.ITEMS.containsKey(new ResourceLocation(json.getString("id")));
            } else {
                return ForgeRegistries.ITEMS.getValue(new ResourceLocation(String.valueOf(json.getInt("id")))) != null;
            }
        }

        return false;
    }

    public static boolean isFluid(CompoundTag json) {
        if (json != null && json.contains("FluidName", CompoundTag.TAG_STRING) && json.contains("Amount", CompoundTag.TAG_ANY_NUMERIC)) {
            String fluidName = json.getString("FluidName");
            ResourceLocation fluidLocation = new ResourceLocation(fluidName);
            return ForgeRegistries.FLUIDS.containsKey(fluidLocation);
        }
        return false;
    }

    public static boolean isEntity(CompoundTag tags) {
        return tags.contains("id") && ForgeRegistries.ENTITY_TYPES.containsKey(new ResourceLocation(tags.getString("id")));
    }

    /**
     * Converts a JsonObject to an ItemStack. May return a placeholder if the correct mods are not installed</br>
     * This should be the standard way to load items into quests in order to retain all potential data
     */
    public static BigItemStack JsonToItemStack(CompoundTag nbt) {
        Item preCheck = ForgeRegistries.ITEMS.getValue(new ResourceLocation(nbt.getString("id")));
        if (preCheck != null && preCheck != ItemPlaceholder.placeholder) return new BigItemStack(nbt);
        return PlaceholderConverter.convertItem(preCheck, nbt.getString("id"), nbt.getInt("Count"), nbt.getShort("Damage"), nbt.getString("OreDict"), !nbt.contains("tag", CompoundTag.TAG_COMPOUND) ? null : nbt.getCompound("tag"));
    }

    /**
     * Use this for quests instead of converter NBT because this doesn't use ID numbers
     */
    public static CompoundTag ItemStackToJson(BigItemStack stack, CompoundTag nbt) {
        if (stack != null) stack.writeToNBT(nbt);
        return nbt;
    }

    public static FluidStack JsonToFluidStack(CompoundTag json) {
        String name = json.contains("FluidName", CompoundTag.TAG_STRING) ? json.getString("FluidName") : "water";
        int amount = json.getInt("Amount");
        CompoundTag tags = !json.contains("Tag", CompoundTag.TAG_COMPOUND) ? null : json.getCompound("Tag");
        Fluid fluid = Fluids.WATER; // Placeholder, as FluidRegistry does not exist anymore

        return PlaceholderConverter.convertFluid(fluid, name, amount, tags);
    }

    public static CompoundTag FluidStackToJson(FluidStack stack, CompoundTag json) {
        if (stack == null) return json;
        ResourceLocation fluidKey = ForgeRegistries.FLUIDS.getKey(stack.getFluid());
        if (fluidKey != null) {
            json.putString("FluidName", fluidKey.toString());
        }
        json.putInt("Amount", stack.getAmount());
        if (stack.getTag() != null) json.put("Tag", stack.getTag());
        return json;
    }

    public static Entity JsonToEntity(CompoundTag tags, Level world) {
        Entity entity = null;

        if (tags.contains("id") && ForgeRegistries.ENTITY_TYPES.containsKey(new ResourceLocation(tags.getString("id")))) {
            entity = EntityType.loadEntityRecursive(tags, world, e -> e);
        }

        return PlaceholderConverter.convertEntity(entity, world, tags);
    }

    public static CompoundTag EntityToJson(Entity entity, CompoundTag json) {
        if (entity == null) {
            return json;
        }

        CompoundTag tags = new CompoundTag();
        entity.save(tags);
        String id = EntityType.getKey(entity.getType()).toString();
        tags.putString("id", id != null ? id : ""); // Some entities don't write this to file in certain cases
        json.merge(tags);
        return json;
    }

    @FunctionalInterface
    public interface IOConsumer<T> {
        void accept(T arg) throws IOException;
    }
}
