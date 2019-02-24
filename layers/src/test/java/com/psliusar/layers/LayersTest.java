package com.psliusar.layers;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.junit.*;
import org.mockito.invocation.*;
import org.mockito.stubbing.*;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

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

    @Test
    public void at_withSameId() {
        final Layers layersAt = layers.at(CHILD_VIEW_1);
        assertSame(layersAt, layers.at(CHILD_VIEW_1)); // same instance
    }

    @Test
    public void at_withAnotherId() {
        final Layers layersAt = layers.at(CHILD_VIEW_1);
        assertNotSame(layersAt, layers.at(CHILD_VIEW_2)); // another instance
    }

    @Test
    public void at_withBundle() {
        final Bundle state = new Bundle();
        Layers layersAt = layers.at(CHILD_VIEW_1, state);
        assertTrue(layersAt.isViewPaused());
    }

    @Test
    public void resumeView_notPaused() {
        final Layers spied = spy(layers);
        spied.resumeView();
        verify(spied, times(0)).ensureViews();
    }

    @Test
    public void resumeView_paused() {
        final Layers layersAt = layers.at(CHILD_VIEW_1); // create layers at container
        layersAt.pauseView();
        final Layers spied = spy(layers);
        spied.pauseView();
        spied.resumeView();
        verify(spied, times(1)).ensureViews();
        assertFalse(layersAt.isViewPaused());
    }

    @Test
    public void pauseView() {
        final Layers layersAt = layers.at(CHILD_VIEW_1); // create layers at container
        final Layers spied = spy(layers);
        spied.pauseView();
        assertTrue(spied.isViewPaused());
        assertFalse(layersAt.isViewPaused());
    }

    @Test
    public void add_transparent() {
        final MockedLayer layer1 = addLayer(MockedLayer.class, null, "Test Layer 1", false);
        final MockedLayer layer2 = addLayer(MockedLayer.class, null, "Test Layer 2", false);
        final MockedLayer layer3 = addLayer(MockedLayer.class, null, "Test Layer 3", false);

        verify(container, times(0)).removeView(any(View.class));

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
    public void add_opaque() {
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
    public void replace_withTransparent() {
        final MockedLayer layer1 = addLayer(MockedLayer.class, null, "Test Layer 1", false);
        final MockedLayer layer2 = addLayer(MockedLayer.class, null, "Test Layer 2", false);
        final MockedLayer layer3 = addLayer(MockedLayer.class, null, "Test Layer 3", false);
        final MockedLayer layer4 = addLayer(MockedLayer.class, null, "Test Layer 4", false);
        assertEquals(4, layers.getStackSize());

        final MockedLayer replacement = replaceLayer(MockedLayer.class, null, "Replacement Layer", false);
        assertEquals(4, layers.getStackSize());
        final Layer<?> topLayer = layers.peek();
        assertNotNull(topLayer);
        assertEquals("Replacement Layer", topLayer.getName());

        // Views
        assertNull(layer4.getView());
        assertNotNull(layer1.getView());
        assertNotNull(layer2.getView());
        assertNotNull(layer3.getView());
        assertNotNull(replacement.getView());
    }

    @Test
    public void replace_withOpaque() {
        final MockedLayer layer1 = addLayer(MockedLayer.class, null, "Test Layer 1", true);
        final MockedLayer layer2 = addLayer(MockedLayer.class, null, "Test Layer 2", true);
        assertEquals(2, layers.getStackSize());

        final MockedLayer replacement = replaceLayer(MockedLayer.class, null, "Replacement Layer", true);
        assertEquals(2, layers.getStackSize());
        final Layer<?> topLayer = layers.peek();
        assertNotNull(topLayer);
        assertEquals("Replacement Layer", topLayer.getName());

        // Views
        assertNull(layer1.getView());
        assertNull(layer2.getView());
        assertNotNull(replacement.getView());
    }

    @Test
    public void replace_withOpaqueInStack() {
        final MockedLayer layer1 = addLayer(MockedLayer.class, null, "Test Layer 1", false);
        final MockedLayer layer2 = addLayer(MockedLayer.class, null, "Test Layer 2", true);
        final MockedLayer layer3 = addLayer(MockedLayer.class, null, "Test Layer 3", false);
        final MockedLayer layer4 = addLayer(MockedLayer.class, null, "Test Layer 4", false);
        assertEquals(4, layers.getStackSize());

        final MockedLayer replacement = replaceLayer(MockedLayer.class, null, "Replacement Layer", true);
        assertEquals(4, layers.getStackSize());
        final Layer<?> topLayer = layers.peek();
        assertNotNull(topLayer);
        assertEquals("Replacement Layer", topLayer.getName());

        // Views
        assertNull(layer1.getView());
        assertNull(layer2.getView());
        assertNull(layer3.getView());
        assertNull(layer4.getView());
        assertNotNull(replacement.getView());
    }

    @Test
    public void remove_byIndex() {
        final MockedLayer layer1 = addLayer(MockedLayer.class, null, "Test Layer 1", true);
        final MockedLayer layer2 = addLayer(MockedLayer.class, null, "Test Layer 2", true);
        final MockedLayer layer3 = addLayer(MockedLayer.class, null, "Test Layer 3", true);
        final MockedLayer layer4 = addLayer(MockedLayer.class, null, "Test Layer 4", true);
        assertEquals(4, layers.getStackSize());

        layers.remove(2).commit();

        assertEquals(3, layers.getStackSize());
        assertSame(layer4, layers.peek());
    }

    @Test
    public void remove_byInstance() {
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
    public void pop_transparent() {
        final MockedLayer layer1 = addLayer(MockedLayer.class, null, "Test Layer 1", false);
        final MockedLayer layer2 = addLayer(MockedLayer.class, null, "Test Layer 2", false);
        final MockedLayer layer3 = addLayer(MockedLayer.class, null, "Test Layer 3", false);
        final MockedLayer layer4 = addLayer(MockedLayer.class, null, "Test Layer 4", false);
        assertEquals(4, layers.getStackSize());

        final Layer<?> pop1 = layers.pop();
        assertNotNull(pop1);
        assertEquals("Test Layer 4", pop1.getName());

        assertEquals(3, layers.getStackSize());
        final Layer<?> pop2 = layers.pop();
        assertNotNull(pop2);
        assertEquals("Test Layer 3", pop2.getName());

        // Views
        assertNotNull(layer1.getView());
        assertNotNull(layer2.getView());
        assertNull(layer3.getView());
        assertNull(layer4.getView());
    }

    @Test
    public void pop_opaque() {
        final MockedLayer layer1 = addLayer(MockedLayer.class, null, "Test Layer 1", true);
        final MockedLayer layer2 = addLayer(MockedLayer.class, null, "Test Layer 2", true);
        final MockedLayer layer3 = addLayer(MockedLayer.class, null, "Test Layer 3", true);
        final MockedLayer layer4 = addLayer(MockedLayer.class, null, "Test Layer 4", true);
        assertEquals(4, layers.getStackSize());

        final Layer<?> pop1 = layers.pop();
        assertNotNull(pop1);
        assertEquals("Test Layer 4", pop1.getName());

        assertEquals(3, layers.getStackSize());
        final Layer<?> pop2 = layers.pop();
        assertNotNull(pop2);
        assertEquals("Test Layer 3", pop2.getName());

        // Views
        assertNull(layer1.getView());
        assertNotNull(layer2.getView());
        assertNull(layer3.getView());
        assertNull(layer4.getView());
    }

    @Test
    public void popLayersTo_transparent() {
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
    }

    @Test
    public void popLayersTo_transparentNotFound() {
        fail();
    }

    @Test
    public void popLayersTo_opaque() {
        fail();
    }

    @Test
    public void onBackPressed() {
        fail();
    }

    @Test
    public void clear() {
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

    @Test
    public void peek() {
        addLayer(MockedLayer.class, null, "Test Layer 1", true);
        addLayer(MockedLayer.class, null, "Test Layer 2", true);

        final Layer<?> pickedLayer = layers.peek();
        assertNotNull(pickedLayer);
        assertEquals("Test Layer 2", pickedLayer.getName());
    }

    @Test
    public void get() {
        fail();
    }

    @Test
    public void find() {
        addLayer(MockedLayer.class, null, "Test Layer 1", true);
        final MockedLayer layer2 = addLayer(MockedLayer.class, null, "Test Layer 2", true);
        addLayer(MockedLayer.class, null, "Test Layer 3", true);

        assertNotNull(layers.find("Test Layer 2"));
        assertSame(layer2, layers.find("Test Layer 2"));
    }

    @Test
    public void hasRunningTransition() {
        fail();
    }

    @Test
    public void isViewPaused() {
        fail();
    }

    @Test
    public void isInSavedState() {
        fail();
    }

    @Test
    public void getStackSize() {
        fail();
    }

    @Test
    public void saveState() {
        final MockedLayer layer1 = addLayer(layers, MockedLayer.class, null, "Test Layer 1", true);
        final MockedLayer layer2 = addLayer(layers, MockedLayer.class, null, "Test Layer 2", true);
        final Layers layers2 = layer2.getLayers().at(CHILD_VIEW_1);
        final MockedLayer layer3 = addLayer(layers2, MockedLayer.class, null, "Child Layer 2-1", true);

        // Save state
        final Bundle state = layers.saveState();

        layers.destroy();

        // Restore state
        final Layers restoredLayers = new Layers(host, 0, state);
        restoredLayers.resumeView();

        assertEquals(2, restoredLayers.getStackSize());

        final MockedLayer restoredLayer1 = restoredLayers.get(0);
        assertTrue(restoredLayer1.isStateRestored());
        assertNull(restoredLayer1.getView());

        final MockedLayer restoredLayer2 = restoredLayers.get(1);
        assertTrue(restoredLayer2.isStateRestored());
        assertNotNull(restoredLayer2.getView());

        final Layers restoredLayers2 = restoredLayers.get(1).getLayers().at(CHILD_VIEW_1);
        assertEquals(1, restoredLayers2.getStackSize());

        final MockedLayer restoredLayer3 = restoredLayers2.get(0);
        assertTrue(restoredLayer3.isStateRestored());
        assertNotNull(restoredLayer3.getView());

        // New instances are not the ones before saving state
        assertNotSame(layer1, restoredLayer1);
        assertNotSame(layer2, restoredLayer2);
        assertNotSame(layer3, restoredLayer3);
    }

    @Test
    public void destroy() {
        fail();
    }

    @Test
    public void moveToState() {
        fail();
    }

    @Test
    public void createLayer_new() {
        // New Layer, without saved state
        final MockedLayer layer = addLayer(MockedLayer.class, null, "Test Layer 1", true);
        layer.getViewModel();

        assertNotNull(layer);
        assertEquals(1, layer.getCreateCalled());
        assertEquals(1, layer.getOnCreatePresenterCalled());
        assertEquals(1, layer.getOnCreateCalled());
    }

    @Test
    public void createLayer_withSavedState() {
        fail();
    }

    @Test
    public void createView() {
        // New Layer, without saved state
        final MockedLayer layer = addLayer(MockedLayer.class, null, "Test Layer 1", true);

        assertNotNull(layer);
        assertEquals(1, layer.getOnCreateViewCalled());
        assertEquals(1, layer.getOnAttachCalled());
        assertEquals(1, layer.getOnBindViewCalled());
        assertFalse(layer.isViewStateRestored());
        assertTrue(layer.isAttached());

        verify(container, times(1)).addView(eq(layer.getView()), eq(0));
    }

    @Test
    public void createView_withSavedState() {
        fail();
    }

    @Test
    public void restoreViewState() {
        fail();
    }

    @Test
    public void saveViewState() {
        fail();
    }

    @Test
    public void destroyView() {
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
    }

    @Test
    public void destroyView_keepSavedState() {
        fail();
    }

    @Test
    public void saveLayerState() {
        fail();
    }

    @Test
    public void destroyLayer() {
        // Delete permanently
        final MockedLayer layer = addLayer(MockedLayer.class, null, "Test Layer 1", true);

        layers.remove(layer).commit();

        assertEquals(1, layer.getOnDestroyCalled());
    }

    @Test
    public void destroyLayer_keepSavedState() {
        fail();
    }

    @Test
    public void addTransition() {
        fail();
    }

    @Test
    public void nextTransition() {
        fail();
    }

    @Test
    public void removeLayerAt() {
        fail();
    }

    @Test
    public void getLowestVisibleLayer() {
        fail();
    }

    @Test
    public void ensureViews() {
        fail();
    }

    @Test
    public void getContainer() {
        fail();
    }

    /* Helper methods */

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
    private <L extends Layer<?>> L replaceLayer(
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
        return layers.peek();
    }
}
