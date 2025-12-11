package fr.esgi.service;

import fr.esgi.models.PatternRow;

import javax.sound.midi.MidiUnavailableException;
import java.util.List;

public interface MidiPlaybackService {
    void startPlayback(List<PatternRow> pattern, int startIndex, int bpm, PlaybackListener listener) throws MidiUnavailableException;
    void stop();
    void pause();
    void resume();
    boolean isPlaying();
    void cleanup();
    void playImmediate(int channel, String note, String octave, String instrument, int volume, String effect) throws MidiUnavailableException;
}

