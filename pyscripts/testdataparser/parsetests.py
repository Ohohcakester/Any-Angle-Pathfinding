import os
files = list(filter(lambda s : s.endswith('.txt'), os.listdir()))
files.sort()

def main():
    readdata('Average Time', 'avgTime.dat')
    readdata('Standard Dev', 'stdev.dat')
    readdata('Average Path Length', 'pathLen.dat')



def readdata(dataTypeStr, fileName):
    def parseData(line):
        args = line.split(':')
        if args[0] == dataTypeStr:
            return args[1].strip()
        return None
        
    outputlines = [['Name', 'Size', 'Density']]
    index = 0
    for file in files:
        f = open(file)
        lines = f.read()
        f.close()
        lines = lines.split('\n')
        outputlines[0].append(file[:-4])
        index += 1
        
        for line in lines:
            if index >= len(outputlines):
                outputlines.append([])
            
            if '===' in line or '<<' in line:
                continue
            
            if len(line.strip()) == 0:
                index += 1
                continue
            
            outputline = outputlines[index]
            if ' - ' in line or 'baldursgate' in line:
                if len(outputline) == 0:
                    outputline += parseName(line)
            elif ':' in line:
                data = parseData(line)
                if data != None:
                    outputline.append(data)
        index = 0
       
    writeToFile(fileName, outputlines)
    
def writeToFile(fileName, outputlines):
    f = open(fileName, 'w+')
    for outputline in outputlines:
        f.write('\t'.join(outputline) + '\n')
    f.close()
        

def parseName(line):
    tokens = []
    args = line.split(' ')
    tokens.append(args[0])
    
    args = args[1:]
    for arg in args:
        if 'x' in arg:
            tokens.append(arg)
            break
    for arg in args:
        if '%' in arg:
            tokens.append(arg)
            break
    
    while len(tokens) < 3:
        tokens.append('')
    return tokens


    



if __name__ == '__main__':
    main()
