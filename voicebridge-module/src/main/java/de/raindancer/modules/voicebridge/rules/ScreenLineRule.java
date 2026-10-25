package de.raindancer.modules.voicebridge.rules;

import de.raindancer.modules.voicebridge.model.BridgeStatus;
import net.kyori.adventure.text.minimessage.MiniMessage;

/** What the voice screens say, as MiniMessage text. */
public final class ScreenLineRule implements IVoiceBridgeRule {

    private static final MiniMessage MINI = MiniMessage.miniMessage();

    /** Discord's members are only counted: a screen is seen by everybody near it, and names are not for everybody. */
    public String bridge(BridgeStatus status, int inGameGroup) {
        StringBuilder text = new StringBuilder("<gold><bold>Voice bridge</bold></gold>\n").append(switch (status.phase()) {
            case CONNECTED -> "<green>● <white>Connected";
            case CONNECTING -> "<yellow>● <white>Connecting";
            case FAILED -> "<red>● <white>Not reachable";
            case OFF -> "<gray>● <white>Off";
        });
        if (status.isConnected()) {
            if (!status.channelName().isBlank()) {
                text.append("\n<gray>channel <white>").append(MINI.escapeTags(status.channelName()));
            }
            text.append("\n<gray>on Discord <white>").append(status.discordMembers().size());
        }
        return text.append("\n<gray>in the game group <white>").append(inGameGroup).toString();
    }

    public String you(boolean inGroup, boolean linked, boolean onDiscordVoice) {
        StringBuilder text = new StringBuilder("<gold><bold>Your voice</bold></gold>\n")
                .append(inGroup ? "<green>✔ <white>In the bridge group" : "<gray>✘ In the bridge group <dark_gray>· <white>/voicebridge")
                .append('\n').append(linked ? "<green>✔ <white>Discord linked" : "<gray>✘ Discord linked");
        if (onDiscordVoice) {
            text.append("\n<green>✔ <white>On Discord voice");
        }
        return text.toString();
    }

    @Override
    public String describe() {
        return "what the voice screens say: the bridge's state and a player's own voice standing";
    }
}
