# Tracker X UI

Central multimídia automotiva para Samsung Galaxy Tab A9+ (Android 16), pensada para um Chevrolet Tracker Premier 2023.
App Android nativo (Kotlin + Jetpack Compose). Projeto independente, sem vínculo com a Chevrolet/GM.

**Estado: MVP 0.1.0.** O código foi escrito mas ainda precisa passar pela primeira compilação e pelo primeiro teste no tablet.

## O que o MVP tem
- **Início:** relógio, data, temperatura externa (Open-Meteo), velocidade por GPS, status de GPS/Bluetooth/carro, música atual com capa e controles, atalhos.
- **Mídia:** controla Spotify, YouTube Music e qualquer player com MediaSession (play/pause, anterior, próxima, volume).
- **Bluetooth:** dispositivo conectado, perfil A2DP e se o áudio está saindo por Bluetooth. O áudio usa o Bluetooth do próprio Android.
- **Carro:** modelo 3D (Filament, arquivo GLB) com rotação, zoom e ângulos prontos; painel de dados com selo REAL / SIMULADO / ESTIMADO / N/D.
- **Mapa:** abre Google Maps ou Waze em tela cheia ou ao lado (tela dividida do Android).
- **Dividir:** dois módulos lado a lado dentro do app (Mapa, Música, Carro 3D, Dados).
- **Apps, Voz, Configurações, tema claro/escuro/automático, orientação horizontal e vertical, modo launcher.**

## Limites conhecidos (por regras do Android)
- Maps/Waze não podem ser embutidos dentro de outro app; a divisão usa a tela dividida do sistema.
- Iniciar sozinho ao ligar só é garantido definindo o app como tela inicial.
- No Android 16, tablets podem ignorar orientação fixa pedida pelo app.
- O modelo 3D é um SUV genérico ilustrativo, não um Tracker oficial nem em escala exata.
- OBD2 (ELM327 Bluetooth clássico) está implementado mas nunca foi testado em um carro.
- Câmeras: só a estrutura (interfaces); nenhuma fonte de vídeo.

## Estrutura (`app/src/main/java/com/trackerx/ui/`)
`ui` telas · `vehicle` dados do veículo · `obd` ELM327 · `bluetooth` · `media` · `navigation` · `voice` · `scene3d` 3D · `settings` · `launcher` · `camera` · `location` · `weather`

## Permissões
| Permissão | Para quê |
|---|---|
| ACCESS_FINE/COARSE_LOCATION | velocidade e posição pelo GPS; posição para a temperatura |
| INTERNET | consultar a temperatura externa (api.open-meteo.com) |
| BLUETOOTH_CONNECT | ler estado/dispositivos pareados e conectar ao adaptador OBD2 |
| RECEIVE_BOOT_COMPLETED | tentar abrir o app ao ligar o tablet |
| Acesso a notificações (concedido nas configurações) | exigência do Android para ler/controlar a mídia de outros apps; o conteúdo das notificações não é lido |

Mais: [docs/BUILD.md](docs/BUILD.md) (compilar, instalar, launcher, release assinado) · [docs/MODELO_3D.md](docs/MODELO_3D.md) (trocar o modelo 3D)
