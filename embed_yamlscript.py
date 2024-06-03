import yaml, yamlscript

ys = yamlscript.YAMLScript()

ys1_text = """
!yamlscript/v0
mapv \(%1 * 10): range(4, 6)
"""
datays1 = ys.load(ys1_text)
print(datays1)

y1_text = """
- 40
- 50
- 60
"""
datay1 = yaml.safe_load(y1_text)
print(datay1)

ys2_text = """
mapv \(%1 * 10): range(4, 6)
"""
datays2 = yaml.safe_load(ys2_text)
print(datays2)

ys3_text = "!yamlscript/v0\n" + yaml.dump(datays2)
datays3 = ys.load(ys3_text)
print(datays3)

# datays4 = ys.load(open('ytranslation.yaml').read())
# print(datays4)
