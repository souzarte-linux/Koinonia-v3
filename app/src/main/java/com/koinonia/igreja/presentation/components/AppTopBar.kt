package com.koinonia.igreja.presentation.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.koinonia.igreja.data.local.entity.MemberEntity

fun formatGreetingName(fullName: String?): String {
    if (fullName.isNullOrBlank()) return "Usuário"
    val parts = fullName.trim().split("\\s+".toRegex()).filter { it.isNotBlank() }
    if (parts.isEmpty()) return "Usuário"
    if (parts.size == 1) return parts.first()

    val firstName = parts.first()
    val prepositions = setOf("de", "da", "do", "dos", "das", "e", "del")
    val filteredSurnames = parts.drop(1).filter { it.lowercase() !in prepositions }
    val lastSurname = if (filteredSurnames.isNotEmpty()) filteredSurnames.last() else parts.last()

    return "$firstName $lastSurname"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppTopBar(
    title: String? = null,
    currentMember: MemberEntity?,
    onMenuClick: (() -> Unit)? = null,
    onBackClick: (() -> Unit)? = null,
    onProfileClick: (() -> Unit)? = null,
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors()
) {
    TopAppBar(
        colors = colors,
        navigationIcon = {
            if (onMenuClick != null) {
                IconButton(onClick = onMenuClick) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu Principal"
                    )
                }
            } else if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar"
                    )
                }
            }
        },
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .then(
                        if (onProfileClick != null) {
                            Modifier.clickable { onProfileClick() }
                        } else Modifier
                    )
                    .padding(vertical = 4.dp)
            ) {
                // Foto redonda pequena do usuário (36.dp)
                Surface(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    if (!currentMember?.photoUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = currentMember.photoUrl,
                            contentDescription = "Foto de perfil",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.clip(CircleShape)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.padding(6.dp),
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    val formattedName = formatGreetingName(currentMember?.fullName)
                    Text(
                        text = "Olá, $formattedName",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (title != null) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    )
}
