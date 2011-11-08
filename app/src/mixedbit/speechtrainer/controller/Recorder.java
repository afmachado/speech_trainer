/**
 * This file is part of Speech Trainer.
 * Copyright (C) 2011 Jan Wrobel <wrr@mixedbit.org>
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package mixedbit.speechtrainer.controller;

import mixedbit.speechtrainer.SpeechTrainerConfig;
import mixedbit.speechtrainer.controller.AudioBufferAllocator.AudioBuffer;
import android.media.AudioRecord;
import android.util.Log;

/**
 * Wrapper over AudioRecord that exposes minimal interface for recording
 * AudioBuffers. Informs AudioEventListener about each executed action
 * (recording started, audio buffer recorded, recording stopped). As in case of
 * the Player interface, this interface is extracted from the RecorderImpl to
 * allow mocking with the EasyMock.
 */
interface Recorder {

    /**
     * Starts recording. Calls to recordAudioBuffer are allowed only after
     * recording was started.
     */
    public abstract void startRecording();

    /**
     * Blocking call that records an audio buffer. Requires recording to be
     * started (startRecording called).
     */
    public abstract void recordAudioBuffer(AudioBuffer audioBuffer);

    /**
     * Stops recording. Recording can be started again with the startRecording
     * method.
     */
    public abstract void stopRecording();

}

class RecorderImpl implements Recorder {
    private final AudioRecord audioRecord;
    private final AudioEventListener audioEventListener;

    /**
     * @param audioRecord
     *            AudioRecord object configured by a caller.
     * @param audioEventListener
     *            Listener that is informed about each action executed by the
     *            recorder.
     */
    public RecorderImpl(AudioRecord audioRecord, AudioEventListener audioEventListener) {
        this.audioRecord = audioRecord;
        this.audioEventListener = audioEventListener;
    }

    @Override
    public void startRecording() {
        audioRecord.startRecording();
        audioEventListener.recordingStarted();
    }

    @Override
    public void recordAudioBuffer(AudioBuffer audioBuffer) {
        final short[] audioData = audioBuffer.getAudioData();
        final int dataRead = audioRecord.read(audioData, 0, audioData.length);
        if (dataRead <= 0) {
            Log.e(SpeechTrainerConfig.LOG_TAG, "Negative size of recorded data " + dataRead);
            audioBuffer.audioDataStored(0);
        } else {
            audioBuffer.audioDataStored(dataRead);
        }
        audioEventListener.audioBufferRecorded(
                audioBuffer.getAudioBufferId(), audioBuffer.getSoundLevel());
    }

    @Override
    public void stopRecording() {
        audioRecord.stop();
        audioEventListener.recordingStopped();
    }
}
