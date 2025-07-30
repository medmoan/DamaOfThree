package com.example.domain.damaGame


import android.util.Log
import com.example.domain.Action

import com.example.domain.NotifyChanges
import com.example.domain.PiecesChanges
import com.example.domain.piece.DamaActions
import com.example.domain.states.DamaState
import com.example.domain.states.Difficulty
import com.example.domain.states.GameState
import com.example.domain.states.Mode
import com.example.domain.piece.Piece
import com.example.domain.states.PieceState
import com.example.domain.states.PieceTapState
import com.example.domain.userConf.UserConf
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.consumeAsFlow


class DamaGame: NotifyChanges, DamaActions {
    companion object {

        private const val TAG = "DamaGame"
        private const val MY_USER = 1
        private const val OTHER_USER = 2
        private const val NA_USER = 0
        private const val INVALID_INDEX = -1
    }
    private val pieces = mutableListOf<Piece>()
    private var position = INVALID_INDEX
    private var mode: Mode = Mode.FullBoard
    private var _youWin = MutableStateFlow(false)
    val youWin = _youWin as StateFlow<Boolean>
    private var _damaState = MutableStateFlow(DamaState(MY_USER, GameState.Play))
    val damaState = _damaState as StateFlow<DamaState>
    private var userConf = UserConf(MY_USER, true)
    private var dif = Difficulty.EASY
    private var selectedPiece = Piece(NA_USER, 1, PieceState())

    private val _piecesChanges = Channel<PiecesChanges>()
    // This is to observe changes in viewmodel
    override val piecesChanges = _piecesChanges.consumeAsFlow()
    private val spots = listOf(1, 2, 3, 4, 5, 6, 7, 8, 9)
    private val moves = mapOf(
        1 to listOf(2, 5, 4),
        2 to listOf(1, 3, 5),
        3 to listOf(5, 2, 6),
        4 to listOf(1, 5, 7),
        5 to listOf(1, 2, 3, 4, 6, 7, 8, 9),
        6 to listOf(3, 5, 9),
        7 to listOf(4, 5, 8),
        8 to listOf(7, 5, 9),
        9 to listOf(8, 5, 6)
    )
    private val winMoves
        get() =
            if (mode == Mode.FullBoard)
                if (selectedPiece.user == MY_USER)
                    listOf(
                        listOf(1, 2, 3),
                        listOf(4, 5, 6),
                        listOf(1, 4, 7),
                        listOf(2, 5, 8),
                        listOf(3, 6, 9),
                        listOf(1, 5, 9),
                        listOf(3, 5, 7)
                    )
                else
                    listOf(
                        listOf(4, 5, 6),
                        listOf(7, 8, 9),
                        listOf(1, 4, 7),
                        listOf(2, 5, 8),
                        listOf(3, 6, 9),
                        listOf(1, 5, 9),
                        listOf(3, 5, 7)
                    )
            else
                listOf(
                    listOf(1, 2, 3),
                    listOf(7, 8, 9),
                    listOf(4, 5, 6),
                    listOf(1, 4, 7),
                    listOf(2, 5, 8),
                    listOf(3, 6, 9),
                    listOf(1, 5, 9),
                    listOf(3, 5, 7)
                )

    private val afterWin: () -> Unit = {
        _damaState.value = DamaState(NA_USER, GameState.Win)
        val user = selectedPiece.user
        _youWin.value = user == MY_USER
        if (youWin.value)
            Log.d(TAG, "You win")
        else
            Log.d(TAG, "You loose")


    }
    private val checkWin: (Piece) -> Boolean = { piece ->
        val playerMoves = pieces.filter { it.user == piece.user }.map { it.spotPosition }
        winMoves.any { it.containsAll(playerMoves) }
    }


    override suspend fun start(
        mode: Mode,
        userConf: UserConf,
        dif: Difficulty
    ) {
        if (userConf.userturn !in 1..2) throw IllegalArgumentException()
        this@DamaGame.userConf = userConf
        this@DamaGame.dif = dif
        this@DamaGame.mode = mode
        val userturn = userConf.userturn
        if (mode == Mode.FullBoard) {
            val addAction = Action.Add
            for (i in 1..3){
                val otherPiece = Piece(OTHER_USER, i, PieceState())
                pieces.add(otherPiece)
                _piecesChanges.send(PiecesChanges(otherPiece, addAction))
            }
            for (i in 7..9){
                val myPiece = Piece(MY_USER, i, PieceState())
                pieces.add(myPiece)
                _piecesChanges.send(PiecesChanges(myPiece, addAction))
            }

        }

        _damaState.value = DamaState(userturn, GameState.Play)
        auto()
    }
    override suspend fun restart(){
        start(mode, userConf, dif)
    }
    private suspend fun autoPlay() {
        val user = OTHER_USER
//        if (_damaState.value.userTurn != user) return

        val userPieces = pieces.filter { it.user == user }
        if (mode == Mode.EmptyBoard && userPieces.size < 3) {
            val emptySpots = getEmptySpots()
            val spot = emptySpots.random()
            val newPiece = Piece(
                user,
                spot,
                PieceState()
            )
            delay(200)
            fTap(newPiece)
            return
        }

        val possibleMoves = mutableListOf<Pair<Piece, Int>>()
        // Gather all possible moves for the user's pieces
        for (piece in userPieces) {
            moves[piece.spotPosition]?.forEach { destination ->
                if (pieces.none { it.spotPosition == destination }) {
                    possibleMoves.add(piece to destination)
                }
            }
        }

        if (possibleMoves.isEmpty()) return
        val (piece, destination) = when (dif) {
            Difficulty.EASY -> {
                // Random safe move
                val safeMoves = possibleMoves.filter { (_, dest) ->
                    !isDisadvantageousMove(user, dest)
                }
                (safeMoves.ifEmpty { possibleMoves }).random()
            }

            Difficulty.NORMAL -> {
                // Prioritize corner or center moves, avoid immediate losses
                val strategicMoves = possibleMoves.filter { (_, dest) ->
                    dest in listOf(1, 3, 5, 7, 9) && !isDisadvantageousMove(user, dest)
                }
                if (strategicMoves.isNotEmpty()) strategicMoves.random() else possibleMoves.random()
            }

            Difficulty.HARD -> {
                // Attempt winning or capture moves first
                possibleMoves.firstOrNull { (_, dest) -> canWinMove(user, dest) }
                    ?: possibleMoves.firstOrNull { (p, dest) ->
                        canCaptureMove(
                            user,
                            p.spotPosition,
                            dest
                        )
                    }
                    ?: possibleMoves.random()
            }
        }

        delay(200)
        iTap(piece)  // Select the piece
        delay(100)
        fTap(piece.copy(spotPosition = destination))  // Move to destination
    }
    // Initial tap to select a piece and get its position in the list
    override suspend fun iTap(piece: Piece){
        Log.d(TAG, "iTap launched!")
        if (piece.user != _damaState.value.userTurn){
            Log.d(TAG, "position: $position")
            return
        }

        val tapState = if (piece.pieceState.pieceTapState == PieceTapState.NoTap)
            PieceTapState.Tap
        else
            PieceTapState.NoTap

        val tempPosition: Int = pieces.indexOfFirst { it.spotPosition == piece.spotPosition }
        Log.d(TAG, "tempPosition $tempPosition")
        // Taped another piece of the same user
        if (tempPosition != position && selectedPiece != piece) {
            selectedPiece =
                selectedPiece.copy(pieceState = PieceState(pieceTapState = tapState))
            if (selectedPiece.user == piece.user){
                Log.d(TAG, "Selected piece")
                //pieces[position] = selectedPiece
                iTap(selectedPiece)
            }
        }
        // Initial tap of that piece and setting a selective piece to it,
        // and in case non scaled piece the position is back to -1
        if (tempPosition != INVALID_INDEX) {
            selectedPiece = piece.copy(pieceState = PieceState(pieceTapState = tapState))
            position = if (tapState == PieceTapState.NoTap) INVALID_INDEX else tempPosition
            pieces[tempPosition] = selectedPiece
            _piecesChanges.send(PiecesChanges(selectedPiece, Action.Replace, tempPosition))
            Log.d(TAG, "tapstate $tapState ${pieces[tempPosition].pieceState.pieceTapState}")

        }
    }
    // Final tap to move selected piece to empty spot according to move allowed
    override suspend fun fTap(piece: Piece) {
        if (_damaState.value.userTurn != piece.user) return
        if (mode == Mode.EmptyBoard) {
            val userPieces = pieces.filter { it.user == piece.user }
            if (userPieces.size < 3){
                val emptySpots = getEmptySpots()
                if (emptySpots.contains(piece.spotPosition)) {
                    pieces.add(piece)
                    _piecesChanges.send(PiecesChanges(piece, Action.Add))
                    if (userPieces.size == 2) {
                        val isWin = checkWin(piece)
                        if (isWin) {
                            afterWin()
                            return
                        }
                    }
                    _damaState.value.userTurn =  if (piece.user == MY_USER) OTHER_USER else MY_USER
                    auto()
                    Log.d(TAG, "piece added")
                    return
                }
                return

            }
        }
        if (position == INVALID_INDEX) return
        var tappedRightSpot = false


        // Validate if the spot can be moved to
        if (getEmptySpots().contains(piece.spotPosition)) {
            moves[selectedPiece.spotPosition]?.let { potentialMoves ->
                if (potentialMoves.contains(piece.spotPosition) && pieces.none { it.spotPosition == piece.spotPosition }) {
                    Log.d(TAG, "User: ${piece.user}, moved the piece to ${piece.spotPosition}")
                    pieces[position] = piece
                    _piecesChanges.send(PiecesChanges(pieces[position], Action.Replace, position))
                    tappedRightSpot = true
                    position = INVALID_INDEX
                }
            }
        }


        if (!tappedRightSpot) {
            Log.d(TAG, "Tapped on an incorrect spot")
            pieces[position] = selectedPiece.copy(
                pieceState = PieceState(pieceTapState = PieceTapState.NoTap)
            )
            _piecesChanges.send(PiecesChanges(pieces[position], Action.Replace, position))
            position = -1
            return
        }

        // Win condition check and switching user turn
        val isWin = checkWin(piece)

        if (isWin) {
            afterWin()
        } else {
            _damaState.value = _damaState.value.copy(userTurn = if (piece.user == MY_USER) OTHER_USER else MY_USER)
            auto()
        }

    }

    private suspend fun auto() {
        val userAuto = userConf.userAuto
        val userTurn = _damaState.value.userTurn
        if (userTurn == OTHER_USER && userAuto) {
            autoPlay()
        }
    }

    // Helper function to check for winning move
    private fun canWinMove(user: Int, destination: Int): Boolean {
        val testMoves = pieces.filter { it.user == user }.map { it.spotPosition }.toMutableList()
        testMoves.add(destination)
        return winMoves.any { it.containsAll(testMoves) }
    }

    // Helper function to check if a move can capture opponent pieces
    private fun canCaptureMove(user: Int, srcPos: Int, destPos: Int): Boolean {
        val opponentPieces = pieces.filter { it.user != user }
        val adjacentPositions = moves[srcPos] ?: return false
        return adjacentPositions.contains(destPos) && opponentPieces.any { it.spotPosition == destPos }
    }

    // Helper function to check if a move is disadvantageous (for EASY & MEDIUM AI)
    private fun isDisadvantageousMove(user: Int, destination: Int): Boolean {
        // Check if moving to this position is likely to be captured
        val opponent = if (user == MY_USER) OTHER_USER else MY_USER
        return pieces.any { it.user == opponent && moves[it.spotPosition]?.contains(destination) == true }
    }
    // Function to find all empty spots on the board
    private fun getEmptySpots(): List<Int> {
        // Find spots not occupied by any piece
        return spots.filter { spotPosition ->
            pieces.none { it.spotPosition == spotPosition }
        }
    }


}
