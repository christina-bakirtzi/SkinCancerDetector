"""Model inference and helper codes"""

import os
os.environ['CUDA_VISIBLE_DEVICES'] = '-1'
import numpy as np
import cv2

from io import BytesIO
from PIL import Image
from tensorflow.keras.models import load_model
from keras.preprocessing.image import img_to_array

# Model package lives at:
# https://github.com/hasibzunair/adversarial-lesions/tree/master/packaging 
# https://test.pypi.org/project/melanet/
from melanet.pretrained_model import get_model

# Setup config and class names
IMAGE_SIZE = 256
classes = ['Non-melanoma', 'Melanoma', ]

def get_prediction_model():
    # Get melanoma detector model
    model = get_model()
    model._make_predict_function()
    return model

def read_image(image_encoded):
    image = Image.open(BytesIO(image_encoded))
    return image

def preprocess(image: Image.Image):
    # Convert to numpy array
    img_array = img_to_array(image)
    # Convert to array
    img_array = cv2.resize(img_array, (IMAGE_SIZE, IMAGE_SIZE))
    # Normalize to [0,1]
    img_array = img_array.astype('float32')
    img_array /= 255
    # Add batch axis
    img_array = np.expand_dims(img_array, 0)
    return img_array

def predict(image, model):
    # Predict
    pred = model.predict(image)
    # Define results
    results = {"success": False}
    results["predictions"] = []
    # Loop over the results and add them to the list of
    # returned predictions
    for (finding, prob) in zip(classes, pred[0]):
        r = {"label": finding, "probability": float(prob)}
        results["predictions"].append(r)
    # Indicate that the request was a success
    results["success"] = True
    return results

