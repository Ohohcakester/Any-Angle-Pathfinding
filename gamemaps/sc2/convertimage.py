import pygame

def convert(fileName):
    img = pygame.image.load(fileName)
    width = img.get_width()
    height = img.get_height()
    b = pygame.image.tostring(img, 'RGB')
    rows = []
    for y in range(0,height):
        row = []
        anchor = y*width*3
        for x in range(0,width):
            index = x*3 + anchor
            if b[index] == 0:
                row.append('1')
            else:
                row.append('0')
        rows.append(row)
        
    for i in range(0,height):
        rows[i] = ' '.join(rows[i])
    rows = '\n'.join(rows)
    header = str(width) + ' ' + str(height) + '\n'
    return header + rows
      

      
      
while(True):
    name = input()
    try:
        s = convert(name + '.bmp')
        f = open(name + '.txt', 'w+')
        f.write(s)
        f.close()
        print('Success. Written to ' + name + '.txt')
    except Exception as e:
        print('invalid name')
        print(e)