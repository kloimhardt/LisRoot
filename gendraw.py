import re

csrc = """ void TF1::Draw	(	Option_t * 	option = ""	) """

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
    b0= [kwmap,
         [kw("A"), [kwmap, [kwrtm, [kwmap]], [kwcxx, [kwcat]]]] if optional_argument else null,
         [kw("B"), [kwmap,
                    [kwrtm, [kwmap, [kw(a20[nameidx]), cvtyp(a20[0])]]],
                    [kwcxx, [kwcat]]]]]
    return b0

def head(a01, a20):
    return [kw(a01[0]), [kwmap, [kw(a01[1]), arg0(a20)]]]

def malli(s):
    rp = re.compile("([()])")
    a = rp.split(s)
    a2 = a[2].split(",")
    a20 = a2[0].split()
    a0 = a[0].split()
    a01 = a0[1].split("::")
    return head(a01, a20)


def listostring(l):
    if type(l) is list:
        return "[" + " ".join(map(listostring, l)) + "]"
    else:
        return l

print(listostring(malli(csrc)))
