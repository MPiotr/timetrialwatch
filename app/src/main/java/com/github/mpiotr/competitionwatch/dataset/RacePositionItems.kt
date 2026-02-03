package com.github.mpiotr.competitionwatch.dataset

data class RacePositionItems(val currentPosition : Int,
                             val leader : Pair<Long, Competitor>?,
                             val chaser : Pair<Long, Competitor>?,
                             val numCompleted : Int)
{
    override fun toString() : String
    {
        return "current: $currentPosition, leader: $leader, chaser $chaser, numCompleted $numCompleted"
    }
}