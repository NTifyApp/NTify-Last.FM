package com.spotifyxp.lastfm;

import com.spotifyxp.PublicValues;
import com.spotifyxp.events.Events;
import com.spotifyxp.events.SpotifyXPEvents;
import com.spotifyxp.injector.InjectorInterface;
import com.spotifyxp.lastfm.panels.LFMArtistPanel;
import com.spotifyxp.panels.ContentPanel;
import com.spotifyxp.panels.SplashPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Initiator implements InjectorInterface {

    @Override
    public String getIdentifier() {
        return "LastFMIntegration";
    }

    @Override
    public String getVersion() {
        return "v1.0";
    }

    @Override
    public String getAuthor() {
        return "Werwolf2303";
    }

    @Override
    public void init() {
        Events.subscribe(SpotifyXPEvents.injectorAPIReady.getName(), new Runnable() {
            @Override
            public void run() {
                SplashPanel.linfo.setText("Init Last.fm");
                new LastFM();
            }
        });
        Events.subscribe(SpotifyXPEvents.onFrameReady.getName(), new Runnable() {
            @Override
            public void run() {
                new LFMArtistPanel();
                new LastFMSettings();
                JMenu lastfm = new JMenu("Last.fm");
                JMenuItem lastfmdashboard = new JMenuItem("Dashboard");
                JMenuItem lastfmuserinfo = new JMenuItem(PublicValues.language.translate("ui.lastfm.userinfo"));
                lastfm.add(lastfmdashboard);
                lastfm.add(lastfmuserinfo);
                lastfmuserinfo.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        new LastFMUserDialog().open();
                    }
                });
                lastfmdashboard.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if(LastFMDialog.isOpen()) {
                            return;
                        }
                        LastFMDialog.openWhenLoggedIn();
                    }
                });
                ContentPanel.bar.add(lastfm);
            }
        });
    }
}
