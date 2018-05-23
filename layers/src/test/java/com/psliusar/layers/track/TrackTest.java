package com.psliusar.layers.track;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TrackTest {

    private SimpleSyncTrack track;
    private MockedCallbacks listener;

    @Before
    public void setUp() {
        track = new SimpleSyncTrack(new String[] { "Hello", "world" });
        listener = new MockedCallbacks();
    }

    @Test
    public void testSingleRun() {
        track.subscribe(listener);
        track.start();

        assertTrue(listener.singleShot());
        assertEquals("Hello, world", listener.getValue());
    }

    @Test
    public void testSingleRunRestart() {
        track.subscribe(listener);
        track.start();
        track.restart();

        assertTrue(listener.shotTimes(2));
        assertTrue(listener.restartTimes(1));
        assertEquals("Hello, world", listener.getValue());
    }

    @Test
    public void testSingleRunReSubscribe() {
        track.subscribe(listener);
        track.start();
        track.unSubscribe();
        track.subscribe(listener);
        track.start();

        assertTrue(listener.shotTimes(2));
        assertTrue(listener.restartTimes(0));
        assertEquals("Hello, world", listener.getValue());
    }
}