# Trocar o modelo 3D

O carro é o arquivo `app/src/main/assets/models/car.glb` (glTF 2.0 binário). Para usar um modelo mais detalhado, substitua esse arquivo mantendo o nome e recompile. Nenhum código muda.

Recomendações para o Tab A9+:
- até ~150 mil triângulos, texturas de até 2048 px, materiais PBR padrão do glTF;
- frente do carro apontando para +X, teto para +Y (é o que os botões Frente/Traseira/Lateral/Superior esperam; se o seu modelo usar outro eixo, ajuste `yaw` em `ViewPreset`, em `scene3d/CarViewer.kt`);
- qualquer escala serve: o visualizador enquadra o modelo automaticamente.

O placeholder atual é gerado por `tools/make_placeholder_glb.py` e é um SUV genérico, não uma representação oficial ou em escala do Tracker. Ao usar um modelo de terceiros, confira a licença.
