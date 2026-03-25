package com.moviebuddy.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.moviebuddy.app.databinding.ActivityMainBinding

// In Kotlin, classes are public and final by default (no need for the 'public' keyword).
// The colon `:` replaces the `extends` and `implements` keywords from Java.
class MainActivity : AppCompatActivity() {

    // `lateinit var` tells Kotlin: "I promise to initialize this variable later, before using it."
    // In Java, this would just be: private ActivityMainBinding binding;
    // `var` means the variable is mutable (can be changed), while `val` means immutable (final).
    private lateinit var binding: ActivityMainBinding

    // `override fun` replaces java's `@Override public void`.
    // `fun` is the keyword to declare a function/method.
    // The `?` in `Bundle?` means this parameter is Nullable. Kotlin is strictly null-safe, so 
    // it forces you to handle nulls, preventing NullPointerExceptions.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
