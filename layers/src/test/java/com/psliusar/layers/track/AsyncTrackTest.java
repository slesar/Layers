package com.psliusar.layers.track;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AsyncTrackTest {

    private SimpleAsyncTrack track;
    private MockedCallbacks listener;

    @Before
    public void setUp() {
        track = new SimpleAsyncTrack(new String[] { "Hello", "world" });
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
}