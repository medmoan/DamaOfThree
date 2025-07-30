package com.example.domain.states

data class PieceState(
    var position: Position? = null,
    var pieceTapState: PieceTapState = PieceTapState.NoTap
)