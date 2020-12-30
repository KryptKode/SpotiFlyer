package com.shabinder.spotiflyer.ui.platforms.spotify

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.navigation.NavController
import com.shabinder.spotiflyer.models.PlatformQueryResult
import com.shabinder.spotiflyer.models.spotify.Source
import com.shabinder.spotiflyer.ui.tracklist.TrackList
import com.shabinder.spotiflyer.utils.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun Spotify(fullLink: String, navController: NavController,) {
    val source: Source = Source.Spotify
    val coroutineScope = rememberCoroutineScope()
    var result by remember { mutableStateOf<PlatformQueryResult?>(null) }

    var spotifyLink =
        "https://" + fullLink.substringAfterLast("https://").substringBefore(" ").trim()
    log("Spotify Fragment Link", spotifyLink)

    coroutineScope.launch(Dispatchers.Default) {

        /*
        * New Link Schema: https://link.tospotify.com/kqTBblrjQbb,
        * Fetching Standard Link: https://open.spotify.com/playlist/37i9dQZF1DX9RwfGbeGQwP?si=iWz7B1tETiunDntnDo3lSQ&amp;_branch_match_id=862039436205270630
        * */
        if (!spotifyLink.contains("open.spotify")) {
            val resolvedLink = resolveLink(spotifyLink, sharedViewModel.gaanaInterface)
            log("Spotify Resolved Link", resolvedLink)
            spotifyLink = resolvedLink
        }

        val link = spotifyLink.substringAfterLast('/', "Error").substringBefore('?')
        val type = spotifyLink.substringBeforeLast('/', "Error").substringAfterLast('/')

        log("Spotify Fragment", "$type : $link")

        if (sharedViewModel.spotifyService.value == null) {//Authentication pending!!
            if (isOnline()) mainActivity.authenticateSpotify()
        }

        if (type == "Error" || link == "Error") {
        showDialog("Please Check Your Link!")
        navController.popBackStack()
        }

        if (type == "episode" || type == "show") {
            //TODO Implementation
            showDialog("Implementing Soon, Stay Tuned!")
        } else {
            if (sharedViewModel.spotifyService.value == null){
                //Authentication Still Pending
                // TODO Better Implementation
                showDialog("Authentication Failed")
                navController.popBackStack()
            }else{
                result = spotifySearch(
                    type,
                    link,
                    sharedViewModel.spotifyService.value!!,
                    sharedViewModel.databaseDAO
                )
            }
        }
    }
    result?.let {
        log("Spotify",it.trackList.size.toString())
        TrackList(
            result = it,
            source = source
        )
    }
}