    package com.example.androidapprpg.ui.activity

    import android.os.Bundle
    import android.widget.PopupMenu
    import androidx.activity.enableEdgeToEdge
    import androidx.appcompat.app.AppCompatActivity
    import androidx.core.view.ViewCompat
    import androidx.core.view.WindowInsetsCompat
    import androidx.core.view.WindowInsetsControllerCompat
    import androidx.core.view.isVisible
    import androidx.core.view.updatePadding
    import androidx.navigation.NavController
    import androidx.navigation.fragment.findNavController
    import com.example.androidapprpg.R
    import com.example.androidapprpg.databinding.ActivityGameManagerBinding
    import dagger.hilt.android.AndroidEntryPoint

    @AndroidEntryPoint
    class ActivityGameMaster : AppCompatActivity() {

        private lateinit var binding: ActivityGameManagerBinding
        private var navController: NavController? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            enableEdgeToEdge()

            binding = ActivityGameManagerBinding.inflate(layoutInflater)
            setContentView(binding.root)

            val navHostFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
            navController = navHostFragment!!.findNavController()

            val popupMenu = PopupMenu(this, null)
            popupMenu.inflate(R.menu.bottom_navigation_menu_game_manager)
            binding.bottomNavigation.setupWithNavController(popupMenu.menu, navController!!)


            navController!!.addOnDestinationChangedListener { _, dest, _ ->
                val isChat = dest.id == R.id.ChatFragment
                binding.bottomNavigation.isVisible = !isChat
                enterImmersive()  // mantém imersivo em qualquer destino
            }

            applyBottomBarInsetsForCutout()
        }


        override fun onWindowFocusChanged(hasFocus: Boolean) {
            super.onWindowFocusChanged(hasFocus)
            if (hasFocus) enterImmersive()
        }

        private fun enterImmersive() {
            val ic = WindowInsetsControllerCompat(window, window.decorView)
            ic.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            ic.hide(WindowInsetsCompat.Type.systemBars())
        }

        private fun applyBottomBarInsetsForCutout() {

            ViewCompat.setOnApplyWindowInsetsListener(binding.bottomNavigation) { v, insets ->
                val cut = insets.getInsets(WindowInsetsCompat.Type.displayCutout())
                v.updatePadding(bottom = cut.bottom)
                insets
            }
        }
    }
