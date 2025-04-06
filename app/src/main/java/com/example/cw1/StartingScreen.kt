package com.example.cw1

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp


@Composable
fun Home(){
    var openAbout by remember { mutableStateOf(false) }
    val context = LocalContext.current

    // Main column layout for the home screen
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("DICE GAME", fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        Row {
            Button(
                onClick = {
                    //Launches NewGame activity
                    val intent = Intent(context,NewGame::class.java)
                    context.startActivity(intent)
                }
            )
            {
                Text("New Game")
            }
            Spacer(modifier = Modifier.width(8.dp))
            Button(onClick = {openAbout = true}) {
                Text("About")
            }
        }
    }

    // Display the About dialog
    if (openAbout){
        AlertDialog(
            onDismissRequest = { openAbout = false},
            confirmButton = {
                Button(onClick = {openAbout = false}) {
                    Text("Ok")
                }
            },
            title = { Text("How To Play", fontWeight = FontWeight.Bold) },
            text = { Text("In Dice Battle, you roll five dice each turn to score points. After rolling, choose which dice to keep and which to reroll (up to two times per turn). Your goal is to reach the target score (default: 101) before the computer does. The computer also rolls dice and makes strategic reroll decisions. If both players reach the target score in the same number of turns, the highest total wins. If tied, sudden-death dice rolls decide the winner. Manage your rerolls wisely to outscore your opponent!") }
        )
    }
}