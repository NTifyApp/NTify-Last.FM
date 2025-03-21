package com.spotifyxp.lastfm;

import com.spotifyxp.deps.com.spotify.metadata.Metadata;
import com.spotifyxp.deps.de.umass.lastfm.Authenticator;
import com.spotifyxp.deps.de.umass.lastfm.Track;
import com.spotifyxp.deps.de.umass.lastfm.exceptions.BadCredentialsException;
import com.spotifyxp.deps.de.umass.lastfm.scrobble.ScrobbleData;
import com.spotifyxp.deps.xyz.gianlu.librespot.audio.MetadataWrapper;
import com.spotifyxp.deps.xyz.gianlu.librespot.metadata.PlayableId;
import com.spotifyxp.deps.xyz.gianlu.librespot.player.Player;
import com.spotifyxp.lastfm.config.ConfigValues;
import com.spotifyxp.logging.ConsoleLogging;
import com.spotifyxp.manager.InstanceManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

import java.time.Instant;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

// https://www.last.fm/api/scrobbling
public class LFMScrobbling implements Player.EventsListener {
    boolean pauseTimer = false;
    long actuallyListenedS = 0;
    long durationOfTrack = 0;
    boolean wasReported = false;

    class PlayerThread extends TimerTask {
        public void run() {
            if (!pauseTimer) {
                if (!InstanceManager.getSpotifyPlayer().isPaused()) {
                    if(wasReported) return;
                    // And the track has been played for at least half its duration, or for 4 minutes (whichever occurs earlier.)
                    if(actuallyListenedS == durationOfTrack / 2 || actuallyListenedS == 240) {
                        if(InstanceManager.getSpotifyPlayer().currentMetadata() == null) {
                            actuallyListenedS++;
                            return;
                        }
                        if(!InstanceManager.getSpotifyPlayer().currentMetadata().isTrack()) {
                            actuallyListenedS++;
                            return;
                        }
                        MetadataWrapper metadata = InstanceManager.getSpotifyPlayer().currentMetadata();
                        try {
                            ScrobbleData scrobbleData = new ScrobbleData();
                            // artist[i] (Required) : The artist name.
                            scrobbleData.setArtist(metadata.getArtist());
                            // track[i] (Required) : The track name.
                            scrobbleData.setTrack(metadata.track.getName());
                            // timestamp[i] (Required) : The time the track started playing, in UNIX timestamp format
                            // (integer number of seconds since 00:00:00, January 1st 1970 UTC). This must be in the UTC time zone.
                            scrobbleData.setTimestamp((int) Instant.now().minusSeconds(actuallyListenedS).getEpochSecond());
                            // album[i] (Optional) : The album name.
                            scrobbleData.setAlbum(metadata.getAlbumName());
                            // duration[i] (Optional) : The length of the track in seconds.
                            scrobbleData.setDuration((int) TimeUnit.MILLISECONDS.toSeconds(metadata.duration()));

                            Track.scrobble(scrobbleData, Authenticator.getMobileSession(
                                    LFMValues.config.getString(ConfigValues.lastfmusername.name),
                                    LFMValues.config.getString(ConfigValues.lastfmpassword.name),
                                    LFMValues.apikey, LFMValues.apisecret
                            ));
                            wasReported = true;
                        } catch (BadCredentialsException e) {
                            ConsoleLogging.Throwable(e);
                        }
                    }
                    actuallyListenedS++;
                }
            }
        }
    }

    public static Timer timer = new Timer();

    public LFMScrobbling() {
        timer.schedule(new PlayerThread(), 0, 1000);
    }

    @Override
    public void onContextChanged(@NotNull Player player, @NotNull String s) {

    }

    @Override
    public void onTrackChanged(@NotNull Player player, @NotNull PlayableId playableId, @Nullable MetadataWrapper metadataWrapper, boolean b) {
        actuallyListenedS = 0;
        wasReported = false;
        if(metadataWrapper != null && metadataWrapper.isTrack()) {
            // The track must be longer than 30 seconds.
            pauseTimer = !(TimeUnit.MILLISECONDS.toSeconds(metadataWrapper.duration()) > 30);

            //Used to notify Last.fm that a user has started listening to a track. Parameter names are case sensitive.
            try {
                ScrobbleData scrobbleData = new ScrobbleData();

                // track (Required) : The track name.
                scrobbleData.setTrack(metadataWrapper.track.getName());

                // album (Optional) : The album name.
                scrobbleData.setAlbum(metadataWrapper.getAlbumName());

                // artist (Required) : The artist name.
                scrobbleData.setArtist(metadataWrapper.getArtist());

                // duration (Optional) : The length of the track in seconds.
                scrobbleData.setDuration((int) TimeUnit.MILLISECONDS.toSeconds(metadataWrapper.duration()));

                Track.updateNowPlaying(scrobbleData, Authenticator.getMobileSession(
                        LFMValues.config.getString(ConfigValues.lastfmusername.name),
                        LFMValues.config.getString(ConfigValues.lastfmpassword.name),
                        LFMValues.apikey, LFMValues.apisecret
                ));
            } catch (BadCredentialsException e) {
                ConsoleLogging.Throwable(e);
            }
        }
    }

    @Override
    public void onPlaybackEnded(@NotNull Player player) {

    }

    @Override
    public void onPlaybackPaused(@NotNull Player player, long l) {
        pauseTimer = true;
    }

    @Override
    public void onPlaybackResumed(@NotNull Player player, long l) {
        pauseTimer = false;
    }

    @Override
    public void onPlaybackFailed(@NotNull Player player, @NotNull Exception e) {
        pauseTimer = true;
    }

    @Override
    public void onTrackSeeked(@NotNull Player player, long l) {

    }

    @Override
    public void onMetadataAvailable(@NotNull Player player, @NotNull MetadataWrapper metadataWrapper) {

    }

    @Override
    public void onPlaybackHaltStateChanged(@NotNull Player player, boolean b, long l) {

    }

    @Override
    public void onInactiveSession(@NotNull Player player, boolean b) {

    }

    @Override
    public void onVolumeChanged(@NotNull Player player, @Range(from = 0L, to = 1L) float v) {

    }

    @Override
    public void onPanicState(@NotNull Player player) {
        pauseTimer = true;
    }

    @Override
    public void onStartedLoading(@NotNull Player player) {

    }

    @Override
    public void onFinishedLoading(@NotNull Player player) {

    }
}
