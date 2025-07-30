package com.example.domain

import com.example.domain.piece.Piece
import kotlinx.coroutines.flow.Flow


interface NotifyChanges {
    val piecesChanges: Flow<PiecesChanges>
}
data class PiecesChanges(
    val piece: Piece,
    val action: Action,
    val position: Int = -1
)
enum class Action {
    Add,
    Replace
}