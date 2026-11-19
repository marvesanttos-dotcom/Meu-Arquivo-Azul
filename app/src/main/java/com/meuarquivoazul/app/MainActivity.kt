package com.meuarquivoazul.app

import android.content.Intent
import android.os.Bundle
import android.os.Build
import android.content.pm.PackageManager
import android.provider.Settings
import android.net.Uri
import android.widget.ImageView
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.viewinterop.AndroidView
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
    private var resumeVersion by mutableIntStateOf(0)

    override fun onResume() {
        super.onResume()
        resumeVersion++
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Azul, secondary = Azul, background = Fundo)) {
                TelaPrincipal(resumeVersion)
            }
        }
    }
}

@Composable
private fun TelaPrincipal(resumeVersion: Int) {
    val context = LocalContext.current
    var mensagens by remember { mutableStateOf(ArchiveStore.all(context)) }
    var midias by remember { mutableStateOf(MediaArchive.all(context)) }
    val statusSalvos = midias.filter { it.file.name.startsWith("status_") }.sortedByDescending { it.file.lastModified() }
    var avisoMidias by remember { mutableStateOf("Importe as mídias comuns do WhatsApp que o Android disponibilizar.") }
    val permissaoMidia = remember {
        if (Build.VERSION.SDK_INT >= 33) arrayOf("android.permission.READ_MEDIA_IMAGES", "android.permission.READ_MEDIA_VIDEO")
        else arrayOf("android.permission.READ_EXTERNAL_STORAGE")
    }
    val pedirPermissao = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { grants ->
        if (grants.values.any { it }) {
            midias = MediaArchive.importAvailable(context)
            avisoMidias = "Arquivos acessíveis copiados para o armazenamento privado do aplicativo."
        } else avisoMidias = "Permissão não concedida. Não foi possível importar as mídias."
    }
    val escolherPastaStatus = androidx.activity.compose.rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri, Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
                )
            } catch (_: Exception) { }
            context.getSharedPreferences("arquivo_azul", android.content.Context.MODE_PRIVATE)
                .edit().putString("status_tree_uri", uri.toString()).apply()
            midias = MediaArchive.importStatusTree(context, uri)
            avisoMidias = "Pasta selecionada. Foram copiadas as fotos e os vídeos acessíveis."
        }
    }
    var statusSelecionado by remember { mutableStateOf<ArchivedMedia?>(null) }
    var contato by remember { mutableStateOf<String?>(null) }
    var aba by remember { mutableStateOf("Conversas") }
    var mostrarConfiguracoes by remember { mutableStateOf(false) }
    val contatos = mensagens.map { it.sender }.distinct()
    val filtradas = mensagens.filter { it.sender == contato }
    LaunchedEffect(resumeVersion) {
        val autorizado = permissaoMidia.all {
            context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
        }
        if (autorizado) midias = MediaArchive.importAvailable(context)
        val statusUri = context.getSharedPreferences("arquivo_azul", android.content.Context.MODE_PRIVATE)
            .getString("status_tree_uri", null)
        if (statusUri != null) {
            try { midias = MediaArchive.importStatusTree(context, android.net.Uri.parse(statusUri)) }
            catch (_: Exception) { }
        }
    }

    Column(Modifier.fillMaxSize().background(Fundo)) {
        Row(
            Modifier.fillMaxWidth().background(Azul).padding(start = 16.dp, end = 6.dp, top = 8.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (mostrarConfiguracoes || contato != null || statusSelecionado != null) {
                IconButton(onClick = {
                    when { mostrarConfiguracoes -> mostrarConfiguracoes = false; statusSelecionado != null -> statusSelecionado = null; else -> contato = null }
                }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Voltar", tint = Color.White)
                }
            }
            androidx.compose.material3.Text(
                when {
                    mostrarConfiguracoes -> "Configurações"
                    statusSelecionado != null -> "Status"
                    contato != null -> contato!!
                    else -> "Meu Arquivo Azul"
                },
                modifier = Modifier.weight(1f),
                color = Color.White,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )
            if (!mostrarConfiguracoes && contato == null && statusSelecionado == null) {
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
        } else if (statusSelecionado != null) {
            val item = statusSelecionado!!
            val indice = statusSalvos.indexOfFirst { it.file.absolutePath == item.file.absolutePath }
            Box(Modifier.fillMaxSize().background(Color.Black)) {
                if (item.isVideo) {
                    AndroidView(
                        factory = { ctx -> VideoView(ctx).apply {
                            setVideoURI(Uri.fromFile(item.file))
                            setOnPreparedListener { it.isLooping = true; start() }
                        } },
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    AndroidView(
                        factory = { ctx -> ImageView(ctx).apply {
                            scaleType = ImageView.ScaleType.FIT_CENTER
                            setImageURI(Uri.fromFile(item.file))
                        } },
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Row(Modifier.align(Alignment.TopCenter).fillMaxWidth().padding(horizontal = 12.dp, vertical = 18.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    statusSalvos.forEachIndexed { i, _ ->
                        Box(Modifier.weight(1f).height(3.dp).background(
                            if (i <= indice) Color.White else Color.White.copy(alpha = 0.35f),
                            RoundedCornerShape(4.dp)))
                    }
                }
                IconButton(onClick = { avisoMidias = "Mídia já está salva no Meu Arquivo Azul." },
                    modifier = Modifier.align(Alignment.TopEnd).padding(top = 28.dp, end = 12.dp)) {
                    Icon(Icons.Default.Download, contentDescription = "Baixar status", tint = Color.White)
                }
                if (indice > 0) {
                    IconButton(onClick = { statusSelecionado = statusSalvos[indice - 1] },
                        modifier = Modifier.align(Alignment.CenterStart).size(56.dp)) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Status anterior",
                            tint = Color.White, modifier = Modifier.size(42.dp))
                    }
                }
                if (indice >= 0 && indice < statusSalvos.lastIndex) {
                    IconButton(onClick = { statusSelecionado = statusSalvos[indice + 1] },
                        modifier = Modifier.align(Alignment.CenterEnd).size(56.dp)) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Próximo status",
                            tint = Color.White, modifier = Modifier.size(42.dp))
                    }
                }
                androidx.compose.material3.Text(
                    "${indice + 1} de ${statusSalvos.size} • toque nas laterais para passar",
                    color = Color.White, fontSize = 11.sp,
                    modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp)
                )
            }
        } else if (contato != null) {
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
        } else {
            // Abas separadas no estilo da navegação do WhatsApp.
            Row(Modifier.fillMaxWidth().background(Azul)) {
                listOf("Conversas", "Status", "Mídias").forEach { nome ->
                    Column(
                        Modifier.weight(1f).clickable { aba = nome }.padding(top = 12.dp, bottom = 0.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        androidx.compose.material3.Text(
                            nome.uppercase(),
                            color = if (aba == nome) Color.White else Color.White.copy(alpha = 0.72f),
                            fontSize = 13.sp,
                            fontWeight = if (aba == nome) FontWeight.Bold else FontWeight.Medium
                        )
                        Spacer(
                            Modifier.padding(top = 10.dp).fillMaxWidth(0.72f).height(3.dp)
                                .background(if (aba == nome) Color.White else Color.Transparent, RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
                        )
                    }
                }
            }

            if (aba == "Conversas") {
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
                            "As prévias recebidas serão organizadas por contato.",
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
            } else if (aba == "Status") {
                Column(Modifier.fillMaxSize()) {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        androidx.compose.material3.Text(
                            "Atualizações",
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF182230),
                            fontSize = 22.sp,
                            modifier = Modifier.weight(1f)
                        )
                        androidx.compose.material3.TextButton(onClick = {
                            val savedTree = context.getSharedPreferences(
                                "arquivo_azul", android.content.Context.MODE_PRIVATE
                            ).getString("status_tree_uri", null)
                            if (savedTree != null) {
                                midias = MediaArchive.importStatusTree(context, Uri.parse(savedTree))
                                avisoMidias = "Status atualizados."
                            } else {
                                escolherPastaStatus.launch(null)
                            }
                        }) {
                            androidx.compose.material3.Text("Atualizar", color = Azul)
                        }
                    }
                    androidx.compose.material3.Text(
                        "Status",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF182230),
                        modifier = Modifier.padding(start = 16.dp, bottom = 10.dp)
                    )
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        item {
                            Column(
                                Modifier.width(145.dp).height(258.dp)
                                    .background(Color.White, RoundedCornerShape(22.dp))
                                    .clickable { escolherPastaStatus.launch(null) }
                                    .padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Box(
                                    Modifier.size(74.dp).background(AzulClaro, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    androidx.compose.material3.Text("+", color = Azul, fontSize = 42.sp)
                                }
                                androidx.compose.material3.Text(
                                    "Adicionar status",
                                    color = Color(0xFF182230),
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    modifier = Modifier.padding(top = 14.dp)
                                )
                                androidx.compose.material3.Text(
                                    "Selecionar pasta",
                                    color = Color(0xFF667085),
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 4.dp)
                                )
                            }
                        }
                        items(statusSalvos) { status ->
                            Column(
                                Modifier.width(145.dp).height(258.dp)
                                    .background(Color(0xFFE8EDF5), RoundedCornerShape(22.dp))
                                    .clickable { statusSelecionado = status }
                            ) {
                                Box(Modifier.fillMaxSize()) {
                                    AndroidView(
                                        factory = { ctx -> ImageView(ctx).apply {
                                            scaleType = ImageView.ScaleType.CENTER_CROP
                                            if (status.isVideo) {
                                                val retriever = android.media.MediaMetadataRetriever()
                                                try {
                                                    retriever.setDataSource(status.file.absolutePath)
                                                    setImageBitmap(retriever.getFrameAtTime(0))
                                                } catch (_: Exception) {
                                                    setImageResource(android.R.drawable.ic_media_play)
                                                } finally {
                                                    try { retriever.release() } catch (_: Exception) { }
                                                }
                                            } else {
                                                setImageURI(Uri.fromFile(status.file))
                                            }
                                        } },
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    Box(
                                        Modifier.align(Alignment.BottomStart).fillMaxWidth()
                                            .background(Color.Black.copy(alpha = 0.48f))
                                            .padding(horizontal = 10.dp, vertical = 12.dp)
                                    ) {
                                        androidx.compose.material3.Text(
                                            status.name,
                                            color = Color.White,
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 2
                                        )
                                    }
                                    if (status.isVideo) {
                                        Box(
                                            Modifier.align(Alignment.TopEnd).padding(8.dp)
                                                .background(Color.Black.copy(alpha = 0.62f), CircleShape)
                                                .padding(horizontal = 8.dp, vertical = 5.dp)
                                        ) {
                                            androidx.compose.material3.Text(
                                                "▶",
                                                color = Color.White,
                                                fontSize = 11.sp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    if (statusSalvos.isEmpty()) {
                        androidx.compose.material3.Text(
                            "Quando houver arquivos de status disponíveis, eles aparecerão aqui. Toque em Adicionar status para autorizar a pasta .Statuses do WhatsApp.",
                            color = Color(0xFF667085),
                            fontSize = 13.sp,
                            modifier = Modifier.padding(horizontal = 18.dp, vertical = 18.dp)
                        )
                    }
                }
            } else {
                Column(Modifier.fillMaxSize().padding(16.dp)) {
                    androidx.compose.material3.Text("Arquivo de fotos e vídeos", fontWeight = FontWeight.Bold,
                        color = Color(0xFF182230), fontSize = 17.sp)
                    androidx.compose.material3.Text(avisoMidias, color = Color(0xFF667085),
                        fontSize = 12.sp, modifier = Modifier.padding(top = 6.dp, bottom = 12.dp))
                    Button(onClick = {
                        val granted = permissaoMidia.all {
                            context.checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED
                        }
                        if (granted) {
                            midias = MediaArchive.importAvailable(context)
                            avisoMidias = "Importação concluída. Só aparecem arquivos acessíveis ao Android."
                        } else pedirPermissao.launch(permissaoMidia)
                    }, modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Azul)) {
                        androidx.compose.material3.Text("Importar mídias do WhatsApp")
                    }
                    androidx.compose.material3.Text("${midias.size} arquivo(s) guardado(s) neste aparelho",
                        color = Azul, fontSize = 13.sp, modifier = Modifier.padding(top = 14.dp, bottom = 8.dp))
                    LazyColumn {
                        items(midias) { media ->
                            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically) {
                                androidx.compose.material3.Text(if (media.isVideo) "▶" else "▧",
                                    color = Azul, fontSize = 22.sp)
                                Column(Modifier.weight(1f).padding(start = 12.dp)) {
                                    androidx.compose.material3.Text(media.name, color = Color(0xFF182230),
                                        fontSize = 13.sp, maxLines = 2)
                                    androidx.compose.material3.Text(if (media.isVideo) "Vídeo salvo" else "Foto salva",
                                        color = Color(0xFF667085), fontSize = 11.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
