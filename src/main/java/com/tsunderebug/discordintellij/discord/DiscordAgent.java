package com.tsunderebug.discordintellij.discord;


import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;
import com.intellij.ide.util.PropertiesComponent;
import com.tsunderebug.discordintellij.Presence;
import com.tsunderebug.discordintellij.PresenceAgent;

import static com.tsunderebug.discordintellij.discord.DiscordAPIKeys.DISCORD_CLIENT_ID;

public class DiscordAgent extends PresenceAgent {
    static final String DISCORD_PRESENCE_ENABLED = "presence.discord.enabled";
    private static final String SMALL_IMAGE_KEY = "tsun";
    private static final String SMALL_IMAGE_TEXT = "TsundereBug's plugin: https://goo.gl/81tZHT";

    private DiscordRichPresence drp = initDiscordRichPresence();

    static {
        PresenceAgent.addAgent(DiscordAgent.class);
    }

    public DiscordAgent() {
        super(new DiscordTogglePresence());
    }

    @SuppressWarnings("unused")
    public static boolean isEnabled() {
        return PropertiesComponent.getInstance().getBoolean(DISCORD_PRESENCE_ENABLED, true);
    }

    static void setEnabled(boolean enabled) {
        setEnabled(enabled, DiscordAgent.class);
    }

    @Override
    public void initializeAgent() {
        DiscordRPC.INSTANCE.Discord_Initialize(DISCORD_CLIENT_ID, new DiscordEventHandlers(), true, "");
    }

    @Override
    public void showPresence() {
        DiscordRPC.INSTANCE.Discord_UpdatePresence(drp);
    }

    @Override
    public void hidePresence() {
        DiscordRPC.INSTANCE.Discord_ClearPresence();
    }

    @Override
    public void stopAgent() {
        DiscordRPC.INSTANCE.Discord_Shutdown();
    }

    @Override
    public String getName() {
        return "Discord";
    }

    private DiscordRichPresence initDiscordRichPresence() {
        DiscordRichPresence drp = new DiscordRichPresence();

        drp.smallImageKey = SMALL_IMAGE_KEY;
        drp.smallImageText = SMALL_IMAGE_TEXT;

        return drp;
    }

    @Override
    public void projectChanged(Presence presence) {
        drp.startTimestamp = presence.getStartTimeStamp();
        drp.smallImageKey = SMALL_IMAGE_KEY;
        drp.smallImageText = SMALL_IMAGE_TEXT;
        drp.largeImageKey = presence.getApplicationKey();
        drp.largeImageText = presence.getVersionName();
        drp.details = presence.getApiVersion();
        if (presence.hasCurrentProject()) {
            drp.state = String.format("Opened %s", presence.getProjectName());
        } else {
            drp.state = String.format("In %s %s", presence.getVersionName(), presence.getFullVersion());
        }

        DiscordRPC.INSTANCE.Discord_UpdatePresence(drp);
    }

    @Override
    public void fileChanged(Presence presence) {
        drp.startTimestamp = presence.getStartTimeStamp();
        if (presence.hasFile()) {
            drp.largeImageKey = presence.getFileTypeKey();
            drp.largeImageText = presence.getFileTypeString();
            drp.smallImageKey = presence.getApplicationKey();
            drp.smallImageText = presence.getApplicationText();
            drp.state = String.format("Working on %s", presence.getProjectName());
            drp.details = String.format("Editing [%s] %s", presence.getFileType(), presence.getFile());
        }

        DiscordRPC.INSTANCE.Discord_UpdatePresence(drp);
    }
}
