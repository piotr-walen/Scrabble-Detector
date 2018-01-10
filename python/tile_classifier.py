from keras.layers import Convolution2D
from keras.layers import Dense
from keras.layers import Dropout
from keras.layers import Flatten
from keras.layers import MaxPooling2D
from keras.models import Sequential
from keras.preprocessing.image import ImageDataGenerator
from sklearn.model_selection import train_test_split
from image_processing import *
from skimage.transform import resize
from keras.models import load_model

class TileClassifier():

    def __init__(self,data_set_path,model_path):
        self._build_classifier()
        self._build_generator(data_set_path)
        self.model_path = model_path

    def _build_classifier(self):
        self.classifier = Sequential()
        self.classifier.add(Convolution2D(32, (3, 3), input_shape=(64, 64, 3), activation='relu'))
        self.classifier.add(Convolution2D(32, (3, 3), input_shape=(64, 64, 3), activation='relu'))
        self.classifier.add(MaxPooling2D(pool_size=(2, 2)))
        self.classifier.add(Dropout(0.15))
        self.classifier.add(Convolution2D(32, (3, 3), input_shape=(64, 64, 3), activation='relu'))
        self.classifier.add(Convolution2D(32, (3, 3), input_shape=(64, 64, 3), activation='relu'))
        self.classifier.add(MaxPooling2D(pool_size=(2, 2)))
        self.classifier.add(Flatten())
        self.classifier.add(Dense(units=128, activation='relu'))
        self.classifier.add(Dense(units=2, activation='softmax'))

        self.classifier.compile(
            optimizer='adam',
            loss='categorical_crossentropy',
            metrics=['accuracy']
        )


    # !!automate batch_size
    def _build_generator(self,data_set_path):
        datagen = ImageDataGenerator(rescale=1. / 255)
        self.itr = datagen.flow_from_directory(
            data_set_path,
            target_size=(64, 64),
            batch_size=1868,
            class_mode='categorical')


    def _generate_test_train_data(self):
        X, y = self.itr.next()
        X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=7)
        return X_train, X_test, y_train, y_test

    def train_evaluate(self,epochs):
        X_train, X_test, y_train, y_test = self._generate_test_train_data()
        self.classifier.fit(X_train, y_train, epochs=epochs, batch_size=X_train.shape[0]//20, verbose=1)
        self.classifier.save_weights(self.model_path)
        print(self.classifier.evaluate(X_test, y_test, batch_size=X_test.shape[0]//20))

    def get_class_indices(self):
        return self.itr.class_indices.copy()

    def load(self):
        self.classifier.load_weights(self.model_path)

    def predict(self,img):
        return self.classifier.predict(img)

    def validate(self, image_path):
        slices = get_slices(image_path)
        indexes = list()
        for slice_ in slices:
            scaled_img = resize(slice_, (64, 64, 3), mode='reflect')
            img = np.expand_dims(scaled_img, axis=0)
            prediction = self.classifier.predict(img)
            indexes.append(np.argmax(prediction.flatten()))
        arr = np.array(indexes).reshape(15, 15)
        return arr

if __name__ == '__main__':

    model_path ='models/tile_classifier.h5'
    data_set_path = './data/data_set_tile'
    model = TileClassifier(data_set_path,model_path)
    model.train_evaluate(epochs=5)
    class_indices = model.get_class_indices()
    print(class_indices)
    image_path = './data/testy/000.jpg'
    model.validate(image_path)




