package com.spotifyxp.lastfm;

import com.spotifyxp.events.EventSubscriber;
import com.spotifyxp.events.Events;
import com.spotifyxp.events.SpotifyXPEvents;
import com.spotifyxp.injector.InjectorInterface;
import com.spotifyxp.lastfm.config.Config;
import com.spotifyxp.lastfm.config.ConfigValues;
import com.spotifyxp.lib.libLanguage;
import com.spotifyxp.panels.ContentPanel;
import com.spotifyxp.panels.SplashPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class Initiator implements InjectorInterface {

    @Override
    public void init() {
        LFMValues.config = new Config();
        LFMValues.config.checkConfig();

        LFMValues.language = new libLanguage(Initiator.class);
        LFMValues.language.setNoAutoFindLanguage("en"); // Only english is supported for now
        LFMValues.language.setLanguageFolder("lfmlang");

        Events.subscribe(SpotifyXPEvents.injectorAPIReady.getName(), new EventSubscriber() {
            @Override
            public void run(Object... data) {
                SplashPanel.linfo.setText("Init Last.fm");
                new LastFM();
            }
        });

        Events.subscribe(SpotifyXPEvents.onFrameReady.getName(), new EventSubscriber() {
            @Override
            public void run(Object... data) {
                new LastFMSettings();
                JMenu lastfm = new JMenu("Last.fm");
                JMenuItem lastfmdashboard = new JMenuItem(LFMValues.language.translate("ui.lastfm.dashboard"));
                JMenuItem lastfmuserinfo = new JMenuItem(LFMValues.language.translate("ui.lastfm.userinfo"));
                lastfm.add(lastfmdashboard);
                lastfm.add(lastfmuserinfo);
                lastfmuserinfo.addActionListener(new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        if(LFMValues.config.getString(ConfigValues.lastfmusername.name).isEmpty() || LFMValues.config.getString(ConfigValues.lastfmpassword.name).isEmpty()) {
                            JOptionPane.showMessageDialog(null, LFMValues.language.translate("loginfirst.dialog.message"));
                            return;
                        }
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
