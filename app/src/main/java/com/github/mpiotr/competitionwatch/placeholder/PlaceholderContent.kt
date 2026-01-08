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
        return Competitor(position.toString(), "1", "John Dow", 1, 18, 0 )
    }


    /**
     * A placeholder item representing a piece of content.
     */
    data class Competitor(val id: String, var bib_number : String, var name: String, var sex: Int, var age : Int, var group : Int) {
        fun copy(other : Competitor) {
            bib_number = other.bib_number
            name = other.name
            sex = other.sex
            age = other.age
            group = other.group
        }
        override fun toString(): String = name
    }
}