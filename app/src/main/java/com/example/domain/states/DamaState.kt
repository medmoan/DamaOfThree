package com.example.domain.states

import com.example.domain.piece.Piece

data class DamaState(
    var userTurn: Int = 1,
    val gameState: GameState
)