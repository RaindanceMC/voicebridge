# RaindanceMC/voicebridge

- **`voicebridge-module`** — Rain's Flexible Modules — Voice Bridge: A Discord voice channel and a Simple Voice Chat group, joined into one call: whoever is in the group hears the Discord channel, and the Discord channel hears the group.
- **`voicebridge-standalone`** — Rain's Voice Bridge: Rain's Voice Bridge, as a plugin of its own: the voicebridge module, the standard wrapper, and the Discord client (JDA + JDAVE) shaded together.

Part of [RaindanceMC](https://github.com/RaindanceMC); built on [RainsCore](https://github.com/RaindanceMC/RainsCore) and the shared parent in [framework](https://github.com/RaindanceMC/framework). Split out of `rains-flexible-modules` on 16.10.2026, history kept.

## Build

```
mvn install
```

Needs Java 25 and read access to `packages.tstieh.de` (server ids `packages-releases` / `packages-snapshots` in `~/.m2/settings.xml`).

## Plugins

- `voicebridge-standalone/target/RainsVoiceBridge-0.5.0.jar` — also published as `de.raindancer.modules:voicebridge-standalone:0.5.0`

## Versions

A module's version is a property in this repository's root `pom.xml`, a plugin's is `plugin.version` in its own pom. CI publishes every version that is not in the registry yet on the next push to master.
