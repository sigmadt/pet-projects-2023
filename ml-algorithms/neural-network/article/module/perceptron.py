import numpy as np
from sklearn.model_selection import train_test_split
import matplotlib.pyplot as plt
from sklearn import datasets


class Perceptron:
    def __init__(self, learning_rate=0.01, iters=300):
        self.l_rate = learning_rate
        self.iters = iters
        self.act_func = self.sign_func
        self.W = None
        self.B = None
        self.w = None

    # def sign_func(self, vector, classes):
    #     return np.where(vector > 0, classes[0], classes[1])
    def sign_func(self, vector):
        return np.where(vector > 0, 1, 0)

    def rescale_y(self, y):
        rescaled = [1 if val > 0 else 0 for val in y]
        return np.array(rescaled)

    def fit(self, X, y):
        rows, cols = X.shape[0], X.shape[1]
        y_true = self.rescale_y(y)
        # 1. initializing
        self.W = np.zeros(cols)
        self.B = 0
        # 2. iterating
        for iter in range(self.iters):
            if iter % 500 == 0:
                print(10 * '_' + f'Iteration number {iter}' + 10 * '_')
            for i in range(rows):
                sum_output = X[i] @ self.W + self.B
                # guessing label
                y_curr_guessed = self.act_func(sum_output)

                delta_y = y_true[i] - y_curr_guessed
                coef = self.l_rate * delta_y
                # updating weights
                self.W += coef * X[i]
                self.B += coef
        # 3.
        self.w = np.concatenate(([self.B], self.W))

    def predict(self, X):
        sum_output = X @ self.w[1:] + self.w[0]
        y_pred = self.act_func(sum_output)

        return y_pred


class PerceptronBest:
    def __init__(self, learning_rate=0.01, iters=300):
        self.l_rate = learning_rate
        self.iters = iters
        self.act_func = self.sign_func
        self.W = None
        self.B = None
        self.w = None
        self.best_weights = None
        self.best_res = 0

    def sign_func(self, vector):
        return np.where(vector > 0, 1, 0)

    def rescale_y(self, y):
        rescaled = [1 if val > 0 else 0 for val in y]
        return np.array(rescaled)

    def fit(self, X, y):
        rows, cols = X.shape[0], X.shape[1]
        y_true = self.rescale_y(y)
        # 1. initializing
        self.W = np.zeros(cols)
        self.B = 0
        # 2. iterating
        for iter in range(self.iters):
            if iter % 500 == 0:
                print(10 * '_' + f'Iteration number {iter}' + 10 * '_')
            for i in range(rows):
                sum_output = X[i] @ self.W + self.B
                # guessing label
                y_curr_guessed = self.act_func(sum_output)

                delta_y = y_true[i] - y_curr_guessed
                coef = self.l_rate * delta_y
                # updating weights
                self.W += coef * X[i]
                self.B += coef

            # decide if iteration is good
            # update best weights
            sum_output = X @ self.W + self.B
            y_pred = self.act_func(sum_output)
            # curr_res = np.sum(np.abs(y_true - y_pred))
            curr_res = np.sum(y_true == y_pred)
            curr_weights = np.concatenate(([self.B], self.W))
            if curr_res > self.best_res:
                print('updating weights')
                self.best_res = curr_res
                self.best_weights = curr_weights

        # 3. we already have an answer in best weights
        self.w = self.best_weights

    def predict(self, X):
        sum_output = X @ self.w[1:] + self.w[0]
        y_pred = self.act_func(sum_output)

        return y_pred


def visualize(X, labels_true, labels_pred, w):
    unique_labels = np.unique(labels_true)
    unique_colors = dict([(l, c) for l, c in zip(unique_labels, [[0.8, 0., 0.], [0., 0., 0.8]])])
    plt.figure(figsize=(9, 9))

    if w[1] == 0:
        plt.plot([X[:, 0].min(), X[:, 0].max()], w[0] / w[2])
    elif w[2] == 0:
        plt.plot(w[0] / w[1], [X[:, 1].min(), X[:, 1].max()])
    else:
        mins, maxs = X.min(axis=0), X.max(axis=0)
        pts = [[mins[0], -mins[0] * w[1] / w[2] - w[0] / w[2]],
               [maxs[0], -maxs[0] * w[1] / w[2] - w[0] / w[2]],
               [-mins[1] * w[2] / w[1] - w[0] / w[1], mins[1]],
               [-maxs[1] * w[2] / w[1] - w[0] / w[1], maxs[1]]]
        pts = [(x, y) for x, y in pts if mins[0] <= x <= maxs[0] and mins[1] <= y <= maxs[1]]
        x, y = list(zip(*pts))
        plt.plot(x, y, c=(0.75, 0.75, 0.75), linestyle="--")

    colors_inner = [unique_colors[l] for l in labels_true]
    colors_outer = [unique_colors[l] for l in labels_pred]
    plt.scatter(X[:, 0], X[:, 1], c=colors_inner, edgecolors=colors_outer)
    plt.show()


def code_col(mat, j1, j2):
    mat = mat * 255
    res_col = np.sum(mat[:, j1:j2])
    res_all = np.sum(mat)

    return res_col / res_all


def code_row(mat, i1, i2):
    mat = mat * 255
    res_row = np.sum(mat[i1:i2, :])
    res_all = np.sum(mat)

    return res_row / res_all


def transform_images(images):
    res = np.zeros((images.shape[0], 2))
    for ind, mat in enumerate(images):
        res[ind, 0] = code_col(mat, 4, 5 + 1)
        res[ind, 1] = code_row(mat, 0, 1 + 1)
    return res


def get_digits(train_image_ids, test_image_ids):
    data = datasets.load_digits()
    images_train, labels_train = data.images[train_image_ids['ImageId'].values], data.target[
        train_image_ids['ImageId'].values]
    images_test, labels_test = data.images[test_image_ids['ImageId'].values], data.target[
        test_image_ids['ImageId'].values]
    return images_train, labels_train, images_test, labels_test


def get_digits_by_mask(images, labels, y0, y1):
    mask = np.logical_or(labels == y0, labels == y1)
    labels = labels[mask]
    images = images[mask]
    images /= np.max(images)
    return images, labels


def get_x_y(train_image_ids, test_image_ids, y0=1, y1=5):
    X_train, y_train, X_test, y_test = get_digits(train_image_ids, test_image_ids)

    X_train, y_train = get_digits_by_mask(X_train, y_train, y0, y1)
    X_train = transform_images(X_train)

    X_test, y_test = get_digits_by_mask(X_test, y_test, y0, y1)
    X_test = transform_images(X_test)

    return X_train, y_train, X_test, y_test


def get_x_y_(y0=1, y1=5):
    data = datasets.load_digits()
    images, labels = data.images, data.target
    mask = np.logical_or(labels == y0, labels == y1)
    labels = labels[mask]
    images = images[mask]
    images /= np.max(images)
    X = transform_images(images)

    X_train, X_test, y_train, y_test = train_test_split(X, labels, train_size=0.8, shuffle=False)
    return X_train, X_test, y_train, y_test


def get_pics(label):
    data = datasets.load_digits()
    images, labels = data.images, data.target
    # mask = np.logical_or(labels == y0, labels == y1)
    mask = (labels == label)
    labels = labels[mask]
    images = images[mask]
    images /= np.max(images)
    X = images
    return X


def num_of_correct_preds(y_true, y_pred):
    delta = np.abs(y_true - y_pred)
    return np.sum(delta)
