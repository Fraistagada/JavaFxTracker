package fr.esgi.service;

import fr.esgi.models.PatternRow;
import fr.esgi.service.MidiPlaybackService;
import fr.esgi.service.PlaybackListener;

import javax.sound.midi.MidiUnavailableException;
import java.util.List;

import static fr.esgi.utils.FxUtils.showError;

public class NoopMidiPlaybackService implements MidiPlaybackService {
    @Override
    public void startPlayback(List<PatternRow> pattern, int startIndex, int bpm, PlaybackListener listener) throws MidiUnavailableException {
        showError("Erreur MIDI", "Synthétiseur MIDI indisponible sur cette machine.");
        if (listener != null) listener.onPlaybackEnded();
    }

    @Override
    public void stop() {
        // no-op
    }

    @Override
    public void pause() {
        // no-op
    }

    @Override
    public void resume() {
        // no-op
    }

    @Override
    public boolean isPlaying() {
        return false;
    }

    @Override
    public void cleanup() {
        // no-op
    }

    @Override
    public void playImmediate(int channel, String note, String octave, String instrument, int volume, String effect) throws MidiUnavailableException {
        showError("Erreur MIDI", "Synthétiseur MIDI indisponible : impossible de jouer la note.");
    }
}

