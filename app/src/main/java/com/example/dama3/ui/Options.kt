package com.example.dama3.ui

import android.util.Log
import com.example.dama3.R

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.domain.states.Difficulty
import com.example.domain.states.Mode
import com.example.domain.userConf.UserConf
const val TAG = "Options-composable"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Options(modifier: Modifier = Modifier,
            onNavigate: (getMode: () -> Mode, UserConf, Difficulty) -> Unit) {
    val scrollState = rememberScrollState()
    //val dark = MaterialTheme.colorScheme.onSurface
    //val light = MaterialTheme.colorScheme.onSurfaceVariant
    val light = Color.Gray
    val dark = Color.Black
    var placabeleColor by remember {
        mutableStateOf(light)
    }
    var unplacabeleColor by remember {
        mutableStateOf(light)
    }
    var mode = remember {
        mutableStateOf(Mode.FullBoard)
    }.value

    Column(modifier = modifier
        .fillMaxSize()
        .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Row(modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(placabeleColor)
                    .clickable {
                        if (placabeleColor == light) {
                            placabeleColor = dark
                            unplacabeleColor = light
                            mode = Mode.FullBoard
                        }
                    },
                contentAlignment = Alignment.Center
            ) {

                Image(
                    modifier = Modifier.size(150.dp),
                    painter = painterResource(id = R.drawable.non_empty),
                    contentDescription = stringResource(id = R.string.non_empty),
                    contentScale = ContentScale.Fit
                )
            }


            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(unplacabeleColor)
                    .clickable {
                        if (unplacabeleColor == light) {
                            unplacabeleColor = dark
                            placabeleColor = light
                            mode = Mode.EmptyBoard
                        }
                    },
                contentAlignment = Alignment.Center
            ) {

                 Image(
                     modifier = Modifier.size(150.dp),
                     painter = painterResource(id = R.drawable.empty),
                     contentDescription = stringResource(id = R.string.empty),
                     contentScale = ContentScale.Fit
                 )

            }
        }
        Spacer(Modifier.height(20.dp))
        Text("Play against friend or Ai")
        Spacer(modifier = Modifier.height(20.dp))
        val playingWith = remember {
            mutableStateListOf("Ai", "Friend")
        }
        var selectedIndexOfPlAgainst by remember {
            mutableIntStateOf(0)
        }
        var selectedIndexOfPlFirst by remember {
            mutableIntStateOf(0)
        }
        var selectedIndexOfDif by remember {
            mutableIntStateOf(0)
        }


        SingleChoiceSegmentedButtonRow {
            playingWith.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = selectedIndexOfPlAgainst == index,
                    onClick = { selectedIndexOfPlAgainst = index },
                    shape = SegmentedButtonDefaults.itemShape(index = index, playingWith.size)
                ) {
                    Text(option)

                }
            }

            Log.d(TAG, playingWith[selectedIndexOfPlAgainst])



        }
        Spacer(modifier = Modifier.height(20.dp))
        Text("Who plays first?")
        val whoPlaysFirst = remember {
            derivedStateOf {
                mutableStateListOf("me", playingWith[selectedIndexOfPlAgainst])
            }
        }.value
        Spacer(modifier = Modifier.height(20.dp))
        SingleChoiceSegmentedButtonRow {
            whoPlaysFirst.forEachIndexed { index, option ->
                SegmentedButton(
                    selected = selectedIndexOfPlFirst == index,
                    onClick = { selectedIndexOfPlFirst = index },
                    shape = SegmentedButtonDefaults.itemShape(index = index, playingWith.size)
                ) {
                    Text(option)

                }
            }

            Log.d(TAG, whoPlaysFirst[selectedIndexOfPlFirst])

        }
        Spacer(modifier = Modifier.height(20.dp))
        Text("Choose the difficulty")
        val difficulty = remember {
                mutableStateListOf(Difficulty.EASY, Difficulty.NORMAL, Difficulty.HARD)
        }
        SingleChoiceSegmentedButtonRow {
            difficulty.forEachIndexed { index, option ->

                SegmentedButton(
                    selected = selectedIndexOfDif == index,
                    onClick = { selectedIndexOfDif = index },
                    shape = SegmentedButtonDefaults.itemShape(index = index, difficulty.size)
                ) {
                    Text(option.toString())

                }
            }

            Log.d(TAG, difficulty[selectedIndexOfDif].toString())

        }
        val getMode = {mode}
        Button(onClick = {
            onNavigate(getMode,
                UserConf(selectedIndexOfPlFirst + 1, selectedIndexOfPlAgainst == 0),
                difficulty[selectedIndexOfDif])
        }) {
            Text("Play!")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PrevOptions() {
    Options(modifier = Modifier.padding(10.dp)) { mode, userConf, dif -> }

}
