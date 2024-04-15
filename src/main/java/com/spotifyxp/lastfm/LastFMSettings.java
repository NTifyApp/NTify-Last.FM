package com.spotifyxp.lastfm;

import com.spotifyxp.PublicValues;
import com.spotifyxp.configuration.ConfigValues;
import com.spotifyxp.panels.SettingsPanel;
import com.spotifyxp.swingextension.JScrollText;

import javax.swing.*;
import java.awt.event.*;
import java.util.Set;

public class LastFMSettings {
    public static JScrollText settingslastfmloginlabel;
    public static JButton settingslastfmlogout;
    public static JButton settingslastfmlogin;
    public static JPanel lastfmborder;

    private boolean hooked = false;

    public LastFMSettings() {
        lastfmborder = new JPanel();
        lastfmborder.setBounds(0, 0, 422, 506);
        lastfmborder.setLayout(null);

        SettingsPanel.switcher.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if(!hooked) {
                    SettingsPanel.switcher.insertTab(PublicValues.language.translate("ui.lastfm.settings.border"), null, lastfmborder, null, SettingsPanel.switcher.getTabCount() - 1);
                    SettingsPanel.switcher.revalidate();
                    SettingsPanel.switcher.repaint();
                    hooked = true;
                }
            }
        });


        settingslastfmloginlabel = new JScrollText(PublicValues.language.translate("ui.lastfm.settings.loggedinas").replace("%s", LFMValues.username + "  "));
        settingslastfmloginlabel.setBounds(10, 25, 120, 20);
        lastfmborder.add(settingslastfmloginlabel);

        settingslastfmlogin = new JButton(PublicValues.language.translate("ui.login"));
        settingslastfmlogin.setBounds(190, 25, 85, 20);
        lastfmborder.add(settingslastfmlogin);

        settingslastfmlogin.addActionListener(e -> new LastFMLogin().open(() -> {
            if(!PublicValues.config.getString(ConfigValues.lastfmusername.name).isEmpty()) settingslastfmlogout.setEnabled(true);
            if(!PublicValues.config.getString(ConfigValues.lastfmusername.name).isEmpty()) settingslastfmlogin.setEnabled(false);
            if(!PublicValues.config.getString(ConfigValues.lastfmusername.name).isEmpty()) settingslastfmloginlabel.setText(PublicValues.language.translate("ui.lastfm.settings.loggedinas").replace("%s", PublicValues.config.getString(ConfigValues.lastfmusername.name) + "  "));
        }));

        settingslastfmlogout = new JButton(PublicValues.language.translate("ui.logout"));
        settingslastfmlogout.setBounds(285, 25, 85, 20);
        lastfmborder.add(settingslastfmlogout);

        settingslastfmlogout.setEnabled(false);

        settingslastfmlogout.addActionListener(e -> {
            PublicValues.config.write(ConfigValues.lastfmusername.name, "");
            PublicValues.config.write(ConfigValues.lastfmpassword.name, "");
            settingslastfmlogout.setEnabled(false);
            settingslastfmlogin.setEnabled(true);
            settingslastfmloginlabel.setText(PublicValues.language.translate("ui.lastfm.settings.loggedinas").replace("%s", PublicValues.config.getString(ConfigValues.lastfmusername.name) + "  "));
        });

        if(!PublicValues.config.getString(ConfigValues.lastfmusername.name).isEmpty()) {
            settingslastfmlogout.setEnabled(true);
            settingslastfmlogin.setEnabled(false);
        }else{
            settingslastfmlogin.setEnabled(true);
            settingslastfmlogout.setEnabled(false);
        }
    }
}
