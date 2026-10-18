package de.raindancer.modules.voicebridge;

import de.raindancer.core.data.settings.Describe;
import de.raindancer.core.data.settings.In;
import de.raindancer.core.data.settings.Key;
import de.raindancer.core.data.settings.Settings;
import de.raindancer.core.data.settings.Title;
import de.raindancer.core.data.settings.Topic;
import org.bukkit.Material;

/** The voice chat groups that are always there: one for everybody, and one for the staff while they are on. */
@Settings(id = "voicebridge-channels", topics = {
        @Topic(path = "voicebridge/channels", title = "Standing voice groups", icon = Material.NOTE_BLOCK),
})
public record StandingVoiceSettings(

        @In("voicebridge/channels") @Title("General group")
        @Describe("A voice chat group everybody can join, there at all times. Nobody is put in it.")
        @Key("general.enabled")
        boolean generalOn,

        @In("voicebridge/channels") @Title("General group name")
        @Key("general.name")
        String generalName,

        @In("voicebridge/channels") @Title("Staff group")
        @Describe("A hidden, locked group that is there while a staff member is online. Staff join it with "
                + "/voicebridge staff; nobody is put in it.")
        @Key("staff.enabled")
        boolean staffOn,

        @In("voicebridge/channels") @Title("Staff group name")
        @Key("staff.name")
        String staffName,

        @In("voicebridge/channels") @Title("Who counts as staff")
        @Describe("The permission somebody needs to see and join the staff group.")
        @Key("staff.permission")
        String staffPermission) {

    public static final StandingVoiceSettings DEFAULTS =
            new StandingVoiceSettings(true, "General", true, "Staff", "rains.moderation.staffchat");

    public String generalNameOrDefault() {
        return generalName == null || generalName.isBlank() ? DEFAULTS.generalName() : generalName.strip();
    }

    public String staffNameOrDefault() {
        return staffName == null || staffName.isBlank() ? DEFAULTS.staffName() : staffName.strip();
    }

    public String staffPermissionOrDefault() {
        return staffPermission == null || staffPermission.isBlank() ? DEFAULTS.staffPermission() : staffPermission.strip();
    }
}
