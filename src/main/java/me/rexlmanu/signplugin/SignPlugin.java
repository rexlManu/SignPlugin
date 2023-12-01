/*
 * © Copyright - Emmanuel Lampe aka. rexlManu 2019.
 */
package me.rexlmanu.signplugin;

import com.google.common.collect.Lists;
import me.rexlmanu.signplugin.utility.TagUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public final class SignPlugin extends JavaPlugin {

    private SimpleDateFormat dateFormat, timeFormat;

    @Override
    public void onEnable() {
        this.getConfig().options().copyDefaults(true);
        this.saveDefaultConfig();
        this.reloadConfig();
        this.dateFormat = new SimpleDateFormat(this.getMessage("DateFormat"));
        this.timeFormat = new SimpleDateFormat(this.getMessage("TimeFormat"));
    }

    @Override
    public boolean onCommand(final CommandSender sender, final Command command, final String label, final String[] args) {

        if (! (sender instanceof Player)) {
            sender.sendMessage(this.getMessage("ConsoleSender"));
            return true;
        }
        final Player player = (Player) sender;
        final ItemStack itemInHand = player.getItemInHand();
        if (itemInHand == null) {
            sender.sendMessage(this.getMessage("NoItemInHand"));
            return true;
        }

        final int heldItemSlot = player.getInventory().getHeldItemSlot();
        final boolean signed = TagUtils.getBooleanMeta(player, heldItemSlot, "Signed");

        switch (label.toLowerCase()) {
            case "sign":
                if (signed && ! this.signOverride()) {
                    player.sendMessage(this.getMessage("ItemAlreadySigned"));
                    return true;
                }
                player.sendMessage(this.getMessage(signed ? "SignOverride" : "SignSuccess"));
                this.signItem(player, itemInHand);
                TagUtils.setBooleanMeta(player, heldItemSlot, "Signed", true);
                return true;
            case "unsign":
                if (! signed) {
                    player.sendMessage(this.getMessage("NotSigned"));
                    return true;
                }
                final ItemMeta itemMeta = itemInHand.getItemMeta();
                itemMeta.setLore(Lists.newArrayList());
                itemInHand.setItemMeta(itemMeta);
                player.sendMessage(this.getMessage("UnsignSuccess"));
                TagUtils.setBooleanMeta(player, heldItemSlot, "Signed", false);
                return true;
        }
        return false;
    }

    private void signItem(final Player player, final ItemStack itemInHand) {
        final ItemMeta itemMeta = itemInHand.getItemMeta();
        final List<String> lore = Lists.newArrayList(itemInHand.getItemMeta().getLore());
        final Date date = new Date();
        this.getConfig().getStringList("Messages.SignFormat").forEach(s -> {
            lore.add(s
                    .replace("%NAME%", player.getName())
                    .replace("%DATUM%", this.dateFormat.format(date))
                    .replace("%UHRZEIT%", this.timeFormat.format(date))
                    .replace('&', '§')
            );
        });
        itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
        itemMeta.setLore(lore);
        itemInHand.setItemMeta(itemMeta);
    }

    private String getMessage(final String key) {
        return ChatColor.translateAlternateColorCodes('&', this.getConfig().getString("Messages." + key));
    }

    private boolean signOverride() {
        return this.getConfig().getBoolean("Settings.ResignAllowed");
    }
}
