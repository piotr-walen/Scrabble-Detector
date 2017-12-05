import cv2
import numpy as np

def get_points(cnt):
    epsilon = 0.01*cv2.arcLength(cnt,True)
    approx = cv2.approxPolyDP(cnt,epsilon,True)
    x_center = np.mean(approx[:,:,0])
    y_center = np.mean(approx[:,:,1])
    
    #sorting
    pts = np.zeros((4,2))
    for x in approx:
        for X, Y in x:
            if(X<x_center and Y<y_center):
                pts[0,0] = X
                pts[0,1] = Y
            if(X<x_center and Y>y_center):
                pts[1,0] = X
                pts[1,1] = Y
            if(X>x_center and Y>y_center):
                pts[2,0] = X
                pts[2,1] = Y
            if(X>x_center and Y<y_center):
                pts[3,0] = X
                pts[3,1] = Y
    size = max(approx.flatten()) - min(approx.flatten())
    return pts, size

def get_contour(img):
    grey = cv2.cvtColor(img,cv2.COLOR_BGR2GRAY)
    blur = cv2.GaussianBlur(grey,(7,7),0)
    edges = cv2.Canny(blur,50,100,apertureSize = 3)
    kernel = np.ones((5,5),np.uint8)
    dilation = cv2.dilate(edges,kernel,iterations = 2)
    contour_img, contours, hierarchy = cv2.findContours(
                                            dilation, 
                                            cv2.RETR_TREE,
                                            cv2.CHAIN_APPROX_SIMPLE)
    cnt = max(contours, key = cv2.contourArea)
    contour_img = img.copy()
    contour_img = cv2.drawContours(contour_img, [cnt], 0, (0,255,0), 3)
    return contour_img, cnt

def warp_image(img, pts, size):
    pts = np.float32(pts)
    pts_sqr = np.float32([[0,0],[0,size],[size,size],[size,0]])
    M = cv2.getPerspectiveTransform(pts,pts_sqr)
    warped_img = cv2.warpPerspective(img,M,(size,size))
    return warped_img
