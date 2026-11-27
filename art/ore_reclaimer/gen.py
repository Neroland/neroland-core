import random, sys
from PIL import Image
OUT=sys.argv[1]
H=(0xa0,0xb0,0xd2); S=(0x1c,0x20,0x2c)
STONE=[(0x56,0x5c,0x64),(0x62,0x68,0x70),(0x6e,0x74,0x7c)]
BASE=[(0x44,0x4a,0x54),(0x4e,0x54,0x5e),(0x56,0x5c,0x64)]
I1=(0xd8,0xdc,0xe4); I2=(0xa8,0xae,0xb8); I3=(0x7a,0x80,0x8a); I4=(0x4a,0x50,0x5c)
K0=(0x0c,0x0c,0x10); K1=(0x14,0x14,0x1a); K2=(0x1e,0x20,0x28); SL=(0x2e,0x34,0x44); SL2=(0x3a,0x40,0x52)
def h(c): return (c>>16&255,c>>8&255,c&255)
def body(seed, base_band=True):
    r=random.Random(seed); im=Image.new('RGBA',(16,16)); p=im.load()
    for y in range(16):
        for x in range(16):
            pal = BASE if (base_band and y>=12) else STONE
            p[x,y]=r.choice(pal)+(255,)
    for i in range(16):
        p[i,0]=H+(255,); p[0,i]=H+(255,)
    for i in range(1,16):
        p[i,15]=S+(255,); p[15,i]=S+(255,)
    return im,p
def put(p,x,y,c): p[x,y]=c+(255,)
def lip(p):
    # iron hopper lip wrapping the top of every vertical face
    for x in range(1,15):
        put(p,x,1,I2); put(p,x,2,I3)
    put(p,1,1,I1); put(p,2,1,I1)
    for x in (3,12): put(p,x,2,I1)
    for x in range(1,15): put(p,x,3,I4)
def base_seam(p):
    for x in range(1,15): put(p,x,11,S)
def side():
    im,p=body(11); lip(p); base_seam(p)
    for y in (5,7,9):
        for x in range(4,12): put(p,x,y,K2)
        for x in range(4,12): put(p,x,y+1,I3 if y<9 else STONE[2])
    for y in range(5,10): put(p,3,y,I4); put(p,12,y,I4)
    put(p,2,13,I2); put(p,13,13,I2)
    return im
ORE=[h(0xd9703a),h(0xff5ac8),h(0xffb02c),h(0xb478ff),h(0xff5a7a)]
OUTC=[h(0xd8dce4),h(0xf2c14e),h(0xe08a4a),h(0xc9a0ff)]
def front(frame=None):
    im,p=body(23); lip(p); base_seam(p)
    on = frame is not None
    # window frame
    for x in range(3,13):
        put(p,x,4,S); put(p,x,10,S)
    for y in range(4,11):
        put(p,3,y,S); put(p,12,y,S)
    for x in range(3,13): put(p,x,11,I3)   # sill
    put(p,3,11,I2); put(p,12,11,I4)
    for y in range(5,10):
        for x in range(4,12):
            put(p,x,y,(K0 if y in (5,9) else K1) if not on else ((0x16,0x10,0x0e) if y in (5,9) else (0x20,0x16,0x12)))
    # interlocking grinder teeth (two rollers)
    ph = (frame or 0)%2
    for x in range(4,12):
        t=(x+ph)%2==0
        put(p,x,6,(I1 if on else I2) if t else I3)
        put(p,x,7,I4 if t else K2)
    if on:
        # ore chunks dropping in from the hopper, vanilla dust falling out
        drops=[[(5,0),(9,2)],[(7,1),(10,3)],[(4,4),(8,0)],[(6,2),(11,1)]][frame]
        for x,c in drops: put(p,x,5,ORE[c])
        outs=[[(6,8,0),(10,9,1)],[(6,9,0),(8,8,2)],[(8,9,2),(5,8,1)],[(5,9,1),(9,8,3)]][frame]
        for x,y,c in outs: put(p,x,y,OUTC[c])
    # outlet chute + status lamp in the base band
    for x in range(6,10): put(p,x,13,K0)
    for x in range(5,11): put(p,x,12,I3)
    put(p,5,13,I4); put(p,10,13,I4)
    lamp = [h(0x4fb36b),h(0x7fe09a),h(0x4fb36b),h(0x3a8f54)][frame] if on else SL
    put(p,13,13,lamp); put(p,2,13,I2)
    return im
def top():
    im,p=body(37,False)
    for i in range(2,14):
        put(p,i,2,I1); put(p,2,i,I1); put(p,i,13,I3); put(p,13,i,I3)
    put(p,13,2,I2); put(p,2,13,I2)
    rings=[(3,SL2),(4,SL),(5,S),(6,K0)]
    for o,c in rings:
        for y in range(o,16-o):
            for x in range(o,16-o): put(p,x,y,c)
    # funnel: lit far walls, dark near walls, seams on the diagonals
    for o,c in [(3,(0x52,0x5c,0x76)),(4,SL2),(5,SL)]:
        for i in range(o,16-o): put(p,i,16-o-1,c); put(p,16-o-1,i,c)
    for o in (3,4,5):
        put(p,o,o,K1); put(p,15-o,15-o,(0x62,0x6c,0x86)); put(p,o,15-o,S); put(p,15-o,o,S)
    for x,y in ((1,1),(14,1),(1,14),(14,14)): put(p,x,y,I2)
    return im
def bottom():
    im,p=body(53,False)
    r=random.Random(5)
    for y in range(1,15):
        for x in range(1,15): put(p,x,y,r.choice(BASE))
    for i in range(4,12):
        put(p,i,4,I2); put(p,4,i,I2); put(p,i,11,I4); put(p,11,i,I4)
    for y in range(5,11):
        for x in range(5,11): put(p,x,y,S)
    for y in range(6,10):
        for x in range(6,10): put(p,x,y,K0)
    for y in range(6,10): put(p,6,y,K1)
    for x in range(6,10): put(p,x,6,K1)
    for x,y in ((2,2),(13,2),(2,13),(13,13)): put(p,x,y,I2)
    for i in range(1,4): put(p,i,7,S); put(p,i,8,S); put(p,15-i,7,S); put(p,15-i,8,S)
    return im
side().save(OUT+'/ore_reclaimer_side.png'); front().save(OUT+'/ore_reclaimer_front.png')
top().save(OUT+'/ore_reclaimer_top.png'); bottom().save(OUT+'/ore_reclaimer_bottom.png')
st=Image.new('RGBA',(16,64))
for f in range(4): st.paste(front(f),(0,16*f))
st.save(OUT+'/ore_reclaimer_front_on.png')
# contact sheet
from PIL import ImageDraw
C=sys.argv[2]
tiles=[('top',top()),('side',side()),('front',front()),('bottom',bottom())]+[('on f%d'%f,front(f)) for f in range(4)]
tiles+=[(n,Image.open(C+'/'+n+'.png').convert('RGBA').crop((0,0,16,16))) for n in ('item_store','item_store_top','trash_can','battery','fluid_tank','hematite_ore','cindrite_ore','voidglass_ore')]
sc=10; cw=16*sc+10; ch=16*sc+22
s=Image.new('RGBA',(8*cw,3*ch+10),(40,40,48,255)); d=ImageDraw.Draw(s)
for i,(n,im) in enumerate(tiles):
    x=(i%8)*cw+5; y=(i//8)*ch+5; s.paste(im.resize((160,160),Image.NEAREST),(x,y)); d.text((x,y+162),n,fill=(255,255,255))
# true-size + pseudo-block row
y=2*ch+5
blk=Image.new('RGBA',(48,32),(0,0,0,0)); blk.paste(side(),(0,16)); blk.paste(front(1),(16,16)); blk.paste(side(),(32,16)); blk.paste(top(),(16,0))
b2=blk.resize((48*5,32*5),Image.NEAREST); s.paste(b2,(5,y),b2)
for i,im in enumerate([top(),side(),front(),front(0),bottom()]): s.paste(im,(270+i*20,y)); s.paste(im.resize((32,32),Image.NEAREST),(270+i*36,y+30))
d.text((270,y+70),'1x / 2x; left: top over side|front(on)|side',fill=(255,255,255))
s.save(sys.argv[3])
