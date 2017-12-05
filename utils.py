import cv2
import numpy as np
import os
from matplotlib import pyplot as plt
plt.rcParams.update({'font.size': 4})


def load_images_from_folder(folder):
    images = []
    for filename in os.listdir(folder):
        img = cv2.imread(os.path.join(folder,filename))
        if img is not None:
            images.append(img)
    return images

def show_images(images,figsize,dpi,grid):

    number_of_subplots = len(images)
    plt.figure(figsize = (figsize,figsize), dpi = dpi)
    
    for v in range(number_of_subplots):
        ax1 = plt.subplot(grid[0],grid[1],v+1)
        plt.title('img #' + str(v))
        ax1.imshow(images[v],  cmap='gray')
    plt.show()
    
def BGR_to_RGB(images):
    RGB_images = []
    for image in images:
        RGB_images.append(image[...,::-1])
    return RGB_images
    
