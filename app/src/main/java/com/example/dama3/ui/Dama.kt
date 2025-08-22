package com.example.dama3.ui


import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProduceStateScope
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.VectorPainter
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp

import com.example.dama3.R
import com.example.dama3.ui.Dama.TAG
import com.example.domain.states.DamaState
import com.example.domain.states.Mode
import com.example.domain.piece.Piece
import com.example.domain.states.GameState
import com.example.domain.states.PieceState
import com.example.domain.states.PieceTapState
import com.example.domain.states.Position
import kotlin.math.min

object Dama {
    const val TAG = "Dama-composable"
}
@Composable
fun Dama(modifier: Modifier = Modifier,
         getDamaState: () -> DamaState,
         getPieces: () -> List<Piece>,
         mode: Mode,
         boardColor: Color = Color.Black,
         onITap: (Piece) -> Unit,
         onFTap: (Piece) -> Unit){



    Box(modifier = modifier
            .width(300.dp)
            .height(400.dp)) {

        val painter1 = rememberVectorPainter(image = ImageVector.vectorResource(R.drawable.purple))
        val painter2 = rememberVectorPainter(image = ImageVector.vectorResource(R.drawable.green))
        val damaState = getDamaState()
        val pieces = getPieces()
        var spots: Map<Int, Position> = emptyMap()
        var pieceSize = 0f
        var user by rememberSaveable {
            mutableIntStateOf(0)
        }



        Canvas(modifier = Modifier.fillMaxSize()
            .pointerInput(Unit){
                detectTapGestures(
                    onTap = { tapOffset ->


                        tapping(damaState = damaState,pieces = pieces, tapOffset = tapOffset, pieceSize = pieceSize,
                           getUser = {user}, changeUser = {chUser -> user = chUser}, mode = mode, spots = spots, onITap = onITap, onFTap = onFTap)


                    }
                )
            }){
                Log.d(TAG, "drawing")
                val width = size.width
                val height = size.height
                val strokeWidth = 5f
                val min = min(height, width)
                pieceSize = min / 8f
                spots =
                    mapOf(
                        1 to Position(x = 0f, y = 0f),
                        2 to Position(x = width / 2f - pieceSize / 2f - strokeWidth / 2f, y = 0f),
                        3 to Position(x = width - pieceSize, y = 0f),
                        4 to Position(x = 0f, y = height / 2f - pieceSize / 2f),
                        5 to Position(x = width / 2f - pieceSize / 2f, y = height / 2f - pieceSize / 2f),
                        6 to Position(x = width - pieceSize, y = height / 2f - (pieceSize / 2f) ),
                        7 to Position(x = 0f, y = height - pieceSize),
                        8 to Position(x = width / 2f - pieceSize / 2f -strokeWidth / 2f, y = height - pieceSize),
                        9 to Position(x = width - pieceSize, y = height - pieceSize)
                    )


                clipRect {
                board(width = width, height = height, pieceSize = pieceSize, color = boardColor, strokeWidth = strokeWidth)
                pieces(
                    pieces = pieces,
                    spots = spots, painter1 = painter1, painter2 = painter2,
                    pieceSize = pieceSize
                )

                }
            }
        }
}


private fun PointerInputScope.tapping(
    damaState: DamaState,
    pieces: List<Piece>,
    tapOffset: Offset,
    pieceSize: Float,
    getUser: () -> Int,
    changeUser: (Int) -> Unit,
    mode: Mode,
    spots: Map<Int, Position>,
    onITap: (Piece) -> Unit,
    onFTap: (Piece) -> Unit
) {
    val user = getUser()
    if (mode == Mode.EmptyBoard) {
        val userPieces = pieces.filter { it.user == damaState.userTurn }
        if (userPieces.size < 3) {
            spots.forEach { spot ->
                Log.d(TAG, "spot loop")
                val x = spot.value.x
                val y = spot.value.y
                if (tapOffset.x > x && tapOffset.x < x + pieceSize &&
                    tapOffset.y > y && tapOffset.y < y + pieceSize
                ) {
                    Log.d(TAG, "Ftapped")
                    onFTap(
                        Piece(
                            damaState.userTurn,
                            spot.key,
                            PieceState()
                        )
                    )

                    return
                }
            }
            return
        }
    }
    run breaking@{
        pieces.forEach { piece ->
            val position = piece.pieceState.position ?: return@breaking

            val x = position.x
            val y = position.y


            if (tapOffset.x > x && tapOffset.x < x + pieceSize &&
                tapOffset.y > y && tapOffset.y < y + pieceSize
            ) {
                Log.d(TAG, "piece is found")
                changeUser(piece.user)
                onITap(piece)
                return
            }

        }
    }


    spots.forEach { spot ->
        Log.d(TAG, "spot loop")
        val x = spot.value.x
        val y = spot.value.y
        if (tapOffset.x > x && tapOffset.x < x + pieceSize &&
            tapOffset.y > y && tapOffset.y < y + pieceSize
        ) {
            Log.d(TAG, "Ftapped")
            if (user == 0) return
            onFTap(
                Piece(
                    user,
                    spot.key,
                    PieceState()
                )
            )
            changeUser(0)

            return
        }
    }
}


private fun DrawScope.pieces(
    pieces: List<Piece>,
    spots: Map<Int, Position>,
    painter1: VectorPainter,
    painter2: VectorPainter,
    pieceSize: Float
) {
    pieces.forEach { piece ->
        val x = spots[piece.spotPosition]!!.x
        val y = spots[piece.spotPosition]!!.y

        piece.pieceState.position = Position(x = x, y = y)
        Log.d(TAG, "tap from uio: ${piece.pieceState.pieceTapState}")

        translate(x, y) {
            var scale = 1f
            if (piece.pieceState.pieceTapState == PieceTapState.Tap) {
                scale = 1.2f
                Log.d(TAG, "scaled")
            }
            scale(scale, pivot = Offset(x = pieceSize / 2f, y = pieceSize / 2f)) {
                if (piece.user == 1) {
                    with(painter1) {
                        draw(
                            size = Size(pieceSize, pieceSize)
                        )
                    }

                } else {
                    with(painter2) {
                        draw(
                            size = Size(pieceSize, pieceSize)
                        )
                    }
                }

            }

        }
    }
}


private fun DrawScope.board(
    width: Float,
    height: Float,
    pieceSize: Float,
    color: Color,
    strokeWidth: Float
) {

    drawRect(
        style = Stroke(strokeWidth),
        size = Size(width - pieceSize, height - pieceSize),
        color = color,
        topLeft = Offset(x = pieceSize / 2f, y = pieceSize / 2f)
    )

    drawLine(
        color = color,
        strokeWidth = strokeWidth,
        start = Offset(x = pieceSize / 2f, y = pieceSize / 2f),
        end = Offset(x = width - pieceSize / 2f, y = height - pieceSize / 2f)
    )
    drawLine(
        color = color,
        strokeWidth = strokeWidth,
        start = Offset(x = width - pieceSize / 2f , y = pieceSize / 2f),
        end = Offset(x = pieceSize / 2f, y = height - pieceSize / 2f)
    )
    drawLine(
        color = color,
        strokeWidth = strokeWidth,
        start = Offset(x = width / 2f - strokeWidth / 2f, y = pieceSize / 2f),
        end = Offset(x = width / 2 - strokeWidth / 2f, y = height - pieceSize / 2f)
    )
    drawLine(
        color = color,
        strokeWidth = strokeWidth,
        start = Offset(x = pieceSize / 2f, y = height / 2f - strokeWidth / 2f),
        end = Offset(x = width - pieceSize / 2f, y = height / 2f - strokeWidth / 2f)
    )
}



