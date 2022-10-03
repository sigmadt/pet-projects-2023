import numpy as np
import pandas as pd

from tqdm import tqdm


def gini(x):
    probs = x.value_counts(normalize=True) ** 2
    return 1 - probs.sum()


def entropy(x):
    probs = x.value_counts(normalize=True)
    p_logp = probs * (np.log2(probs))
    return -p_logp.sum()


def gain(df, feat, val, criterion):
    if not df.shape[0]:
        return 0
    left = df.loc[df[feat] < val]
    right = df.loc[df[feat] >= val]
    return criterion(df['y']) - (left.shape[0] / df.shape[0] * criterion(left['y'])
                                 + right.shape[0] / df.shape[0] * criterion(right['y']))


class DecisionTreeLeaf:
    def __init__(self, array):
        self.y = None
        self.v = array.value_counts(normalize=True)
        # check length
        if len(self.v.index):
            self.y = self.v.index[0]
        self.d = {key: val for key, val in zip(self.v.index, self.v.values)}


class DecisionTreeNode:
    def __init__(self, split_dim, split_value, left, right):
        self.split_dim = split_dim
        self.split_value = split_value
        self.left = left
        self.right = right


class DecisionTreeClassifier:
    def __init__(self, criterion, max_depth=3, min_samples_leaf=15):
        # root, tree, max depth and min samples
        self.root = None
        self.tree = []
        self.max_depth = max_depth
        self.min_samples_leaf = min_samples_leaf

    def fit(self, X, y):
        # function for searching best split
        def best_split(df):
            df_for_splits = pd.DataFrame()
            for col in tqdm(df.drop('y', axis=1).columns):
                # i decided to pick 7 values for iterating for speeding up the algorithm
                # of course we can tune this parametr or even put it in init
                vals_for_split = np.linspace(df[col].min(), df[col].max(), num=7)

                feat_split_df = pd.DataFrame()
                feat_split_df['vals_for_split'] = vals_for_split
                feat_split_df['feature'] = col
                # calculating gain
                feat_split_df['gain_by_criterion'] = feat_split_df['vals_for_split']. \
                    apply(lambda v: gain(df, col, v, gini))
                # merging
                df_for_splits = pd.concat([df_for_splits, feat_split_df])

            # sort by max gain
            df_for_splits.sort_values('gain_by_criterion', ascending=False, inplace=True)
            # best row is the first row in sorted dataframe
            best_row = df_for_splits.iloc[0]

            # finding feature and value for best split
            feature_name, splitting_val = best_row['feature'], best_row['vals_for_split']

            return feature_name, splitting_val

        # this function makes recursive calls for each node while the given root is not a Leaf
        def split_rec(tree, df, max_depth, min_samples_leaf):
            # conditions for assigning the node as lead
            if max_depth == 0 or df.shape[0] < min_samples_leaf or df['y'].value_counts(normalize=True).max() == 1:
                node = DecisionTreeLeaf(df['y'])
                return node
            # finding best option
            feat_for_split, value_for_split = best_split(df)

            # we are 1 step closer to finish
            max_depth -= 1

            df_left = df.loc[df[feat_for_split] < value_for_split]
            df_right = df.loc[df[feat_for_split] >= value_for_split]

            # recursive calls for each child
            node = DecisionTreeNode(feat_for_split, value_for_split,
                                    split_rec(tree, df_left, max_depth, min_samples_leaf),
                                    split_rec(tree, df_right, max_depth, min_samples_leaf))
            tree.append(node)
            return node

        # building a tree
        df = pd.concat([X, y], axis=1)
        self.root = split_rec(self.tree, df, self.max_depth, self.min_samples_leaf)

    def predict_proba(self, X):
        # df for storing probabilities and its labels
        proba_df = pd.DataFrame(X.index, columns=['ind'])
        proba_dict, pred_dict = {}, {}

        # we are iterating the tree while reaching the leaf
        def sift_down(X, root, proba_dict, pred_dict):
            # if it is not leaf please make a recursive call
            if isinstance(root, DecisionTreeNode):
                X_left = X.loc[X[root.split_dim] < root.split_value]
                sift_down(X_left, root.left, proba_dict, pred_dict)

                X_right = X.loc[X[root.split_dim] >= root.split_value]
                sift_down(X_right, root.right, proba_dict, pred_dict)
            else:
                # if it is a leaf store label and prob
                for i in X.index:
                    proba_dict[i] = root.d
                    pred_dict[i] = root.y

        # initial call for new data and root
        sift_down(X, self.root, proba_dict, pred_dict)
        proba_df['pair'] = proba_df['ind'].map(proba_dict)
        proba_df['pred'] = proba_df['ind'].map(pred_dict)

        return proba_df

    def predict(self, X):
        proba = self.predict_proba(X)
        return proba['pred']
