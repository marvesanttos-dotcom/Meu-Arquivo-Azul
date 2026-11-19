# Meu Arquivo Azul

Aplicativo Android complementar ao WhatsApp, com interface azul e arquivo local de prévias de notificações.

## Compilar
O GitHub Actions executa `gradle assembleDebug` a cada envio para a branch `main` e publica o APK de teste como artefato da execução.

## Como testar
1. Instale o APK de debug.
2. Abra **Meu Arquivo Azul**.
3. Toque em **Configurar acesso**.
4. Nas configurações do Android, autorize o acesso às notificações para Meu Arquivo Azul.
5. Quando o WhatsApp apresentar notificações com nome e prévia de mensagem, o app poderá armazenar essas prévias localmente.

## Limitações importantes
- Este app é complementar: não substitui o WhatsApp oficial.
- O Android exige autorização explícita para acesso às notificações.
- Só podem ser arquivadas as informações que o WhatsApp efetivamente expõe nas notificações. Pré-visualizações desativadas, notificações ausentes, conteúdo protegido e mídia não são recuperados.
- Não acessa o banco de dados privado do WhatsApp e não garante recuperação de mensagens apagadas antes da captura, mensagens de visualização única, fotos de perfil ou status.
- As abas Fotos, Áudios e Status são etapas futuras; nesta primeira versão ainda não fazem captura de mídia.
- O arquivo é local no aparelho e pode ser perdido se os dados do app forem apagados ou o app for desinstalado.

## Tecnologia
Kotlin, Jetpack Compose, Android NotificationListenerService e armazenamento local.
