package com.psliusar.layers

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNotSame
import org.junit.Assert.assertNull
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt

private const val CHILD_VIEW_1 = 1
private const val CHILD_VIEW_2 = 2

class LayersTest {

    private val context: Context = mock()
    private val container: ViewGroup = spy(MockedViewGroup(context))
    private val inflater: LayoutInflater = mock {
        on { inflate(eq(0), any(), eq(false)) } doAnswer {
            val mockedView1: FrameLayout = mock()
            val mockedView2: FrameLayout = mock()
            mock {
                on { findViewById<View>(CHILD_VIEW_1) } doReturn mockedView1
                on { findViewById<View>(CHILD_VIEW_2) } doReturn mockedView2
            }
        }
    }
    private val activity: Activity = mock {
        on { layoutInflater } doReturn inflater
    }
    private val host: LayersHost = mock {
        on { getView<View>(0) } doReturn container
        on { activity } doReturn activity
    }
    private val layers = Layers(host, 0, null)

    @Test
    fun at_withSameId() {
        val layersAt = layers.at(CHILD_VIEW_1)
        assertSame(layersAt, layers.at(CHILD_VIEW_1)) // same instance
    }

    @Test
    fun at_withAnotherId() {
        val layersAt = layers.at(CHILD_VIEW_1)
        assertNotSame(layersAt, layers.at(CHILD_VIEW_2)) // another instance
    }

    @Test
    fun at_withBundle() {
        val state = Bundle()
        val layersAt = layers.at(CHILD_VIEW_1, state)
        assertTrue(layersAt.isViewPaused)
    }

    @Test
    fun resumeView_notPaused() {
        val spied = spy(layers)
        spied.resumeView()
        verify(spied, times(0)).ensureViews()
    }

    @Test
    fun resumeView_paused() {
        val layersAt = layers.at(CHILD_VIEW_1) // create layers at container
        layersAt.pauseView()
        val spied = spy(layers)
        spied.pauseView()
        spied.resumeView()
        verify(spied, times(1)).ensureViews()
        assertFalse(layersAt.isViewPaused)
    }

    @Test
    fun pauseView() {
        val layersAt = layers.at(CHILD_VIEW_1) // create layers at container
        val spied = spy(layers)
        spied.pauseView()
        assertTrue(spied.isViewPaused)
        assertFalse(layersAt.isViewPaused)
    }

    @Test
    fun add_transparent() {
        val layer1 = addLayer(MockedLayer::class.java, null, "Test Layer 1", false)
        val layer2 = addLayer(MockedLayer::class.java, null, "Test Layer 2", false)
        val layer3 = addLayer(MockedLayer::class.java, null, "Test Layer 3", false)

        verify(container, times(0)).removeView(any())

        verify(container, times(1)).addView(eq(layer1.view!!), eq(0))
        assertTrue(layer1.isAttached)
        assertNotNull(layer1.view)

        verify(container, times(1)).addView(eq(layer2.view!!), eq(1))
        assertTrue(layer2.isAttached)
        assertNotNull(layer2.view)

        verify(container, times(1)).addView(eq(layer3.view!!), eq(2))
        assertTrue(layer3.isAttached)
        assertNotNull(layer3.view)
    }

    @Test
    fun add_opaque() {
        val layer1 = addLayer(MockedLayer::class.java, null, "Test Layer 1", true)
        val layer2 = addLayer(MockedLayer::class.java, null, "Test Layer 2", false)
        val layer3 = addLayer(MockedLayer::class.java, null, "Test Layer 3", true)
        val layer4 = addLayer(MockedLayer::class.java, null, "Test Layer 4", false)

        assertNull(layer1.view)
        assertFalse(layer1.isAttached)
        assertNull(layer2.view)
        assertFalse(layer2.isAttached)
        assertNotNull(layer3.view)
        assertTrue(layer3.isAttached)
        assertNotNull(layer4.view)
        assertTrue(layer4.isAttached)
        verify(container, times(2)).removeView(any())
        verify(container, times(4)).addView(any(), anyInt())
    }

    @Test
    fun replace_withTransparent() {
        val layer1 = addLayer(MockedLayer::class.java, null, "Test Layer 1", false)
        val layer2 = addLayer(MockedLayer::class.java, null, "Test Layer 2", false)
        val layer3 = addLayer(MockedLayer::class.java, null, "Test Layer 3", false)
        val layer4 = addLayer(MockedLayer::class.java, null, "Test Layer 4", false)
        assertEquals(4, layers.stackSize)

        val replacement = replaceLayer(MockedLayer::class.java, null, "Replacement Layer", false)
        assertEquals(4, layers.stackSize)
        val topLayer = layers.peek<Layer>()
        assertNotNull(topLayer)
        assertEquals("Replacement Layer", topLayer!!.name)

        // Views
        assertNull(layer4.view)
        assertNotNull(layer1.view)
        assertNotNull(layer2.view)
        assertNotNull(layer3.view)
        assertNotNull(replacement.view)
    }

    @Test
    fun replace_withOpaque() {
        val layer1 = addLayer(MockedLayer::class.java, null, "Test Layer 1", true)
        val layer2 = addLayer(MockedLayer::class.java, null, "Test Layer 2", true)
        assertEquals(2, layers.stackSize)

        val replacement = replaceLayer(MockedLayer::class.java, null, "Replacement Layer", true)
        assertEquals(2, layers.stackSize)
        val topLayer = layers.peek<Layer>()
        assertNotNull(topLayer)
        assertEquals("Replacement Layer", topLayer!!.name)

        // Views
        assertNull(layer1.view)
        assertNull(layer2.view)
        assertNotNull(replacement.view)
    }

    @Test
    fun replace_withOpaqueInStack() {
        val layer1 = addLayer(MockedLayer::class.java, null, "Test Layer 1", false)
        val layer2 = addLayer(MockedLayer::class.java, null, "Test Layer 2", true)
        val layer3 = addLayer(MockedLayer::class.java, null, "Test Layer 3", false)
        val layer4 = addLayer(MockedLayer::class.java, null, "Test Layer 4", false)
        assertEquals(4, layers.stackSize)

        val replacement = replaceLayer(MockedLayer::class.java, null, "Replacement Layer", true)
        assertEquals(4, layers.stackSize)
        val topLayer = layers.peek<Layer>()
        assertNotNull(topLayer)
        assertEquals("Replacement Layer", topLayer!!.name)

        // Views
        assertNull(layer1.view)
        assertNull(layer2.view)
        assertNull(layer3.view)
        assertNull(layer4.view)
        assertNotNull(replacement.view)
    }

    @Test
    fun remove_byIndex() {
        val layer1 = addLayer(MockedLayer::class.java, null, "Test Layer 1", true)
        val layer2 = addLayer(MockedLayer::class.java, null, "Test Layer 2", true)
        val layer3 = addLayer(MockedLayer::class.java, null, "Test Layer 3", true)
        val layer4 = addLayer(MockedLayer::class.java, null, "Test Layer 4", true)
        assertEquals(4, layers.stackSize)

        layers.remove<Layer>(2)

        assertEquals(3, layers.stackSize)
        assertSame(layer4, layers.peek())
    }

    @Test
    fun remove_byInstance() {
        val layer1 = addLayer(MockedLayer::class.java, null, "Test Layer 1", false)
        layers.remove(layer1)

        assertEquals(0, layers.stackSize)

        val layer2 = addLayer(MockedLayer::class.java, null, "Test Layer 2", false)
        val layer3 = addLayer(MockedLayer::class.java, null, "Test Layer 3", true)
        val layer4 = addLayer(MockedLayer::class.java, null, "Test Layer 4", false)
        val layer5 = addLayer(MockedLayer::class.java, null, "Test Layer 5", false)
        layers.remove(layer2)
        layers.remove(layer4)

        assertEquals(2, layers.stackSize)

        assertNotNull(layer3.view)
        assertTrue(layer3.isAttached)

        val topLayer = layers.peek<MockedLayer>()
        assertNotNull(topLayer)
        assertEquals("Test Layer 5", topLayer!!.name)
        assertNotNull(topLayer.view)
        assertTrue(topLayer.isAttached)

        verify(container, times(3)).removeView(any())
    }

    @Test
    fun pop_transparent() {
        val layer1 = addLayer(MockedLayer::class.java, null, "Test Layer 1", false)
        val layer2 = addLayer(MockedLayer::class.java, null, "Test Layer 2", false)
        val layer3 = addLayer(MockedLayer::class.java, null, "Test Layer 3", false)
        val layer4 = addLayer(MockedLayer::class.java, null, "Test Layer 4", false)
        assertEquals(4, layers.stackSize)

        val pop1 = layers.pop<Layer>()
        assertNotNull(pop1)
        assertEquals("Test Layer 4", pop1!!.name)

        assertEquals(3, layers.stackSize)
        val pop2 = layers.pop<Layer>()
        assertNotNull(pop2)
        assertEquals("Test Layer 3", pop2!!.name)

        // Views
        assertNotNull(layer1.view)
        assertNotNull(layer2.view)
        assertNull(layer3.view)
        assertNull(layer4.view)
    }

    @Test
    fun pop_opaque() {
        val layer1 = addLayer(MockedLayer::class.java, null, "Test Layer 1", true)
        val layer2 = addLayer(MockedLayer::class.java, null, "Test Layer 2", true)
        val layer3 = addLayer(MockedLayer::class.java, null, "Test Layer 3", true)
        val layer4 = addLayer(MockedLayer::class.java, null, "Test Layer 4", true)
        assertEquals(4, layers.stackSize)

        val pop1 = layers.pop<Layer>()
        assertNotNull(pop1)
        assertEquals("Test Layer 4", pop1!!.name)

        assertEquals(3, layers.stackSize)
        val pop2 = layers.pop<Layer>()
        assertNotNull(pop2)
        assertEquals("Test Layer 3", pop2!!.name)

        // Views
        assertNull(layer1.view)
        assertNotNull(layer2.view)
        assertNull(layer3.view)
        assertNull(layer4.view)
    }

    @Test
    fun popLayersTo_transparent() {
        // Pop to Layer 2, inclusive
        val layer1 = addLayer(MockedLayer::class.java, null, "Test Layer 1", false)
        val layer2 = addLayer(MockedLayer::class.java, null, "Test Layer 2", false)
        addLayer(MockedLayer::class.java, null, "Test Layer 3", false)
        addLayer(MockedLayer::class.java, null, "Test Layer 4", false)

        val popped2 = layers.popLayersTo<Layer>("Test Layer 2", true)
        assertEquals(1, layers.stackSize)
        assertSame(popped2, layer2)
        assertSame(layer1, layers.peek())

        // Pop to Layer 6, not inclusive
        val layer5 = addLayer(MockedLayer::class.java, null, "Test Layer 5", false)
        val layer6 = addLayer(MockedLayer::class.java, null, "Test Layer 6", false)
        val layer7 = addLayer(MockedLayer::class.java, null, "Test Layer 7", false)
        assertEquals(4, layers.stackSize)

        val popped7 = layers.popLayersTo<Layer>("Test Layer 6", false)
        assertEquals(3, layers.stackSize)
        assertSame(popped7, layer7)
        assertSame(layer6, layers.peek())

        // Pop with name == null, not inclusive
        addLayer(MockedLayer::class.java, null, "Test Layer 8", false)
        addLayer(MockedLayer::class.java, null, "Test Layer 9", false)
        assertEquals(5, layers.stackSize)

        val popped5 = layers.popLayersTo<Layer>(null, false)
        assertEquals(1, layers.stackSize)
        assertSame(popped5, layer5)
        assertSame(layer1, layers.peek())

        // Pop with name == null, inclusive
        addLayer(MockedLayer::class.java, null, "Test Layer 10", false)
        addLayer(MockedLayer::class.java, null, "Test Layer 11", false)
        assertEquals(3, layers.stackSize)

        val popped1 = layers.popLayersTo<Layer>(null, true)
        assertEquals(0, layers.stackSize)
        assertSame(popped1, layer1)
    }

    @Test
    fun popLayersTo_transparentNotFound() {
        TODO()
    }

    @Test
    fun popLayersTo_opaque() {
        TODO()
    }

    @Test
    fun onBackPressed() {
        TODO()
    }

    @Test
    fun clear() {
        addLayer(MockedLayer::class.java, null, "Test Layer 1", false)
        addLayer(MockedLayer::class.java, null, "Test Layer 2", false)
        addLayer(MockedLayer::class.java, null, "Test Layer 3", true)
        addLayer(MockedLayer::class.java, null, "Test Layer 4", true)
        assertEquals(4, layers.stackSize)

        verify(container, times(4)).addView(any(), anyInt())
        layers.clear()
        assertEquals(0, layers.stackSize)

        verify(container, times(4)).removeView(any())
    }

    @Test
    fun peek() {
        addLayer(MockedLayer::class.java, null, "Test Layer 1", true)
        addLayer(MockedLayer::class.java, null, "Test Layer 2", true)

        val pickedLayer = layers.peek<Layer>()
        assertNotNull(pickedLayer)
        assertEquals("Test Layer 2", pickedLayer!!.name)
    }

    @Test
    fun get() {
        TODO()
    }

    @Test
    fun find() {
        addLayer(MockedLayer::class.java, null, "Test Layer 1", true)
        val layer2 = addLayer(MockedLayer::class.java, null, "Test Layer 2", true)
        addLayer(MockedLayer::class.java, null, "Test Layer 3", true)

        assertNotNull(layers.find("Test Layer 2"))
        assertSame(layer2, layers.find("Test Layer 2"))
    }

    @Test
    fun hasRunningTransition() {
        TODO()
    }

    @Test
    fun isViewPaused() {
        TODO()
    }

    @Test
    fun isInSavedState() {
        TODO()
    }

    @Test
    fun getStackSize() {
        TODO()
    }

    @Test
    fun saveState() {
        val layer1 = addLayer(layers, MockedLayer::class.java, null, "Test Layer 1", true)
        val layer2 = addLayer(layers, MockedLayer::class.java, null, "Test Layer 2", true)
        val layers2 = layer2.layers.at(CHILD_VIEW_1)
        val layer3 = addLayer(layers2, MockedLayer::class.java, null, "Child Layer 2-1", true)

        // Save state
        val state = layers.saveState()

        layers.destroy()

        // Restore state
        val restoredLayers = Layers(host, 0, state)
        restoredLayers.resumeView()

        assertEquals(2, restoredLayers.stackSize)

        val restoredLayer1 = restoredLayers.get<MockedLayer>(0)!!
        assertTrue(restoredLayer1.stateRestored)
        assertNull(restoredLayer1.view)

        val restoredLayer2 = restoredLayers.get<MockedLayer>(1)!!
        assertTrue(restoredLayer2.stateRestored)
        assertNotNull(restoredLayer2.view)

        val restoredLayers2 = restoredLayers.get<Layer>(1)!!.layers.at(CHILD_VIEW_1)
        assertEquals(1, restoredLayers2.stackSize)

        val restoredLayer3 = restoredLayers2.get<MockedLayer>(0)!!
        assertTrue(restoredLayer3.stateRestored)
        assertNotNull(restoredLayer3.view)

        // New instances are not the ones before saving state
        assertNotSame(layer1, restoredLayer1)
        assertNotSame(layer2, restoredLayer2)
        assertNotSame(layer3, restoredLayer3)
    }

    @Test
    fun destroy() {
        TODO()
    }

    @Test
    fun moveToState() {
        TODO()
    }

    @Test
    fun createLayer_new() {
        // New Layer, without saved state
        val layer = addLayer(MockedLayer::class.java, null, "Test Layer 1", true)

        assertNotNull(layer)
        assertEquals(1, layer.onCreateCalled)
    }

    @Test
    fun createLayer_withSavedState() {
        TODO()
    }

    @Test
    fun createView() {
        // New Layer, without saved state
        val layer = addLayer(MockedLayer::class.java, null, "Test Layer 1", true)

        assertNotNull(layer)
        assertEquals(1, layer.onCreateViewCalled)
        assertEquals(1, layer.onAttachCalled)
        assertEquals(1, layer.onBindViewCalled)
        assertFalse(layer.stateRestored)
        assertTrue(layer.isAttached)

        verify(container, times(1)).addView(eq(layer.view!!), eq(0))
    }

    @Test
    fun createView_withSavedState() {
        TODO()
    }

    @Test
    fun restoreViewState() {
        TODO()
    }

    @Test
    fun saveViewState() {
        TODO()
    }

    @Test
    fun destroyView() {
        // Delete permanently
        val layer = addLayer(MockedLayer::class.java, null, "Test Layer 1", true)
        val view = layer.view
        layers.remove(layer)

        assertEquals(0, layer.saveViewStateCalled)
        assertEquals(1, layer.onDetachCalled)
        verify(container, times(1)).removeView(view)
        assertEquals(1, layer.onDestroyViewCalled)
        assertFalse(layer.isAttached)
        assertNull(layer.view)
    }

    @Test
    fun destroyView_keepSavedState() {
        TODO()
    }

    @Test
    fun saveLayerState() {
        TODO()
    }

    @Test
    fun destroyLayer() {
        // Delete permanently
        val layer = addLayer(MockedLayer::class.java, null, "Test Layer 1", true)

        layers.remove(layer)

        assertEquals(1, layer.onDestroyCalled)
    }

    @Test
    fun destroyLayer_keepSavedState() {
        TODO()
    }

    @Test
    fun addTransition() {
        TODO()
    }

    @Test
    fun nextTransition() {
        TODO()
    }

    @Test
    fun removeLayerAt() {
        TODO()
    }

    @Test
    fun getLowestVisibleLayer() {
        TODO()
    }

    @Test
    fun ensureViews() {
        TODO()
    }

    @Test
    fun getContainer() {
        TODO()
    }

    /* Helper methods */

    private fun <L : Layer> addLayer(
        layers: Layers,
        layerClass: Class<L>,
        arguments: Bundle?,
        name: String?,
        opaque: Boolean
    ): L {
        layers.add(layerClass) {
            this.arguments = arguments
            this.name = name
            this.opaque = opaque
        }
        return layers.peek()!!
    }

    private fun <L : Layer> addLayer(
        layerClass: Class<L>,
        arguments: Bundle?,
        name: String?,
        opaque: Boolean
    ): L {
        return addLayer(layers, layerClass, arguments, name, opaque)
    }

    private fun <L : Layer> replaceLayer(
        layerClass: Class<L>,
        arguments: Bundle?,
        name: String?,
        opaque: Boolean
    ): L {
        layers.replace(layerClass) {
            this.arguments = arguments
            this.name = name
            this.opaque = opaque
        }
        return layers.peek()!!
    }
}
