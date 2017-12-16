import image_slicer
from image_processing import *


if __name__ == '__main__':
    folder = './data/letter_img'
    images = load_images_from_folder(folder)
    #show_images(bgr_to_rgb(images), 10, 200, (len(images), 1))

    output_images = []
    for img in images:
        contour_img, cnt = get_contour(img)
        pts, size = get_points(cnt)
        warped_img = warp_image(img, pts, size)
        output_images.append(warped_img)
    #show_images(bgr_to_rgb(images), 10, 200, (len(images), 1))

    print(len(output_images))
    for i in range(len(output_images)):
        file = './data/warped_images/warped' + str(i) + '.jpg'
        cv2.imwrite(file, output_images[i])
        tiles = image_slicer.slice(file, 225, save=False)
        image_slicer.save_tiles(tiles, directory='./data/sliced_letters', prefix='slice' + str(i), format='png')
