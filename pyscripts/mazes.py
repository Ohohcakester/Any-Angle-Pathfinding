import os
import json
import re
import numpy

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
        self.f.write(str(message))
        self.f.write('\n')
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


def openFile(fileName, mazeName=None):
    global state
    mazeName = selectMaze(mazeName)
    if mazeName == None:
        return None

    path = getPath(mazeName) + fileName
    try:
        f = open(path)
        return f
    except Exception as e:
        printOut(e)
        return None


def getMazeAttributes(mazeName=None):
    attrs = {}
    mazeName = selectMaze(mazeName)
    f = openFile('analysis.txt', mazeName)
    if f == None:
        return None

    for line in f:
        if line[:2] == '--':
            break
        pair = line.split(':')
        if (len(pair) == 2):
            key = pair[0].strip()
            value = pair[1].strip()
            attrs[key] = value
    f.close()
            
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

    f = openFile('maze_pretty.txt', mazeName)
    if f != None:
        for line in f:
            printOut(line)
    f.close()


def printCorrelation(vector1, vector2):
    printOut("Number of data points: " + str(len(vector1)))

    A = numpy.array([vector1, numpy.ones(len(vector1))])
    y = vector2
    w = numpy.linalg.lstsq(A.T,y)[0]

    printOut('Line: y = ' + str(w[0]) + 'x + ' + str(w[1]))

    r = numpy.corrcoef(vector1, vector2)
    printOut('r = ' + str(r[0][1]))


def retrieveFloatVectors(var1, var2, conditionArgs):
    conditions = parseConditions(conditionArgs)
    matches, attrList = filterMazes(mazes, conditions)

    vector1 = []
    vector2 = []
    try:
        for attr in attrList:
            if var1 in attr and var2 in attr:
                vector1.append(float(attr[var1]))
                vector2.append(float(attr[var2]))
    except ValueError as e:
        printOut(e)
        return None, None

    return vector1, vector2

def retrieveDataVector(var, conditionArgs):
    conditions = parseConditions(conditionArgs)
    matches, attrList = filterMazes(mazes, conditions)

    vector = []
    mazeNames = []
    try:
        for i in range(0,len(attrList)):
            attr = attrList[i]
            match = matches[i]
            if var in attr:
                vector.append(attr[var])
                mazeNames.append(match)
    except ValueError as e:
        printOut(e)
        return None, None

    return vector, mazeNames
    
    
def listData(args):
    vector, mazeNames = retrieveDataVector(args[0], args[1:])
    for i in range(0,len(mazeNames)):
        s = mazeNames[i] + ': ' + vector[i]
        printOut(s)
        
        
def getMean(args):
    vector, mazeNames = retrieveDataVector(args[0], args[1:])
    
    sum = 0
    sumSquares = 0
    for val in vector:
        val = float(val)
        sum += val
        sumSquares += val*val
    
    n = len(vector)
    mean = sum/n
    meanSquare = sumSquares/n
    variance = meanSquare - mean*mean
    sd = variance**0.5
    printOut(args[0])
    printOut('Mean = ' + str(mean))
    printOut('SD = ' + str(sd))
    
    

    
    

def correlate(args):
    global mazes

    var1 = args[0]
    var2 = args[1]
    conditionArgs = args[2:]

    vector1, vector2 = retrieveFloatVectors(var1, var2, conditionArgs)

    try:
        printCorrelation(vector1, vector2)
    except ValueError as e:
        printOut(e)
        printOut(vector1)
        printOut(vector2)


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


def makeCondition(left=None, right=None, symbol=None):
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
    try:
        symbolS = re.search(r'[!<=>]', s).start()
        symbolE = re.search(r'[^!<=>]', s[symbolS:]).start()
        symbol = s[symbolS:symbolS+symbolE]
    except AttributeError as e:
        printOut('Error parsing condition: ' + s)
        printOut(e)
        symbol = None

    if len(args) <= 0 or symbol == None:
        return makeCondition()
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
    if attrs == None: return False
    for condition in conditions:
        if not condition(attrs):
            return False
    return True

    cond = 'hasSqueezableCorners'
    return cond in attrs and attrs['hasSqueezableCorners'] == 'true'


def filterMazes(mazeNames, conditions):
    matches = []
    matchAttrs = []
    for maze in mazeNames:
        attrs = getMazeAttributes(maze)
        if meetsConditions(attrs, conditions):
            matches.append(maze)
            matchAttrs.append(attrs)
    return matches, matchAttrs


def findProperties(args):
    global mazes

    conditions = parseConditions(args)
    matches, attrs = filterMazes(mazes, conditions)
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
    commands['corr'] = lambda args : correlate(args)
    commands['data'] = lambda args : listData(args)
    commands['mean'] = lambda args : getMean(args)
    


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