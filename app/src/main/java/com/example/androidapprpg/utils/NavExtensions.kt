
package com.example.androidapprpg.utils

import androidx.fragment.app.Fragment
import com.example.androidapprpg.ui.activity.ActivityGameMaster
import com.example.androidapprpg.ui.activity.ActivityMainCard

fun Fragment.activityGameId(): Long =
    (requireActivity() as ActivityMainCard).gameId()

fun Fragment.gmActivityGameId(): Long =
    (requireActivity() as ActivityGameMaster).gameId()
