package com.example.dailydog.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import com.example.dailydog.R
import com.example.dailydog.databinding.ActivityMainBinding
import com.example.dailydog.viewmodel.DogViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: DogViewModel
    private lateinit var adapter: DogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[DogViewModel::class.java]

        setupRecyclerView()
        setupObservers()
        
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }

        binding.toggleGroup.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btnAll -> viewModel.toggleFavorites(false)
                    R.id.btnFavorites -> viewModel.toggleFavorites(true)
                }
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = DogAdapter(mutableListOf()) { dog ->
            val intent = Intent(this, DetailsActivity::class.java).apply {
                putExtra("EXTRA_IMAGE_URL", dog.imageUrl)
                putExtra("EXTRA_BREED", dog.breed)
            }
            startActivity(intent)
        }
        binding.recyclerViewDogs.layoutManager = GridLayoutManager(this, 2)
        binding.recyclerViewDogs.adapter = adapter
    }

    private fun setupObservers() {
        viewModel.dogs.observe(this) { dogsList ->
            adapter.updateData(dogsList)
            // Immediately stop refreshing when list observes changes
            binding.swipeRefreshLayout.isRefreshing = false
        }

        viewModel.isLoading.observe(this) { isLoading ->
            if (!isLoading) {
                binding.swipeRefreshLayout.isRefreshing = false
            }
            binding.progressBar.visibility = if (isLoading && adapter.itemCount == 0) android.view.View.VISIBLE else android.view.View.GONE
        }
        
        viewModel.errorEvent.observe(this) { errMsg ->
            Toast.makeText(this, errMsg, Toast.LENGTH_LONG).show()
        }
    }
}
