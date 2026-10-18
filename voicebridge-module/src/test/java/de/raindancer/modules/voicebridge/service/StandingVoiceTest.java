package de.raindancer.modules.voicebridge.service;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.raindancer.modules.voicebridge.StandingVoiceSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("the standing voice channels: General always, Staff while staff are on")
class StandingVoiceTest {

    private final UUID mod = UUID.randomUUID();
    private final UUID player = UUID.randomUUID();
    private VoicechatServerApi api;
    private final List<Group> existing = new ArrayList<>();
    private final List<Group.Builder> builders = new ArrayList<>();
    private Set<UUID> staff = Set.of(mod);
    private StandingVoice voice;

    @BeforeEach
    void setUp() {
        api = mock(VoicechatServerApi.class);
        when(api.getGroups()).thenAnswer(ask -> List.copyOf(existing));
        when(api.getGroup(any())).thenAnswer(ask -> existing.stream()
                .filter(group -> group.getId().equals(ask.getArgument(0))).findFirst().orElse(null));
        when(api.groupBuilder()).thenAnswer(ask -> {
            Group.Builder builder = mock(Group.Builder.class, Answers.RETURNS_SELF);
            UUID[] id = {UUID.randomUUID()};
            when(builder.setId(any())).thenAnswer(set -> {
                id[0] = set.getArgument(0);
                return builder;
            });
            when(builder.build()).thenAnswer(build -> {
                Group made = mock(Group.class);
                when(made.getId()).thenReturn(id[0]);
                existing.add(made);
                return made;
            });
            builders.add(builder);
            return builder;
        });
        when(api.removeGroup(any())).thenAnswer(ask -> existing.removeIf(group -> group.getId().equals(ask.getArgument(0))));
        voice = new StandingVoice(() -> Optional.of(api), who -> staff.contains(who), StandingVoiceSettings.DEFAULTS);
    }

    private VoicechatConnection connection(UUID who) {
        VoicechatConnection connection = mock(VoicechatConnection.class);
        when(connection.isInstalled()).thenReturn(true);
        when(api.getConnectionOf(who)).thenReturn(connection);
        return connection;
    }

    @Test
    @DisplayName("General is made once, open to everybody, and kept")
    void general() {
        voice.tick(List.of(player));
        voice.tick(List.of(player));

        assertThat(builders).hasSize(1);
        verify(builders.getFirst()).setName("General");
        verify(builders.getFirst()).setPersistent(true);
        verify(builders.getFirst()).setHidden(false);
        verify(builders.getFirst(), never()).setPassword(any());
    }

    @Test
    @DisplayName("Staff appears when a staff member is on — hidden, locked — and nobody is moved into it")
    void staffAppears() {
        VoicechatConnection connection = connection(mod);

        voice.tick(List.of(mod, player));

        assertThat(existing).hasSize(2);
        Group.Builder staffBuilder = builders.get(1);
        verify(staffBuilder).setName("Staff");
        verify(staffBuilder).setHidden(true);
        verify(staffBuilder).setPassword(any());
        verify(staffBuilder).setType(Group.Type.ISOLATED);
        verify(connection, never()).setGroup(any());
    }

    @Test
    @DisplayName("Staff goes when the last staff member leaves")
    void staffGoes() {
        voice.tick(List.of(mod));
        voice.tick(List.of(player));

        assertThat(existing).hasSize(1);
        verify(api, times(1)).removeGroup(any());
    }

    @Test
    @DisplayName("staff join it without the password; anybody else is refused")
    void joining() {
        voice.tick(List.of(mod, player));
        VoicechatConnection modConnection = connection(mod);
        VoicechatConnection playerConnection = connection(player);

        assertThat(voice.joinStaff(mod)).isEmpty();
        assertThat(voice.joinStaff(player)).isEqualTo(StandingVoice.NOT_STAFF);

        verify(modConnection).setGroup(existing.get(1));
        verify(playerConnection, never()).setGroup(any());
    }

    @Test
    @DisplayName("somebody no longer staff is taken out of the staff channel")
    void noLongerStaff() {
        voice.tick(List.of(mod));
        VoicechatConnection connection = connection(mod);
        when(connection.getGroup()).thenReturn(existing.get(1));
        UUID other = UUID.randomUUID();
        staff = Set.of(other);

        voice.tick(List.of(mod, other));

        verify(connection).setGroup(null);
    }

    @Test
    @DisplayName("switched off, neither channel is made")
    void off() {
        voice.settings(new StandingVoiceSettings(false, "General", false, "Staff", "rains.moderation.staffchat"));

        voice.tick(List.of(mod));

        assertThat(existing).isEmpty();
    }

    @Test
    @DisplayName("without voice chat on the server, nothing happens and joining says so")
    void noVoiceChat() {
        StandingVoice none = new StandingVoice(Optional::empty, who -> true, StandingVoiceSettings.DEFAULTS);
        none.tick(List.of(mod));
        assertThat(none.joinStaff(mod)).isEqualTo(StandingVoice.NO_VOICE_CHAT);
    }
}
