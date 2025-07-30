package com.example.domain.piece

import com.example.domain.states.Difficulty
import com.example.domain.states.Mode
import com.example.domain.userConf.UserConf

interface DamaActions {
    suspend fun iTap(piece: Piece)
    suspend fun fTap(piece: Piece)
    suspend fun start(mode: Mode, userConf: UserConf, dif: Difficulty)
    suspend fun restart()
}