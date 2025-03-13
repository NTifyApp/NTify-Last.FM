package com.spotifyxp.lastfm;

import com.spotifyxp.PublicValues;
import com.spotifyxp.deps.de.umass.lastfm.*;
import com.spotifyxp.events.Events;
import com.spotifyxp.events.SpotifyXPEvents;
import com.spotifyxp.guielements.DefTable;
import com.spotifyxp.lastfm.config.ConfigValues;
import com.spotifyxp.logging.ConsoleLogging;
import com.spotifyxp.manager.InstanceManager;
import com.spotifyxp.panels.ContentPanel;
import com.spotifyxp.panels.HomePanel;
import com.spotifyxp.swingextension.JFrame;

import javax.swing.*;
import javax.swing.plaf.basic.BasicTabbedPaneUI;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.TextStyle;
import java.util.ArrayList;
import java.util.Date;

public class LastFMDialog extends JFrame {
    final JPanel scrobblespanel;
    final JPanel userchartspanel;
    final JPanel chartspanel;
    final JScrollPane scrobbles;
    final JScrollPane usercharts;
    final JScrollPane charts;
    final JTabbedPane tabs = new JTabbedPane(SwingConstants.TOP);
    final DefTable scrobblestable;
    final DefTable userchartsartists;
    final DefTable userchartsalbums;
    final DefTable userchartstracks;
    final DefTable chartsartists;
    final DefTable chartstracks;
    final ArrayList<String> scrobblesuricache = new ArrayList<>();
    final ArrayList<String> userchartsartistsuricache = new ArrayList<>();
    final ArrayList<String> userchartsalbumsuricache = new ArrayList<>();
    final ArrayList<String> userchartstracksuricache = new ArrayList<>();
    final ArrayList<String> chartsartistsuricache = new ArrayList<>();
    final ArrayList<String> chartstracksuricache = new ArrayList<>();
    final JScrollPane scrobblestablescroll;
    final JScrollPane userchartsartistsscroll;
    final JScrollPane userchartsalbumsscroll;
    final JScrollPane userchartstracksscroll;
    final JScrollPane chartsartistsscroll;
    final JScrollPane chartstracksscroll;
    int scrobblescurrent = 0;
    int userchartsartistscurrent = 0;
    int userchartsalbumscurrent = 0;
    int userchartstrackscurrent = 0;
    int chartsartistscurrent = 0;
    int chartstrackscurrent = 0;
    private static JFrame frame;

    public LastFMDialog() {
        setPreferredSize(new Dimension(800, 600));
        setTitle("Last.fm - Dashboard");
        setLayout(null);
        scrobblespanel = new JPanel();
        userchartspanel = new JPanel();
        chartspanel = new JPanel();
        userchartspanel.setPreferredSize(new Dimension(800, 1690));
        chartspanel.setPreferredSize(new Dimension(800, 1140));
        scrobbles = new JScrollPane(scrobblespanel);
        usercharts = new JScrollPane(userchartspanel);
        charts = new JScrollPane(chartspanel);
        usercharts.setPreferredSize(new Dimension(800, 1690));
        charts.setPreferredSize(new Dimension(800, 1140));
        scrobblespanel.setLayout(null);
        userchartspanel.setLayout(null);
        chartspanel.setLayout(null);
        tabs.setForeground(PublicValues.globalFontColor);
        tabs.addTab(LFMValues.language.translate("ui.lastfm.scrobbles"), scrobbles);
        tabs.addTab(LFMValues.language.translate("ui.lastfm.usercharts"), usercharts);
        tabs.addTab(LFMValues.language.translate("ui.lastfm.charts"), charts);
        tabs.setUI(new BasicTabbedPaneUI() {
            @Override
            protected int calculateTabWidth(int tabPlacement, int tabIndex, FontMetrics metrics) {
                return 800 / tabs.getTabCount();
            }
        });
        add(tabs);
        tabs.setBounds(0, 0, 800, 600);
        JLabel scrobbleslabel = new JLabel(LFMValues.language.translate("ui.lastfm.scrobbles"));
        scrobbleslabel.setBounds(6, 6, 788, 23);
        scrobbleslabel.setForeground(PublicValues.globalFontColor);
        scrobblespanel.add(scrobbleslabel);
        scrobblestable = new DefTable() {
        };
        scrobblestable.setForeground(PublicValues.globalFontColor);
        scrobblestable.getTableHeader().setForeground(PublicValues.globalFontColor);
        scrobblestable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    if(scrobblestable.getModel().getValueAt(scrobblestable.getSelectedRow(), 2).toString().equalsIgnoreCase("load more")) {
                        Thread thread = new Thread(() -> {
                            scrobblescurrent++;
                            ((DefaultTableModel) scrobblestable.getModel()).setRowCount(scrobblestable.getRowCount() - 1);
                            for(Track t : User.getRecentTracks(LFMValues.config.getString(ConfigValues.lastfmusername.name), scrobblescurrent, LFMValues.tracklimit, LFMValues.apikey)) {
                                scrobblestable.addModifyAction(() -> {
                                    ((DefaultTableModel) scrobblestable.getModel()).addRow(new Object[] {t.getName(), t.getArtist(), formatDate(t.getPlayedWhen())});
                                    scrobblesuricache.add(LastFMConverter.getTrackURIfromName(t.getName()));
                                });
                            }
                            if(User.getRecentTracks(LFMValues.config.getString(ConfigValues.lastfmusername.name), scrobblescurrent, LFMValues.tracklimit, LFMValues.apikey).getTotalPages() != scrobblescurrent) {
                                scrobblestable.addModifyAction(() -> ((DefaultTableModel) scrobblestable.getModel()).addRow(new Object[] {LFMValues.language.translate("ui.general.loadmore"), LFMValues.language.translate("ui.general.loadmore"), LFMValues.language.translate("ui.general.loadmore")}));
                            }
                        });
                        return;
                    }
                    InstanceManager.getSpotifyPlayer().load(scrobblesuricache.get(scrobblestable.getSelectedRow()), true, false);
                    Events.triggerEvent(SpotifyXPEvents.queueUpdate.getName());
                }
            }
        });
        scrobblestablescroll = new JScrollPane(scrobblestable);
        scrobblestable.setModel(new DefaultTableModel(
                new Object[][] {
                },
                new String[] {
                        LFMValues.language.translate("ui.lastfm.scrobbles.name"), LFMValues.language.translate("ui.lastfm.scrobbles.artist"), LFMValues.language.translate("ui.lastfm.scrobbles.at")
                }
        ));
        scrobblestablescroll.setBounds(6, 32, 778, 503);
        scrobblespanel.add(scrobblestablescroll);

        tabs.addChangeListener(e -> {
            if(tabs.getSelectedIndex() == 0) {
                parseScrobbles();
            }
            if(tabs.getSelectedIndex() == 1) {
                parseUserCharts();
            }
            if(tabs.getSelectedIndex() == 2) {
                parseCharts();
            }
        });
        JLabel userchartsartistslabel = new JLabel(LFMValues.language.translate("ui.lastfm.userchartsartists"));
        userchartsartistslabel.setBounds(6, 6, 788, 23);
        userchartsartistslabel.setForeground(PublicValues.globalFontColor);
        userchartspanel.add(userchartsartistslabel);
        userchartsartists = new DefTable() {
        };
        userchartsartists.setForeground(PublicValues.globalFontColor);
        userchartsartists.getTableHeader().setForeground(PublicValues.globalFontColor);
        userchartsartists.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    if(userchartsartists.getModel().getValueAt(userchartsartists.getSelectedRow(), 1).toString().equals(LFMValues.language.translate("ui.general.loadmore"))) {
                        Thread thread = new Thread(() -> {
                            userchartsartistscurrent++;
                            ((DefaultTableModel) userchartsartists.getModel()).setRowCount(userchartsartists.getRowCount() - 1);
                            for(Artist a : User.getTopArtists(LFMValues.config.getString(ConfigValues.lastfmusername.name), Period.OVERALL, LFMValues.tracklimit, userchartsartistscurrent, LFMValues.apikey)) {
                                userchartsartists.addModifyAction(() -> {
                                    ((DefaultTableModel) userchartsartists.getModel()).addRow(new Object[]{a.getName(), a.getPlaycount()});
                                    userchartsartistsuricache.add(LastFMConverter.getArtistURIfromName(a.getName()));
                                });
                            }
                            if(User.getTopArtists(LFMValues.config.getString(ConfigValues.lastfmusername.name), Period.OVERALL, LFMValues.tracklimit, userchartsartistscurrent, LFMValues.apikey).getTotalPages() != userchartsartistscurrent) {
                                userchartsartists.addModifyAction(() -> ((DefaultTableModel) userchartsartists.getModel()).addRow(new Object[]{LFMValues.language.translate("ui.general.loadmore"), LFMValues.language.translate("ui.general.loadmore")}));
                            }
                        });
                        thread.start();
                        return;
                    }
                    ContentPanel.switchView(ContentPanel.lastView);
                    ContentPanel.showArtistPanel(userchartsartistsuricache.get(userchartsartists.getSelectedRow()));
                }
            }
        });
        userchartsartists.setModel(new DefaultTableModel(
                new Object[][] {
                },
                new String[] {
                        LFMValues.language.translate("ui.lastfm.charts.artist"), LFMValues.language.translate("ui.lastfm.multiuse.scrobbles")
                }
        ));
        userchartsartistsscroll = new JScrollPane(userchartsartists);
        userchartsartistsscroll.setBounds(6, 32, 778, 503);
        userchartspanel.add(userchartsartistsscroll);
        JLabel userchartsalbumlabel = new JLabel(LFMValues.language.translate("ui.lastfm.userchartsalbums"));
        userchartsalbumlabel.setBounds(6, 570, 788, 23);
        userchartsalbumlabel.setForeground(PublicValues.globalFontColor);
        userchartspanel.add(userchartsalbumlabel);
        userchartsalbums = new DefTable() {
        };
        userchartsalbums.setForeground(PublicValues.globalFontColor);
        userchartsalbums.getTableHeader().setForeground(PublicValues.globalFontColor);
        userchartsalbums.setModel(new DefaultTableModel(
                new Object[][] {
                },
                new String[] {
                        LFMValues.language.translate("ui.lastfm.charts.album"), LFMValues.language.translate("ui.lastfm.charts.artist"), LFMValues.language.translate("ui.lastfm.multiuse.scrobbles")
                }
        ));
        userchartsalbums.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(userchartsalbums.getModel().getValueAt(userchartsalbums.getSelectedRow(), 2).toString().equals(LFMValues.language.translate("ui.general.loadmore"))) {
                    Thread thread = new Thread(() -> {
                        userchartsalbumscurrent++;
                        ((DefaultTableModel) userchartsalbums.getModel()).setRowCount(userchartsalbums.getRowCount() - 1);
                        for(Album a : User.getTopAlbums(LFMValues.config.getString(ConfigValues.lastfmusername.name), Period.OVERALL, LFMValues.tracklimit, userchartsalbumscurrent, LFMValues.apikey)) {
                            userchartsalbums.addModifyAction(() -> {
                                ((DefaultTableModel) userchartsalbums.getModel()).addRow(new Object[]{a.getName(), a.getArtist(), a.getPlaycount()});
                                userchartsalbumsuricache.add(LastFMConverter.getAlbumURIfromName(a.getName()));
                            });
                        }
                        if(User.getTopAlbums(LFMValues.config.getString(ConfigValues.lastfmusername.name), Period.OVERALL, LFMValues.tracklimit, userchartsalbumscurrent, LFMValues.apikey).getTotalPages() != userchartsalbumscurrent) {
                            userchartsalbums.addModifyAction(() -> ((DefaultTableModel) userchartsalbums.getModel()).addRow(new Object[]{LFMValues.language.translate("ui.general.loadmore"), LFMValues.language.translate("ui.general.loadmore"), LFMValues.language.translate("ui.general.loadmore")}));
                        }
                    });
                    thread.start();
                    return;
                }
                if(e.getClickCount() == 2) {
                    ContentPanel.switchView(ContentPanel.lastView);
                    ContentPanel.trackPanel.open(userchartsalbumsuricache.get(userchartsalbums.getSelectedRow()), HomePanel.ContentTypes.album);
                }
            }
        });
        userchartsalbumsscroll = new JScrollPane(userchartsalbums);
        userchartsalbumsscroll.setBounds(6, 600, 778, 503);
        userchartspanel.add(userchartsalbumsscroll);
        JLabel userchartstrackslabel = new JLabel(LFMValues.language.translate("ui.lastfm.userchartstracks"));
        userchartstrackslabel.setBounds(6, 1138, 788, 23);
        userchartstrackslabel.setForeground(PublicValues.globalFontColor);
        userchartspanel.add(userchartstrackslabel);
        userchartstracks = new DefTable() {
        };
        userchartstracks.setForeground(PublicValues.globalFontColor);
        userchartstracks.getTableHeader().setForeground(PublicValues.globalFontColor);
        userchartstracks.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    if(userchartstracks.getModel().getValueAt(userchartstracks.getSelectedRow(), 2).toString().equals(LFMValues.language.translate("ui.general.loadmore"))) {
                        Thread thread = new Thread(() -> {
                            userchartstrackscurrent++;
                            ((DefaultTableModel) userchartstracks.getModel()).setRowCount(userchartstracks.getRowCount() - 1);
                            for(Track t : User.getTopTracks(LFMValues.config.getString(ConfigValues.lastfmusername.name), Period.OVERALL, LFMValues.tracklimit, userchartstrackscurrent, LFMValues.apikey)) {
                                userchartstracks.addModifyAction(() -> {
                                    ((DefaultTableModel) userchartstracks.getModel()).addRow(new Object[]{t.getName(), t.getArtist(), t.getPlaycount()});
                                    userchartstracksuricache.add(LastFMConverter.getTrackURIfromName(t.getName()));
                                });
                            }
                            if(User.getTopTracks(LFMValues.config.getString(ConfigValues.lastfmusername.name), Period.OVERALL, LFMValues.tracklimit, userchartstrackscurrent, LFMValues.apikey).getTotalPages() != userchartstrackscurrent) {
                                userchartstracks.addModifyAction(() -> ((DefaultTableModel) userchartstracks.getModel()).addRow(new Object[]{LFMValues.language.translate("ui.general.loadmore"), LFMValues.language.translate("ui.general.loadmore"), LFMValues.language.translate("ui.general.loadmore")}));
                            }
                        });
                        thread.start();
                        return;
                    }
                    InstanceManager.getSpotifyPlayer().load(userchartstracksuricache.get(userchartstracks.getSelectedRow()), true, false);
                    Events.triggerEvent(SpotifyXPEvents.queueUpdate.getName());
                }
            }
        });
        userchartstracks.setModel(new DefaultTableModel(
                new Object[][] {
                },
                new String[] {
                        LFMValues.language.translate("ui.lastfm.charts.track"), LFMValues.language.translate("ui.lastfm.charts.artist"), LFMValues.language.translate("ui.lastfm.multiuse.scrobbles")
                }
        ));
        userchartstracksscroll = new JScrollPane(userchartstracks);
        userchartstracksscroll.setBounds(6, 1164, 778, 503);
        userchartspanel.add(userchartstracksscroll);

        JLabel chartsartistslabel = new JLabel(LFMValues.language.translate("ui.lastfm.chartsartists"));
        chartsartistslabel.setBounds(6, 6, 788, 23);
        chartsartistslabel.setForeground(PublicValues.globalFontColor);
        chartspanel.add(chartsartistslabel);
        chartsartists = new DefTable() {
        };
        chartsartists.setForeground(PublicValues.globalFontColor);
        chartsartists.getTableHeader().setForeground(PublicValues.globalFontColor);
        chartsartists.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    if(chartsartists.getModel().getValueAt(chartsartists.getSelectedRow(), 1).toString().equals(LFMValues.language.translate("ui.general.loadmore"))) {
                        Thread thread = new Thread(() -> {
                            chartsartistscurrent++;
                            ((DefaultTableModel) chartsartists.getModel()).setRowCount(chartsartists.getRowCount() - 1);
                            for(Artist a : Chart.getTopArtists(LFMValues.tracklimit, chartsartistscurrent, LFMValues.apikey)) {
                                chartsartists.addModifyAction(() -> {
                                    ((DefaultTableModel) chartsartists.getModel()).addRow(new Object[]{a.getName(), a.getPlaycount()});
                                    chartsartistsuricache.add(LastFMConverter.getArtistURIfromName(a.getName()));
                                });
                            }
                            if(Chart.getTopArtists(LFMValues.tracklimit, chartsartistscurrent, LFMValues.apikey).getTotalPages() != chartsartistscurrent) {
                                chartsartists.addModifyAction(() -> ((DefaultTableModel) chartsartists.getModel()).addRow(new Object[]{LFMValues.language.translate("ui.general.loadmore"), LFMValues.language.translate("ui.general.loadmore")}));
                            }
                        });
                        thread.start();
                        return;
                    }
                    ContentPanel.switchView(ContentPanel.lastView);
                    ContentPanel.showArtistPanel(chartsartistsuricache.get(chartsartists.getSelectedRow()));
                }
            }
        });
        chartsartists.setModel(new DefaultTableModel(
                new Object[][] {
                },
                new String[] {
                        LFMValues.language.translate("ui.lastfm.charts.artist"), LFMValues.language.translate("ui.lastfm.multiuse.scrobbles")
                }
        ));
        chartsartistsscroll = new JScrollPane(chartsartists);
        chartsartistsscroll.setBounds(6, 32, 778, 503);
        chartspanel.add(chartsartistsscroll);
        JLabel chartstrackslabel = new JLabel(LFMValues.language.translate("ui.lastfm.chartstracks"));
        chartstrackslabel.setBounds(6, 570, 788, 23);
        chartstrackslabel.setForeground(PublicValues.globalFontColor);
        chartspanel.add(chartstrackslabel);
        chartstracks = new DefTable() {
        };
        chartstracks.setForeground(PublicValues.globalFontColor);
        chartstracks.getTableHeader().setForeground(PublicValues.globalFontColor);
        chartstracks.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    if(chartstracks.getModel().getValueAt(chartstracks.getSelectedRow(), 2).toString().equals(LFMValues.language.translate("ui.general.loadmore"))) {
                        Thread thread = new Thread(() -> {
                            chartstrackscurrent++;
                            ((DefaultTableModel) chartstracks.getModel()).setRowCount(chartstracks.getRowCount() - 1);
                            for(Track t : Chart.getTopTracks(LFMValues.tracklimit, chartstrackscurrent, LFMValues.apikey)) {
                                chartstracks.addModifyAction(() -> {
                                    ((DefaultTableModel) chartstracks.getModel()).addRow(new Object[]{t.getName(), t.getArtist(), t.getPlaycount()});
                                    chartstracksuricache.add(LastFMConverter.getTrackURIfromName(t.getName()));
                                });
                            }
                            if(Chart.getTopTracks(LFMValues.tracklimit, chartstrackscurrent, LFMValues.apikey).getTotalPages() != chartstrackscurrent) {
                                chartstracks.addModifyAction(() -> ((DefaultTableModel) chartstracks.getModel()).addRow(new Object[]{LFMValues.language.translate("ui.general.loadmore"), LFMValues.language.translate("ui.general.loadmore"), LFMValues.language.translate("ui.general.loadmore")}));
                            }
                        });
                        thread.start();
                        return;
                    }
                    InstanceManager.getSpotifyPlayer().load(chartstracksuricache.get(chartstracks.getSelectedRow()), true, false);
                    Events.triggerEvent(SpotifyXPEvents.queueUpdate.getName());
                }
            }
        });
        chartstracks.setModel(new DefaultTableModel(
                new Object[][] {
                },
                new String[] {
                        LFMValues.language.translate("ui.lastfm.charts.track"), LFMValues.language.translate("ui.lastfm.charts.artist"), LFMValues.language.translate("ui.lastfm.multiuse.scrobbles")
                }
        ));
        chartstracksscroll = new JScrollPane(chartstracks);
        chartstracksscroll.setBounds(6, 600, 778, 503);
        chartspanel.add(chartstracksscroll);
        JMenuBar bar = new JMenuBar();
        JMenu account = new JMenu(LFMValues.language.translate("ui.lastfm.account"));
        JMenuItem open = new JMenuItem(LFMValues.language.translate("ui.lastfm.account.open"));
        account.add(open);
        bar.add(account);
        open.addActionListener(e -> {
            LastFMUserDialog dialog = new LastFMUserDialog();
            dialog.open();
        });
        setJMenuBar(bar);
        parseScrobbles();
        frame = this;
    }

    public static String formatDate(Date d) {
        try {
            LocalDateTime dateTime = LocalDateTime.ofInstant(d.toInstant(), ZoneId.systemDefault());
            int dayOfMonth = dateTime.getDayOfMonth();
            String monthName = dateTime.getMonth().getDisplayName(TextStyle.FULL, LFMValues.language.getLocale());
            int hours = dateTime.getHour();
            int minutes = dateTime.getMinute();
            return String.format("%d %s %02d:%02d", dayOfMonth, monthName, hours, minutes);
        }catch (NullPointerException e) {
            return "N/A";
        }
    }

    public static boolean isOpen() {
        try {
            return frame.isVisible();
        }catch (NullPointerException e) {
            return false;
        }
    }

    void parseScrobbles() {
        if(scrobblestable.getModel().getRowCount() != 0) {
            return;
        }
        Thread t = new Thread(() -> {
            scrobblescurrent++;
            for(Track t1 : User.getRecentTracks(LFMValues.config.getString(ConfigValues.lastfmusername.name), scrobblescurrent, LFMValues.tracklimit, LFMValues.apikey)) {
                scrobblestable.addModifyAction(() -> {
                    try {
                        ((DefaultTableModel) scrobblestable.getModel()).addRow(new Object[]{t1.getName(), t1.getArtist(), formatDate(t1.getPlayedWhen())});
                        scrobblesuricache.add(LastFMConverter.getTrackURIfromName(t1.getName()));
                    }catch (NullPointerException e) {
                        ConsoleLogging.Throwable(e);
                    }
                    });
            }
            if(User.getRecentTracks(LFMValues.config.getString(ConfigValues.lastfmusername.name), scrobblescurrent, LFMValues.tracklimit, LFMValues.apikey).getTotalPages() != scrobblescurrent) {
                scrobblestable.addModifyAction(() -> ((DefaultTableModel) scrobblestable.getModel()).addRow(new Object[] {LFMValues.language.translate("ui.general.loadmore"), LFMValues.language.translate("ui.general.loadmore"), LFMValues.language.translate("ui.general.loadmore")}));
            }
        });
        t.start();
    }

    void parseUserCharts() {
        if(userchartsartists.getModel().getRowCount() != 0) {
            return;
        }
        Thread artistthread = new Thread(() -> {
            userchartsartistscurrent++;
            for(Artist a : User.getTopArtists(LFMValues.config.getString(ConfigValues.lastfmusername.name), Period.OVERALL, LFMValues.tracklimit, userchartsartistscurrent, LFMValues.apikey)) {
                userchartsartists.addModifyAction(() -> {
                    ((DefaultTableModel) userchartsartists.getModel()).addRow(new Object[]{a.getName(), a.getPlaycount()});
                    userchartsartistsuricache.add(LastFMConverter.getArtistURIfromName(a.getName()));
                });
            }
            if(User.getTopArtists(LFMValues.config.getString(ConfigValues.lastfmusername.name), Period.OVERALL, LFMValues.tracklimit, userchartsartistscurrent, LFMValues.apikey).getTotalPages() != userchartsartistscurrent) {
                userchartsartists.addModifyAction(() -> ((DefaultTableModel) userchartsartists.getModel()).addRow(new Object[]{LFMValues.language.translate("ui.general.loadmore"), LFMValues.language.translate("ui.general.loadmore")}));
            }
        });
        artistthread.start();

        Thread albumthread = new Thread(() -> {
            userchartsalbumscurrent++;
            for(Album a : User.getTopAlbums(LFMValues.config.getString(ConfigValues.lastfmusername.name), Period.OVERALL, LFMValues.tracklimit, userchartsalbumscurrent, LFMValues.apikey)) {
                userchartsalbums.addModifyAction(() -> {
                    ((DefaultTableModel) userchartsalbums.getModel()).addRow(new Object[]{a.getName(), a.getArtist(), a.getPlaycount()});
                    userchartsalbumsuricache.add(LastFMConverter.getAlbumURIfromName(a.getName()));
                });
            }
            if(User.getTopAlbums(LFMValues.config.getString(ConfigValues.lastfmusername.name), Period.OVERALL, LFMValues.tracklimit, userchartsalbumscurrent, LFMValues.apikey).getTotalPages() != userchartsalbumscurrent) {
                userchartsalbums.addModifyAction(() -> ((DefaultTableModel) userchartsalbums.getModel()).addRow(new Object[]{LFMValues.language.translate("ui.general.loadmore"), LFMValues.language.translate("ui.general.loadmore"), LFMValues.language.translate("ui.general.loadmore")}));
            }
        });
        albumthread.start();

        Thread trackthread = new Thread(() -> {
            userchartstrackscurrent++;
            for(Track t : User.getTopTracks(LFMValues.config.getString(ConfigValues.lastfmusername.name), Period.OVERALL, LFMValues.tracklimit, userchartstrackscurrent, LFMValues.apikey)) {
                userchartstracks.addModifyAction(() -> {
                    ((DefaultTableModel) userchartstracks.getModel()).addRow(new Object[]{t.getName(), t.getArtist(), t.getPlaycount()});
                    userchartstracksuricache.add(LastFMConverter.getTrackURIfromName(t.getName()));
                });
            }
            if(User.getTopTracks(LFMValues.config.getString(ConfigValues.lastfmusername.name), Period.OVERALL, LFMValues.tracklimit, userchartstrackscurrent, LFMValues.apikey).getTotalPages() != userchartstrackscurrent) {
                userchartstracks.addModifyAction(() -> ((DefaultTableModel) userchartstracks.getModel()).addRow(new Object[]{LFMValues.language.translate("ui.general.loadmore"), LFMValues.language.translate("ui.general.loadmore"), LFMValues.language.translate("ui.general.loadmore")}));
            }
        });
        trackthread.start();
    }

    void parseCharts() {
        if(chartsartists.getModel().getRowCount() != 0) {
            return;
        }
        Thread artistthread = new Thread(() -> {
            chartsartistscurrent++;
            for(Artist a : Chart.getTopArtists(LFMValues.tracklimit, chartsartistscurrent, LFMValues.apikey)) {
                chartsartists.addModifyAction(() -> {
                    ((DefaultTableModel) chartsartists.getModel()).addRow(new Object[]{a.getName(), a.getPlaycount()});
                    chartsartistsuricache.add(LastFMConverter.getArtistURIfromName(a.getName()));
                });
            }
            if(Chart.getTopArtists(LFMValues.tracklimit, chartsartistscurrent, LFMValues.apikey).getTotalPages() != chartsartistscurrent) {
                chartsartists.addModifyAction(() -> ((DefaultTableModel) chartsartists.getModel()).addRow(new Object[]{LFMValues.language.translate("ui.general.loadmore"), LFMValues.language.translate("ui.general.loadmore")}));
            }
        });
        artistthread.start();

        Thread trackthread = new Thread(() -> {
            chartstrackscurrent++;
            for(Track t : Chart.getTopTracks(LFMValues.tracklimit, chartstrackscurrent, LFMValues.apikey)) {
                chartstracks.addModifyAction(() -> {
                    ((DefaultTableModel) chartstracks.getModel()).addRow(new Object[]{t.getName(), t.getArtist(), t.getPlaycount()});
                    chartstracksuricache.add(LastFMConverter.getTrackURIfromName(t.getName()));
                });
            }
            if(Chart.getTopTracks(LFMValues.tracklimit, chartstrackscurrent, LFMValues.apikey).getTotalPages() != chartstrackscurrent) {
                chartstracks.addModifyAction(() -> ((DefaultTableModel) chartstracks.getModel()).addRow(new Object[]{LFMValues.language.translate("ui.general.loadmore"), LFMValues.language.translate("ui.general.loadmore"), LFMValues.language.translate("ui.general.loadmore")}));
            }
        });
        trackthread.start();
    }

    public static void openWhenLoggedIn() {
        try {
            if (frame.isVisible()) {
                return;
            }
        }catch (NullPointerException ignored) {
        }
        if(LFMValues.config.getString(ConfigValues.lastfmusername.name).isEmpty() || LFMValues.config.getString(ConfigValues.lastfmusername.name).isEmpty()) {
            JOptionPane.showMessageDialog(null, "Please log in first");
            return;
        }
        LastFMDialog dialog = new LastFMDialog();
        dialog.open();
    }
}
