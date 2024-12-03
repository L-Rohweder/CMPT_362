package com.example.beacon.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.widget.SwitchCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.beacon.R
import com.example.beacon.activities.LoginActivity
import com.example.beacon.activities.ProfileActivity
import com.example.beacon.databinding.FragmentSettingsBinding
import com.example.beacon.utils.Constants
import com.example.beacon.view_models.UserViewModel

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var profileButton: TextView
    private lateinit var anonSwitch: SwitchCompat
    private lateinit var logoutButton: Button
    private lateinit var prefs: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root
        prefs = requireActivity().getSharedPreferences(Constants.SP_KEY, Context.MODE_PRIVATE)

        // Setup default as anonymous check
        anonSwitch = root.findViewById(R.id.anonSwitch)
        anonSwitch.isChecked = prefs.getBoolean(Constants.SP_IS_ANON, false)
        anonSwitch.setOnCheckedChangeListener { _, value ->
            prefs.edit().putBoolean(Constants.SP_IS_ANON, value).apply()
        }

        // Setup profile button
        profileButton = root.findViewById(R.id.openProfileText)
        profileButton.setOnClickListener {
            startActivity(Intent(activity, ProfileActivity::class.java))
        }

        // Setup logout button
        logoutButton = root.findViewById(R.id.logoutButton)
        logoutButton.setOnClickListener {
            // Clear auth preferences
            requireActivity().getSharedPreferences("AUTH", Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply()

            // Return to login screen
            startActivity(Intent(requireActivity(), LoginActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
            requireActivity().finish()
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}