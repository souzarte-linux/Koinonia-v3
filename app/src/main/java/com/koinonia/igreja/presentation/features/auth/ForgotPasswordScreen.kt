package com.koinonia.igreja.presentation.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel,
    onResetSent: () -> Unit,
    onBackToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    var email by remember { mutableStateOf("") }
    val authState by viewModel.authState.collectAsState()
    var showConfirmation by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Recuperar Acesso",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Insira seu e-mail cadastrado para receber instruções de redefinição.",
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.padding(horizontal = 8.dp),
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            if (showConfirmation) {
                Text(
                    text = "E-mail de recuperação enviado com sucesso para $email!",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 24.dp)
                )
                Button(
                    onClick = onBackToLogin,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Voltar para o Login")
                }
            } else {
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text("E-mail") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (authState is AuthState.Error) {
                    Text(
                        text = (authState as AuthState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                if (authState is AuthState.Loading) {
                    CircularProgressIndicator()
                } else {
                    Button(
                        onClick = {
                            viewModel.sendPasswordReset(email) { success ->
                                if (success) {
                                    showConfirmation = true
                                    onResetSent()
                                }
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        Text("Enviar Link de Redefinição", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = onBackToLogin) {
                    Text("Voltar ao Login")
                }
            }
        }
    }
}
