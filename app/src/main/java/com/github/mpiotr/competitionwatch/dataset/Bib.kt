package com.github.mpiotr.competitionwatch.dataset

data class Bib(val bib_number : Int, val bib_color : Int) : Comparable<Bib> {
    override fun compareTo(other: Bib): Int {
        if(bib_color != other.bib_color) return bib_color - other.bib_color
        else return bib_number - other.bib_number
    }
}