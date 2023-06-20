package edu.put.inf151872_2

data class BoardGame(
    var bggId: Int,
    var releaseYear: Int,
    var minPlayers: Int,
    var maxPlayers: Int,
    var playTime: Int,
    var bggRank: Int? = null,
    var rating: Float,
    var isExpansion: Boolean,
    var title: String,
    var imageUrl: String,
    var description: String ) {
}
