package de.raindancer.modules.voicebridge.service;

import de.raindancer.core.ui.hologram.ScreenKind;
import de.raindancer.core.ui.hologram.Screens;
import de.raindancer.modules.voicebridge.VoiceBridgeSettings;
import de.raindancer.modules.voicebridge.rules.ScreenLineRule;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Material;
import org.bukkit.plugin.Plugin;

import java.util.List;

/** The voice screens for Core's {@code /screen}: the bridge's state, and each player's own voice. */
public final class VoiceScreens implements IVoiceBridgeService {

    public static final List<String> KINDS = List.of("voice-bridge", "voice-you");
    private static final MiniMessage MINI = MiniMessage.miniMessage();

    private final BridgeService bridge;
    private final VoicechatGateway gateway;
    private final LinkService links;
    private final LobbyService lobby;
    private final ScreenLineRule rule = new ScreenLineRule();

    public VoiceScreens(BridgeService bridge, VoicechatGateway gateway, LinkService links, LobbyService lobby) {
        this.bridge = bridge;
        this.gateway = gateway;
        this.links = links;
        this.lobby = lobby;
    }

    @Override
    public void settings(VoiceBridgeSettings settings) {
        // The screens read what the services hold; nothing here is configurable.
    }

    public void register(Screens screens, Plugin owner) {
        screens.register(owner, ScreenKind.everyone("voice-bridge", "Voice bridge",
                "Whether the Discord voice bridge is up, its channel, and how many are on each side.",
                Material.NOTE_BLOCK, () -> MINI.deserialize(rule.bridge(bridge.status(), gateway.members().size()))));
        screens.register(owner, ScreenKind.personal("voice-you", "Your voice",
                "Everybody's own voice: in the bridge group, Discord linked, on Discord voice.", Material.JUKEBOX,
                this::you));
    }

    private Component you(ScreenKind.Viewer viewer) {
        return MINI.deserialize(rule.you(gateway.isMember(viewer.id()), links.discordOf(viewer.id()).isPresent(),
                lobby.onDiscord(viewer.id())));
    }
}
