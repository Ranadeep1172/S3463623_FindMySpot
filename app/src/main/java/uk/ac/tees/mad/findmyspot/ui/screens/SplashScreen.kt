package uk.ac.tees.mad.findmyspot.ui.screens


import android.os.Handler
import android.os.Looper
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay
import uk.ac.tees.mad.findmyspot.R

@Composable
fun SplashScreen(navController: NavController) {
    var startAnimation by remember { mutableStateOf(false) }
    val alphaAnim = animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0f,
        animationSpec = tween(durationMillis = 1000), label = "fadeAnimation"
    )

    LaunchedEffect(Unit) {
        startAnimation = true
        Handler(Looper.getMainLooper()).postDelayed({
            val user = FirebaseAuth.getInstance().currentUser
            startAnimation = false
        }, 1500)

        Handler(Looper.getMainLooper()).postDelayed({
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                navController.navigate("home") { popUpTo("splash") { inclusive = true } }
            } else {
                navController.navigate("auth") { popUpTo("splash") { inclusive = true } }
            }
        }, 2000)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.alpha(alphaAnim.value)
        ) {
            Image(
                painter = painterResource(id = R.drawable.app_logo),
                contentDescription = "FindMySpot Logo",
                modifier = Modifier.size(150.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "FindMySpot",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}