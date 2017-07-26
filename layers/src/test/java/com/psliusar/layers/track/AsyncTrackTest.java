package com.psliusar.layers.track;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AsyncTrackTest {

    private SimpleAsyncTrack track;
    private MockedListener listener;

    @Before
    public void setUp() {
        track = new SimpleAsyncTrack(new String[] { "Hello", "world" });
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
