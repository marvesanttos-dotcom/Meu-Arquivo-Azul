package com.meuarquivoazul.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.DonutLarge
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TextButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val Verde = Color(0xFF20A66A)
private val VerdeClaro = Color(0xFFDDF8D8)
private val Texto = Color(0xFF111418)
private val Cinza = Color(0xFF667078)
private val FundoBusca = Color(0xFFF4F3F1)
private val Linha = Color(0xFFE9E9E9)

private data class ConversaDemo(
    val nome: String,
    val mensagem: String,
    val horario: String,
    val avatar: String,
    val cor: Color,
    val naoLidas: Int = 0,
    val silenciada: Boolean = false,
    val enviada: Boolean = false
)

private val conversasDemo = listOf(
    ConversaDemo("Moisés", "😂😂", "11:28", "M", Color(0xFFF1D9CC), enviada = true),
    ConversaDemo("feira do rolo 🤝", "~ Alexandre adicionou ~ Lucas.", "11:36", "Feira\ndo Rolo", Color(0xFF202B77), silenciada = true),
    ConversaDemo("+55 74 9192-09... (Você)", "Ma", "9:49", "Você", Color(0xFFB8D4B0), enviada = true),
    ConversaDemo("Colheita 2026 🌄🚜🌱", "~ Gabriel Castoldi: Kkkkkkk", "Ontem", "🌄", Color(0xFFB7D0DC), 2, true),
    ConversaDemo("𝓗𝓪𝓻𝓾𝓷𝓪❤️❤️❤️", "▧  Foto", "Ontem", "H", Color(0xFFD9B6A9), 1),
    ConversaDemo("+55 74 9113-3522", "Olá Cliente ! 📝 Passando para lembrar...", "Ontem", "A", Color(0xFF18265D)),
    ConversaDemo("Cleilton Alfacred Especialista...", "Obrigado pelo contato!", "Ontem", "C", Color(0xFFD5D5D5))
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        @Suppress("DEPRECATION")
        window.decorView.systemUiVisibility =
            android.view.View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or
            android.view.View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
        window.statusBarColor = android.graphics.Color.WHITE
        window.navigationBarColor = android.graphics.Color.WHITE
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(
                primary = Verde,
                onPrimary = Color.White,
                background = Color.White,
                surface = Color.White
            )) {
                Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
                    TelaConversas()
                }
            }
        }
    }
}

@Composable
private fun TelaConversas() {
    var aba by remember { mutableStateOf("Conversas") }
    val context = LocalContext.current
    val preferencias = remember { context.getSharedPreferences("aparencia_app", android.content.Context.MODE_PRIVATE) }
    var nomeApp by remember { mutableStateOf(preferencias.getString("nome_app", "WhatsApp") ?: "WhatsApp") }
    var fraseApp by remember { mutableStateOf(preferencias.getString("frase_app", "") ?: "") }
    var mostrarConfiguracoes by remember { mutableStateOf(false) }
    var nomeEditado by remember { mutableStateOf(nomeApp) }
    var fraseEditada by remember { mutableStateOf(fraseApp) }
    val filtros = listOf("Todas")
    val lista = conversasDemo

    Column(Modifier.fillMaxSize().background(Color.White)) {
        if (mostrarConfiguracoes) {
            AlertDialog(
                onDismissRequest = { mostrarConfiguracoes = false },
                title = { Text("Personalizar cabeçalho") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        OutlinedTextField(
                            value = nomeEditado,
                            onValueChange = { nomeEditado = it },
                            label = { Text("Nome do aplicativo") },
                            singleLine = true
                        )
                        OutlinedTextField(
                            value = fraseEditada,
                            onValueChange = { if (it.length <= 320) fraseEditada = it },
                            label = { Text("Frase") },
                            supportingText = { Text("${fraseEditada.length}/320") },
                            minLines = 2,
                            maxLines = 4
                        )
                        Text("Toque e segure o nome no topo para abrir estas configurações.",
                            color = Cinza, fontSize = 12.sp)
                    }
                },
                confirmButton = {
                    Button(onClick = {
                        nomeApp = nomeEditado.ifBlank { "WhatsApp" }
                        fraseApp = fraseEditada
                        preferencias.edit().putString("nome_app", nomeApp)
                            .putString("frase_app", fraseApp).apply()
                        mostrarConfiguracoes = false
                    }) { Text("Salvar") }
                },
                dismissButton = {
                    TextButton(onClick = { mostrarConfiguracoes = false }) { Text("Cancelar") }
                }
            )
        }
        Row(
            Modifier.fillMaxWidth().height(68.dp).padding(start = 28.dp, end = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
                Text(nomeApp, modifier = Modifier.combinedClickable(
                    onClick = { },
                    onLongClick = {
                        nomeEditado = nomeApp
                        fraseEditada = fraseApp
                        mostrarConfiguracoes = true
                    }
                ), color = Verde, fontSize = 28.sp, fontWeight = FontWeight.Bold,
                    letterSpacing = (-0.7).sp)
                if (fraseApp.isNotBlank()) {
                    Text(fraseApp, color = Cinza, fontSize = 11.sp,
                        maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
            }
            Icon(Icons.Default.MoreVert, contentDescription = "Mais opções",
                tint = Texto, modifier = Modifier.size(27.dp))
        }

        Row(
            Modifier.fillMaxWidth()
                .padding(start = 31.dp, top = 15.dp, bottom = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            filtros.forEach { item ->
                Box(
                    Modifier.padding(end = 13.dp).clip(CircleShape)
                        .background(VerdeClaro)
                        .border(1.5.dp, Color(0xFFB8DDBB), CircleShape)
                        .padding(horizontal = 19.dp, vertical = 11.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(item, color = Color(0xFF185C3A),
                        fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(top = 3.dp, bottom = 8.dp)
        ) {
            items(lista) { conversa -> LinhaConversa(conversa) }
        }

        Row(
            Modifier.fillMaxWidth().height(82.dp).border(0.5.dp, Linha)
                .background(Color.White).padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            ItemNavegacao("Conversas", Icons.Default.Chat, aba == "Conversas") { aba = "Conversas" }
            ItemNavegacao("Atualizações", Icons.Default.DonutLarge, aba == "Atualizações") { aba = "Atualizações" }
            Spacer(Modifier.width(28.dp))
        }
    }
}

@Composable
private fun LinhaConversa(item: ConversaDemo) {
    Row(
        Modifier.fillMaxWidth().heightIn(min = 116.dp)
            .clickable { }.padding(start = 22.dp, end = 17.dp, top = 10.dp, bottom = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier.size(82.dp).clip(CircleShape).background(item.cor)
                .border(3.dp, Color(0xFFE0E3E4), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(item.avatar, color = if (item.avatar == "Feira\ndo Rolo") Color.White else Color(0xFF72513F),
                fontSize = if (item.avatar.length > 2) 12.sp else 25.sp,
                fontWeight = FontWeight.Bold, lineHeight = 13.sp)
        }
        Column(Modifier.weight(1f).padding(start = 20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(item.nome, modifier = Modifier.weight(1f), color = Texto,
                    fontSize = 18.sp, fontWeight = FontWeight.SemiBold,
                    maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text(item.horario, color = if (item.horario == "Ontem") Verde else Cinza,
                    fontSize = 14.sp)
            }
            Spacer(Modifier.height(5.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (item.enviada) {
                    Text("✓✓", color = Color(0xFF8C969B), fontSize = 16.sp)
                    Spacer(Modifier.width(5.dp))
                }
                Text(item.mensagem, modifier = Modifier.weight(1f), color = Cinza,
                    fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (item.silenciada) {
                    Icon(Icons.Default.NotificationsOff, contentDescription = "Silenciada",
                        tint = Color(0xFF92999D), modifier = Modifier.size(19.dp))
                    Spacer(Modifier.width(5.dp))
                }
                if (item.naoLidas > 0) {
                    Box(Modifier.size(29.dp).clip(CircleShape).background(Verde),
                        contentAlignment = Alignment.Center) {
                        Text(item.naoLidas.toString(), color = Color.White,
                            fontSize = 14.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ItemNavegacao(
    titulo: String,
    icone: ImageVector,
    selecionado: Boolean,
    onClick: () -> Unit
) {
    Column(
        Modifier.widthIn(min = 75.dp).clip(RoundedCornerShape(24.dp))
            .clickable(onClick = onClick).padding(horizontal = 5.dp, vertical = 7.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            Modifier.width(78.dp).height(43.dp)
                .clip(RoundedCornerShape(25.dp))
                .background(if (selecionado) VerdeClaro else Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Icon(icone, contentDescription = titulo,
                tint = Color(0xFF172027), modifier = Modifier.size(27.dp))
        }
        Spacer(Modifier.height(3.dp))
        Text(titulo, color = Texto, fontSize = 12.sp,
            fontWeight = if (selecionado) FontWeight.Bold else FontWeight.Medium,
            maxLines = 1)
    }
}
