package com.example.dama3.ui

import android.content.Context
import android.os.Build.VERSION.SDK_INT
import android.util.Log
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.ImageLoader
import coil.compose.rememberAsyncImagePainter
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.request.ImageRequest
import coil.size.Size
import com.example.dama3.R
import com.example.domain.piece.Piece
import com.example.domain.states.Difficulty
import com.example.domain.states.GameState
import com.example.domain.states.Mode
import com.example.domain.userConf.UserConf
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import okhttp3.internal.toImmutableList

@Composable
private fun LoadGif(@DrawableRes resource: Int, context: Context, contentDescription: String) {
    val imgLoader = ImageLoader.Builder(context)
        .components{
            if (SDK_INT >= 28) {
                add(ImageDecoderDecoder.Factory())
            } else {
                add(GifDecoder.Factory())
            }
        }.build()
    Image(painter = rememberAsyncImagePainter(
        ImageRequest.Builder(context).data(data = resource).apply(block = {
            size(Size.ORIGINAL)
        }).build(), imageLoader = imgLoader
    ), contentDescription = contentDescription )
}

@Composable
fun Play(modifier: Modifier = Modifier,
         mode: Mode,
         userConf: UserConf,
         dif: Difficulty
         ) {

    Column(modifier = Modifier
        .fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center)
    {

        val damaViewModel = viewModel<DamaViewModel>()
        var launched by rememberSaveable {
            mutableStateOf(false)
        }
        LaunchedEffect(launched) {
            if (launched) return@LaunchedEffect
            damaViewModel.start(mode, userConf, dif)
            launched = true
        }
        val pieces = damaViewModel.pieces
        val damaState by damaViewModel.damaState.collectAsStateWithLifecycle()
        val youWin by damaViewModel.youWin.collectAsStateWithLifecycle()
        val context = LocalContext.current

        when(damaState.gameState){
            GameState.Play -> {
                Dama(
                    getDamaState = {damaState},
                    getPieces = {pieces},
                    mode = mode,
                    onITap = { piece ->
                        damaViewModel.iTap(piece)
                    },
                    onFTap = { piece ->
                        damaViewModel.fTap(piece)
                    })
            }
            GameState.Win -> {

                if (youWin){
                    LoadGif(resource = R.drawable.youwin,
                        context = context,
                        contentDescription = "You win!")
                }
            }

        }


    }

}