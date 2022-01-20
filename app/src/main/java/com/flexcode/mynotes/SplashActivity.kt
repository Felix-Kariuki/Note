package com.flexcode.mynotes

import android.app.KeyguardManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.biometric.BiometricPrompt
import com.flexcode.mynotes.databinding.ActivitySplashBinding
import java.util.concurrent.Executor

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    lateinit var executor: Executor
    lateinit var biometricPrompt: BiometricPrompt
    private lateinit var callBack: BiometricPrompt.AuthenticationCallback
    private var keyguardManager: KeyguardManager? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
        checkDeviceCanAuthenticateWithBiometrics()
    }

    private fun init() {

    }

    private fun checkDeviceCanAuthenticateWithBiometrics() {

    }
}