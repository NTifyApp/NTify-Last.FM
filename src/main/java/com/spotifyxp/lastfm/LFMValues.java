package com.spotifyxp.lastfm;

import com.spotifyxp.configuration.Config;
import com.spotifyxp.lastfm.config.ConfigValues;
import com.spotifyxp.lib.libLanguage;
import com.spotifyxp.manager.InstanceManager;
import de.umass.lastfm.Authenticator;
import de.umass.lastfm.Session;

public class LFMValues {
    public static libLanguage language;
    public static Config.RuntimeConfig<ConfigValues> config;
    public static boolean lastfmInitialized = false;
    private static Session session;

    public static Session getSession() {
        if(session == null) {
            session = Authenticator.getMobileSession(
                    LFMValues.config.getFields().username,
                    LFMValues.config.getFields().password,
                    LFMValues.config.getFields().apiKey,
                    LFMValues.config.getFields().apiSharedSecret
            );
        }
        return session;
    }
}
