import os
import json
import re

folderPath = '../mazedata'

mazes = os.listdir(folderPath)

commands = {}

class State:
    def __init__(self):
        self.currentMaze = None
        self.fileName = 'output.txt'

    def toConsole(self):
        self.output = print

    def write(self, message):
        self.f.write(message)
        print(message)

    def toFile(self):
        self.f = open(self.fileName, 'w+')
        self.output = self.write

    def closeFile(self):
        self.f.close()
        print(': Written to ' + self.fileName)

state = State()

def printOut(message):
    global state
    state.output(message)

def printMazeList(mazeList):
    columnWidth = 35
    spaces = ' '*columnWidth

    lines = []
    line = ''
    for i in range(0,len(mazeList)):
        if i%2 == 0:
            line = mazeList[i]
        else:
            line += ' '
            pad = columnWidth-len(line)
            if pad < 0:
                pad = 0
            line += spaces[:pad]
            line += mazeList[i]
            lines.append(line)
            line = ''

    if (len(line) > 0):
        lines.append(line)

    printOut('\n'.join(lines))


def listMazes():
    global mazes
    printMazeList(mazes)


def getPath(mazeName):
    return folderPath + '/' + mazeName + '/'

def countPrefixMatch(s1, s2):
    i = 0
    maxMatch = min(len(s1),len(s2))
    while i < maxMatch:
        if s1[i] == s2[i]:
            i += 1
        else:
            break
    return i

def findClosestMatch(mazeName):
    global mazes
    if mazeName in mazes:
        return mazeName

    longestMatch = 0
    bestMatch = None
    matches = 0
    for maze in mazes:
        match = countPrefixMatch(mazeName, maze)
        if match > longestMatch:
            longestMatch = match
            bestMatch = maze
            matches = 1
        elif match == longestMatch:
            matches += 1

    if matches == 1:
        return bestMatch
    else:
        return None




def selectMaze(mazeName=None):
    global folderPath, state
    if mazeName == None:
        return state.currentMaze
    mazeName = findClosestMatch(mazeName)
    state.currentMaze = mazeName
    return mazeName


def readFile(fileName, mazeName=None):
    global state
    mazeName = selectMaze(mazeName)
    if mazeName == None:
        return None

    path = getPath(mazeName) + fileName
    try:
        f = open(path)
        s = f.read()
        f.close()
        return s
    except Exception as e:
        printOut(e)
        return None


def getMazeAttributes(mazeName=None):
    attrs = {}
    mazeName = selectMaze(mazeName)
    s = readFile('analysis.txt', mazeName)
    if s == None:
        return None

    lines = s.split('\n')
    for line in lines:
        if line[:2] == '--':
            break
        pair = line.split(':')
        if (len(pair) == 2):
            key = pair[0].strip()
            value = pair[1].strip()
            attrs[key] = value

    attrs['name'] = mazeName
    attrs['type'] = mazeName[:mazeName.find('_')]
    return attrs

def prettyPrint(attrs):
    columnWidth = 25
    spaces = ' '*columnWidth

    for key in sorted(attrs.keys()):
        s = key + ' '
        pad = columnWidth-len(key)
        if pad < 0:
            pad = 0
        s += spaces[:pad]
        s += '= ' + attrs[key]
        printOut(s)


def jsonPrettyPrint(dictionary):
    printOut(json.dumps(dictionary, sort_keys=True, indent=4))


def viewAttributes(args):
    mazeName = None
    if (len(args) > 0):
        mazeName = args[0]

    attrs = getMazeAttributes(mazeName)
    if attrs != None:
        prettyPrint(attrs)


def viewMaze(args):
    mazeName = None
    if (len(args) > 0):
        mazeName = args[0]

    s = readFile('maze_pretty.txt', mazeName)
    if s != None:
        printOut(s)


def parseAttrs(attrs, left, right):
    if left in attrs:
        left = attrs[left]
    if right in attrs:
        right = attrs[right]
    return left, right

def parseAttrsFloat(attrs, left, right):
    left, right = parseAttrs(attrs, left, right)
    try:
        left = float(left)
        right = float(right)
        return left, right
    except ValueError as e:
        return None, None


def makeCondition(left, right, symbol):
    parse = None
    compare = None

    if symbol == '==' or symbol == '=':
        parse = parseAttrs
        compare = lambda l, r: l == r
    elif symbol == '!=':
        parse = parseAttrs
        compare = lambda l, r: l != r
    elif symbol == '<':
        parse = parseAttrsFloat
        compare = lambda l, r: l < r
    elif symbol == '<=':
        parse = parseAttrsFloat
        compare = lambda l, r: l <= r
    elif symbol == '>':
        parse = parseAttrsFloat
        compare = lambda l, r: l > r
    elif symbol == '>=':
        parse = parseAttrsFloat
        compare = lambda l, r: l >= r

    if parse == None:
        def condition(attrs):
            return False
        return condition

    def condition(attrs):
        l, r = parse(attrs, left, right)
        if l == None:
            return False
        return compare(l, r)
    return condition


def parseCondition(s):
    #Accepted symbols: < = > <= >=
    args = re.split(r'[!<=>]*', s)
    symbolS = re.search(r'[!<=>]', s).start()
    symbolE = re.search(r'[^!<=>]', s[symbolS:]).start()
    symbol = s[symbolS:symbolS+symbolE]

    return makeCondition(args[0].strip(), args[1].strip(), symbol)



def parseConditions(args):
    conditions = []
    args = ' '.join(args).strip()
    if (len(args) <= 0):
        return conditions

    args = args.split(',')
    for arg in args:
        conditions.append(parseCondition(arg.strip()))
    return conditions


def meetsConditions(attrs, conditions):
    for condition in conditions:
        if not condition(attrs):
            return False
    return True

    cond = 'hasSqueezableCorners'
    return cond in attrs and attrs['hasSqueezableCorners'] == 'true'


def findProperties(args):
    global mazes
    mazeAttrs = {}
    for maze in mazes:
        mazeAttrs[maze] = getMazeAttributes(maze)

    conditions = parseConditions(args)

    matches = []
    for key in mazeAttrs.keys():
        value = mazeAttrs[key]
        if meetsConditions(value, conditions):
            matches.append(key)

    matches.sort()
    printMazeList(matches)


def writeToFile(args):
    global state
    state.toFile()
    execCommand(args)
    state.closeFile()


def initCommands():
    global commands
    commands['ls'] = lambda args : listMazes()
    commands['q'] = lambda args: quit()
    commands['a'] = lambda args: viewAttributes(args)
    commands['v'] = lambda args: viewMaze(args)
    commands['f'] = lambda args : findProperties(args)
    commands['write'] = lambda args : writeToFile(args)


def execCommand(args):
    cmd = args[0]
    args = args[1:]
    if cmd in commands:
        commands[cmd](args)
    else:
        print("Unknown command.")


initCommands()
if __name__ == '__main__':
    s = input('>> ')
    while True:
        state.toConsole()
        args = s.split()
        execCommand(args)
        s = input('>> ')