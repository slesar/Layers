package com.psliusar.layers;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SuppressWarnings("ConstantConditions")
public class LayersTest {

    private static final int CHILD_VIEW_1 = 1;
    private static final int CHILD_VIEW_2 = 2;

    private LayersHost host;
    private Layers layers;
    private ViewGroup container;

    @Before
    public void setUp() {
        container = spy(new MockedViewGroup(mock(Context.class)));

        final LayoutInflater inflater = mock(LayoutInflater.class);
        when(inflater.inflate(eq(0), (ViewGroup) any(), eq(false))).thenAnswer(new Answer<View>() {
            @Override
            public View answer(InvocationOnMock invocation) {
                final FrameLayout mockedView = mock(FrameLayout.class);
                when(mockedView.findViewById(CHILD_VIEW_1)).thenReturn(mock(FrameLayout.class));
                when(mockedView.findViewById(CHILD_VIEW_2)).thenReturn(mock(FrameLayout.class));
                return mockedView;
            }
        });

        final Activity activity = mock(Activity.class);
        when(activity.getLayoutInflater()).thenReturn(inflater);

        host = mock(LayersHost.class);
        when(host.getView(0)).thenReturn(container);
        when(host.getActivity()).thenReturn(activity);

        layers = new Layers(host, 0, null);
    }

    public void testPauseView() {
        // TODO
    }

    public void testResumeView() {
        // TODO
    }

    @Test
    public void testSaveState() {
        // TODO
        // create new instance of Layers
        // restore stack
        // compare old and new instances of same layer - shouldn't be equal


        final MockedLayer layer1 = addLayer(MockedLayer.class, null, "Test Layer 1", true);
        final MockedLayer layer2 = addLayer(MockedLayer.class, null, "Test Layer 2", true);
        addLayer(layer2.getLayers().at(CHILD_VIEW_1), MockedLayer.class, null, "Child Layer 3", true);

        final Bundle state = layers.saveState();

        layers.destroy();

        layers = new Layers(host, 0, state);
        layers.resumeView();

        assertEquals(2, layers.getStackSize());

        assertTrue(layers.<MockedLayer>get(0).isStateRestored());

        assertTrue(layers.<MockedLayer>get(1).isStateRestored());
        assertNotNull(layers.get(1).getView());

        assertEquals(1, layers.get(1).getLayers().at(CHILD_VIEW_1).getStackSize());

        assertTrue(layers.get(1).getLayers().at(CHILD_VIEW_1).<MockedLayer>get(0).isStateRestored());
        layers.get(1).getLayers().at(CHILD_VIEW_1).resumeView();
        assertNotNull(layers.get(1).getLayers().at(CHILD_VIEW_1).get(0).getView());

    }

    @Test
    public void testCreateLayer() {
        // New Layer, without saved state
        final MockedLayer layer = addLayer(MockedLayer.class, null, "Test Layer 1", true);
        layer.getViewModel();

        assertNotNull(layer);
        assertEquals(1, layer.getCreateCalled());
        assertEquals(1, layer.getOnCreatePresenterCalled());
        assertEquals(1, layer.getOnCreateCalled());

        // Restored Layer, with saved state
        // TODO
    }

    @Test
    public void testCreateView() {
        // New Layer, without saved state
        final MockedLayer layer = addLayer(MockedLayer.class, null, "Test Layer 1", true);

        assertNotNull(layer);
        assertEquals(1, layer.getOnCreateViewCalled());
        assertEquals(1, layer.getOnAttachCalled());
        assertEquals(1, layer.getOnBindViewCalled());
        assertFalse(layer.isViewStateRestored());
        assertTrue(layer.isAttached());

        verify(container, times(1)).addView(eq(layer.getView()), eq(0));

        // Restored Layer, with saved state
        // TODO
    }

    @Test
    public void testDestroyView() {
        // Delete permanently
        final MockedLayer layer = addLayer(MockedLayer.class, null, "Test Layer 1", true);
        final View view = layer.getView();
        layers.remove(layer).commit();

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
    public void testDestroyLayer() {
        // Delete permanently
        final MockedLayer layer = addLayer(MockedLayer.class, null, "Test Layer 1", true);

        layers.remove(layer).commit();

        assertEquals(1, layer.getOnDestroyCalled());

        // Delete keeping in the stack (saving state)
        // TODO
    }

    @Test
    public void testAddTransparent() {
        final MockedLayer layer1 = addLayer(MockedLayer.class, null, "Test Layer 1", false);
        final MockedLayer layer2 = addLayer(MockedLayer.class, null, "Test Layer 2", false);
        final MockedLayer layer3 = addLayer(MockedLayer.class, null, "Test Layer 3", false);

        verify(container, times(0)).removeView(any(View.class));

        // TODO mock container.getChildCount();
        verify(container, times(1)).addView(eq(layer1.getView()), eq(0));
        assertTrue(layer1.isAttached());
        assertNotNull(layer1.getView());

        verify(container, times(1)).addView(eq(layer2.getView()), eq(1));
        assertTrue(layer2.isAttached());
        assertNotNull(layer2.getView());

        verify(container, times(1)).addView(eq(layer3.getView()), eq(2));
        assertTrue(layer3.isAttached());
        assertNotNull(layer3.getView());
    }

    @Test
    public void testAddOpaque() {
        final MockedLayer layer1 = addLayer(MockedLayer.class, null, "Test Layer 1", true);
        final MockedLayer layer2 = addLayer(MockedLayer.class, null, "Test Layer 2", false);
        final MockedLayer layer3 = addLayer(MockedLayer.class, null, "Test Layer 3", true);
        final MockedLayer layer4 = addLayer(MockedLayer.class, null, "Test Layer 4", false);

        assertNull(layer1.getView());
        assertFalse(layer1.isAttached());
        assertNull(layer2.getView());
        assertFalse(layer2.isAttached());
        assertNotNull(layer3.getView());
        assertTrue(layer3.isAttached());
        assertNotNull(layer4.getView());
        assertTrue(layer4.isAttached());
        verify(container, times(2)).removeView(any(View.class));
        verify(container, times(4)).addView(any(View.class), anyInt());
    }

    @Test
    public void testRemove() {
        final MockedLayer layer1 = addLayer(MockedLayer.class, null, "Test Layer 1", false);
        layers.remove(layer1).commit();

        assertEquals(0, layers.getStackSize());

        final MockedLayer layer2 = addLayer(MockedLayer.class, null, "Test Layer 2", false);
        final MockedLayer layer3 = addLayer(MockedLayer.class, null, "Test Layer 3", true);
        final MockedLayer layer4 = addLayer(MockedLayer.class, null, "Test Layer 4", false);
        final MockedLayer layer5 = addLayer(MockedLayer.class, null, "Test Layer 5", false);
        layers.remove(layer2).commit();
        layers.remove(layer4).commit();

        assertEquals(2, layers.getStackSize());

        assertNotNull(layer3.getView());
        assertTrue(layer3.isAttached());

        final MockedLayer topLayer = layers.peek();
        assertNotNull(topLayer);
        assertEquals("Test Layer 5", topLayer.getName());
        assertNotNull(topLayer.getView());
        assertTrue(topLayer.isAttached());

        verify(container, times(3)).removeView(any(View.class));
    }

    @Test
    public void testReplace() {
        addLayer(MockedLayer.class, null, "Test Layer 1", false);
        addLayer(MockedLayer.class, null, "Test Layer 2", false);
        addLayer(MockedLayer.class, null, "Test Layer 3", false);
        addLayer(MockedLayer.class, null, "Test Layer 4", false);
        assertEquals(4, layers.getStackSize());

        replaceLayer(MockedLayer.class, null, "Replacement Layer", false);
        assertEquals(4, layers.getStackSize());
        final Layer<?> topLayer = layers.peek();
        assertNotNull(topLayer);
        assertEquals("Replacement Layer", topLayer.getName());

        // TODO replace with opaque
        // TODO replace with opaque in stack
    }

    @Test
    public void testFind() {
        addLayer(MockedLayer.class, null, "Test Layer 1", true);
        final MockedLayer layer2 = addLayer(MockedLayer.class, null, "Test Layer 2", true);
        addLayer(MockedLayer.class, null, "Test Layer 3", true);

        assertNotNull(layers.find("Test Layer 2"));
        assertSame(layer2, layers.find("Test Layer 2"));
    }

    @Test
    public void testPeek() {
        addLayer(MockedLayer.class, null, "Test Layer 1", true);
        addLayer(MockedLayer.class, null, "Test Layer 2", true);

        final Layer<?> pickedLayer = layers.peek();
        assertNotNull(pickedLayer);
        assertEquals("Test Layer 2", pickedLayer.getName());
    }

    public void testGet() {
        // TODO
    }

    @Test
    public void testPop() {
        addLayer(MockedLayer.class, null, "Test Layer 1", false);
        addLayer(MockedLayer.class, null, "Test Layer 2", false);
        addLayer(MockedLayer.class, null, "Test Layer 3", false);
        addLayer(MockedLayer.class, null, "Test Layer 4", false);
        assertEquals(4, layers.getStackSize());

        final Layer<?> pop1 = layers.pop();
        assertNotNull(pop1);
        assertEquals("Test Layer 4", pop1.getName());
        layers.pop();

        assertEquals(2, layers.getStackSize());
        final Layer<?> pop2 = layers.pop();
        assertNotNull(pop2);
        assertEquals("Test Layer 2", pop2.getName());

        // TODO pop opaque
    }

    @Test
    public void testPopTo() {
        // Pop to Layer 2, inclusive
        final MockedLayer layer1 = addLayer(MockedLayer.class, null, "Test Layer 1", false);
        final MockedLayer layer2 = addLayer(MockedLayer.class, null, "Test Layer 2", false);
        addLayer(MockedLayer.class, null, "Test Layer 3", false);
        addLayer(MockedLayer.class, null, "Test Layer 4", false);

        final Layer<?> popped2 = layers.popLayersTo("Test Layer 2", true);
        assertEquals(1, layers.getStackSize());
        assertSame(popped2, layer2);
        assertSame(layer1, layers.peek());

        // Pop to Layer 6, not inclusive
        final MockedLayer layer5 = addLayer(MockedLayer.class, null, "Test Layer 5", false);
        final MockedLayer layer6 = addLayer(MockedLayer.class, null, "Test Layer 6", false);
        final MockedLayer layer7 = addLayer(MockedLayer.class, null, "Test Layer 7", false);
        assertEquals(4, layers.getStackSize());

        final Layer<?> popped7 = layers.popLayersTo("Test Layer 6", false);
        assertEquals(3, layers.getStackSize());
        assertSame(popped7, layer7);
        assertSame(layer6, layers.peek());

        // Pop with name == null, not inclusive
        addLayer(MockedLayer.class, null, "Test Layer 8", false);
        addLayer(MockedLayer.class, null, "Test Layer 9", false);
        assertEquals(5, layers.getStackSize());

        final Layer<?> popped5 = layers.popLayersTo(null, false);
        assertEquals(1, layers.getStackSize());
        assertSame(popped5, layer5);
        assertSame(layer1, layers.peek());

        // Pop with name == null, inclusive
        addLayer(MockedLayer.class, null, "Test Layer 10", false);
        addLayer(MockedLayer.class, null, "Test Layer 11", false);
        assertEquals(3, layers.getStackSize());

        final Layer<?> popped1 = layers.popLayersTo(null, true);
        assertEquals(0, layers.getStackSize());
        assertSame(popped1, layer1);

        // TODO when not found
        // TODO with opaque
    }

    @Test
    public void testClear() {
        addLayer(MockedLayer.class, null, "Test Layer 1", false);
        addLayer(MockedLayer.class, null, "Test Layer 2", false);
        addLayer(MockedLayer.class, null, "Test Layer 3", true);
        addLayer(MockedLayer.class, null, "Test Layer 4", true);
        assertEquals(4, layers.getStackSize());

        verify(container, times(4)).addView(any(View.class), anyInt());
        layers.clear();
        assertEquals(0, layers.getStackSize());

        verify(container, times(4)).removeView(any(View.class));
    }

    @NonNull
    private <L extends Layer<?>> L addLayer(
            @NonNull Layers layers,
            @NonNull Class<L> layerClass,
            @Nullable Bundle arguments,
            @Nullable String name,
            boolean opaque
    ) {
        final Transition<L> transition = layers.add(layerClass).setOpaque(opaque);
        if (arguments != null) {
            transition.setArguments(arguments);
        }
        if (name != null) {
            transition.setName(name);
        }
        transition.commit();
        return layers.peek();
    }

    @NonNull
    private <L extends Layer<?>> L addLayer(
            @NonNull Class<L> layerClass,
            @Nullable Bundle arguments,
            @Nullable String name,
            boolean opaque
    ) {
        return addLayer(layers, layerClass, arguments, name, opaque);
    }

    @NonNull
    private <L extends Layer<?>> void replaceLayer(
            @NonNull Class<L> layerClass,
            @Nullable Bundle arguments,
            @Nullable String name,
            boolean opaque
    ) {
        final Transition<L> transition = layers.replace(layerClass).setOpaque(opaque);
        if (arguments != null) {
            transition.setArguments(arguments);
        }
        if (name != null) {
            transition.setName(name);
        }
        transition.commit();
    }
}
