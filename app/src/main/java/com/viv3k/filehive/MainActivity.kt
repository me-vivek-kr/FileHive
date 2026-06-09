package com.viv3k.filehive

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.fragment.app.FragmentActivity
import com.viv3k.filehive.core.navigation.AppNavigation
import com.viv3k.filehive.ui.StoragePermissionGate

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            StoragePermissionGate {
                AppNavigation()
            }
        }
    }
}
