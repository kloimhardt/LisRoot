class Linear:
    def __call__(self, arr, par):
        return par[0] + arr[0]*par[1]

l = Linear()
print(l([1.0], [2.0, 5.0]))

class LinearB:
    def __init__(self, k, d):
        self._k = k
        self._d = d
    def __call__(self, arr):
        return self._d + arr[0]*self._k

lb = LinearB(2.0, 5.0)
print(lb([1.0])) #comment => 7.0

lb._d = 4.0
print(lb([1.0])) #comment => 6.0
