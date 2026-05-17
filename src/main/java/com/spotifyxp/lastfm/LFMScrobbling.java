package com.spotifyxp.lastfm;

import de.umass.lastfm.Authenticator;
import de.umass.lastfm.CallException;
import de.umass.lastfm.Track;
import de.umass.lastfm.scrobble.ScrobbleData;
import xyz.gianlu.librespot.audio.MetadataWrapper;
import xyz.gianlu.librespot.metadata.PlayableId;
import xyz.gianlu.librespot.player.Player;
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
    public boolean pauseTimer = false;
    public long actuallyListenedS = 0;
    public volatile long durationOfTrack = 0;
    public Timer timer = new Timer();
    public ScrobbleData scrobbleDataCurrentTrack;

    class PlayerThread extends TimerTask {
        public void run() {
            if (!pauseTimer) {
                if (durationOfTrack == 0) return;

                if (!InstanceManager.getSpotifyPlayer().isPaused()) {
                    // And the track has been played for at least half its duration, or for 4 minutes (whichever occurs earlier.)
                    if(actuallyListenedS >= durationOfTrack / 2 || actuallyListenedS >= 240) {
                        scrobbleTrack();
                        pauseTimer = true;
                    }

                    actuallyListenedS = TimeUnit.MILLISECONDS.toSeconds(InstanceManager.getSpotifyPlayer().time());
                }
            }
        }
    }

    @Override
    public void onContextChanged(@NotNull Player player, @NotNull String s) {
    }

    public void scrobbleTrack() {
        //Used to notify Last.fm that a user has started listening to a track. Parameter names are case sensitive.
        try {
            Track.scrobble(scrobbleDataCurrentTrack, LFMValues.getSession());
        } catch (CallException e) {
            ConsoleLogging.Throwable(e);
        }
    }

    @Override
    public void onTrackChanged(@NotNull Player player, @NotNull PlayableId playableId, @Nullable MetadataWrapper metadataWrapper, boolean b) {
        actuallyListenedS = 0;
    }

    public void triggerNewTrack(MetadataWrapper metadataWrapper) {
        durationOfTrack = TimeUnit.MILLISECONDS.toSeconds(metadataWrapper.duration());

        // The track must be longer than 30 seconds.
        pauseTimer = !(TimeUnit.MILLISECONDS.toSeconds(metadataWrapper.duration()) > 30);

        ScrobbleData scrobbleData = new ScrobbleData();

        // track (Required) : The track name.
        scrobbleData.setTrack(metadataWrapper.track.getName());

        // album (Optional) : The album name.
        scrobbleData.setAlbum(metadataWrapper.getAlbumName());

        // artist (Required) : The artist name.
        scrobbleData.setArtist(metadataWrapper.getArtist());

        // duration (Optional) : The length of the track in seconds.
        scrobbleData.setDuration((int) TimeUnit.MILLISECONDS.toSeconds(metadataWrapper.duration()));

        // timestamp[i] (Required) : The time the track started playing, in UNIX timestamp format (integer number of seconds since 00:00:00, January 1st 1970 UTC). This must be in the UTC time zone.
        scrobbleData.setTimestamp((int) Instant.now().getEpochSecond());

        scrobbleDataCurrentTrack = scrobbleData;

        Track.updateNowPlaying(scrobbleData, LFMValues.getSession());
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
        if(metadataWrapper.isTrack()) {
            triggerNewTrack(metadataWrapper);
        }
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

    public void init() {
        timer.schedule(new PlayerThread(), 0, 1000);
    }
}
