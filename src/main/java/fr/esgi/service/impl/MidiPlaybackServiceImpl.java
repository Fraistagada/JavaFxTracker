package fr.esgi.service.impl;

import fr.esgi.constants.Constants;
import fr.esgi.constants.Effects;
import fr.esgi.constants.Instruments;
import fr.esgi.models.PatternRow;
import fr.esgi.service.MidiPlaybackService;
import fr.esgi.service.PlaybackListener;
import fr.esgi.service.SchedulerService;

import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Synthesizer;
import java.util.List;

public class MidiPlaybackServiceImpl implements MidiPlaybackService {

    private final Synthesizer synth;
    private final SchedulerService schedulerService;

    private Thread playThread;
    private volatile boolean isPlaying = false;
    private volatile boolean isPaused = false;
    private int currentRowIndex = 0;

    private volatile int bpm = 120;

    public MidiPlaybackServiceImpl(SchedulerService schedulerService) throws MidiUnavailableException {
        this.schedulerService = schedulerService;
        this.synth = MidiSystem.getSynthesizer();
    }

    @Override
    public void startPlayback(List<PatternRow> pattern, int startIndex, int bpm, PlaybackListener listener) throws MidiUnavailableException {
        if (isPlaying && !isPaused) return;
        if (isPaused) {
            isPaused = false;
            return;
        }

        this.bpm = bpm;

        isPlaying = true;
        isPaused = false;
        synth.open();

        playThread = new Thread(() -> {
            try {
                for (int i = startIndex; i < pattern.size() && isPlaying; i++) {
                    while (isPaused) {
                        Thread.sleep(50);
                    }

                    currentRowIndex = i;
                    PatternRow row = pattern.get(i);

                    if (row.hasNote(1)) {
                        playSample(1, row.getNote(1), row.getOctave(1), row.getInstrument(1), Integer.parseInt(row.getVolume(1)), row.getEffect(1));
                    }

                    if (row.hasNote(2)) {
                        playSample(2, row.getNote(2), row.getOctave(2), row.getInstrument(2), Integer.parseInt(row.getVolume(2)), row.getEffect(2));
                    }

                    if (listener != null) listener.onRowPlayed(currentRowIndex);

                    int delay = (int) ((60.0 / this.bpm) * 1000 / 4);
                    Thread.sleep(delay);
                }

            } catch (InterruptedException ignored) {
            } finally {
                if (!isPaused) {
                    if (listener != null) listener.onPlaybackEnded();
                    currentRowIndex = 0;
                }
            }
        });

        playThread.start();
    }

    @Override
    public void stop() {
        isPlaying = false;
        isPaused = false;
        currentRowIndex = 0;

        if (playThread != null && playThread.isAlive()) {
            playThread.interrupt();
            try {
                playThread.join(200);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (synth.isOpen()) {
            for (MidiChannel channel : synth.getChannels()) {
                channel.allNotesOff();
                channel.setPitchBend(8192);
            }
            synth.close();
        }

        this.bpm = 120;
    }

    @Override
    public void pause() {
        isPaused = true;

        if (synth.isOpen()) {
            for (MidiChannel channel : synth.getChannels()) {
                channel.allNotesOff();
            }
        }
    }

    @Override
    public void resume() {
        isPaused = false;
    }

    @Override
    public boolean isPlaying() {
        return isPlaying && !isPaused;
    }

    @Override
    public void cleanup() {
        stop();
    }

    @Override
    public void playImmediate(int channel, String note, String octave, String instrument, int volume, String effect) throws MidiUnavailableException {
        MidiChannel midiChannel = synth.getChannels()[channel - 1];
        int instrumentId = Instruments.getMidiId(instrument);
        midiChannel.programChange(instrumentId);

        int noteMidi = 12 * (Integer.parseInt(octave) + 1) + Constants.NOTES.get(note);
        int duration = (int) ((60.0 / 120) * 1000);
        int vel = Math.max(0, Math.min(127, volume));

        Effects.EffectData fx = Effects.parse(effect);

        if (!fx.isEmpty()) {
            switch (fx.type) {
                case "8" -> midiChannel.controlChange(10, Math.min(127, fx.fullParam));
                case "C" -> vel = Math.min(127, fx.fullParam);
            }
        }

        midiChannel.noteOn(noteMidi, vel);
        if (schedulerService != null) {
            schedulerService.schedule(() -> midiChannel.noteOff(noteMidi), duration);
        }
    }

    private void playSample(int channel, String note, String octave, String instrument, int volume, String effect) {
        if (note == null || note.equals("---") || octave == null || octave.equals("-")) return;

        MidiChannel midiChannel = synth.getChannels()[channel - 1];

        int instrumentId = Instruments.getMidiId(instrument);
        midiChannel.programChange(instrumentId);

        int noteMidi = noteToMidi(note, Integer.parseInt(octave));
        int duration = (int) ((60.0 / this.bpm) * 1000);

        int vel = Math.max(0, Math.min(127, volume));

        Effects.EffectData fx = Effects.parse(effect);

        if (!fx.isEmpty()) {
            switch (fx.type) {
                case "8" -> {
                    int pan = Math.min(127, fx.fullParam);
                    midiChannel.controlChange(10, pan);
                }
                case "C" -> vel = Math.min(127, fx.fullParam);
                case "F" -> {
                }
            }
        }

        midiChannel.noteOn(noteMidi, vel);

        if (!fx.isEmpty()) {
            switch (fx.type) {
                case "4" -> {
                    int speed = fx.param1;
                    int depth = fx.param2;
                    if (speed > 0 && depth > 0) {
                        applyVibrato(midiChannel, noteMidi, speed, depth, duration);
                    }
                }
                case "0" -> {
                    if (fx.param1 > 0 || fx.param2 > 0) {
                        applyArpeggio(midiChannel, noteMidi, fx.param1, fx.param2, vel, duration);
                        return;
                    }
                }
            }
        }

        if (schedulerService != null) {
            schedulerService.schedule(() -> midiChannel.noteOff(noteMidi), duration);
        }
    }

    private void applyVibrato(MidiChannel channel, int note, int speed, int depth, int duration) {
        int steps = Math.max(1, duration / 50);
        int currentDepth = depth * 100;

        for (int i = 0; i < steps; i++) {
            final int step = i;
            if (schedulerService != null) {
                schedulerService.schedule(() -> {
                    double angle = (step * speed * Math.PI) / 8;
                    int bend = (int) (8192 + Math.sin(angle) * currentDepth);
                    bend = Math.max(0, Math.min(16383, bend));
                    channel.setPitchBend(bend);
                }, i * 50L);
            }
        }

        if (schedulerService != null) {
            schedulerService.schedule(() -> channel.setPitchBend(8192), duration);
        }
    }

    private void applyArpeggio(MidiChannel channel, int baseNote, int semi1, int semi2, int velocity, int duration) {
        int[] notes = {baseNote, baseNote + semi1, baseNote + semi2};
        int stepDuration = Math.max(1, duration / 6);

        for (int i = 0; i < 6; i++) {
            final int noteIndex = i % 3;
            final int currentNote = notes[noteIndex];

            if (schedulerService != null) {
                schedulerService.schedule(() -> {
                    channel.allNotesOff();
                    channel.noteOn(currentNote, velocity);
                }, i * stepDuration);
            }
        }

        if (schedulerService != null) {
            schedulerService.schedule(() -> channel.noteOff(notes[0]), duration);
        }
    }

    private int noteToMidi(String note, int octave) {
        return 12 * (octave + 1) + Constants.NOTES.get(note);
    }
}
