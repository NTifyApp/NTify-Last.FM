package com.spotifyxp.lastfm;

import com.spotifyxp.configuration.ConfigValueTypes;
import com.spotifyxp.guielements.Settings;
import com.spotifyxp.lastfm.config.ConfigValues;
import com.spotifyxp.manager.InstanceManager;
import com.spotifyxp.panels.ContentPanel;

public class LastFMSettings {

    public LastFMSettings() {
        ContentPanel.settings.addSetting(
                LFMValues.language.translate("ui.lastfm.settings.border"),
                LFMValues.language.translate("settings.username"),
                ConfigValueTypes.STRING,
                "",
                LFMValues.config.getString(ConfigValues.lastfmusername.name),
                new Settings.OnWrite() {
                    @Override
                    public void run(Object data) {
                        LFMValues.config.write(ConfigValues.lastfmusername.name, data);
                        checkInit();
                    }
                }
        );

        ContentPanel.settings.addSetting(
                LFMValues.language.translate("ui.lastfm.settings.border"),
                LFMValues.language.translate("settings.password"),
                ConfigValueTypes.STRING,
                "",
                LFMValues.config.getString(ConfigValues.lastfmpassword.name),
                new Settings.OnWrite() {
                    @Override
                    public void run(Object data) {
                        LFMValues.config.write(ConfigValues.lastfmpassword.name, data);
                        checkInit();
                    }
                }
        );
    }

    private void checkInit() {
        if(LFMValues.config.getString(ConfigValues.lastfmusername.name).isEmpty() || LFMValues.config.getString(ConfigValues.lastfmpassword.name).isEmpty()) {
            return;
        }
        InstanceManager.getSpotifyPlayer().addEventsListener(new LFMScrobbling());
    }
}
