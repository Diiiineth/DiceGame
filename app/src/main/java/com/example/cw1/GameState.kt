package com.example.cw1

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue

// Object to maintain and persist game state across activities
object GameState{
    var humanWins by mutableIntStateOf(0)
    var computerWins by mutableIntStateOf(0)
}
