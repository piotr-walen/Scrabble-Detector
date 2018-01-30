import os
import cv2
import numpy as np
from matplotlib import pyplot as plt

def load_images_from_folder(folder):
    filenames = os.listdir(folder)
    paths = (os.path.join(folder, filename) for filename in filenames)
    imgs = (cv2.imread(path) for path in paths)
    return [img for img in imgs if img is not None]
    # images = []
    # for filename in os.listdir(folder):
    #     img = cv2.imread(os.path.join(folder, filename))
    #     if img is not None:
    #         images.append(img)
    # return images


def show_images(images, figsize, dpi, grid):
    number_of_subplots = len(images)
    plt.figure(figsize=(figsize, figsize), dpi=dpi)

    for v in range(number_of_subplots):
        ax1 = plt.subplot(grid[0], grid[1], v + 1)
        plt.title('img #' + str(v))
        ax1.imshow(images[v], cmap='gray')
    plt.show()


def bgr_to_rgb(images):
    RGB_images = []
    for image in images:
        RGB_images.append(image[..., ::-1])
    return RGB_images


def get_contour(img):
    grey = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)
    blur = cv2.GaussianBlur(grey, (7, 7), 0)
    edges = cv2.Canny(blur, 50, 100, apertureSize=3)
    kernel = np.ones((5, 5), np.uint8)
    dilation = cv2.dilate(edges, kernel, iterations=2)
    contour_img, contours, hierarchy = cv2.findContours(
        dilation,
        cv2.RETR_TREE,
        cv2.CHAIN_APPROX_SIMPLE)
    cnt = max(contours, key=cv2.contourArea)
    contour_img = img.copy()
    contour_img = cv2.drawContours(contour_img, [cnt], 0, (0, 255, 0), 3)
    return contour_img, cnt


def get_points(cnt):
    epsilon = 0.01 * cv2.arcLength(cnt, True)
    approx = cv2.approxPolyDP(cnt, epsilon, True)
    x_center = np.mean(approx[:, :, 0])
    y_center = np.mean(approx[:, :, 1])

    # sorting
    pts = np.zeros((4, 2))
    for x in approx:
        for X, Y in x:
            if X < x_center and Y < y_center:
                pts[0, 0] = X
                pts[0, 1] = Y
            if X < x_center and Y > y_center:
                pts[1, 0] = X
                pts[1, 1] = Y
            if X > x_center and Y > y_center:
                pts[2, 0] = X
                pts[2, 1] = Y
            if X > x_center and Y < y_center:
                pts[3, 0] = X
                pts[3, 1] = Y
    size = max(approx.flatten()) - min(approx.flatten())
    return pts, size


def warp_image(img, pts, size):
    pts = np.float32(pts)
    pts_sqr = np.float32([[0, 0], [0, size], [size, size], [size, 0]])
    M = cv2.getPerspectiveTransform(pts, pts_sqr)
    warped_img = cv2.warpPerspective(img, M, (size, size))
    return warped_img

def slice_image(image):
    slices = list()
    samples_x = np.linspace(0,image.shape[0],16, dtype=np.int)
    samples_y = np.linspace(0,image.shape[1],16,dtype=np.int)
    for x_start, x_end in zip(samples_x, samples_x[1:]):
        for y_start, y_end in zip(samples_y, samples_y[1:]):
            slices.append(image[x_start:x_end,y_start:y_end])
    return slices

def get_slices(image_path):
    img = cv2.imread(image_path)
    
    contour_img, cnt = get_contour(img)
    pts, size = get_points(cnt)
    board = warp_image(img, pts, size)
    warped_img = board[..., ::-1]
    
    plt.figure(figsize = (6,6), dpi = 100)
    plt.imshow(warped_img)
    plt.show()
    slices = slice_image(warped_img)
    return slices