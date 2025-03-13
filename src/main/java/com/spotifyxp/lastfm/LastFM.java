package com.spotifyxp.lastfm;

import com.spotifyxp.PublicValues;
import com.spotifyxp.deps.de.umass.lastfm.Authenticator;
import com.spotifyxp.deps.de.umass.lastfm.Session;
import com.spotifyxp.deps.de.umass.lastfm.exceptions.BadCredentialsException;
import com.spotifyxp.lastfm.config.ConfigValues;

public class LastFM {
    private Session session;

    public LastFM() {
        if(LFMValues.config.getInt(ConfigValues.lastfmartistlimit.name) != 0) LFMValues.artistlimit = LFMValues.config.getInt(ConfigValues.lastfmartistlimit.name);
        if(LFMValues.config.getInt(ConfigValues.lastfmtracklimit.name) != 0) LFMValues.tracklimit = LFMValues.config.getInt(ConfigValues.lastfmtracklimit.name);
    }

    public Session getSession() throws BadCredentialsException {
        if(session == null) {
            session = Authenticator.getMobileSession(
                    LFMValues.config.getString(ConfigValues.lastfmusername.name()),
                    LFMValues.config.getString(ConfigValues.lastfmpassword.name()),
                    LFMValues.apikey,
                    LFMValues.apisecret
            );
        }
        return session;
    }
}