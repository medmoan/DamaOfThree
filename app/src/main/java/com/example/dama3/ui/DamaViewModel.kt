package com.example.dama3.ui


import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.domain.Action
import com.example.domain.damaGame.DamaGame
import com.example.domain.states.Mode
import com.example.domain.piece.Piece
import com.example.domain.states.Difficulty
import com.example.domain.userConf.UserConf
import kotlinx.coroutines.launch


class DamaViewModel: ViewModel() {
    companion object {
        const val TAG = "DamaViewModel"
    }
    private val dama = DamaGame()
    private val piecesChanges = dama.piecesChanges
    private val _pieces = mutableStateListOf<Piece>()
    val pieces: List<Piece> = _pieces
    val damaState = dama.damaState
    val youWin = dama.youWin



    init {
        viewModelScope.launch {
            piecesChanges.collect { pieceChange ->
                val piece = pieceChange.piece
                val action = pieceChange.action
                when(action) {
                    Action.Add -> {
                        Log.d(TAG, "Added piece to state list")
                        _pieces.add(
                            piece
                        )
                    }
                    Action.Replace -> {
                        val position = pieceChange.position
                        Log.d(TAG, "Changed piece in state list")
                        _pieces[position] = piece
                    }
                }
            }
        }


    }

    fun iTap(piece: Piece){
        viewModelScope.launch {
            dama.iTap(piece)


        }
    }

    fun fTap(piece: Piece){
        viewModelScope.launch {
            dama.fTap(piece)
        }
    }
    fun start(mode: Mode, userConf: UserConf, dif: Difficulty){

        viewModelScope.launch {
            dama.start(mode = mode, userConf = userConf, dif = dif)

        }
    }
    fun restart(){
        viewModelScope.launch {
            dama.restart()
        }
    }


}




