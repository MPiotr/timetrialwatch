package com.github.mpiotr.competitionwatch.placeholder

import java.util.ArrayList
import java.util.HashMap

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 *
 * TODO: Replace all uses of this class before publishing your app.
 */
object PlaceholderContent {

    /**
     * An array of sample (placeholder) items.
     */
    val ITEMS: MutableList<Competitor> = ArrayList()

    /**
     * A map of sample (placeholder) items, by ID.
     */
    val ITEM_MAP: MutableMap<String, Competitor> = HashMap()

    private val COUNT = 25

    init {
        // Add some sample items.
        for (i in 1..COUNT) {
            addItem(createPlaceholderItem(i))
        }
    }

    private fun addItem(item: Competitor) {
        ITEMS.add(item)
        ITEM_MAP.put(item.id, item)
    }

    private fun createPlaceholderItem(position: Int): Competitor {
        return Competitor(position.toString(), "1", "John Dow")
    }


    /**
     * A placeholder item representing a piece of content.
     */
    data class Competitor(val id: String,
                          var bib_number : String,
                          var name: String,
                          var sex: Int = 1,
                          var age : Int = 18,
                          var group : Int = 0,
                          var started : Boolean = false,
                          var finished : Boolean = false,
                          var startTime : Long = 0L,
                          val splits : MutableList<Long> = mutableListOf()) {

        override fun toString(): String = name
    }
}