package com.example.domain.piece

import com.example.domain.states.PieceState

data class Piece(
    var user: Int,
    var spotPosition: Int,
    var pieceState: PieceState
)