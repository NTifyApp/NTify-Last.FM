package com.spotifyxp.lastfm.debug;

import com.spotifyxp.lastfm.LFMScrobbling;
import com.spotifyxp.swingextension.JFrame;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

public class DebugInfo extends JFrame {
    LFMScrobbling lfmScrobbling;

    Map<String, DebugInfoEntry> entries = new HashMap<String, DebugInfoEntry>() {
        @Override
        public DebugInfoEntry put(String key, DebugInfoEntry value) {
            value.setName(key);
            value.setText("");
            return super.put(key, value);
        }
    };

    TimerTask refreshTask;

    static class DebugInfoEntry extends JLabel {
        @Override
        public void setText(String text) {
            super.setText(getName() + ": " + text);
        }
    }

    public DebugInfo(JMenu menu, LFMScrobbling scrobbling) {
        this.lfmScrobbling = scrobbling;

        JMenuItem lastFMDebug = new JMenuItem("Open Debug");

        // Attach to JMenu
        menu.add(lastFMDebug);

        lastFMDebug.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                open();
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                close();
            }
        });

        getContentPane().setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        entries.put("[LFMScrobbling] pauseTimer", new DebugInfoEntry());
        entries.put("[LFMScrobbling] actuallyListenedS", new DebugInfoEntry());
        entries.put("[LFMScrobbling] durationOfTrack", new DebugInfoEntry());
        entries.put("[LFMScrobbling] scrobbleDataCurrentTrack", new DebugInfoEntry());
        entries.put("[LFMScrobbling] scrobbleDataCurrentTrack.track", new DebugInfoEntry());
        entries.put("[LFMScrobbling] scrobbleDataCurrentTrack.album", new DebugInfoEntry());
        entries.put("[LFMScrobbling] scrobbleDataCurrentTrack.artist", new DebugInfoEntry());
        entries.put("[LFMScrobbling] scrobbleDataCurrentTrack.duration", new DebugInfoEntry());
        entries.put("[LFMScrobbling] scrobbleDataCurrentTrack.timestamp", new DebugInfoEntry());

        entries.forEach((entryName, entry) -> {
            add(entry);
        });
    }

    private void refreshInfo() {
        entries.get("[LFMScrobbling] pauseTimer").setText(String.valueOf(lfmScrobbling.pauseTimer));
        entries.get("[LFMScrobbling] actuallyListenedS").setText(String.valueOf(lfmScrobbling.actuallyListenedS));
        entries.get("[LFMScrobbling] durationOfTrack").setText(String.valueOf(lfmScrobbling.durationOfTrack));
        entries.get("[LFMScrobbling] scrobbleDataCurrentTrack.track").setText(String.valueOf(lfmScrobbling.scrobbleDataCurrentTrack.getTrack()));
        entries.get("[LFMScrobbling] scrobbleDataCurrentTrack.album").setText(String.valueOf(lfmScrobbling.scrobbleDataCurrentTrack.getAlbum()));
        entries.get("[LFMScrobbling] scrobbleDataCurrentTrack.artist").setText(String.valueOf(lfmScrobbling.scrobbleDataCurrentTrack.getArtist()));
        entries.get("[LFMScrobbling] scrobbleDataCurrentTrack.duration").setText(String.valueOf(lfmScrobbling.scrobbleDataCurrentTrack.getDuration()));
        entries.get("[LFMScrobbling] scrobbleDataCurrentTrack.timestamp").setText(Instant.ofEpochSecond(lfmScrobbling.scrobbleDataCurrentTrack.getTimestamp()).toString());
    }

    @Override
    public void open() {
        super.open();

        if (refreshTask == null) {
            refreshTask = new TimerTask() {
                @Override
                public void run() {
                    refreshInfo();
                }
            };
        }

        lfmScrobbling.timer.schedule(refreshTask, 0, 1000);
    }

    @Override
    public void close() {
        super.close();

        refreshTask.cancel();
        refreshTask = null;
    }
}
