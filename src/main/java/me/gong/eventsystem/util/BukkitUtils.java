package me.gong.eventsystem.util;

import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.chat.ComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class BukkitUtils {

    private static Class<?> CL_packetTitleType;
    private static Method getHandle, sendPacket, serialize;
    private static Constructor<?> CchatPacket, CpacketPlayOutTitle, CcomponentText;
    private static Field playerConnection;
    private static Field networkManager;

    static {
        try {
            CL_packetTitleType = getClass("PacketPlayOutTitle$EnumTitleAction", true);
            Class<?> CL_craftPlayer = getClass("entity.CraftPlayer", false);
            Class<?> CL_packet = getClass("Packet", true);
            Class<?> CL_entityPlayer = getClass("EntityPlayer", true);
            Class<?> CL_playerConnection = getClass("PlayerConnection", true);
            Class<?> CL_networkManager = getClass("NetworkManager", true);
            Class<?> CL_chatPacket = getClass("PacketPlayOutChat", true);
            Class<?> CL_chatBase = getClass("IChatBaseComponent", true);
            Class<?> CL_chatSerializer = getClass("IChatBaseComponent$ChatSerializer", true);
            Class<?> CL_packetTitle = getClass("PacketPlayOutTitle", true);
            Class<?> CL_chatComponentText = getClass("ChatComponentText", true);

            getHandle = CL_craftPlayer.getMethod("getHandle");
            sendPacket = CL_networkManager.getMethod("handle", CL_packet);
            serialize = CL_chatSerializer.getMethod("a", String.class);

            CchatPacket = CL_chatPacket.getConstructor(CL_chatBase, byte.class);
            CpacketPlayOutTitle = CL_packetTitle.getConstructor(CL_packetTitleType, CL_chatBase, int.class, int.class, int.class);
            CcomponentText = CL_chatComponentText.getConstructor(String.class);

            playerConnection = CL_entityPlayer.getField("playerConnection");
            networkManager = CL_playerConnection.getField("networkManager");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static String getCraftVersion() {
        return org.bukkit.Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

    public enum TitleType {
        TITLE,
        SUBTITLE,
        TIMES,
        CLEAR,
        RESET;

        private Object toNMS() {
            try {
                return CL_packetTitleType.getEnumConstants()[ordinal()];
            } catch (Exception ex) {
                ex.printStackTrace();
                return null;
            }
        }
    }

    public static class Title {
        protected boolean isSub;
        protected int fadeInTime, displayTime, fadeOutTime;
        protected String message;

        public Title(String title, boolean isSub, int fadeInTime, int displayTime, int fadeOutTime) {

            this.message = StringUtils.format(title);
            this.isSub = isSub;
            this.fadeInTime = fadeInTime;
            this.displayTime = displayTime;
            this.fadeOutTime = fadeOutTime;
        }

        public Title(String title, boolean isSub) {
            this(title, isSub, -1, -1, -1);
        }

        public Title(String title) {
            this(title, false);
        }

        public Title(int fadeInTime, int displayTime, int fadeOutTime) {
            this(null, false, fadeInTime, displayTime, fadeOutTime);
        }

        public Title() {
            this(null);
        }

        public void sendTo(Player player, boolean sendEmptyTitle, boolean sendResets) {
            if (sendResets) {
                sendPacket(player, createTitlePacket(TitleType.RESET, null));
                sendPacket(player, createTitlePacket(TitleType.CLEAR, null));
            }
            if (message == null) return;
            else if (fadeInTime != -1 || displayTime != -1 || fadeOutTime != -1)
                sendPacket(player, createTitlePacket(fadeInTime, displayTime, fadeOutTime));
            if (isSub && sendEmptyTitle) sendPacket(player, createTitlePacket(TitleType.TITLE, ""));
            sendPacket(player, createTitlePacket(isSub ? TitleType.SUBTITLE : TitleType.TITLE, message));
        }

        public void sendTo(Player player) {
            sendTo(player, true, true);
        }
    }

    public static Object createTitlePacket(TitleType type, String message, int fadeInTime, int displayTime, int fadeOutTime) {
        message = message == null ? null : StringUtils.format(message);
        Object base = message != null ? toBaseComponent(message) : null, nmsType = type.toNMS();
        try {
            return CpacketPlayOutTitle.newInstance(nmsType, base, fadeInTime, displayTime, fadeOutTime);
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }

    public static Object createTitlePacket(TitleType type, String message) {
        return createTitlePacket(type, message, -1, -1, -1);
    }

    public static Object createTitlePacket(int fadeInTime, int displayTime, int fadeOutTime) {
        return createTitlePacket(TitleType.TIMES, null, fadeInTime, displayTime, fadeOutTime);
    }

    public static void sendGlobalAction(String message) {
        Bukkit.getOnlinePlayers().forEach(p -> sendActionMessage(p, message));
    }

    public static void sendActionMessage(Player player, String message) {
        try {
            sendPacket(player, CchatPacket.newInstance(CcomponentText.newInstance(StringUtils.format(message)), (byte) 2));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static Object toBaseComponent(String message) {
        try {
            return serialize.invoke(null, toJSON(message));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String toJSON(String message) {
        try {
            return ComponentSerializer.toString(TextComponent.fromLegacyText(message));
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static void sendPacket(Player player, Object packet) {
        try {

            Object netMan = networkManager.get(playerConnection.get(getHandle.invoke(player)));
            sendPacket.invoke(netMan, packet);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static final BlockFace[] axis = {BlockFace.NORTH, BlockFace.EAST, BlockFace.SOUTH, BlockFace.WEST};
    public static final BlockFace[] radial = {BlockFace.NORTH, BlockFace.NORTH_EAST, BlockFace.EAST, BlockFace.SOUTH_EAST, BlockFace.SOUTH, BlockFace.SOUTH_WEST, BlockFace.WEST, BlockFace.NORTH_WEST};

    /**
     * Gets the horizontal Block Face from a given yaw angle<br>
     * This includes the NORTH_WEST faces
     *
     * @param yaw angle
     * @return The Block Face of the angle
     */
    public static BlockFace yawToFace(float yaw) {
        return yawToFace(yaw, true);
    }

    /**
     * Gets the horizontal Block Face from a given yaw angle
     *
     * @param yaw                      angle
     * @param useSubCardinalDirections setting, True to allow NORTH_WEST to be returned
     * @return The Block Face of the angle
     */
    public static BlockFace yawToFace(float yaw, boolean useSubCardinalDirections) {
        if (useSubCardinalDirections) {
            return radial[Math.round(yaw / 45f) & 0x7];
        } else {
            return axis[Math.round(yaw / 90f) & 0x3];
        }
    }

    private static Class<?> getClass(String name, boolean nms) {
        try {
            return Class.forName(nms ? "net.minecraft.server." + getCraftVersion() + "." + name : "org.bukkit.craftbukkit." + getCraftVersion() + "." + name);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}