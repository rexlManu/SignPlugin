/*
 * © Copyright - Emmanuel Lampe aka. rexlManu 2019.
 */
package me.rexlmanu.signplugin.utility;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.Objects;

public final class TagUtils {

    public static void setBooleanMeta(final Player player, final int slot, final String key, final boolean value) {
        try {
            final Object itemFromSlot = getItemFromSlot(player, slot);
            Object tag = getTagFromItem(itemFromSlot);
            if (Objects.isNull(tag)) {
                final Object nbtTagCompound = getNMSClass("NBTTagCompound").getConstructor().newInstance();
                Objects.requireNonNull(itemFromSlot).getClass().getMethod("setTag", nbtTagCompound.getClass())
                        .invoke(itemFromSlot, nbtTagCompound);
                tag = getTagFromItem(itemFromSlot);
            }
            final Method method = Objects.requireNonNull(tag).getClass().getMethod("setBoolean", String.class, boolean.class);
            method.invoke(tag, key, value);
        } catch (final Exception e) {
            e.printStackTrace();
        }
    }

    public static boolean getBooleanMeta(final Player player, final int slot, final String key) {
        try {
            final Object tag = getTagFromItem(getItemFromSlot(player, slot));
            final Method method = Objects.requireNonNull(tag).getClass().getMethod("getBoolean", String.class);
            return (boolean) method.invoke(tag, key);
        } catch (final Exception e) {
            return false;
        }
    }

    private static Object getTagFromItem(final Object item) {
        try {
            return Objects.requireNonNull(item).getClass().getMethod("getTag").invoke(item);
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Object getItemFromSlot(final Player player, final int slot) {
        try {
            final Method getHandle = player.getClass().getMethod("getHandle");
            final Object nmsPlayer = getHandle.invoke(player);
            final Object inventory = nmsPlayer.getClass().getField("inventory").get(nmsPlayer);
            return inventory.getClass().getMethod("getItem", int.class).invoke(inventory, slot);
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static Class<?> getNMSClass(final String nmsClassString) throws ClassNotFoundException {
        final String version = Bukkit.getServer().getClass().getPackage().getName().replace(".", ",").split(",")[3] + ".";
        final String name = "net.minecraft.server." + version + nmsClassString;
        return Class.forName(name);
    }


}
