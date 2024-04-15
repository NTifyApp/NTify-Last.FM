package com.spotifyxp.lastfm.panels;

import com.spotifyxp.PublicValues;
import com.spotifyxp.configuration.ConfigValues;
import com.spotifyxp.deps.de.umass.lastfm.Artist;
import com.spotifyxp.deps.se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import com.spotifyxp.deps.se.michaelthelin.spotify.model_objects.specification.Track;
import com.spotifyxp.guielements.DefTable;
import com.spotifyxp.lastfm.LFMValues;
import com.spotifyxp.lastfm.LastFMConverter;
import com.spotifyxp.logging.ConsoleLogging;
import com.spotifyxp.manager.InstanceManager;
import com.spotifyxp.panels.ArtistPanel;
import com.spotifyxp.panels.ContentPanel;
import com.spotifyxp.threading.DefThread;
import com.spotifyxp.utils.ClipboardUtil;
import com.spotifyxp.utils.SpotifyUtils;
import com.spotifyxp.utils.TrackUtils;
import org.apache.hc.core5.http.ParseException;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class LFMArtistPanel {
    public ArrayList<String> artistpopularuricache = new ArrayList<>();
    public ArrayList<String> artistalbumuricache = new ArrayList<>();
    public ArrayList<String> lastfmartisturicache = new ArrayList<>();
    public JScrollPane lastfmartistscrollpanel;
    public DefTable lastfmartisttable;
    public ArrayList<String> lastfmuricache = new ArrayList<>();
    public ArrayList<ArrayList<Object>> lastfmcache = new ArrayList<>();
    public ArrayList<ArrayList<Object>> albumcache = new ArrayList<>();
    public ArrayList<ArrayList<Object>> popularcache = new ArrayList<>();
    public String artistcache;
    public InputStream artistbackgroundcache;
    public InputStream artistimagecache;

    public LFMArtistPanel() {
        ContentPanel.artistPanel.setPreferredSize(new Dimension(ContentPanel.artistPanel.getWidth(), 1360));

        ArtistPanel.artistpopularsonglistcontextmenu.addItem(PublicValues.language.translate("ui.general.copyuri"), () -> ClipboardUtil.set(artistpopularuricache.get(ArtistPanel.artistpopularsonglist.getSelectedRow())));


        lastfmartisttable = new DefTable() {
        };

        lastfmartisttable.setModel(new DefaultTableModel(
                new Object[][]{
                },
                new String[]{
                        PublicValues.language.translate("ui.artist.tablename")
                }
        ));

        lastfmartistscrollpanel = new JScrollPane(lastfmartisttable);

        lastfmartisttable.setForeground(PublicValues.globalFontColor);
        lastfmartisttable.getTableHeader().setForeground(PublicValues.globalFontColor);

        JLabel lastfmsimilarartistslabel = new JLabel("Last.fm  Similar Artists");
        lastfmsimilarartistslabel.setBounds(5, 1000, 212, 18);
        ContentPanel.artistPanel.add(lastfmsimilarartistslabel);

        lastfmartisttable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if(e.getClickCount() == 2) {
                    prepareSwitch();
                    try {
                        com.spotifyxp.deps.se.michaelthelin.spotify.model_objects.specification.Artist a = InstanceManager.getSpotifyApi().getArtist(lastfmartisturicache.get(lastfmartisttable.getSelectedRow()).split(":")[2]).build().execute();
                        try {
                            ArtistPanel.artistimage.setImage(new URL(SpotifyUtils.getImageForSystem(a.getImages()).getUrl()).openStream());
                        } catch (ArrayIndexOutOfBoundsException exception) {
                            //No artist image (when this is raised it's a bug)
                        }
                        ArtistPanel.artisttitle.setText(a.getName());
                        DefThread trackthread = new DefThread(() -> {
                            try {
                                for (Track t : InstanceManager.getSpotifyApi().getArtistsTopTracks(lastfmartisturicache.get(lastfmartisttable.getSelectedRow()).split(":")[2], PublicValues.countryCode).build().execute()) {
                                    ArtistPanel.popularuricache.add(t.getUri());
                                    InstanceManager.getSpotifyAPI().addSongToList(TrackUtils.getArtists(t.getArtists()), t, ArtistPanel.artistpopularsonglist);
                                }
                            } catch (IOException | ParseException | SpotifyWebApiException ex) {
                                ConsoleLogging.Throwable(ex);
                            }
                        });
                        DefThread albumthread = new DefThread(() -> InstanceManager.getSpotifyAPI().addAllAlbumsToList(ArtistPanel.albumuricache, lastfmartisturicache.get(lastfmartisttable.getSelectedRow()), ArtistPanel.artistalbumalbumtable));
                        albumthread.start();
                        trackthread.start();
                        lastfmsimilarartistslabel.setEnabled(false);
                        lastfmartisttable.setEnabled(false);
                        PublicValues.blockArtistPanelBackButton = true;
                        javax.swing.SwingUtilities.invokeLater(() -> ArtistPanel.contentPanel.getVerticalScrollBar().setValue(0));
                        ContentPanel.artistPanelBackButton.addActionListener(e1 -> {
                            if(!PublicValues.blockArtistPanelBackButton) {
                                return;
                            }
                            lastfmsimilarartistslabel.setEnabled(true);
                            lastfmartisttable.setEnabled(false);
                            restoreCache();
                            clearCache();
                            SwingUtilities.invokeLater(() -> ArtistPanel.contentPanel.getVerticalScrollBar().setValue(0));
                            DefThread thread = new DefThread(() -> {
                                while(ContentPanel.artistPanelBackButton.getModel().isPressed()) {
                                    try {
                                        Thread.sleep(TimeUnit.SECONDS.toMillis(1));
                                    }catch (Exception ignored) {
                                    }
                                }
                                PublicValues.blockArtistPanelBackButton = false;
                            });
                            thread.start();
                        });
                        finalizeSwitch();
                    }catch (Exception e2) {
                        throw new RuntimeException(e2);
                    }
                }
            }
        });

        lastfmsimilarartistslabel.setForeground(PublicValues.globalFontColor);

        lastfmartistscrollpanel.setBounds(5, 1035, 760, 295);
        ContentPanel.artistPanel.add(lastfmartistscrollpanel);

        ArtistPanel.runWhenOpeningArtistPanel.add(new Runnable() {
            @Override
            public void run() {
                lastfmartisturicache.clear();
                ((DefaultTableModel) lastfmartisttable.getModel()).setRowCount(0);
                DefThread thread = new DefThread(() -> {
                    if(PublicValues.config.getString(ConfigValues.lastfmusername.name).equalsIgnoreCase("")) {
                        return;
                    }
                    for(Artist a : Artist.getSimilar(ArtistPanel.artisttitle.getText(), 10, LFMValues.apikey)) {
                        lastfmartisttable.addModifyAction(() -> {
                            ((DefaultTableModel) lastfmartisttable.getModel()).addRow(new Object[]{a.getName()});
                            lastfmartisturicache.add(LastFMConverter.getArtistURIfromName(a.getName()));
                        });
                    }
                });
                thread.start();
            }
        });
    }

    public void buildCache() {
        albumcache = parseTable(ArtistPanel.artistalbumalbumtable);
        popularcache = parseTable(ArtistPanel.artistpopularsonglist);
        lastfmcache = parseTable(lastfmartisttable);
        artistcache = ArtistPanel.artisttitle.getText();
        artistimagecache = ArtistPanel.artistimage.getImageStream();
        if(ArtistPanel.artistbackgroundimage.getImageStream() != null) artistbackgroundcache = ArtistPanel.artistbackgroundimage.getImageStream();
    }

    public void finalizeSwitch() {
        ((DefaultTableModel) lastfmartisttable.getModel()).setRowCount(0);
        lastfmartisturicache.clear();
        ArtistPanel.contentPanel.getHorizontalScrollBar().setValue(ArtistPanel.contentPanel.getHorizontalScrollBar().getMinimum());
    }

    ArrayList<ArrayList<Object>> parseTable(DefTable table) {
        ArrayList<ArrayList<Object>> objects = new ArrayList<>();
        for(int row = 0; row < table.getModel().getRowCount(); row++) {
            ArrayList<Object> rows = new ArrayList<>();
            for (int col = 0; col < table.getModel().getColumnCount(); col++) {
                rows.add(table.getModel().getValueAt(row, col));
            }
            objects.add(rows);
        }
        return objects;
    }

    public void restoreTable(DefTable table, ArrayList<ArrayList<Object>> rows) {
        ((DefaultTableModel) table.getModel()).setRowCount(0);
        for(ArrayList<Object> o : rows) {
            table.addModifyAction(() -> ((DefaultTableModel) table.getModel()).addRow(o.toArray()));
        }
    }

    public void prepareSwitch() {
        buildCache();
        ((DefaultTableModel) ArtistPanel.artistalbumalbumtable.getModel()).setRowCount(0);
        ArtistPanel.albumuricache.clear();
        ArtistPanel.artisttitle.setText("");
        ((DefaultTableModel) ArtistPanel.artistpopularsonglist.getModel()).setRowCount(0);
        ArtistPanel.popularuricache.clear();
    }

    public void restoreCache() {
        restoreTable(lastfmartisttable, lastfmcache);
        restoreTable(ArtistPanel.artistalbumalbumtable, albumcache);
        restoreTable(ArtistPanel.artistpopularsonglist, popularcache);
        ArtistPanel.albumuricache = artistalbumuricache;
        ArtistPanel.popularuricache = artistpopularuricache;
        lastfmartisturicache = lastfmuricache;
        ArtistPanel.artisttitle.setText(artistcache);
        ArtistPanel.artistimage.setImage(artistimagecache);
        if(artistbackgroundcache != null) ArtistPanel.artistbackgroundimage.setImage(artistbackgroundcache);
    }

    public void clearCache() {
        artistalbumuricache = new ArrayList<>();
        artistpopularuricache = new ArrayList<>();
        lastfmuricache = new ArrayList<>();
        albumcache = new ArrayList<>();
        popularcache = new ArrayList<>();
        lastfmcache = new ArrayList<>();
        artistcache = "";
        artistbackgroundcache = null;
        artistimagecache = null;
    }
}
