import numpy as np
from numpy.linalg import inv


class NormalLR:
    def __init__(self, reg_coef=0):
        self.W = None
        self.C = reg_coef
        self.n_rows = None
        self.n_cols = None
        self.model_fitted = False

    def fit(self, X, y):
        self.n_rows, self.n_cols = X.shape[0], X.shape[1]
        Z = X.T @ X + self.C * np.identity(self.n_cols)
        self.W = inv(Z) @ X.T @ y
        self.model_fitted = True

    def predict(self, X):
        if self.model_fitted:
            y_pred = np.dot(X, self.W)
            return y_pred
        else:
            print("It looks like you haven't fit your Awesome LR model. Use .fit method.")
