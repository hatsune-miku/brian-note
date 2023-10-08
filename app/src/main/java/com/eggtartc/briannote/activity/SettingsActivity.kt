package com.eggtartc.briannote.activity

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.eggtartc.briannote.R
import com.eggtartc.briannote.constants.Keys
import com.eggtartc.briannote.databinding.ActivitySettingsBinding
import com.eggtartc.briannote.fragment.SettingsFragment
import com.google.android.material.elevation.SurfaceColors

class SettingsActivity: BaseActivity() {
    private lateinit var binding: ActivitySettingsBinding
    lateinit var applicationResultLauncher: ActivityResultLauncher<Intent>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.topAppBar.setBackgroundColor(SurfaceColors.SURFACE_2.getColor(this))
        binding.topAppBar.setNavigationOnClickListener {
            finish()
        }

        val fragment = SettingsFragment(this)
        supportFragmentManager.beginTransaction()
            .replace(R.id.frameLayout, fragment)
            .commit()

        applicationResultLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            fragment.onActivityResult(
                result.resultCode == Activity.RESULT_OK,
                result.data?.getStringExtra(Keys.ASSOCIATED_OPERATION) ?: ""
            )
        }
    }
}
