# Compilar, instalar e assinar

## Compilar
Requisitos: JDK 17, Android SDK (plataforma 36), Gradle 8.13+ (ou abra a pasta no Android Studio, que cuida de tudo).

    gradle testDebugUnitTest     # testes
    gradle assembleDebug         # APK em app/build/outputs/apk/debug/app-debug.apk
    gradle assembleRelease       # APK de release (assinado se houver keystore.properties)

Pelo GitHub: cada push na branch `main` roda `.github/workflows/build.yml` e publica o `TrackerX-debug.apk` em **Releases**.

O APK de debug é assinado com `keystore/debug.keystore` (fixa no repositório), então novas versões atualizam por cima da anterior.

## Instalar no Galaxy Tab A9+
1. Baixe o `TrackerX-debug.apk` no tablet (ou copie por cabo/Drive).
2. Abra o arquivo; quando o Android pedir, permita "Instalar apps desconhecidos" para o navegador/gerenciador de arquivos.
3. Se o Play Protect avisar, escolha "Instalar mesmo assim".
4. Na primeira abertura, aceite Localização e Dispositivos por perto. Em Mídia, toque em "Liberar controle de mídia".

## Definir como launcher
Configurações do app > Geral > "Usar como tela inicial" (ou Android: Configurações > Apps > Escolher apps padrão > App de início) e escolha Tracker X UI. Para desfazer, escolha "One UI Início" no mesmo lugar. O app nunca bloqueia o acesso às configurações do Android.

## Release assinado
1. Gere sua chave (guarde o arquivo e as senhas; sem eles não dá para atualizar o app):

       keytool -genkeypair -v -keystore keystore/release.jks -alias trackerx -keyalg RSA -keysize 2048 -validity 10000

2. Crie `keystore.properties` na raiz (não vai para o git):

       storeFile=keystore/release.jks
       storePassword=SUA_SENHA
       keyAlias=trackerx
       keyPassword=SUA_SENHA

3. `gradle assembleRelease` gera `app/build/outputs/apk/release/app-release.apk`.

Debug e release têm assinaturas diferentes: para trocar de um para o outro é preciso desinstalar antes.

## OBD2
Pareie o adaptador ELM327 (Bluetooth clássico) no Android, depois em Configurações > Carro desligue "Usar dados simulados" e escolha o adaptador. Parâmetros que o carro não responde aparecem como N/D.
