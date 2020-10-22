package com.kolibree.android.app.unity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.kolibree.R
import com.kolibree.android.unity.GameMiddlewareActivity.Companion.launchUnityGame
import com.kolibree.android.unity.MiddlewareUnityGame
import com.kolibree.databinding.ActivityUnityNextGenPlaygroundBinding

class UnityNextGenPlaygroundActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUnityNextGenPlaygroundBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_unity_next_gen_playground)
        binding.lifecycleOwner = this

        binding.launchArchaeologyButton.setOnClickListener {
            launchUnityGame(MiddlewareUnityGame.ARCHAELOGY, UnityNextGenPlaygroundActivity::class.java)
        }

        binding.launchRabbidsButton.setOnClickListener {
            launchUnityGame(MiddlewareUnityGame.RABBIDS, UnityNextGenPlaygroundActivity::class.java)
        }
    }
}

fun Activity.launchUnityNextGenPlaygroundActivity() {
    startActivity(Intent(this, UnityNextGenPlaygroundActivity::class.java))
}
