def splitE(s):
    s = s.split()
    for i in range(1,len(s)):
        if s[i] > s[0]:
            yield [int(s[0]),int(s[i])]

n = input()
e = []

for i in range(0, n):
    for j in splitE(raw_input()):
        e.append(j)

def replace(e,i,j):
    global groups
    _e = []
    for k in e:
        a = [k[0],k[1]]
        if k[0] == j :
            a[0] = i
        elif k[1] == j:
            a[1] = i
        if a[0] != a[1]:
            _e.append(a)
    rep = []
    for k in range(1,n+1):
        try:
            if groups[k][0] == j :
                rep = groups[k]
                groups[k] = []
                break
        except IndexError:
            pass
    for k in range(1,n+1):
        try:
            if groups[k][0] == i :
                groups[k].extend(rep)
        except IndexError:
            pass
    return _e

from random import choice
def cut(e):
    global n
    N = n
    while(N > 2):
        rem = choice(e)
        e = replace(e,rem[0],rem[1])
        N -= 1
    return e

gro2 = []    
mini = 10000000000
for i in range(1,20):
    global groups
    groups = [[]]
    for j in range(1,n+1):
        groups.append([j])
    miniTemp = len(cut(e))
    if mini > miniTemp:
        mini = miniTemp
        gro2 = groups
    print miniTemp


print mini
print gro2