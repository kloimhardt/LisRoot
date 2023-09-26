import re

def mutcons(e, l):
    l.insert(0, e)
    return l

def kw(s):
    return ":" + s

kwmap = kw("map")
kwcat = kw("cat")
kwcxx = kw("cxx")
kwrtm = kw("rtm")
kwstring = kw("string")
kwdouble = kw("double")

def cvtyp(s):
    if s == "Option_t":
        return kwstring
    else:
        return kw(s)

def arg0(a20):
    optional_argument = True if "=" in a20 else False
    nameidx = -3 if optional_argument else -1
    erg = [[kw("A"), [kwmap, [kwrtm, [kwmap]], [kwcxx, [kwcat]]]] if optional_argument else false,
           [kw("B"), [kwmap,
                      [kwrtm, [kwmap, [kw(a20[nameidx]), cvtyp(a20[0])]]],
                      [kwcxx, [kwcat, cvtyp(a20[0])]]]]]
    return erg

def head(a01, a20):
    return [kw(a01[0]), [kwmap, [kw(a01[1]), mutcons(kwmap, arg0(a20))]]]

def malli(s):
    rp = re.compile("([()])")
    a = rp.split(s)
    a0 = a[0].split()
    a01 = a0[1].split("::")
    a2 = a[2].split(",")
    a20 = a2[0].split()
    erg = list(filter(lambda x: x, head(a01, a20)))
    return erg

def listostring(l):
    if type(l) is list:
        return "[" + " ".join(map(listostring, l)) + "]"
    else:
        return l

csrc = """ void TF1::Draw	(	Option_t * 	option = ""	) """
print(listostring(malli(csrc)))
