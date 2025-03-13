package com.spotifyxp.lastfm;

import com.spotifyxp.manager.InstanceManager;
import java.io.IOException;

public class LastFMConverter {
    public static String getArtistURIfromName(String name) {
        try {
            return InstanceManager.getSpotifyApi().searchArtists(name).build().execute().getItems()[0].getUri();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static String getAlbumURIfromName(String name) {
        try {
            return InstanceManager.getSpotifyApi().searchAlbums(name).build().execute().getItems()[0].getUri();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static String getTrackURIfromName(String name) {
        try {
            return InstanceManager.getSpotifyApi().searchTracks(name).build().execute().getItems()[0].getUri();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}