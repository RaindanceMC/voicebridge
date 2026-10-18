package de.raindancer.modules.voicebridge.service;

import de.maxhenkel.voicechat.api.Group;
import de.maxhenkel.voicechat.api.VoicechatConnection;
import de.maxhenkel.voicechat.api.VoicechatServerApi;
import de.raindancer.modules.voicebridge.StandingVoiceSettings;

import java.security.SecureRandom;
import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;
import java.util.function.Supplier;

/**
 * The groups that are simply there: General for everybody at all times, Staff while a staff member is online.
 * Nobody is ever moved into either; {@link #tick} only makes and removes the groups.
 *
 * <p>Staff is hidden, isolated and locked with a password nobody is told — staff get in through
 * {@link #joinStaff}, everybody else cannot see it. Groups live per server (each backend has its own voice chat).
 */
public final class StandingVoice {

    public static final String NOT_STAFF = "voicebridge.channels.not-staff";
    public static final String NO_VOICE_CHAT = "voicebridge.channels.no-voice-chat";
    public static final String NO_MOD = "voicebridge.channels.no-mod";
    public static final String NO_STAFF_GROUP = "voicebridge.channels.no-staff-group";

    private static final UUID GENERAL_ID = VoicechatGateway.groupIdFor("standing:general");
    private static final UUID STAFF_ID = VoicechatGateway.groupIdFor("standing:staff");

    private final Supplier<Optional<VoicechatServerApi>> api;
    private final Predicate<UUID> isStaff;
    private final SecureRandom random = new SecureRandom();
    private volatile StandingVoiceSettings settings;

    public StandingVoice(Supplier<Optional<VoicechatServerApi>> api, Predicate<UUID> isStaff,
                         StandingVoiceSettings settings) {
        this.api = api;
        this.isStaff = isStaff;
        settings(settings);
    }

    public void settings(StandingVoiceSettings fresh) {
        this.settings = fresh == null ? StandingVoiceSettings.DEFAULTS : fresh;
    }

    /** Brings the groups in line with who is online. Idempotent; run every few seconds and on join/quit. */
    public synchronized void tick(Collection<UUID> online) {
        Optional<VoicechatServerApi> live = api.get();
        if (live.isEmpty()) {
            return;
        }
        VoicechatServerApi voice = live.get();
        StandingVoiceSettings now = settings;
        if (now.generalOn()) {
            if (voice.getGroup(GENERAL_ID) == null) {
                voice.groupBuilder().setId(GENERAL_ID).setName(now.generalNameOrDefault()).setPersistent(true)
                        .setHidden(false).setType(Group.Type.NORMAL).build();
            }
        } else if (voice.getGroup(GENERAL_ID) != null) {
            voice.removeGroup(GENERAL_ID);
        }

        boolean staffOn = now.staffOn() && online.stream().anyMatch(isStaff);
        Group staffGroup = voice.getGroup(STAFF_ID);
        if (!staffOn) {
            if (staffGroup != null) {
                voice.removeGroup(STAFF_ID);
            }
            return;
        }
        if (staffGroup == null) {
            voice.groupBuilder().setId(STAFF_ID).setName(now.staffNameOrDefault()).setPersistent(true)
                    .setHidden(true).setType(Group.Type.ISOLATED)
                    .setPassword(Long.toHexString(random.nextLong()) + Long.toHexString(random.nextLong())).build();
            return;
        }
        // Somebody who lost their staff rank while in it does not stay listening.
        for (UUID player : online) {
            if (isStaff.test(player)) {
                continue;
            }
            VoicechatConnection connection = voice.getConnectionOf(player);
            if (connection != null && connection.getGroup() != null && STAFF_ID.equals(connection.getGroup().getId())) {
                connection.setGroup(null);
            }
        }
    }

    /** @return a refusal message key, empty when they are in */
    public synchronized String joinStaff(UUID player) {
        Optional<VoicechatServerApi> live = api.get();
        if (live.isEmpty()) {
            return NO_VOICE_CHAT;
        }
        if (!isStaff.test(player)) {
            return NOT_STAFF;
        }
        VoicechatConnection connection = live.get().getConnectionOf(player);
        if (connection == null || !connection.isInstalled()) {
            return NO_MOD;
        }
        Group group = live.get().getGroup(STAFF_ID);
        if (group == null) {
            return NO_STAFF_GROUP;
        }
        connection.setGroup(group);
        return "";
    }

    /** @return a refusal message key, empty when they are in */
    public synchronized String joinGeneral(UUID player) {
        Optional<VoicechatServerApi> live = api.get();
        if (live.isEmpty()) {
            return NO_VOICE_CHAT;
        }
        VoicechatConnection connection = live.get().getConnectionOf(player);
        if (connection == null || !connection.isInstalled()) {
            return NO_MOD;
        }
        Group group = live.get().getGroup(GENERAL_ID);
        if (group == null) {
            return NO_VOICE_CHAT;
        }
        connection.setGroup(group);
        return "";
    }
}
