package com.psliusar.layers;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class LayersTest {

    private Layers layers;
    private ViewGroup container;

    @Before
    public void setUp() {
        container = mock(ViewGroup.class);

        LayoutInflater inflater = mock(LayoutInflater.class);
        when(inflater.inflate(0, container, false)).thenAnswer(new Answer<View>() {
            @Override
            public View answer(InvocationOnMock invocation) throws Throwable {
                return mock(View.class);
            }
        });

        Activity activity = mock(Activity.class);
        //when(activity.getApplicationContext()).thenReturn(context);
        when(activity.getLayoutInflater()).thenReturn(inflater);

        LayersHost host = mock(LayersHost.class);
        when(host.getView(0)).thenReturn(container);
        when(host.getActivity()).thenReturn(activity);

        layers = new Layers(host, 0, null);
    }

    /*@Test
    public void testAt() throws Exception {
        //noinspection ResourceType
        Layers layers1 = layers.at(1);
        //noinspection ResourceType
        Layers layers2 = layers.at(2);

        assertNotNull(layers1);
        assertNotNull(layers2);
        assertFalse(layers1 == layers2);
    }*/

    public void testStart() throws Exception {
        // TODO
    }

    public void testPauseView() throws Exception {
        // TODO
    }

    public void testResumeView() throws Exception {
        // TODO
    }

    public void testStop() throws Exception {
        // TODO
    }

    public void testSaveState() throws Exception {
        // TODO
        // create new instance of Layers
        // restore stack
        // compare old and new instances of same layer - shouldn't be equal
    }

    @Test
    public void testCreateLayer() throws Exception {
        // New Layer, without saved state
        MockedLayer layer = layers.add(MockedLayer.class, null, "Test Layer 1", true);

        assertNotNull(layer);
        assertEquals(1, layer.getCreateCalled());
        assertEquals(1, layer.getOnCreatePresenterCalled());
        assertEquals(1, layer.getOnCreateCalled());

        // Restored Layer, with saved state
        // TODO
    }

    @Test
    public void testCreateView() throws Exception {
        // New Layer, without saved state
        MockedLayer layer = layers.add(MockedLayer.class, null, "Test Layer 1", true);

        assertNotNull(layer);
        assertEquals(1, layer.getOnCreateViewCalled());
        assertEquals(1, layer.getOnAttachCalled());
        assertEquals(1, layer.getOnBindViewCalled());
        assertEquals(0, layer.getRestoreViewStateCalled());
        assertTrue(layer.isAttached());

        verify(container, times(1)).addView(layer.getView());

        // Restored Layer, with saved state
        // TODO
    }

    @Test
    public void testDestroyView() throws Exception {
        // Delete permanently
        MockedLayer layer = layers.add(MockedLayer.class, null, "Test Layer 1", true);
        View view = layer.getView();
        layers.remove(layer);

        assertEquals(0, layer.getSaveViewStateCalled());
        assertEquals(1, layer.getOnDetachCalled());
        verify(container, times(1)).removeView(view);
        assertEquals(1, layer.getOnDestroyViewCalled());
        assertFalse(layer.isAttached());
        assertNull(layer.getView());

        // Delete keeping in the stack (saving state)
        // TODO
    }

    @Test
    public void testDestroyLayer() throws Exception {
        // Delete permanently
        MockedLayer layer = layers.add(MockedLayer.class, null, "Test Layer 1", true);

        layers.remove(layer);

        assertEquals(1, layer.getOnDestroyCalled());

        // Delete keeping in the stack (saving state)
        // TODO
    }

    @Test
    public void testAdd() throws Exception {
        MockedLayer layer1 = layers.add(MockedLayer.class, null, "Test Layer 1", false);
        MockedLayer layer2 = layers.add(MockedLayer.class, null, "Test Layer 2", false);
        MockedLayer layer3 = layers.add(MockedLayer.class, null, "Test Layer 3", false);

        verify(container, times(0)).removeView(any(View.class));

        verify(container, times(1)).addView(layer1.getView());
        assertTrue(layer1.isAttached());
        assertNotNull(layer1.getView());

        verify(container, times(1)).addView(layer2.getView());
        assertTrue(layer2.isAttached());
        assertNotNull(layer2.getView());

        verify(container, times(1)).addView(layer3.getView());
        assertTrue(layer3.isAttached());
        assertNotNull(layer3.getView());

        // TODO add opaque
    }

    @Test
    public void testRemove() throws Exception {
        MockedLayer layer1 = layers.add(MockedLayer.class, null, "Test Layer 1", false);
        layers.remove(layer1);

        assertEquals(0, layers.getStackSize());

        MockedLayer layer2 = layers.add(MockedLayer.class, null, "Test Layer 2", false);
        layers.add(MockedLayer.class, null, "Test Layer 3", false);
        MockedLayer layer4 = layers.add(MockedLayer.class, null, "Test Layer 4", false);
        layers.remove(layer2);
        layers.remove(layer4);

        assertEquals(1, layers.getStackSize());

        Layer<?> topLayer = layers.peek();
        assertNotNull(topLayer);
        assertEquals("Test Layer 3", topLayer.getName());

        verify(container, times(3)).removeView(any(View.class));

        // TODO remove with opaque in stack
    }

    @Test
    public void testReplace() throws Exception {
        layers.add(MockedLayer.class, null, "Test Layer 1", false);
        layers.add(MockedLayer.class, null, "Test Layer 2", false);
        layers.add(MockedLayer.class, null, "Test Layer 3", false);
        layers.add(MockedLayer.class, null, "Test Layer 4", false);
        assertEquals(4, layers.getStackSize());

        layers.replace(MockedLayer.class, null, "Replacement Layer", false);
        assertEquals(4, layers.getStackSize());
        Layer<?> topLayer = layers.peek();
        assertNotNull(topLayer);
        assertEquals("Replacement Layer", topLayer.getName());

        // TODO replace with opaque
        // TODO replace with opaque in stack
    }

    @Test
    public void testFind() throws Exception {
        layers.add(MockedLayer.class, null, "Test Layer 1", true);
        MockedLayer layer2 = layers.add(MockedLayer.class, null, "Test Layer 2", true);
        layers.add(MockedLayer.class, null, "Test Layer 3", true);

        assertNotNull(layers.find("Test Layer 2"));
        assertTrue(layer2 == layers.find("Test Layer 2"));
    }

    @Test
    public void testPeek() throws Exception {
        layers.add(MockedLayer.class, null, "Test Layer 1", true);
        layers.add(MockedLayer.class, null, "Test Layer 2", true);

        Layer<?> pickedLayer = layers.peek();
        assertNotNull(pickedLayer);
        assertEquals("Test Layer 2", pickedLayer.getName());
    }

    @Test
    public void testPop() throws Exception {
        layers.add(MockedLayer.class, null, "Test Layer 1", false);
        layers.add(MockedLayer.class, null, "Test Layer 2", false);
        layers.add(MockedLayer.class, null, "Test Layer 3", false);
        layers.add(MockedLayer.class, null, "Test Layer 4", false);
        assertEquals(4, layers.getStackSize());

        Layer<?> pop1 = layers.pop();
        assertNotNull(pop1);
        assertEquals("Test Layer 4", pop1.getName());
        layers.pop();

        assertEquals(2, layers.getStackSize());
        Layer<?> pop2 = layers.pop();
        assertNotNull(pop2);
        assertEquals("Test Layer 2", pop2.getName());

        // TODO pop opaque
    }

    @Test
    public void testPopTo() throws Exception {
        // Pop to Layer 2, inclusive
        MockedLayer layer1 = layers.add(MockedLayer.class, null, "Test Layer 1", false);
        MockedLayer layer2 = layers.add(MockedLayer.class, null, "Test Layer 2", false);
        layers.add(MockedLayer.class, null, "Test Layer 3", false);
        layers.add(MockedLayer.class, null, "Test Layer 4", false);

        Layer<?> popped2 = layers.popTo("Test Layer 2", true);
        assertEquals(1, layers.getStackSize());
        assertTrue(popped2 == layer2);
        assertTrue(layer1 == layers.peek());

        // Pop to Layer 6, not inclusive
        MockedLayer layer5 = layers.add(MockedLayer.class, null, "Test Layer 5", false);
        MockedLayer layer6 = layers.add(MockedLayer.class, null, "Test Layer 6", false);
        MockedLayer layer7 = layers.add(MockedLayer.class, null, "Test Layer 7", false);
        assertEquals(4, layers.getStackSize());

        Layer<?> popped7 = layers.popTo("Test Layer 6", false);
        assertEquals(3, layers.getStackSize());
        assertTrue(popped7 == layer7);
        assertTrue(layer6 == layers.peek());

        // Pop with name == null, not inclusive
        layers.add(MockedLayer.class, null, "Test Layer 8", false);
        layers.add(MockedLayer.class, null, "Test Layer 9", false);
        assertEquals(5, layers.getStackSize());

        Layer<?> popped5 = layers.popTo(null, false);
        assertEquals(1, layers.getStackSize());
        assertTrue(popped5 == layer5);
        assertTrue(layer1 == layers.peek());

        // Pop with name == null, inclusive
        layers.add(MockedLayer.class, null, "Test Layer 10", false);
        layers.add(MockedLayer.class, null, "Test Layer 11", false);
        assertEquals(3, layers.getStackSize());

        Layer<?> popped1 = layers.popTo(null, true);
        assertEquals(0, layers.getStackSize());
        assertTrue(popped1 == layer1);

        // TODO when not found
        // TODO with opaque
    }

    @Test
    public void testClear() throws Exception {
        layers.add(MockedLayer.class, null, "Test Layer 1", false);
        layers.add(MockedLayer.class, null, "Test Layer 2", false);
        layers.add(MockedLayer.class, null, "Test Layer 3", true);
        layers.add(MockedLayer.class, null, "Test Layer 4", true);
        assertEquals(4, layers.getStackSize());

        layers.clear();
        assertEquals(0, layers.getStackSize());

        verify(container, times(4)).addView(any(View.class));
        verify(container, times(4)).removeView(any(View.class));
    }
}
