package com.spotifyxp.lastfm;

import com.spotifyxp.api.UnofficialSpotifyAPI;
import com.spotifyxp.api.UnofficialSpotifyAPI.SearchV2Response;
import xyz.gianlu.librespot.core.TokenProvider;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Searches Spotify for tracks, albums, and artists using the librespot-java API.
 */
public class SpotifyMatcher {

    private static final int DEFAULT_LIMIT = 10;

    // --- Result classes ---

    public static class TrackResult {
        public final String uri;
        public final String name;
        public final String albumName;
        public final List<String> artistNames;
        public final long durationMs;

        public TrackResult(String uri, String name, String albumName, List<String> artistNames, long durationMs) {
            this.uri = uri;
            this.name = name;
            this.albumName = albumName;
            this.artistNames = Collections.unmodifiableList(artistNames);
            this.durationMs = durationMs;
        }
    }

    public static class AlbumResult {
        public final String uri;
        public final String name;
        public final List<String> artistNames;
        public final String type;
        public final Integer year;

        public AlbumResult(String uri, String name, List<String> artistNames, String type, Integer year) {
            this.uri = uri;
            this.name = name;
            this.artistNames = Collections.unmodifiableList(artistNames);
            this.type = type;
            this.year = year;
        }
    }

    public static class ArtistResult {
        public final String uri;
        public final String name;

        public ArtistResult(String uri, String name) {
            this.uri = uri;
            this.name = name;
        }
    }

    // --- Search methods ---

    /**
     * Search for tracks matching the query.
     *
     * @param query  search term (e.g. track name, "artist - track")
     * @param limit  max number of results
     * @return list of matching tracks, empty list if nothing found
     */
    public static List<TrackResult> searchTracks(String query, int limit) {
        SearchV2Response response = executeSearch(query, limit);
        if (response == null
                || response.data == null
                || response.data.searchV2 == null
                || response.data.searchV2.tracksV2 == null
                || response.data.searchV2.tracksV2.items == null) {
            return Collections.emptyList();
        }

        List<TrackResult> results = new ArrayList<>();
        for (SearchV2Response.TracksV2Item item : response.data.searchV2.tracksV2.items) {
            if (item.item == null || item.item.data == null) continue;
            SearchV2Response.TrackData t = item.item.data;

            List<String> artists = new ArrayList<>();
            if (t.artists != null && t.artists.items != null) {
                for (SearchV2Response.ArtistItem a : t.artists.items) {
                    if (a.profile != null && a.profile.name != null) {
                        artists.add(a.profile.name);
                    }
                }
            }

            String albumName = (t.albumOfTrack != null) ? t.albumOfTrack.name : null;
            long durationMs = (t.duration != null && t.duration.totalMilliseconds != null)
                    ? t.duration.totalMilliseconds : 0;

            results.add(new TrackResult(t.uri, t.name, albumName, artists, durationMs));
        }
        return results;
    }

    public static List<TrackResult> searchTracks(String query) {
        return searchTracks(query, DEFAULT_LIMIT);
    }

    /**
     * Search for albums matching the query.
     *
     * @param query  search term (e.g. album name)
     * @param limit  max number of results
     * @return list of matching albums, empty list if nothing found
     */
    public static List<AlbumResult> searchAlbums(String query, int limit) {
        SearchV2Response response = executeSearch(query, limit);
        if (response == null
                || response.data == null
                || response.data.searchV2 == null
                || response.data.searchV2.albumsV2 == null
                || response.data.searchV2.albumsV2.items == null) {
            return Collections.emptyList();
        }

        List<AlbumResult> results = new ArrayList<>();
        for (SearchV2Response.AlbumResponseWrapper wrapper : response.data.searchV2.albumsV2.items) {
            if (wrapper.data == null) continue;
            SearchV2Response.Album album = wrapper.data;

            List<String> artists = new ArrayList<>();
            if (album.artists != null && album.artists.items != null) {
                for (SearchV2Response.ArtistItem a : album.artists.items) {
                    if (a.profile != null && a.profile.name != null) {
                        artists.add(a.profile.name);
                    }
                }
            }

            Integer year = (album.date != null) ? album.date.year : null;
            results.add(new AlbumResult(album.uri, album.name, artists, album.type, year));
        }
        return results;
    }

    public static List<AlbumResult> searchAlbums(String query) {
        return searchAlbums(query, DEFAULT_LIMIT);
    }

    /**
     * Search for artists matching the query.
     *
     * @param query  search term (e.g. artist name)
     * @param limit  max number of results
     * @return list of matching artists, empty list if nothing found
     */
    public static List<ArtistResult> searchArtists(String query, int limit) {
        SearchV2Response response = executeSearch(query, limit);
        if (response == null
                || response.data == null
                || response.data.searchV2 == null
                || response.data.searchV2.artists == null
                || response.data.searchV2.artists.items == null) {
            return Collections.emptyList();
        }

        List<ArtistResult> results = new ArrayList<>();
        for (SearchV2Response.ArtistsItemData item : response.data.searchV2.artists.items) {
            if (item.data == null) continue;
            String name = (item.data.profile != null) ? item.data.profile.name : null;
            results.add(new ArtistResult(item.data.uri, name));
        }
        return results;
    }

    public static List<ArtistResult> searchArtists(String query) {
        return searchArtists(query, DEFAULT_LIMIT);
    }

    // --- Convenience single-result methods (like LastFMConverter) ---

    /**
     * Returns the Spotify URI of the first matching track, or null if none found.
     */
    public static String getTrackURI(String query) {
        List<TrackResult> results = searchTracks(query, 1);
        return results.isEmpty() ? null : results.get(0).uri;
    }

    /**
     * Returns the Spotify URI of the first matching album, or null if none found.
     */
    public static String getAlbumURI(String query) {
        List<AlbumResult> results = searchAlbums(query, 1);
        return results.isEmpty() ? null : results.get(0).uri;
    }

    /**
     * Returns the Spotify URI of the first matching artist, or null if none found.
     */
    public static String getArtistURI(String query) {
        List<ArtistResult> results = searchArtists(query, 1);
        return results.isEmpty() ? null : results.get(0).uri;
    }

    // --- Internal ---

    private static SearchV2Response executeSearch(String query, int limit) {
        try {
            return UnofficialSpotifyAPI.search(
                    query, 0, limit, 1,
                    false, false, false, false
            );
        } catch (IOException | TokenProvider.TokenException e) {
            throw new RuntimeException(e);
        }
    }
}
