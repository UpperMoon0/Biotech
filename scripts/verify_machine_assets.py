#!/usr/bin/env python3
"""Asset/registration/schema contract checks with only the Python standard library."""
from pathlib import Path
import json, struct, subprocess, sys, hashlib, re
ROOT=Path(__file__).resolve().parents[1]
parts=['frame','panel','vent','vessel','drive','grow_light','framed_glass','pipe']
assets=ROOT/'common/src/main/resources/assets/biotech'
lang=json.loads((assets/'lang/en_us.json').read_text())
for part in parts:
 id='biotech_'+part
 assert 'block.biotech.'+id in lang,id
 model=json.loads((assets/f'models/block/{id}.json').read_text())
 assert json.loads((assets/f'models/item/{id}.json').read_text())['parent']=='biotech:block/'+id
 assert json.loads((assets/f'blockstates/{id}.json').read_text())['variants']['']['model']=='biotech:block/'+id
 for texture in model['textures'].values():
  if texture.startswith('#'):continue
  png=assets/(texture.split(':')[1].replace('block/','textures/block/')+'.png')
  data=png.read_bytes();assert data[:8]==b'\x89PNG\r\n\x1a\n'
  assert struct.unpack('>II',data[16:24])==(32,32),png
 for target in ['forge-1.20.1','neoforge-1.21.1','neoforge-26.1.2']:
  module=ROOT/target;res=module/'src/main/resources';forge=target.startswith('forge');modern=target=='neoforge-26.1.2'
  for kind in ['blocks/BlockRegistries','items/ItemRegistries']:
   text=(module/f'src/main/java/com/nstut/biotech/{kind}.java').read_text();assert id.upper() in text,(id,target,kind)
  recipe=json.loads((res/f'data/biotech/{"recipes" if forge else "recipe"}/{id}.json').read_text())
  assert recipe['result']['item' if forge else 'id']=='biotech:'+id
  assert recipe['result']['count']==4 and len(recipe['ingredients'])==3
  assert all(isinstance(i,str if modern else dict) for i in recipe['ingredients'])
  loot=json.loads((res/f'data/biotech/{"loot_tables" if forge else "loot_table"}/blocks/{id}.json').read_text())
  assert loot['pools'][0]['entries'][0]['name']=='biotech:'+id
  if modern:assert json.loads((res/f'assets/biotech/items/{id}.json').read_text())['model']['model']=='biotech:item/'+id
  for tag in ['mineable/pickaxe','needs_stone_tool']:
   tagpath=res/f'data/minecraft/tags/{"blocks" if forge else "block"}/{tag}.json'
   if not tagpath.exists():tagpath=ROOT/'common/src/main/resources'/f'data/minecraft/tags/blocks/{tag}.json'
   assert 'biotech:'+id in json.loads(tagpath.read_text())['values']
subprocess.run([sys.executable,str(ROOT/'scripts/generate_machine_structures.py'),'--check'],check=True)
print('Verified eight parts: registrations, 32px imagegen texture exports, models, item definitions, crafting, drops and tags on all three targets.')

for name, expected in json.loads((ROOT/'design/legacy-2.5-manifest.json').read_text()).items():
 text=(ROOT/name).read_text()
 body=text.split('public MultiblockPattern getLegacyMultiblockPattern() {',1)[1].split('    @Override',1)[0].strip()
 offsets=re.search(r'Vec3i\[\] southOffset = \{.*?\n        };',text,re.S).group()
 assert hashlib.sha256(body.encode()).hexdigest()==expected['pattern_sha256'],name
 assert hashlib.sha256(offsets.encode()).hexdigest()==expected['hatches_sha256'],name
print('Verified all twelve legacy pattern implementations and hatch offset tables are byte-identical to 2.5.')

for target in ['forge-1.20.1','neoforge-1.21.1','neoforge-26.1.2']:
 common=ROOT/'common/src/main/resources';local=ROOT/target/'src/main/resources'
 # Additions must not collide with common resources (Gradle Copy rejects duplicates).
 for name in [f'data/minecraft/tags/{"blocks" if target.startswith("forge") else "block"}/mineable/pickaxe.json',f'data/minecraft/tags/{"blocks" if target.startswith("forge") else "block"}/needs_stone_tool.json']:
  assert not ((common/name).exists() and (local/name).exists()),(target,name,'duplicate resource')
