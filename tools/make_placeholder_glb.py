#!/usr/bin/env python3
"""Gera o SUV placeholder (GLB) usado na tela Carro.

NÃO é um modelo oficial nem dimensionalmente exato do Chevrolet Tracker:
é um SUV genérico estilizado, só para a arquitetura 3D funcionar.
Uso: python3 tools/make_placeholder_glb.py app/src/main/assets/models/car.glb
"""
import json, math, struct, sys

meshes = []  # (name, positions, normals, indices, material_index)
materials = [
    {"name": "body", "pbrMetallicRoughness": {"baseColorFactor": [0.10, 0.62, 0.60, 1], "metallicFactor": 0.3, "roughnessFactor": 0.35}},
    {"name": "glass", "pbrMetallicRoughness": {"baseColorFactor": [0.05, 0.07, 0.10, 1], "metallicFactor": 0.4, "roughnessFactor": 0.15}},
    {"name": "tire", "pbrMetallicRoughness": {"baseColorFactor": [0.03, 0.03, 0.03, 1], "metallicFactor": 0.0, "roughnessFactor": 0.9}},
    {"name": "rim", "pbrMetallicRoughness": {"baseColorFactor": [0.75, 0.77, 0.80, 1], "metallicFactor": 0.5, "roughnessFactor": 0.3}},
    {"name": "light", "pbrMetallicRoughness": {"baseColorFactor": [1, 1, 0.9, 1], "metallicFactor": 0.0, "roughnessFactor": 0.4}, "emissiveFactor": [1, 1, 0.85]},
    {"name": "tail", "pbrMetallicRoughness": {"baseColorFactor": [0.8, 0.05, 0.05, 1], "metallicFactor": 0.0, "roughnessFactor": 0.4}, "emissiveFactor": [0.8, 0.05, 0.05]},
    {"name": "trim", "pbrMetallicRoughness": {"baseColorFactor": [0.06, 0.06, 0.07, 1], "metallicFactor": 0.2, "roughnessFactor": 0.7}},
]

def norm(v):
    l = math.sqrt(sum(c * c for c in v)) or 1.0
    return [c / l for c in v]

def sub(a, b): return [a[i] - b[i] for i in range(3)]
def cross(a, b): return [a[1]*b[2]-a[2]*b[1], a[2]*b[0]-a[0]*b[2], a[0]*b[1]-a[1]*b[0]]

def add_quads(name, quads, mat):
    """quads: lista de 4 pontos em ordem anti-horária vista de fora (normais planas)."""
    pos, nor, idx = [], [], []
    for q in quads:
        n = norm(cross(sub(q[1], q[0]), sub(q[2], q[0])))
        b = len(pos)
        pos += q; nor += [n] * 4
        idx += [b, b + 1, b + 2, b, b + 2, b + 3]
    meshes.append((name, pos, nor, idx, mat))

def prism(name, profile, z0, z1, mat):
    """Extruda um perfil lateral (lista de (x, y), anti-horário visto de +Z) entre z0 e z1."""
    quads = []
    n = len(profile)
    for i in range(n):
        a, b = profile[i], profile[(i + 1) % n]
        quads.append([[a[0], a[1], z1], [a[0], a[1], z0], [b[0], b[1], z0], [b[0], b[1], z1]])
    pos, nor, idx = [], [], []
    # tampas (leque a partir do centroide)
    cx = sum(p[0] for p in profile) / n; cy = sum(p[1] for p in profile) / n
    for z, nz in ((z1, 1.0), (z0, -1.0)):
        b = len(pos)
        pos.append([cx, cy, z]); nor.append([0, 0, nz])
        for p in profile:
            pos.append([p[0], p[1], z]); nor.append([0, 0, nz])
        for i in range(n):
            i1, i2 = b + 1 + i, b + 1 + (i + 1) % n
            idx += [b, i1, i2] if nz > 0 else [b, i2, i1]
    for q in quads:
        nn = norm(cross(sub(q[1], q[0]), sub(q[2], q[0])))
        b = len(pos)
        pos += q; nor += [nn] * 4
        idx += [b, b + 1, b + 2, b, b + 2, b + 3]
    meshes.append((name, pos, nor, idx, mat))

def box(name, c, s, mat):
    x0, x1 = c[0] - s[0] / 2, c[0] + s[0] / 2
    y0, y1 = c[1] - s[1] / 2, c[1] + s[1] / 2
    prism(name, [(x0, y0), (x1, y0), (x1, y1), (x0, y1)], c[2] - s[2] / 2, c[2] + s[2] / 2, mat)

def wheel(name, cx, cy, cz, r, w, mat, seg=28):
    prof = [(cx + r * math.cos(2 * math.pi * i / seg), cy + r * math.sin(2 * math.pi * i / seg)) for i in range(seg)]
    prism(name, prof, cz - w / 2, cz + w / 2, mat)

# Dimensões aproximadas de um SUV compacto (metros): 4.27 x 1.79 x 1.62
L, W = 4.27, 1.79
hw = W / 2
# carroceria inferior (perfil lateral, x = frente positiva)
body = [(-2.10, 0.30), (2.05, 0.30), (2.13, 0.55), (2.05, 0.88), (1.20, 1.00), (-2.00, 1.02), (-2.13, 0.80)]
prism("body_lower", body, -hw, hw, 0)
# cabine
cabin = [(-1.95, 1.02), (1.15, 1.00), (0.45, 1.56), (-1.45, 1.62), (-1.90, 1.45)]
prism("cabin", cabin, -hw + 0.10, hw - 0.10, 0)
# vidros (levemente para fora da cabine)
glass_side = [(-1.75, 1.08), (0.95, 1.07), (0.42, 1.49), (-1.40, 1.54), (-1.72, 1.42)]
prism("glass_sides", glass_side, -hw + 0.085, hw - 0.085, 1)
prism("windshield", [(0.50, 1.50), (1.10, 1.035), (1.17, 1.035), (0.56, 1.52)], -hw + 0.18, hw - 0.18, 1)
prism("rear_glass", [(-1.93, 1.43), (-1.97, 1.10), (-1.90, 1.10), (-1.87, 1.44)], -hw + 0.18, hw - 0.18, 1)
# para-choques e saias
box("bumper_front", (2.09, 0.42, 0), (0.12, 0.26, W * 0.96), 6)
box("bumper_rear", (-2.10, 0.45, 0), (0.12, 0.28, W * 0.96), 6)
box("roof_rails_l", (-0.55, 1.635, hw - 0.22), (1.6, 0.04, 0.05), 6)
box("roof_rails_r", (-0.55, 1.635, -hw + 0.22), (1.6, 0.04, 0.05), 6)
# faróis e lanternas
for z in (hw - 0.30, -hw + 0.30):
    box("headlight", (2.085, 0.80, z), (0.06, 0.10, 0.40), 4)
    box("taillight", (-2.105, 0.92, z), (0.06, 0.12, 0.36), 5)
# rodas
for x in (1.32, -1.32):
    for z in (hw - 0.09, -hw + 0.09):
        wheel("tire", x, 0.34, z, 0.34, 0.22, 2)
        wheel("rim", x, 0.34, z + (0.02 if z > 0 else -0.02), 0.20, 0.21, 3)

# ---- escreve o GLB ----
buf = bytearray()
views, accessors, gl_meshes, nodes = [], [], [], []

def push(data, target):
    while len(buf) % 4: buf.append(0)
    off = len(buf); buf.extend(data)
    views.append({"buffer": 0, "byteOffset": off, "byteLength": len(data), "target": target})
    return len(views) - 1

for name, pos, nor, idx, mat in meshes:
    pv = push(b"".join(struct.pack("<3f", *p) for p in pos), 34962)
    nv = push(b"".join(struct.pack("<3f", *n) for n in nor), 34962)
    iv = push(b"".join(struct.pack("<H", i) for i in idx), 34963)
    mn = [min(p[i] for p in pos) for i in range(3)]; mx = [max(p[i] for p in pos) for i in range(3)]
    a = len(accessors)
    accessors += [
        {"bufferView": pv, "componentType": 5126, "count": len(pos), "type": "VEC3", "min": mn, "max": mx},
        {"bufferView": nv, "componentType": 5126, "count": len(nor), "type": "VEC3"},
        {"bufferView": iv, "componentType": 5123, "count": len(idx), "type": "SCALAR"},
    ]
    gl_meshes.append({"name": name, "primitives": [{"attributes": {"POSITION": a, "NORMAL": a + 1}, "indices": a + 2, "material": mat}]})
    nodes.append({"name": name, "mesh": len(gl_meshes) - 1})

root = {"name": "placeholder_suv", "children": list(range(len(nodes)))}
nodes.append(root)
while len(buf) % 4: buf.append(0)
gltf = {
    "asset": {"version": "2.0", "generator": "TrackerX placeholder (SUV genérico, não oficial)"},
    "scene": 0, "scenes": [{"nodes": [len(nodes) - 1]}],
    "nodes": nodes, "meshes": gl_meshes, "materials": materials,
    "accessors": accessors, "bufferViews": views, "buffers": [{"byteLength": len(buf)}],
}
js = json.dumps(gltf, separators=(",", ":")).encode()
js += b" " * ((4 - len(js) % 4) % 4)
out = sys.argv[1] if len(sys.argv) > 1 else "car.glb"
with open(out, "wb") as f:
    f.write(struct.pack("<4sII", b"glTF", 2, 12 + 8 + len(js) + 8 + len(buf)))
    f.write(struct.pack("<I4s", len(js), b"JSON")); f.write(js)
    f.write(struct.pack("<I4s", len(buf), b"BIN\0")); f.write(buf)
print("ok", out, len(buf), "bytes,", len(meshes), "malhas")
