# Dice Game Android Application
![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-0095D5?&style=for-the-badge&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-4285F4?style=for-the-badge&logo=jetpack-compose&logoColor=white)

A Kotlin-based Android application that simulates a dice game between a human player and a computer. Built with Jetpack Compose, the game features interactive dice rolling, strategic rerolls, and score tracking.

## Key Features

### Game Mechanics
- **Dice Rolling**: Both players roll 5 dice simultaneously with random outcomes (1-6)
- **Reroll Strategy**:
  - Human player can select which dice to keep for up to 2 rerolls per turn
  - Computer uses an intelligent strategy to decide which dice to reroll
- **Scoring**: Sum of dice values after each round adds to the player's total
- **Winning**: First to reach the target score (default: 101) wins

### Computer Strategy
The computer employs a sophisticated decision-making algorithm:
- Always keeps 6s and 5s (high-value dice)
- Conditionally keeps 4s based on game state
- Rerolls low-value dice (1-3) to improve score
- Adjusts strategy when significantly behind

### UI Components
- Interactive dice display with visual selection indicators
- Score tracking for both players
- Win/lose popups with color-coded messages
- Target score customization

### Technical Implementation
- **State Management**: Uses `rememberSaveable` to maintain game state across configuration changes
- **Composable Architecture**: Clean separation of UI and game logic
- **Coroutines**: Handles delayed state transitions
- **Responsive Design**: Adapts to screen orientation changes

## Game Flow
1. Players take turns rolling dice
2. Human selects which dice to keep between rolls
3. Computer automatically makes strategic reroll decisions
4. Scores are tallied after each round
5. Game continues until a player reaches the target score
6. Tiebreaker rounds resolve equal scores at target

## Code Structure
The main composable `StartGame()` handles:
- Game state management
- Player interactions
- Computer decision logic
- UI rendering

Key functions include:
- `computerReroll()` - Implements the AI strategy
- `checkWinner()` - Determines game outcome
- `score()` - Handles round completion
- `throwDice()` - Manages dice rolling mechanics

The UI features:
- Dice displays with click handlers
- Score tracking headers
- Control buttons (Throw, Score, Set Target)
- Popup dialogs for game messages
