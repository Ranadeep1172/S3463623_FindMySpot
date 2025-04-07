package uk.ac.tees.mad.findmyspot.ui.screens

import android.util.Patterns
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import uk.ac.tees.mad.findmyspot.R

@Composable
fun AuthScreen(navController: NavController) {
    val context = LocalContext.current
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    var isLogin by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    val auth = FirebaseAuth.getInstance()

    fun validateInput(): Boolean {
        return when {
            email.text.isEmpty() || password.text.isEmpty() -> {
                errorMessage = "Please fill in all fields."
                false
            }

            !android.util.Patterns.EMAIL_ADDRESS.matcher(email.text).matches() -> {
                errorMessage = "Invalid email format."
                false
            }

            password.text.length < 6 -> {
                errorMessage = "Password must be at least 6 characters."
                false
            }

            else -> true
        }
    }

    fun handleAuth() {
        if (!validateInput()) return

        isLoading = true
        errorMessage = null

        if (isLogin) {
            auth.signInWithEmailAndPassword(email.text, password.text)
                .addOnCompleteListener { task ->
                    isLoading = false
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                        navController.navigate("home") { popUpTo("auth") { inclusive = true } }
                    } else {
                        errorMessage = task.exception?.message ?: "Login failed. Please try again."
                    }
                }
        } else {
            auth.createUserWithEmailAndPassword(email.text, password.text)
                .addOnCompleteListener { task ->
                    isLoading = false
                    if (task.isSuccessful) {
                        Toast.makeText(context, "Signup Successful", Toast.LENGTH_SHORT).show()
                        navController.navigate("home") { popUpTo("auth") { inclusive = true } }
                    } else {
                        errorMessage = task.exception?.message ?: "Signup failed. Please try again."
                    }
                }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 24.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.app_logo),
            contentDescription = "App Logo",
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 60.dp)
                .size(80.dp)
        )

        Card(
            modifier = Modifier
                .padding(24.dp)
                .fillMaxWidth()
                .align(Alignment.Center),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 32.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = if (isLogin) "Welcome Back" else "Create Account",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = if (isLogin) "Sign in to continue" else "Sign up to get started",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                )

                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("Email address") },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = "Email Icon") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    leadingIcon = {
                        Icon(
                            Icons.Default.Lock,
                            contentDescription = "Password Icon"
                        )
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    visualTransformation = PasswordVisualTransformation(),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                if (isLogin) {
                    Text(
                        text = "Forgot password?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .align(Alignment.End)
                            .padding(top = 8.dp, bottom = 24.dp)
                            .clickable { /* Handle forgot password */ }
                    )
                } else {
                    Spacer(modifier = Modifier.height(24.dp))
                }

                Button(
                    onClick = { handleAuth() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    } else {
                        Text(
                            text = if (isLogin) "Sign In" else "Create Account",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isLogin) "Don't have an account? " else "Already have an account? ",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = if (isLogin) "Sign Up" else "Sign In",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { isLogin = !isLogin }
                    )
                }
            }
        }
    }
}