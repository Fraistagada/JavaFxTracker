package fr.esgi.service;

public interface PlaybackListener {
    void onRowPlayed(int rowIndex);
    void onPlaybackEnded();
}

