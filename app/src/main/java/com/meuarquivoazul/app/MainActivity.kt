package com.meuarquivoazul.app

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Azul = Color(0xFF2563EB)
private val AzulClaro = Color(0xFFEAF1FF)
private val Fundo = Color(0xFFF7F9FC)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Azul, secondary = Azul, background = Fundo)) {
                TelaPrincipal()
            }
        }
    }
}

@Composable
private fun TelaPrincipal() {
    val context = LocalContext.current
    var mensagens by remember { mutableStateOf(ArchiveStore.all(context)) }
    var contato by remember { mutableStateOf<String?>(null) }
    var mostrarConfiguracoes by remember { mutableStateOf(false) }
    val contatos = mensagens.map { it.sender }.distinct()
    val filtradas = if (contato == null) mensagens else mensagens.filter { it.sender == contato }

    Column(Modifier.fillMaxSize().background(Fundo)) {
        Row(
            Modifier.fillMaxWidth().background(Azul).padding(start = 18.dp, end = 8.dp, top = 10.dp, bottom = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (mostrarConfiguracoes || contato != null) {
                IconButton(onClick = {
                    if (mostrarConfiguracoes) mostrarConfiguracoes = false else contato = null
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                }
            }
            androidx.compose.material3.Text(
                when {
                    mostrarConfiguracoes -> "Configurações"
                    contato != null -> contato!!
                    else -> "Meu Arquivo Azul"
                },
                modifier = Modifier.weight(1f),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            if (!mostrarConfiguracoes && contato == null) {
                IconButton(onClick = { mostrarConfiguracoes = true }) {
                    Icon(Icons.Default.Settings, contentDescription = "Configurações", tint = Color.White)
                }
            }
        }

        if (mostrarConfiguracoes) {
            Column(Modifier.fillMaxWidth().padding(20.dp)) {
                androidx.compose.material3.Text("Privacidade e armazenamento", fontSize = 17.sp, fontWeight = FontWeight.Bold, color = Color(0xFF182230))
                androidx.compose.material3.Text(
                    "Autorize o acesso às notificações para guardar prévias compatíveis das mensagens recebidas.",
                    color = Color(0xFF667085), fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp, bottom = 16.dp)
                )
                Button(
                    onClick = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Azul)
                ) { androidx.compose.material3.Text("Configurar acesso às notificações") }
                Button(
                    onClick = { mensagens = ArchiveStore.all(context) },
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AzulClaro, contentColor = Azul)
                ) { androidx.compose.material3.Text("Atualizar arquivo") }
                androidx.compose.material3.Text(
                    "O aplicativo guarda apenas prévias recebidas como notificações. Ele não acessa conversas privadas nem garante recuperar mensagens ou mídias apagadas.",
                    color = Color(0xFF667085), fontSize = 12.sp, modifier = Modifier.padding(top = 18.dp)
                )
            }
        } else if (contato == null) {
            if (contatos.isEmpty()) {
                Column(
                    Modifier.fillMaxWidth().padding(horizontal = 28.dp, vertical = 72.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    androidx.compose.material3.Text("💬", fontSize = 38.sp)
                    androidx.compose.material3.Text(
                        "Suas conversas aparecerão aqui",
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF182230),
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    androidx.compose.material3.Text(
                        "Use a engrenagem para configurar o arquivamento de notificações.",
                        color = Color(0xFF667085), fontSize = 13.sp,
                        modifier = Modifier.padding(top = 6.dp)
                    )
                }
            } else {
                LazyColumn {
                    items(contatos) { nome ->
                        val ultima = mensagens.first { it.sender == nome }
                        Row(
                            Modifier.fillMaxWidth().clickable { contato = nome }.padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(Modifier.size(50.dp).background(AzulClaro, CircleShape), contentAlignment = Alignment.Center) {
                                androidx.compose.material3.Text(nome.take(1).uppercase(), color = Azul, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                            }
                            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                androidx.compose.material3.Text(nome, fontWeight = FontWeight.SemiBold, color = Color(0xFF182230), fontSize = 15.sp)
                                androidx.compose.material3.Text(ultima.message, color = Color(0xFF667085), fontSize = 13.sp, maxLines = 1)
                            }
                            androidx.compose.material3.Text(
                                mensagens.count { it.sender == nome }.toString(),
                                color = Azul, fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        } else {
            LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                items(filtradas) { item ->
                    Column(
                        Modifier.fillMaxWidth().padding(vertical = 6.dp)
                            .background(Color.White, RoundedCornerShape(14.dp)).padding(13.dp)
                    ) {
                        androidx.compose.material3.Text(item.message, color = Color(0xFF182230), fontSize = 14.sp)
                        androidx.compose.material3.Text(
                            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")).format(Date(item.timestamp)),
                            color = Color(0xFF98A2B3), fontSize = 11.sp,
                            modifier = Modifier.padding(top = 7.dp)
                        )
                    }
                }
            }
        }
    }
}
