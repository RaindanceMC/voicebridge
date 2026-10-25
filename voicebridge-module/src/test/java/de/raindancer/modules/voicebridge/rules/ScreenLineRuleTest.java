package de.raindancer.modules.voicebridge.rules;

import de.raindancer.modules.voicebridge.model.BridgeStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("What the voice screens say")
class ScreenLineRuleTest {

    private final ScreenLineRule rule = new ScreenLineRule();

    @Test
    @DisplayName("a connected bridge names its channel and counts both sides, never naming Discord members")
    void connected() {
        BridgeStatus status = new BridgeStatus(BridgeStatus.Phase.CONNECTED, "", "General <b>",
                List.of("ada", "bo", "cy"));
        String text = rule.bridge(status, 2);
        assertThat(text).startsWith("<gold><bold>Voice bridge</bold></gold>")
                .contains("<green>● <white>Connected")
                .contains("<gray>channel <white>General \\<b>")
                .contains("<gray>on Discord <white>3")
                .contains("<gray>in the game group <white>2")
                .doesNotContain("ada");
    }

    @Test
    @DisplayName("a bridge that is not connected says which state it is in and counts only the game side")
    void notConnected() {
        assertThat(rule.bridge(BridgeStatus.off("x"), 0)).contains("<gray>● <white>Off").doesNotContain("channel");
        assertThat(rule.bridge(BridgeStatus.connecting(), 1)).contains("<yellow>● <white>Connecting");
        assertThat(rule.bridge(BridgeStatus.failed("x"), 0)).contains("<red>● <white>Not reachable");
    }

    @Test
    @DisplayName("a player's own voice: group, linked Discord account, on Discord voice")
    void you() {
        assertThat(rule.you(true, true, true)).contains("<green>✔ <white>In the bridge group")
                .contains("<green>✔ <white>Discord linked").contains("<green>✔ <white>On Discord voice");
        String none = rule.you(false, false, false);
        assertThat(none).contains("<gray>✘ In the bridge group").contains("<white>/voicebridge")
                .contains("<gray>✘ Discord linked").doesNotContain("On Discord voice");
    }
}
