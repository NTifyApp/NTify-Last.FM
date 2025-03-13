package com.spotifyxp.lastfm.config;

import com.spotifyxp.audio.Quality;

/**
 * Holds all registered config values with their type and default value
 */
public enum ConfigValues {
    lastfmpassword("lastfm.password", ConfigValueTypes.STRING, ""),
    lastfmusername("lastfm.username", ConfigValueTypes.STRING, ""),
    lastfmtracklimit("lastfm.user.settings.tracklimit", ConfigValueTypes.INT, 20),
    lastfmartistlimit("lastfm.user.settings.artistlimit",ConfigValueTypes.INT, 10);

    public final String name;
    public final ConfigValueTypes type;
    public final Object defaultValue;
    ConfigValues(String name, ConfigValueTypes type, Object defaultValue) {
        this.name = name;
        this.type = type;
        this.defaultValue = defaultValue;
    }

    /**
     * Returns the ConfigValues instance for the config value
     * @param name name of the config value e.g. user.settings.spconnect
     * @return ConfigValues
     */
    public static ConfigValues get(String name) {
        for(ConfigValues value : ConfigValues.values()) {
            if(value.name.equals(name)) {
                return value;
            }
        }
        return null;
    }
}
