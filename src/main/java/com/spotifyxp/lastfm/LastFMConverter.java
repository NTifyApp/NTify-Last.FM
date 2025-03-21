package com.spotifyxp.lastfm;

import com.spotifyxp.manager.InstanceManager;
import java.io.IOException;

public class LastFMConverter {
    public static String getArtistURI(String query) {
        try {
            return InstanceManager.getSpotifyApi().searchArtists(query).build().execute().getItems()[0].getUri();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static String getAlbumURI(String query) {
        try {
            return InstanceManager.getSpotifyApi().searchAlbums(query).build().execute().getItems()[0].getUri();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static String getTrackURI(String query) {
        try {
            return InstanceManager.getSpotifyApi().searchTracks(query).build().execute().getItems()[0].getUri();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}