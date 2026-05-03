package com.example.dailydog.compose

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.dailydog.data.DogDatabase
import com.example.dailydog.data.DogItem
import com.example.dailydog.viewmodel.DogViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@Composable
fun DailyDogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
        darkColorScheme(
            primary = Color(0xFFBB86FC),
            secondary = Color(0xFF03DAC5),
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF6200EE),
            secondary = Color(0xFF03DAC5),
            background = Color(0xFFF6F6F6),
            surface = Color.White
        )
    }

    MaterialTheme(
        colorScheme = colors,
        content = content
    )
}

class MainActivity : ComponentActivity() {
    private val viewModel: DogViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DailyDogTheme {
                var selectedDog by remember { mutableStateOf<DogItem?>(null) }
                var showingFavorites by remember { mutableStateOf(false) }

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(if (selectedDog != null) "Dog Details" else if (showingFavorites) "Favorites" else "DailyDog") },
                            navigationIcon = {
                                if (selectedDog != null) {
                                    IconButton(onClick = { selectedDog = null }) {
                                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                                    }
                                }
                            },
                            actions = {
                                if (selectedDog == null) {
                                    TextButton(onClick = {
                                        showingFavorites = !showingFavorites
                                        viewModel.toggleFavorites(showingFavorites)
                                    }) {
                                        Text(if (showingFavorites) "All Dogs" else "Favorites", color = MaterialTheme.colorScheme.primary)
                                    }
                                }
                            }
                        )
                    }
                ) { paddingValues ->
                    Surface(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        if (selectedDog != null) {
                            DogDetailScreen(
                                dog = selectedDog!!,
                            )
                        } else {
                            DogListScreen(
                                viewModel = viewModel,
                                onDogClick = { selectedDog = it }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DogListScreen(viewModel: DogViewModel, onDogClick: (DogItem) -> Unit) {
    val dogs by viewModel.dogs.observeAsState(emptyList())
    val isLoading by viewModel.isLoading.observeAsState(false)
    val errorEvent by viewModel.errorEvent.observeAsState()

    val context = LocalContext.current
    LaunchedEffect(errorEvent) {
        errorEvent?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    if (isLoading && dogs.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    } else {
        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 150.dp),
            contentPadding = PaddingValues(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(dogs) { dog ->
                DogCard(dog = dog, onClick = { onDogClick(dog) })
            }
        }
    }
}

@Composable
fun DogCard(dog: DogItem, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth()
            .aspectRatio(1f)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AsyncImage(
                model = dog.imageUrl,
                contentDescription = dog.breed,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth(),
                color = Color.Black.copy(alpha = 0.6f)
            ) {
                Text(
                    text = dog.breed,
                    color = Color.White,
                    modifier = Modifier.padding(8.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}

@Composable
fun DogDetailScreen(dog: DogItem) {
    val context = LocalContext.current
    val dogDao = remember { DogDatabase.getDatabase(context).dogDao() }
    val scope = rememberCoroutineScope()
    var isFavorite by remember { mutableStateOf(false) }

    LaunchedEffect(dog.imageUrl) {
        val dogInDb = withContext(Dispatchers.IO) { dogDao.getDogByUrl(dog.imageUrl) }
        isFavorite = dogInDb?.isFavorite == true
    }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            AsyncImage(
                model = dog.imageUrl,
                contentDescription = dog.breed,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = dog.breed,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            // Animation feature: animateContentSize for favorite button
            Box(
                modifier = Modifier
                    .animateContentSize(
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
            ) {
                IconButton(onClick = {
                    scope.launch {
                        withContext(Dispatchers.IO) {
                            if (isFavorite) {
                                dogDao.removeFavorite(dog.imageUrl)
                            } else {
                                dogDao.addFavoriteFIFO(DogItem(dog.imageUrl, dog.breed))
                            }
                        }
                        isFavorite = !isFavorite
                    }
                }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) Color.Red else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(if (isFavorite) 36.dp else 24.dp) // Micro-animation size change
                    )
                }
            }
        }
    }
}
