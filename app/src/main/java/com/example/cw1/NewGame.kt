package com.example.cw1

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import com.example.cw1.ui.theme.CW1Theme
import kotlinx.coroutines.delay

class NewGame:ComponentActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent{
            CW1Theme {
                StartGame()
            }
        }
    }
}

@Composable
fun StartGame(){
    //Store dice images inside of a list
    val diceImages = listOf(
        R.drawable.one,
        R.drawable.two,
        R.drawable.three,
        R.drawable.four,
        R.drawable.five,
        R.drawable.six,
    )

    var computerPlayerScore by rememberSaveable { mutableIntStateOf(0) }
    var humanPlayerScore by rememberSaveable { mutableIntStateOf(0) }
    var humanPlayerDiceValuesList by rememberSaveable { mutableStateOf(List(5){1}) }
    var computerPlayerDiceValuesList by rememberSaveable { mutableStateOf(List(5){1}) }
    var totalHumanScore by rememberSaveable { mutableIntStateOf(0) }
    var totalComputerScore by rememberSaveable { mutableIntStateOf(0) }
    var selectedHumanDiceIndex by rememberSaveable { mutableStateOf(setOf<Int>()) }
    var throwCountHuman by rememberSaveable { mutableIntStateOf(0) }
    var throwCountComputer by rememberSaveable { mutableIntStateOf(0) }
    var winnerPopup by remember { mutableStateOf(false) }
    var winMsg by remember { mutableStateOf("") }
    var winColor by remember { mutableStateOf(Color.Black) }
    var gameOver by rememberSaveable { mutableStateOf(false) }
    var targetScore by rememberSaveable { mutableIntStateOf(101) }
    var tempTargetScore by rememberSaveable{ mutableStateOf("") }
    var targetScorePopup by remember { mutableStateOf(false) }
    val humanWins by rememberSaveable { mutableIntStateOf(GameState.humanWins) }
    val computerWins by rememberSaveable { mutableIntStateOf(GameState.computerWins) }
    var gameStarted by rememberSaveable { mutableStateOf(false) }
    var targetErrorPopup by remember { mutableStateOf(false) }
    val context = LocalContext.current
    var resetDice by rememberSaveable { mutableStateOf(false) }
    var isResetting by rememberSaveable { mutableStateOf(false) }

    /**
     * Computer's dice reroll strategy
     */
    fun computerReroll(){
        val scoreNeeded = targetScore-totalComputerScore
        val isFarBehind = totalComputerScore < totalHumanScore-30

        // Determine which dice to keep based on strategy
        val diceToKeep = computerPlayerDiceValuesList.mapIndexed { index, value ->
            when {
                value == 6 -> index
                value == 5 -> index
                value == 4 && !isFarBehind -> index
                value == 4 && (scoreNeeded <30 || throwCountComputer >2) -> index
                else -> -1
            }
        }.filter { it != -1 }

        // Reroll dice not marked for keeping
        if(diceToKeep.size < 5){
            val diceToRoll = (1..4).filter { it !in diceToKeep }
            computerPlayerDiceValuesList = computerPlayerDiceValuesList.mapIndexed { index, value ->
                if(index in diceToRoll) (1..6).random() else value
            }
            throwCountComputer ++
        }else{
            //If all dices are keeping stop rerolling for that round
            throwCountComputer = 3
        }
        /**
         * Computer Player Strategy for Rerolling Dice
         *
         * The computer player must decide whether to reroll some dice, considering that it can roll a maximum of 3 times.
         * The computer does not know the individual dice values of the human player but knows the total scores.
         *
         * Strategy Explanation:
         * 1. Always keep sixes and fives: These are high-value dice and should not be rerolled.
         * 2. Keep fours in most cases, except when:
         *    - The computer is far behind the human player (difference > 30 points), in which case it may take risks.
         *    - The remaining score needed to win is less than 30, or this is the last reroll available.
         * 3. Always reroll dice valued 3, 2, or 1: These are low-value dice that could be improved.
         * 4. The computer may take more risks when behind by a large margin: If the computer is significantly behind,
         *    it prioritizes rerolling even higher values like 4s to attempt a better outcome.
         * 5. If all dices are kept, stop rolling early to avoid unnecessary risks.
         *
         * Performance Justification:
         * - This strategy ensures that the computer maximizes its score while minimizing unnecessary risks.
         * - By focusing on rerolling low values, it improves the chances of getting 4, 5, or 6.
         * - When far behind, taking more risks can help recover lost points.
         *
         * Advantages:
         * - Efficient and simple.
         * - Balances risk and reward based on game state.
         *
         * Disadvantages:
         * - May sometimes take unnecessary risks when behind.
         */
    }

    /**
     * Check if either player has won the game
     */
    fun checkWinner(){
        var winner = ""

        // Check all possible win conditions
        if(totalHumanScore >=targetScore && totalComputerScore>=targetScore && totalComputerScore == totalHumanScore){
            // Continue rolling if both players are tied at the target score.
            while (true) {
                humanPlayerDiceValuesList = List(5) { (1..6).random() }
                computerPlayerDiceValuesList = List(5) { (1..6).random() }

                humanPlayerScore = humanPlayerDiceValuesList.sum()
                computerPlayerScore = computerPlayerDiceValuesList.sum()

                if(humanPlayerScore >computerPlayerScore){
                    winner = "human"
                    break
                }else{
                    winner = "computer"
                    break
                }
            }
        } else if(totalHumanScore >=targetScore && totalComputerScore >=targetScore){
            winner = if(totalHumanScore >totalComputerScore){
                "human"
            }else{
                "computer"
            }
        } else if (totalHumanScore >= targetScore) {
            winner = "human"
        } else if (totalComputerScore >= targetScore) {
            winner = "computer"
        }

        //Update the message, color, and game state of the winner
        if(winner == "human"){
            winMsg = "You win!"
            winColor = Color.Green
            winnerPopup = true
            GameState.humanWins++
        }else if(winner== "computer"){
            winMsg = "You lose"
            winColor = Color.Red
            winnerPopup = true
            GameState.computerWins++
        }
    }

    //Scores the current round for both players
    fun score(){
        // Prevent rolling if the game is over or it's the first round
        if(gameOver || throwCountHuman == 0)return

        // Let computer complete its remaining rolls
        while(throwCountComputer<3){
            computerReroll()
        }

        //update scores
        humanPlayerScore = humanPlayerDiceValuesList.sum()
        computerPlayerScore = computerPlayerDiceValuesList.sum()
        totalHumanScore += humanPlayerScore
        totalComputerScore += computerPlayerScore

        //After scoring check if either player has won the game
        checkWinner()

        // Indicate that dice should be reset
        resetDice = true
    }

    // Reset dice for next round
    if(resetDice){
        isResetting = true
        throwCountHuman = 0
        throwCountComputer = 0
        // Delay for 1.5 seconds to let user see the scores
        LaunchedEffect(Unit) {
            delay(1500)

            computerPlayerDiceValuesList = List(5){1}
            humanPlayerDiceValuesList = List(5){1}

            resetDice = false
            isResetting = false
        }
    }

    /**
     * Handles dice throwing logic for both players
     */
    fun throwDice(){
        // Prevent rolling if the game is over or during reset
        if(gameOver || isResetting)return

        gameStarted = true

        if(throwCountHuman==0){
            // First throw - roll all dices for both players
            humanPlayerDiceValuesList = List(5) { (1..6).random() }
            computerPlayerDiceValuesList = List(5) { (1..6).random() }
            throwCountHuman++
            throwCountComputer++
            Toast.makeText(context,"Select dice to keep or roll all again",Toast.LENGTH_SHORT).show()
        } else if (selectedHumanDiceIndex.isEmpty()){
            // No dice selected - reroll all human dices
            humanPlayerDiceValuesList = List(5) { (1..6).random() }
            throwCountHuman++
            computerReroll()
        }else{
            // Reroll only unselected human dice
            humanPlayerDiceValuesList = humanPlayerDiceValuesList.mapIndexed { index, value ->
                if(index in selectedHumanDiceIndex) {
                    value
                } else {
                    (1..6).random()
                }
            }
            selectedHumanDiceIndex = emptySet()
            throwCountHuman++
            computerReroll()
        }

        humanPlayerScore = humanPlayerDiceValuesList.sum()
        computerPlayerScore = computerPlayerDiceValuesList.sum()

        //Auto score after 3rd throw
        if(throwCountHuman == 3){
            score()
        }
    }

    //Main UI layout
    Column(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ){
            Text("H:$humanWins/C:$computerWins", fontWeight = FontWeight.Bold)
            Column {
                Text("Total Computer Score: $totalComputerScore")
                Text("Total Human Score: $totalHumanScore")
            }
        }

        Column (
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center
        ){
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                //Displaying computer dices
                computerPlayerDiceValuesList.forEach { diceValue ->
                    Image(
                        painter = painterResource(id = diceImages[diceValue - 1]),
                        contentDescription = null,
                        modifier = Modifier.size(70.dp)
                    )
                }
            }

            // Controls for dice rolls, scoring, and target setting with current dice score display
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Computer Score: $computerPlayerScore")

                Row {
                    Button(
                        onClick = { throwDice() },
                        enabled = !gameOver
                    ) {
                        Text("Throw")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { score() },
                        enabled = !gameOver
                    ) {
                        Text("Score")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        enabled = !gameOver,
                        onClick = {
                            // Only allow setting new target score before the game starts
                            if(!gameStarted) {
                                targetScorePopup = true
                            }else{
                                targetErrorPopup = true
                            }
                        }
                    ) {
                        Text("Set Target")
                    }
                }

                Text("Human Score: $humanPlayerScore")
            }

            //Displaying human dices (clickable)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
            ) {
                humanPlayerDiceValuesList.forEachIndexed { index, diceValue ->
                    Image(
                        painter = painterResource(id = diceImages[diceValue - 1]),
                        contentDescription = null,
                        modifier = Modifier
                            .size(70.dp)
                            .clickable {
                                // Only allow selection if it's not the first throw
                                if (throwCountHuman != 0) {
                                    selectedHumanDiceIndex = if (index in selectedHumanDiceIndex) {
                                        selectedHumanDiceIndex - index
                                    } else {
                                        selectedHumanDiceIndex + index
                                    }
                                }
                            },
                        // Apply red tint to the selected dices
                        colorFilter = if (index in selectedHumanDiceIndex) {
                            ColorFilter.tint(color = Color.Red)
                        } else {
                            null
                        }
                    )
                }
            }
        }
    }

    //Popup window for display winner
    if(winnerPopup){
        gameOver = true
        Popup(
            alignment = Alignment.Center,
            onDismissRequest = {winnerPopup = false},
        ) {
            Column (
                modifier = Modifier
                    .size(350.dp, 500.dp)
                    .background(
                        color = Color.Gray,
                        shape = RoundedCornerShape(20)
                    ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ){
                Text(
                    winMsg,
                    color = winColor,
                    fontSize = 30.sp,
                    fontWeight = FontWeight.Bold,
                )
            }
        }
    }

    //Set custom target score
    if(targetScorePopup){
        AlertDialog(
            onDismissRequest = {targetScorePopup=false},
            confirmButton = {
                Row {
                    Button(
                        onClick = { targetScorePopup = false }
                    ) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Button(
                        onClick = {
                            // Convert input to integer and set as target score
                            tempTargetScore.toIntOrNull()?.let { targetScore = it }
                            targetScorePopup = false
                        }
                    ) {
                        Text("Change")
                    }
                }
            },
            title = { Text("Set target score") },
            text = {
                OutlinedTextField(
                    value = tempTargetScore,
                    //Update the value when user types
                    onValueChange = { newTarget ->
                        tempTargetScore = newTarget
                    },
                    //Ensure only numeric input
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            }
        )
    }

    // Error popup for target score setting
    if(targetErrorPopup){
        AlertDialog(
            onDismissRequest = { targetErrorPopup = false },
            confirmButton = {},
            title = {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Warning,
                        contentDescription = "error icon",
                        tint = Color.Red.copy(alpha = 0.7f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Error", fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Text("Cannot set the target after game has started ",fontSize = 16.sp
            ) }
        )
    }
}