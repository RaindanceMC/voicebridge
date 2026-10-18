package de.raindancer.modules.voicebridge;

import de.raindancer.core.data.settings.SettingsSchema;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

class StandingVoiceSettingsTest {

    @Test
    @DisplayName("the record is a valid settings schema")
    void validSchema() {
        assertThatCode(() -> SettingsSchema.of(StandingVoiceSettings.class, StandingVoiceSettings.DEFAULTS))
                .doesNotThrowAnyException();
    }
}
