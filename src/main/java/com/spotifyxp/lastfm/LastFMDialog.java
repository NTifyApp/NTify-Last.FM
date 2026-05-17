package com.spotifyxp.lastfm;

import com.spotifyxp.PublicValues;
import de.umass.lastfm.*;
import com.spotifyxp.events.SpotifyXPEvents;
import com.spotifyxp.guielements.DefTable;
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
    final JScrollPane scrobblestablescroll;
    final JScrollPane userchartsartistsscroll;
    final JScrollPane userchartsalbumsscroll;
    final JScrollPane userchartstracksscroll;
    final JScrollPane chartsartistsscroll;
    final JScrollPane chartstracksscroll;
    final JLabel scrobbleslabel;
    final JLabel userchartsartistslabel;
    final JLabel userchartsalbumlabel;
    final JLabel userchartstrackslabel;
    final JLabel chartsartistslabel;
    final JLabel chartstrackslabel;
    int scrobblescurrent = 0;
    int chartsartistscurrent = 0;
    int chartstrackscurrent = 0;
    private static JFrame frame;

    private String getSearchString(DefTable table, int row) {
        String name = table.getModel().getValueAt(row, 0).toString();
        String artist = table.getModel().getValueAt(row, 1).toString();
        return String.format("%s %s", name, artist);
    }

    private void resizeComponents() {
        Dimension userChartsPreferred = usercharts.getPreferredSize();
        userChartsPreferred.height = 1690;
        userchartspanel.setPreferredSize(userChartsPreferred);
        Dimension chartsPreferred = charts.getPreferredSize();
        chartsPreferred.height = 1140;
        chartspanel.setPreferredSize(chartsPreferred);

        scrobbleslabel.setBounds(6, 6, getWidth() - scrobbles.getVerticalScrollBar().getWidth() - 10, 23);
        scrobblestablescroll.setBounds(6, 32, scrobbles.getViewport().getWidth() - 10, scrobbles.getViewport().getHeight() - 43);
        userchartsartistslabel.setBounds(6, 6, getWidth() - usercharts.getVerticalScrollBar().getWidth() - 43, 23);
        userchartsartistsscroll.setBounds(6, 32, getWidth() - usercharts.getVerticalScrollBar().getWidth() - 43, usercharts.getViewport().getHeight() - 43);
        userchartsalbumlabel.setBounds(6, 570, getWidth() - usercharts.getVerticalScrollBar().getWidth() - 43, 23);
        userchartsalbumsscroll.setBounds(6, 600, getWidth() - usercharts.getVerticalScrollBar().getWidth() - 43, usercharts.getViewport().getHeight() - 43);
        userchartstrackslabel.setBounds(6, 1138, getWidth() - usercharts.getVerticalScrollBar().getWidth() - 43, 23);
        userchartstracksscroll.setBounds(6, 1164, getWidth() - usercharts.getVerticalScrollBar().getWidth() - 43, usercharts.getViewport().getHeight() - 43);
        chartsartistslabel.setBounds(6, 6, getWidth() - charts.getVerticalScrollBar().getWidth() - 43, 23);
        chartsartistsscroll.setBounds(6, 32, getWidth() - charts.getVerticalScrollBar().getWidth() - 43, charts.getViewport().getHeight() - 43);
        chartstrackslabel.setBounds(6, 570, getWidth() - charts.getVerticalScrollBar().getWidth() - 43, 23);
        chartstracksscroll.setBounds(6, 600, getWidth() - charts.getVerticalScrollBar().getWidth() - 43, charts.getViewport().getHeight() - 43);
    }

    public LastFMDialog() {
        setPreferredSize(new Dimension(800, 600));
        setTitle(LFMValues.language.translate("ui.title"));
        setLayout(new BorderLayout());
        setResizable(false);
        setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

        scrobblespanel = new JPanel();
        userchartspanel = new JPanel();
        chartspanel = new JPanel();
        scrobbles = new JScrollPane(scrobblespanel);
        usercharts = new JScrollPane(userchartspanel);
        charts = new JScrollPane(chartspanel);
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
        add(tabs, BorderLayout.CENTER);
        scrobbleslabel = new JLabel(LFMValues.language.translate("ui.lastfm.scrobbles"));
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
                        new Thread(() -> {
                            scrobblescurrent++;
                            ((DefaultTableModel) scrobblestable.getModel()).setRowCount(scrobblestable.getRowCount() - 1);
                            for(Track t : User.getRecentTracks(LFMValues.config.getFields().username, scrobblescurrent, LFMValues.config.getFields().trackLimit, LFMValues.config.getFields().apiKey)) {
                                scrobblestable.addModifyAction(() -> {
                                    ((DefaultTableModel) scrobblestable.getModel()).addRow(new Object[] {t.getName(), t.getArtist(), formatDate(t.getPlayedWhen())});
                                });
                            }
                            if(User.getRecentTracks(LFMValues.config.getFields().username, scrobblescurrent, LFMValues.config.getFields().trackLimit, LFMValues.config.getFields().apiKey).getTotalPages() != scrobblescurrent) {
                                scrobblestable.addModifyAction(() -> ((DefaultTableModel) scrobblestable.getModel()).addRow(new Object[] {LFMValues.language.translate("ui.general.loadmore"), LFMValues.language.translate("ui.general.loadmore"), LFMValues.language.translate("ui.general.loadmore")}));
                            }
                        }).start();
                        return;
                    }
                    InstanceManager.getSpotifyPlayer().load(SpotifyMatcher.getTrackURI(getSearchString(scrobblestable, scrobblestable.getSelectedRow())), true, false);
                    SpotifyXPEvents.queueUpdate.trigger();
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
        userchartsartistslabel = new JLabel(LFMValues.language.translate("ui.lastfm.userchartsartists"));
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
                    ContentPanel.switchView(ContentPanel.lastView);
                    ContentPanel.showArtistPanel(SpotifyMatcher.getArtistURI(getSearchString(userchartsartists, userchartsartists.getSelectedRow())));
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
        userchartspanel.add(userchartsartistsscroll);
        userchartsalbumlabel = new JLabel(LFMValues.language.translate("ui.lastfm.userchartsalbums"));
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
                if(e.getClickCount() == 2) {
                    ContentPanel.switchView(ContentPanel.lastView);
                    ContentPanel.trackPanel.open(SpotifyMatcher.getAlbumURI(getSearchString(userchartsalbums, userchartsalbums.getSelectedRow())), HomePanel.ContentTypes.album);
                }
            }
        });
        userchartsalbumsscroll = new JScrollPane(userchartsalbums);
        userchartspanel.add(userchartsalbumsscroll);
        userchartstrackslabel = new JLabel(LFMValues.language.translate("ui.lastfm.userchartstracks"));
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
                    InstanceManager.getSpotifyPlayer().load(SpotifyMatcher.getTrackURI(getSearchString(userchartstracks, userchartstracks.getSelectedRow())), true, false);
                    SpotifyXPEvents.queueUpdate.trigger();
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
        userchartspanel.add(userchartstracksscroll);

        chartsartistslabel = new JLabel(LFMValues.language.translate("ui.lastfm.chartsartists"));
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
                            for(Artist a : Chart.getTopArtists(chartsartistscurrent, LFMValues.config.getFields().apiKey)) {
                                chartsartists.addModifyAction(() -> {
                                    ((DefaultTableModel) chartsartists.getModel()).addRow(new Object[]{a.getName(), a.getPlaycount()});
                                });
                            }
                            if(Chart.getTopArtists(chartsartistscurrent, LFMValues.config.getFields().apiKey).getTotalPages() != chartsartistscurrent) {
                                chartsartists.addModifyAction(() -> ((DefaultTableModel) chartsartists.getModel()).addRow(new Object[]{LFMValues.language.translate("ui.general.loadmore"), LFMValues.language.translate("ui.general.loadmore")}));
                            }
                        });
                        thread.start();
                        return;
                    }
                    ContentPanel.switchView(ContentPanel.lastView);
                    ContentPanel.showArtistPanel(SpotifyMatcher.getArtistURI(getSearchString(chartsartists, chartsartists.getSelectedRow())));
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
        chartspanel.add(chartsartistsscroll);
        chartstrackslabel = new JLabel(LFMValues.language.translate("ui.lastfm.chartstracks"));
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
                            for(Track t : Chart.getTopTracks(chartstrackscurrent, LFMValues.config.getFields().apiKey)) {
                                chartstracks.addModifyAction(() -> {
                                    ((DefaultTableModel) chartstracks.getModel()).addRow(new Object[]{t.getName(), t.getArtist(), t.getPlaycount()});
                                });
                            }
                            if(Chart.getTopTracks(chartstrackscurrent, LFMValues.config.getFields().apiKey).getTotalPages() != chartstrackscurrent) {
                                chartstracks.addModifyAction(() -> ((DefaultTableModel) chartstracks.getModel()).addRow(new Object[]{LFMValues.language.translate("ui.general.loadmore"), LFMValues.language.translate("ui.general.loadmore"), LFMValues.language.translate("ui.general.loadmore")}));
                            }
                        });
                        thread.start();
                        return;
                    }
                    InstanceManager.getSpotifyPlayer().load(SpotifyMatcher.getTrackURI(getSearchString(chartstracks, chartstracks.getSelectedRow())), true, false);
                    SpotifyXPEvents.queueUpdate.trigger();
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
        chartspanel.add(chartstracksscroll);
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
            for(Track t1 : User.getRecentTracks(LFMValues.config.getFields().username, scrobblescurrent, LFMValues.config.getFields().trackLimit, LFMValues.config.getFields().apiKey)) {
                scrobblestable.addModifyAction(() -> {
                    try {
                        ((DefaultTableModel) scrobblestable.getModel()).addRow(new Object[]{t1.getName(), t1.getArtist(), formatDate(t1.getPlayedWhen())});
                    }catch (NullPointerException e) {
                        ConsoleLogging.Throwable(e);
                    }
                    });
            }
            if(User.getRecentTracks(LFMValues.config.getFields().username, scrobblescurrent, LFMValues.config.getFields().trackLimit, LFMValues.config.getFields().apiKey).getTotalPages() != scrobblescurrent) {
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
            for(Artist a : User.getTopArtists(LFMValues.config.getFields().username, Period.OVERALL, LFMValues.config.getFields().trackLimit, LFMValues.config.getFields().apiKey)) {
                userchartsartists.addModifyAction(() -> {
                    ((DefaultTableModel) userchartsartists.getModel()).addRow(new Object[]{a.getName(), a.getPlaycount()});
                });
            }
        });
        artistthread.start();

        Thread albumthread = new Thread(() -> {
            for(Album a : User.getTopAlbums(LFMValues.config.getFields().username, Period.OVERALL, LFMValues.config.getFields().apiKey)) {
                userchartsalbums.addModifyAction(() -> {
                    ((DefaultTableModel) userchartsalbums.getModel()).addRow(new Object[]{a.getName(), a.getArtist(), a.getPlaycount()});
                });
            }
        });
        albumthread.start();

        Thread trackthread = new Thread(() -> {
            for(Track t : User.getTopTracks(LFMValues.config.getFields().username, Period.OVERALL, LFMValues.config.getFields().apiKey)) {
                userchartstracks.addModifyAction(() -> {
                    ((DefaultTableModel) userchartstracks.getModel()).addRow(new Object[]{t.getName(), t.getArtist(), t.getPlaycount()});
                });
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
            for(Artist a : Chart.getTopArtists(chartsartistscurrent, LFMValues.config.getFields().apiKey)) {
                chartsartists.addModifyAction(() -> {
                    ((DefaultTableModel) chartsartists.getModel()).addRow(new Object[]{a.getName(), a.getPlaycount()});
                });
            }
            if(Chart.getTopArtists(chartsartistscurrent, LFMValues.config.getFields().apiKey).getTotalPages() != chartsartistscurrent) {
                chartsartists.addModifyAction(() -> ((DefaultTableModel) chartsartists.getModel()).addRow(new Object[]{LFMValues.language.translate("ui.general.loadmore"), LFMValues.language.translate("ui.general.loadmore")}));
            }
        });
        artistthread.start();

        Thread trackthread = new Thread(() -> {
            chartstrackscurrent++;
            for(Track t : Chart.getTopTracks(chartstrackscurrent, LFMValues.config.getFields().apiKey)) {
                chartstracks.addModifyAction(() -> {
                    ((DefaultTableModel) chartstracks.getModel()).addRow(new Object[]{t.getName(), t.getArtist(), t.getPlaycount()});
                });
            }
            if(Chart.getTopTracks(chartstrackscurrent, LFMValues.config.getFields().apiKey).getTotalPages() != chartstrackscurrent) {
                chartstracks.addModifyAction(() -> ((DefaultTableModel) chartstracks.getModel()).addRow(new Object[]{LFMValues.language.translate("ui.general.loadmore"), LFMValues.language.translate("ui.general.loadmore"), LFMValues.language.translate("ui.general.loadmore")}));
            }
        });
        trackthread.start();
    }

    @Override
    public void open() {
        super.open();
        resizeComponents();
        revalidate();
        repaint();
    }

    public static void openWhenLoggedIn() {
        try {
            if (frame.isVisible()) {
                return;
            }
        }catch (NullPointerException ignored) {
        }
        if(LFMValues.config.getFields().username.isEmpty() || LFMValues.config.getFields().password.isEmpty()) {
            JOptionPane.showMessageDialog(ContentPanel.frame, LFMValues.language.translate("loginfirst.dialog.message"));
            return;
        }
        LastFMDialog dialog = new LastFMDialog();
        dialog.open();
    }
}
