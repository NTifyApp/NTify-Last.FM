package com.spotifyxp.lastfm.config;

import com.spotifyxp.configuration.Config;
import com.spotifyxp.configuration.IConfig;
import com.spotifyxp.lastfm.LFMValues;

public class ConfigValues implements IConfig {

    @Config.Text(id = "lastfm.username", category = "ui.lastfm.settings.border")
    public String username = "";

    @Config.Text(id = "lastfm.password", category = "ui.lastfm.settings.border")
    public String password = "";

    @Config.Numbers(id = "lastfm.user.settings.tracklimit", category = "ui.lastfm.settings.border")
    public int trackLimit = 20;

    @Config.Numbers(id ="lastfm.user.settings.artistlimit", category = "ui.lastfm.settings.border")
    public int artistLimit = 10;

    @Config.Text(id = "lastfm.api.key", category = "ui.lastfm.settings.border")
    public String apiKey = "";

    @Config.Text(id = "lastfm.api.shared.secret", category = "ui.lastfm.settings.border")
    public String apiSharedSecret = "";

    @Override
    public String translate(String s) {
        return LFMValues.language.translate(s);
    }
}
