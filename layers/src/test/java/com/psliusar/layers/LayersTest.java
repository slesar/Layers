package com.psliusar.layers;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("ResourceType")
public class LayersTest {

    private static final int CHILD_VIEW_1 = 1;
    private static final int CHILD_VIEW_2 = 2;

    private LayersHost host;
    private Layers layers;
    private ViewGroup container;

    @Before
    public void setUp() {
        container = mock(ViewGroup.class);

        LayoutInflater inflater = mock(LayoutInflater.class);
        when(inflater.inflate(eq(0), (ViewGroup) any(), eq(false))).thenAnswer(new Answer<View>() {
            @Override
            public View answer(InvocationOnMock invocation) throws Throwable {
                final FrameLayout mockedView = mock(FrameLayout.class);
                when(mockedView.findViewById(CHILD_VIEW_1)).thenReturn(mock(FrameLayout.class));
                when(mockedView.findViewById(CHILD_VIEW_2)).thenReturn(mock(FrameLayout.class));
                return mockedView;
            }
        });

        Activity activity = mock(Activity.class);
        //when(activity.getApplicationContext()).thenReturn(context);
        when(activity.getLayoutInflater()).thenReturn(inflater);

        host = mock(LayersHost.class);
        when(host.getView(0)).thenReturn(container);
        when(host.getActivity()).thenReturn(activity);

        layers = new Layers(host, 0, null);
    }

    public void testPauseView() throws Exception {
        // TODO
    }

    public void testResumeView() throws Exception {
        // TODO
    }

    @Test
    public void testSaveState() throws Exception {
        // TODO
        // create new instance of Layers
        // restore stack
        // compare old and new instances of same layer - shouldn't be equal


        MockedLayer layer1 = addLayer(MockedLayer.class, null, "Test Layer 1", true);
        MockedLayer layer2 = addLayer(MockedLayer.class, null, "Test Layer 2", true);
        addLayer(layer2.getLayers().at(CHILD_VIEW_1), MockedLayer.class, null, "Child Layer 3", true);

        Bundle state = layers.saveState();

        layers.destroy();

        layers = new Layers(host, 0, state);
        layers.resumeView();

        assertEquals(2, layers.getStackSize());

        assertTrue(((MockedLayer) layers.get(0)).isStateRestored());

        assertTrue(((MockedLayer) layers.get(1)).isStateRestored());
        assertNotNull(layers.get(1).getView());

        assertEquals(1, layers.get(1).getLayers().at(CHILD_VIEW_1).getStackSize());

        assertTrue(((MockedLayer) layers.get(1).getLayers().at(CHILD_VIEW_1).get(0)).isStateRestored());
        layers.get(1).getLayers().at(CHILD_VIEW_1).resumeView();
        assertNotNull(layers.get(1).getLayers().at(CHILD_VIEW_1).get(0).getView());

    }

    @Test
    public void testCreateLayer() throws Exception {
        // New Layer, without saved state
        MockedLayer layer = addLayer(MockedLayer.class, null, "Test Layer 1", true);
        layer.getPresenter();

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
        MockedLayer layer = addLayer(MockedLayer.class, null, "Test Layer 1", true);

        assertNotNull(layer);
        assertEquals(1, layer.getOnCreateViewCalled());
        assertEquals(1, layer.getOnAttachCalled());
        assertEquals(1, layer.getOnBindViewCalled());
        assertFalse(layer.isViewStateRestored());
        assertTrue(layer.isAttached());

        verify(container, times(1)).addView(layer.getView());

        // Restored Layer, with saved state
        // TODO
    }

    @Test
    public void testDestroyView() throws Exception {
        // Delete permanently
        MockedLayer layer = addLayer(MockedLayer.class, null, "Test Layer 1", true);
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
        MockedLayer layer = addLayer(MockedLayer.class, null, "Test Layer 1", true);

        layers.remove(layer);

        assertEquals(1, layer.getOnDestroyCalled());

        // Delete keeping in the stack (saving state)
        // TODO
    }

    @Test
    public void testAddTransparent() throws Exception {
        MockedLayer layer1 = addLayer(MockedLayer.class, null, "Test Layer 1", false);
        MockedLayer layer2 = addLayer(MockedLayer.class, null, "Test Layer 2", false);
        MockedLayer layer3 = addLayer(MockedLayer.class, null, "Test Layer 3", false);

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
    }

    @Test
    public void testAddOpaque() throws Exception {
        addLayer(MockedLayer.class, null, "Test Layer 1", true);
        addLayer(MockedLayer.class, null, "Test Layer 2", false);
        addLayer(MockedLayer.class, null, "Test Layer 3", true);
        addLayer(MockedLayer.class, null, "Test Layer 4", false);

        verify(container, times(2)).removeView(any(View.class));
        verify(container, times(4)).addView(any(View.class));
    }

    @Test
    public void testRemove() throws Exception {
        MockedLayer layer1 = addLayer(MockedLayer.class, null, "Test Layer 1", false);
        layers.remove(layer1);

        assertEquals(0, layers.getStackSize());

        MockedLayer layer2 = addLayer(MockedLayer.class, null, "Test Layer 2", false);
        addLayer(MockedLayer.class, null, "Test Layer 3", false);
        MockedLayer layer4 = addLayer(MockedLayer.class, null, "Test Layer 4", false);
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
        addLayer(MockedLayer.class, null, "Test Layer 1", false);
        addLayer(MockedLayer.class, null, "Test Layer 2", false);
        addLayer(MockedLayer.class, null, "Test Layer 3", false);
        addLayer(MockedLayer.class, null, "Test Layer 4", false);
        assertEquals(4, layers.getStackSize());

        replaceLayer(MockedLayer.class, null, "Replacement Layer", false);
        assertEquals(4, layers.getStackSize());
        Layer<?> topLayer = layers.peek();
        assertNotNull(topLayer);
        assertEquals("Replacement Layer", topLayer.getName());

        // TODO replace with opaque
        // TODO replace with opaque in stack
    }

    @Test
    public void testFind() throws Exception {
        addLayer(MockedLayer.class, null, "Test Layer 1", true);
        MockedLayer layer2 = addLayer(MockedLayer.class, null, "Test Layer 2", true);
        addLayer(MockedLayer.class, null, "Test Layer 3", true);

        assertNotNull(layers.find("Test Layer 2"));
        assertTrue(layer2 == layers.find("Test Layer 2"));
    }

    @Test
    public void testPeek() throws Exception {
        addLayer(MockedLayer.class, null, "Test Layer 1", true);
        addLayer(MockedLayer.class, null, "Test Layer 2", true);

        Layer<?> pickedLayer = layers.peek();
        assertNotNull(pickedLayer);
        assertEquals("Test Layer 2", pickedLayer.getName());
    }

    public void testGet() {
        // TODO
    }

    @Test
    public void testPop() throws Exception {
        addLayer(MockedLayer.class, null, "Test Layer 1", false);
        addLayer(MockedLayer.class, null, "Test Layer 2", false);
        addLayer(MockedLayer.class, null, "Test Layer 3", false);
        addLayer(MockedLayer.class, null, "Test Layer 4", false);
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
        MockedLayer layer1 = addLayer(MockedLayer.class, null, "Test Layer 1", false);
        MockedLayer layer2 = addLayer(MockedLayer.class, null, "Test Layer 2", false);
        addLayer(MockedLayer.class, null, "Test Layer 3", false);
        addLayer(MockedLayer.class, null, "Test Layer 4", false);

        Layer<?> popped2 = layers.popLayersTo("Test Layer 2", true);
        assertEquals(1, layers.getStackSize());
        assertTrue(popped2 == layer2);
        assertTrue(layer1 == layers.peek());

        // Pop to Layer 6, not inclusive
        MockedLayer layer5 = addLayer(MockedLayer.class, null, "Test Layer 5", false);
        MockedLayer layer6 = addLayer(MockedLayer.class, null, "Test Layer 6", false);
        MockedLayer layer7 = addLayer(MockedLayer.class, null, "Test Layer 7", false);
        assertEquals(4, layers.getStackSize());

        Layer<?> popped7 = layers.popLayersTo("Test Layer 6", false);
        assertEquals(3, layers.getStackSize());
        assertTrue(popped7 == layer7);
        assertTrue(layer6 == layers.peek());

        // Pop with name == null, not inclusive
        addLayer(MockedLayer.class, null, "Test Layer 8", false);
        addLayer(MockedLayer.class, null, "Test Layer 9", false);
        assertEquals(5, layers.getStackSize());

        Layer<?> popped5 = layers.popLayersTo(null, false);
        assertEquals(1, layers.getStackSize());
        assertTrue(popped5 == layer5);
        assertTrue(layer1 == layers.peek());

        // Pop with name == null, inclusive
        addLayer(MockedLayer.class, null, "Test Layer 10", false);
        addLayer(MockedLayer.class, null, "Test Layer 11", false);
        assertEquals(3, layers.getStackSize());

        Layer<?> popped1 = layers.popLayersTo(null, true);
        assertEquals(0, layers.getStackSize());
        assertTrue(popped1 == layer1);

        // TODO when not found
        // TODO with opaque
    }

    @Test
    public void testClear() throws Exception {
        addLayer(MockedLayer.class, null, "Test Layer 1", false);
        addLayer(MockedLayer.class, null, "Test Layer 2", false);
        addLayer(MockedLayer.class, null, "Test Layer 3", true);
        addLayer(MockedLayer.class, null, "Test Layer 4", true);
        assertEquals(4, layers.getStackSize());

        layers.clear();
        assertEquals(0, layers.getStackSize());

        verify(container, times(4)).addView(any(View.class));
        verify(container, times(4)).removeView(any(View.class));
    }

    @NonNull
    private <L extends Layer<?>> L addLayer(
            @NonNull Layers layers,
            @NonNull Class<L> layerClass,
            @Nullable Bundle arguments,
            @Nullable String name,
            boolean opaque) {
        final Transition<L> transition = layers.add(layerClass).setOpaque(opaque);
        if (arguments != null) {
            transition.setArguments(arguments);
        }
        if (name != null) {
            transition.setName(name);
        }
        return transition.commit();
    }

    @NonNull
    private <L extends Layer<?>> L addLayer(
            @NonNull Class<L> layerClass,
            @Nullable Bundle arguments,
            @Nullable String name,
            boolean opaque) {
        return addLayer(layers, layerClass, arguments, name, opaque);
    }

    @NonNull
    private <L extends Layer<?>> L replaceLayer(
            @NonNull Class<L> layerClass,
            @Nullable Bundle arguments,
            @Nullable String name,
            boolean opaque) {
        final Transition<L> transition = layers.replace(layerClass).setOpaque(opaque);
        if (arguments != null) {
            transition.setArguments(arguments);
        }
        if (name != null) {
            transition.setName(name);
        }
        return transition.commit();
    }
}
