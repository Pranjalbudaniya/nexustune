package com.nexus.tune.widget

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class NexusTuneQueueWidgetTest {

    @Test
    fun `queue widget action constants are distinct and properly namespaced`() {
        val actions = listOf(
            NexusTuneQueueWidgetProvider.ACTION_QUEUE_TOGGLE_PLAY_PAUSE,
            NexusTuneQueueWidgetProvider.ACTION_QUEUE_SKIP_NEXT,
            NexusTuneQueueWidgetProvider.ACTION_QUEUE_SKIP_PREV,
            NexusTuneQueueWidgetProvider.ACTION_UPDATE_QUEUE_STATE
        )

        assertEquals(4, actions.distinct().size)
        actions.forEach { action ->
            assertNotNull(action)
            assert(action.startsWith("com.nexus.tune.widget."))
        }
    }
}

