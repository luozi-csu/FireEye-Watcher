from  detect import parse_opt,main
import cv2
import os
import numpy as np
import tensorflow as tf

from pathlib import Path
import sys, os

FILE = Path(__file__).resolve()
ROOT = FILE.parents[0]
if str(ROOT) not in sys.path:
    sys.path.append(str(ROOT))
ROOT = Path(os.path.relpath(ROOT, Path.cwd()))

#处理图片
def data_process(prefix):
    a = np.zeros((1, 130, 130, 3))
    b = np.zeros((1, 10, 130, 130, 3))
    count = 0
    data_list = os.listdir(ROOT / "runs/detect/exp/crops/file_hole")
    for filename in data_list:
        if not filename.startswith(prefix):
            continue
        path = str(ROOT / "runs/detect/exp/crops/file_hole") + "/" + filename
        pic = cv2.imread(path)
        frame = cv2.resize(pic, (130, 130))
        if count < 10:
            frame = np.reshape(np.array(frame), (1, 130, 130, 3))
            # print(a.shape,frame.shape)
            a = np.vstack((a, frame))
            count += 1
        elif count == 10:
            a = a[1:, :, :]
            a = a[np.newaxis, :]
            b = np.vstack((b, a))
            a = np.zeros((1, 130, 130, 3))
            count = 0
    return b

#ConvLSTM模型预测
def model_predict(path):
    model = tf.saved_model.load(ROOT / 'mymodel')
    data = data_process(path.split("/")[-1].split(".")[0])
    x_test = tf.cast(data, dtype=tf.float32) / 255
    db_test = tf.data.Dataset.from_tensor_slices((x_test))
    db_test = db_test.batch(4)

    def predict(data):
        ans = model(data, training=False)
        prob = tf.nn.softmax(ans, axis=1)
        pred = tf.argmax(prob, axis=1)
        pred = tf.cast(pred, dtype=tf.int32)
        return pred

    total = []
    for step, (x) in enumerate(db_test):
        pred = predict(x).numpy()
        a = np.bincount(pred)
        ans = np.argmax(a)
        total.append(ans)

    stasticate = np.bincount(total)
    maxi = np.argmax(stasticate)
    dic = {0: '偏低', 1: '正常', 2: '偏高'}
    print('状态：', dic[maxi],'置信度：', stasticate[maxi] / len(total))
    res=maxi
    return res