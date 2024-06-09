package betterquesting.api2.utils;

import net.minecraft.client.resources.language.I18n;

public class QuestTranslation {
    public static String translate(String text, Object... args) {
        if (!I18n.exists(text)) {
            return text;
        }

        return I18n.get(text, args);
    }

    public static String translateTrimmed(String text, Object... args) {
        return translate(text, args).replaceAll("\r", "");
    }
}
