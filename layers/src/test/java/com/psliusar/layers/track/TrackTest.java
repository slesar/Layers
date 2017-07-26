package com.psliusar.layers.track;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class TrackTest {

    private SimpleSyncTrack track;
    private MockedListener listener;

    @Before
    public void setUp() {
        track = new SimpleSyncTrack(new String[] { "Hello", "world" });
        listener = new MockedListener();
    }

    @Test
    public void testSingleRun() {
        track.subscribe(listener);
        track.start();

        assertTrue(listener.isSingleShot());
        assertEquals("Hello, world", listener.getValue());
    }

    @Test
    public void testSingleRunRestart() {
        track.subscribe(listener);
        track.start();
        track.restart();

        assertTrue(listener.isShotTimes(2));
        assertEquals("Hello, world", listener.getValue());
    }
}