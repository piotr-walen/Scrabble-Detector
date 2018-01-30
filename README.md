# Scrabble-Detector
Automatic point counting system for Scrabble game 
Used technologies: Python, Keras, Tensorflow, OpenCV, Android, JUnit

Application detects game board and tiles with letter, using OpenCV image processing library, then classifies each tile with two convolutional neural networks built in Keras, and finally counts points for each player turn.
<p align="center">
  Input image:
  <br>
  <img src="python/screenshots/input.jpg" width="350"/>
</p>
<p align="center">
  Detected board with recognized letters (console output):
  <br>
  <img src="python/screenshots/outputNN.png" width="350"/>
</p>
