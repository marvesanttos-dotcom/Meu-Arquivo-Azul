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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Azul, secondary = Azul, background = Color(0xFFF7F9FC))) {
                TelaPrincipal()
            }
        }
    }
}

@Composable
private fun TelaPrincipal() {
    val context = LocalContext.current
    var aba by remember { mutableStateOf("Conversas") }
    var mensagens by remember { mutableStateOf(ArchiveStore.all(context)) }
    var contato by remember { mutableStateOf<String?>(null) }
    val contatos = mensagens.map { it.sender }.distinct()
    val filtradas = if (contato == null) mensagens else mensagens.filter { it.sender == contato }

    Column(Modifier.fillMaxSize().background(Color(0xFFF7F9FC))) {
        Column(Modifier.fillMaxWidth().background(Azul).padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 18.dp)) {
            androidx.compose.material3.Text("Meu Arquivo Azul", color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
            androidx.compose.material3.Text("Seu arquivo pessoal de notificações", color = Color.White.copy(alpha = .9f), fontSize = 13.sp)
        }
        Row(Modifier.fillMaxWidth().background(Color.White), horizontalArrangement = Arrangement.SpaceEvenly) {
            listOf("Conversas", "Fotos", "Áudios", "Status").forEach { nome ->
                Column(
                    Modifier.weight(1f).clickable { aba = nome; contato = null }.padding(vertical = 14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    androidx.compose.material3.Text(nome, color = if (aba == nome) Azul else Color(0xFF667085), fontWeight = if (aba == nome) FontWeight.Bold else FontWeight.Medium, fontSize = 12.sp)
                    Spacer(Modifier.height(7.dp).fillMaxWidth(.55f).background(if (aba == nome) Azul else Color.Transparent, CircleShape))
                }
            }
        }
        if (aba == "Conversas") {
            Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.Text(if (contato == null) "Conversas arquivadas" else contato!!, modifier = Modifier.weight(1f), fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF182230))
                if (contato != null) androidx.compose.material3.Text("Voltar", color = Azul, modifier = Modifier.clickable { contato = null })
            }
            if (contato == null) {
                if (contatos.isEmpty()) {
                    Column(Modifier.fillMaxWidth().padding(30.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        androidx.compose.material3.Text("💬", fontSize = 32.sp)
                        androidx.compose.material3.Text("Nenhuma conversa arquivada", fontWeight = FontWeight.SemiBold, color = Color(0xFF182230), modifier = Modifier.padding(top = 12.dp))
                        androidx.compose.material3.Text("Ative o acesso às notificações para começar a guardar prévias compatíveis.", color = Color(0xFF667085), fontSize = 13.sp, modifier = Modifier.padding(top = 6.dp))
                    }
                } else {
                    LazyColumn {
                        items(contatos) { nome ->
                            val ultima = mensagens.first { it.sender == nome }
                            Row(
                                Modifier.fillMaxWidth().clickable { contato = nome }.padding(horizontal = 16.dp, vertical = 13.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(Modifier.size(48.dp).background(AzulClaro, CircleShape), contentAlignment = Alignment.Center) {
                                    androidx.compose.material3.Text(nome.take(1).uppercase(), color = Azul, fontWeight = FontWeight.Bold, fontSize = 19.sp)
                                }
                                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                    androidx.compose.material3.Text(nome, fontWeight = FontWeight.SemiBold, color = Color(0xFF182230), fontSize = 15.sp)
                                    androidx.compose.material3.Text(ultima.message, color = Color(0xFF667085), fontSize = 13.sp, maxLines = 1)
                                }
                                androidx.compose.material3.Text(mensagens.count { it.sender == nome }.toString(), color = Azul, fontSize = 12.sp)
                            }
                        }
                    }
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)) {
                    items(filtradas) { item ->
                        Column(Modifier.fillMaxWidth().padding(vertical = 6.dp).background(Color.White, RoundedCornerShape(14.dp)).padding(13.dp)) {
                            androidx.compose.material3.Text(item.message, color = Color(0xFF182230), fontSize = 14.sp)
                            androidx.compose.material3.Text(
                                SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("pt", "BR")).format(Date(item.timestamp)),
                                color = Color(0xFF98A2B3), fontSize = 11.sp, modifier = Modifier.padding(top = 7.dp)
                            )
                        }
                    }
                }
            }
        } else {
            Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                androidx.compose.material3.Text(aba, color = Azul, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                androidx.compose.material3.Text("Esta seção será desenvolvida nas próximas etapas.", color = Color(0xFF667085), fontSize = 14.sp)
            }
        }
        Spacer(Modifier.weight(1f))
        Surface(color = Color.White, shadowElevation = 4.dp) {
            Column(Modifier.fillMaxWidth().padding(16.dp)) {
                androidx.compose.material3.Text("Permitir acesso às notificações", color = Color(0xFF182230), fontWeight = FontWeight.SemiBold)
                androidx.compose.material3.Text(
                    "O Android pedirá sua autorização. O app só guarda prévias que chegarem como notificações; não acessa conversas privadas do WhatsApp.",
                    color = Color(0xFF667085), fontSize = 12.sp, modifier = Modifier.padding(top = 4.dp, bottom = 10.dp)
                )
                Button(
                    onClick = { context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Azul)
                ) { androidx.compose.material3.Text("Configurar acesso") }
                Button(
                    onClick = { mensagens = ArchiveStore.all(context) },
                    modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AzulClaro, contentColor = Azul)
                ) { androidx.compose.material3.Text("Atualizar arquivo") }
            }
        }
    }
}
