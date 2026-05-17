package com.spotifyxp.lastfm;

import com.google.gson.Gson;
import com.spotifyxp.PublicValues;
import com.spotifyxp.configuration.Config;
import com.spotifyxp.events.EventSubscriber;
import com.spotifyxp.events.SpotifyXPEvents;
import com.spotifyxp.injector.InjectorInterface;
import com.spotifyxp.lastfm.config.ConfigValues;
import com.spotifyxp.lastfm.debug.DebugInfo;
import com.spotifyxp.lib.libLanguage;
import com.spotifyxp.manager.InstanceManager;
import com.spotifyxp.panels.ContentPanel;
import com.spotifyxp.panels.SplashPanel;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Initiator implements InjectorInterface {

    @Override
    public void init() {
        try {
            LFMValues.config = Config.newInstance(new File(new File(PublicValues.configfilepath).getParent(), "last-fm.config.json").getAbsolutePath(), ConfigValues.class, new Gson());
        } catch (IOException | IllegalAccessException | InstantiationException | NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

        LFMValues.language = new libLanguage(Initiator.class);
        LFMValues.language.setNoAutoFindLanguage("en"); // Only english is supported for now
        LFMValues.language.setLanguageFolder("lfmlang");

        // Initialize plugin when NTify has finished initializing
        SpotifyXPEvents.onFrameReady.subscribe(new EventSubscriber<Object>() {
            @Override
            public void run(Object o) {
                // Notify user when no api key/secret was set
                if (LFMValues.config.getFields().apiKey.isEmpty() || LFMValues.config.getFields().apiSharedSecret.isEmpty()) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            JOptionPane.showMessageDialog(null, LFMValues.language.translate("ui.lastfm.noapikey.message"));
                        }
                    });
                    return;
                }

                // Notify user when not logged in
                if(LFMValues.config.getFields().username.isEmpty() || LFMValues.config.getFields().password.isEmpty()) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            JOptionPane.showMessageDialog(null, LFMValues.language.translate("loginfirst.dialog.message"));
                        }
                    });
                    return;
                }

                LFMScrobbling scrobbling = new LFMScrobbling();

                InstanceManager.getSpotifyPlayer().addEventsListener(scrobbling);

                try {
                    ContentPanel.settings.addSettings("f5c61aeb-048f-4ed1-acd3-ee2ba8903640", PublicValues.config);
                } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                         NoSuchFieldException | InstantiationException e) {
                    throw new RuntimeException(e);
                }

                JMenu lastfm = new JMenu("Last.fm");
                JMenuItem lastfmdashboard = new JMenuItem(LFMValues.language.translate("ui.lastfm.dashboard"));
                JMenuItem lastfmuserinfo = new JMenuItem(LFMValues.language.translate("ui.lastfm.userinfo"));
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

                if (InstanceManager.getSpotifyPlayer().currentMetadata() != null) {
                    scrobbling.triggerNewTrack(InstanceManager.getSpotifyPlayer().currentMetadata());
                }

                if (PublicValues.devMode) {
                    new DebugInfo(lastfm, scrobbling);
                }

                LFMValues.lastfmInitialized = true;

                scrobbling.init();
            }
        });
    }
}
