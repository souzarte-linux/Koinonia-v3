package com.koinonia.igreja.presentation.features.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.koinonia.igreja.domain.model.AppRole
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.Facebook
import io.github.jan.supabase.auth.providers.Apple

import androidx.compose.ui.platform.LocalContext
import androidx.fragment.app.FragmentActivity
import androidx.compose.material.icons.filled.Fingerprint
import com.koinonia.igreja.core.biometric.BiometricPromptManager

@Composable
fun LoginScreen(
    onNavigateToHome: (AppRole) -> Unit,
    onForgotPasswordClick: () -> Unit,
    viewModel: AuthViewModel = hiltViewModel(),
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val activity = context as? FragmentActivity
    val biometricPromptManager = remember(context) { BiometricPromptManager(context.applicationContext) }
    val isBiometricEnabled by viewModel.isBiometricEnabled.collectAsState()

    val savedEmail by viewModel.savedEmail.collectAsState()
    val rememberEmail by viewModel.rememberEmail.collectAsState()

    var email by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    var isSignUpMode by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }

    // Sincroniza estados iniciais com as preferências salvas
    LaunchedEffect(savedEmail, rememberEmail) {
        email = savedEmail
        rememberMe = rememberEmail
    }

    // Controladores de Foco
    val passwordFocusRequester = remember { FocusRequester() }

    val authState by viewModel.authState.collectAsState()

    // Reage ao estado de sucesso para navegar
    LaunchedEffect(authState) {
        if (authState is AuthState.Success) {
            onNavigateToHome((authState as AuthState.Success).role)
        }
    }

    fun triggerBiometricAuth() {
        if (activity != null && biometricPromptManager.isBiometricAvailable()) {
            biometricPromptManager.showBiometricPrompt(
                activity = activity,
                onSuccess = {
                    viewModel.loginWithBiometrics()
                },
                onError = { /* Exibe erro apenas se necessário */ }
            )
        }
    }

    // Dispara validação por digital ao abrir se estiver ativada e houver usuário salvo
    LaunchedEffect(isBiometricEnabled, savedEmail) {
        if (isBiometricEnabled && savedEmail.isNotBlank() && !isSignUpMode) {
            triggerBiometricAuth()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isSignUpMode) "Criar Nova Conta" else "Ministério do Diácono",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(32.dp))

            // Campo E-mail com teclado de e-mail e botão 'Próximo' no teclado
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail ou celular") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { passwordFocusRequester.requestFocus() }
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Campo Senha com botão 'Entrar' (Done) no teclado
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Senha") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = {
                        if (email.isNotBlank() && password.isNotBlank() && authState !is AuthState.Loading) {
                            viewModel.saveRememberedCredentials(email, password, rememberMe)
                            if (isSignUpMode) {
                                viewModel.signUp(email, password)
                            } else {
                                viewModel.login(email, password)
                            }
                        }
                    }
                ),
                trailingIcon = {
                    val image = if (passwordVisible) {
                        Icons.Default.Visibility
                    } else {
                        Icons.Default.VisibilityOff
                    }
                    val description = if (passwordVisible) "Esconder senha" else "Mostrar senha"

                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = description)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(passwordFocusRequester),
                shape = RoundedCornerShape(12.dp)
            )
            
            Spacer(modifier = Modifier.height(8.dp))

            // Opção "Gravar usuário" e "Esqueceu a senha?" na mesma linha
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = rememberMe,
                        onCheckedChange = { rememberMe = it }
                    )
                    Text(
                        text = "Gravar usuário",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                if (!isSignUpMode) {
                    TextButton(onClick = onForgotPasswordClick) {
                        Text("Esqueceu a senha?")
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (authState is AuthState.Error) {
                Text(
                    text = (authState as AuthState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(bottom = 16.dp)
                )
            }

            Button(
                onClick = {
                    viewModel.saveRememberedCredentials(email, password, rememberMe)
                    if (isSignUpMode) {
                        viewModel.signUp(email, password)
                    } else {
                        viewModel.login(email, password)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .clip(RoundedCornerShape(12.dp)),
                enabled = authState !is AuthState.Loading && email.isNotBlank() && password.isNotBlank()
            ) {
                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text(
                        text = if (isSignUpMode) "Cadastrar" else "Entrar",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            if (!isSignUpMode && biometricPromptManager.isBiometricAvailable()) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedButton(
                    onClick = { triggerBiometricAuth() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "Impressão Digital",
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Entrar com Impressão Digital",
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Alternador entre Login e Cadastro
            TextButton(
                onClick = { isSignUpMode = !isSignUpMode }
            ) {
                Text(
                    text = if (isSignUpMode) "Já possui uma conta? Faça Login" else "Não tem cadastro? Crie uma conta"
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Seção de Login/Cadastro Social
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = if (isSignUpMode) "Ou crie com" else "Ou acesse com",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Botão Google
                OutlinedButton(
                    onClick = { viewModel.loginWithProvider(Google) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "Google",
                        color = Color(0xFF4285F4),
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // Botão Facebook
                Button(
                    onClick = { viewModel.loginWithProvider(Facebook) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1877F2)),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "Facebook",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                // Botão Apple
                Button(
                    onClick = { viewModel.loginWithProvider(Apple) },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Text(
                        text = "Apple",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }
        }
    }

    // Popup Dialog de E-mail de Confirmação Pendente
    if (authState is AuthState.VerificationSent) {
        val verifiedEmail = (authState as AuthState.VerificationSent).email
        AlertDialog(
            onDismissRequest = { /* Não permite fechar clicando fora */ },
            title = { Text("Ativação de Conta") },
            text = {
                Text(
                    text = "Cadastro realizado! Enviamos um e-mail de ativação para $verifiedEmail. Por favor, confirme na sua caixa de entrada antes de fazer login.",
                    style = MaterialTheme.typography.bodyMedium
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        // 1. Preenche e-mail e limpa senha
                        email = verifiedEmail
                        password = ""
                        // 2. Transiciona para a tela de login
                        isSignUpMode = false
                        // 3. Reseta o estado na ViewModel
                        viewModel.resetAuthState()
                        // 4. Solicita foco no input de senha
                        passwordFocusRequester.requestFocus()
                    }
                ) {
                    Text("OK")
                }
            }
        )
    }
}
