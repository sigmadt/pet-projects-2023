# Imports
import numpy as np
import pandas as pd
import random
import matplotlib.pyplot as plt
import matplotlib

from tqdm import tqdm

from sklearn.model_selection import train_test_split
from sklearn import metrics
from sklearn.metrics import roc_auc_score, accuracy_score, roc_curve, auc

import decision_tree_impl

from collections import defaultdict


class DecisionTreeClassifier:
    def __init__(self, criterion, max_depth=3, min_samples_leaf=15):
        # root, tree, max depth and min samples
        self.root = None
        self.tree = []
        self.max_depth = max_depth
        self.min_samples_leaf = min_samples_leaf
        self.crit = criterion

    def fit(self, X, y):
        # function for searching best split
        def best_split(df):
            df_for_splits = pd.DataFrame()
            #             print(df.columns)
            for col in df.drop('y', axis=1).columns:
                # i decided to pick 7 values for iterating for speeding up the algorithm
                # of course we can tune this parameter or even put it in init
                vals_for_split = np.linspace(df[col].min(), df[col].max(), num=7)

                feat_split_df = pd.DataFrame()
                feat_split_df['vals_for_split'] = vals_for_split
                feat_split_df['feature'] = col
                # calculating gain
                feat_split_df['gain_by_criterion'] = feat_split_df['vals_for_split']. \
                    apply(lambda v: decision_tree_impl.gain(df, col, v, self.crit))
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
                node = decision_tree_impl.DecisionTreeLeaf(df['y'])
                return node
            # finding best option
            feat_for_split, value_for_split = best_split(df)

            # we are 1 step closer to finish
            max_depth -= 1

            df_left = df.loc[df[feat_for_split] < value_for_split]
            df_right = df.loc[df[feat_for_split] >= value_for_split]

            # recursive calls for each child
            node = decision_tree_impl.DecisionTreeNode(feat_for_split, value_for_split,
                                    split_rec(tree, df_left, max_depth, min_samples_leaf),
                                    split_rec(tree, df_right, max_depth, min_samples_leaf), df['y'])
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
            if isinstance(root, decision_tree_impl.DecisionTreeNode):
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

    def predict_cut_tree(self, X, new_depth):
        proba_df = pd.DataFrame(X.index, columns=['ind'])
        pred_dict = {}

        def sift_down(X, root, pred_dict, new_depth, level=0):
            #             print(level, '-----<<')

            if isinstance(root, decision_tree_impl.DecisionTreeLeaf) or new_depth == level:
                for i in X.index:
                    pred_dict[i] = root.y
            else:

                X_left = X.loc[X[root.split_dim] < root.split_value]
                sift_down(X_left, root.left, pred_dict, new_depth, level + 1)

                X_right = X.loc[X[root.split_dim] >= root.split_value]
                sift_down(X_right, root.right, pred_dict, new_depth, level + 1)

        # initial call for new data and root
        sift_down(X, self.root, pred_dict, new_depth)

        proba_df['pred'] = proba_df['ind'].map(pred_dict)

        return proba_df


# Tree related
def tree_depth(tree_root):
    if isinstance(tree_root, decision_tree_impl.DecisionTreeNode):
        return max(tree_depth(tree_root.left), tree_depth(tree_root.right)) + 1
    else:
        return 1


def draw_tree_rec(tree_root, x_left, x_right, y):
    x_center = (x_right - x_left) / 2 + x_left
    if isinstance(tree_root, decision_tree_impl.DecisionTreeNode):
        x_center = (x_right - x_left) / 2 + x_left
        x = draw_tree_rec(tree_root.left, x_left, x_center, y - 1)
        plt.plot((x_center, x), (y - 0.1, y - 0.9), c=(0, 0, 0))
        x = draw_tree_rec(tree_root.right, x_center, x_right, y - 1)
        plt.plot((x_center, x), (y - 0.1, y - 0.9), c=(0, 0, 0))
        plt.text(x_center, y, "x[%i] < %f" % (int(tree_root.split_dim), tree_root.split_value),
                 horizontalalignment='center')
    else:
        plt.text(x_center, y, str(tree_root.y),
                 horizontalalignment='center')
    return x_center


def draw_tree(tree_root, save_path='./tree1.png'):
    td = tree_depth(tree_root)
    plt.figure(figsize=(0.33 * 2 ** td, 2 * td))
    plt.xlim(-1, 1)
    plt.ylim(0.95, td + 0.05)
    plt.axis('off')
    draw_tree_rec(tree_root, -1, 1, td)
    plt.tight_layout()
    if save_path is not None:
        plt.savefig(save_path)
    plt.show()


# RANDOM FOREST
def major_voting_for_list(list_with_preds, idx):
    res_preds_list, res_preds_probs = [], []
    n_obs = len(idx)
    n_preds = len(list_with_preds)
    for i in range(n_obs):
        s = 0
        for tree in list_with_preds:
            s += tree[i]

        avg = s / n_preds
        res_preds_probs.append(avg)
        if avg > 0.5:
            res_preds_list.append(1)
        else:
            res_preds_list.append(0)

    return res_preds_list, res_preds_probs


class RandomForestClassifier:
    def __init__(self, criterion=decision_tree_impl.gini, max_depth=3, min_samples_leaf=20, max_features=4, n_estimators=10):
        self.forest = []

        self.max_depth = max_depth
        self.min_samples = min_samples_leaf
        self.max_features = max_features
        self.n = n_estimators
        self.crit = criterion
        self.missing_subsets = defaultdict(list)

        self.train_df_feat = None
        self.train_labels = None


    def fit(self, X, y):
        self.train_df_feat, self.train_labels = X.copy(), y.copy()
        for i in range(self.n):
            number_of_est = i

            clf = DecisionTreeClassifier(criterion=self.crit, max_depth=self.max_depth,
                                         min_samples_leaf=self.min_samples)

            # generating random features for learning
            rand_feat_idx = list(
                np.random.choice([i for i in range(len(X.columns))], replace=False, size=self.max_features))
            rand_obs_idx = list(np.random.choice(X.index, replace=True, size=len(X.index)))

            X_feat = X.iloc[rand_obs_idx, rand_feat_idx]
            y_feat = y.iloc[rand_obs_idx, :]

            clf.fit(X_feat, y_feat)
            print('Tree number {} is fitting on {} observations and features are from: {}'. \
                  format(i, len(rand_obs_idx), list(X_feat.columns)))
            print('---***---')
            print('{} trees to go! almost there'.format(self.n - i))
            self.forest.append(clf)

    def predict(self, X):
        prediction_list = []
        for tree in self.forest:
            prediction_list.append(tree.predict(X))

        return major_voting_for_list(prediction_list, X.index)

    def predict_less_trees_depth(self, X, n_trees, new_depth):
        print('*' * 20 + '-' * 5 + '_' * 7 + '-' * 5 + '*' * 20)
        print(f'Cutting tree using parametrs: {n_trees} estimators and {new_depth} is a new depth')
        print('*' * 20 + '-' * 5 + '_' * 7 + '-' * 5 + '*' * 20)

        new_cutted_trees_pred_list = []

        rand_trees_idx = np.random.choice([x for x in range(self.n)], replace=False, size=n_trees)

        for i in range(len(rand_trees_idx)):
            j = rand_trees_idx[i]
            print(f'I chose {j}-th random tree')
            print('_' * 10)
            curr_preds = self.forest[j].predict_cut_tree(X, new_depth)['pred']
            new_cutted_trees_pred_list.append(curr_preds)
            print(f'{len(curr_preds)} is length of j-th tree preds')
            print('-' * 10)

        return major_voting_for_list(new_cutted_trees_pred_list, X.index)

    def get_feature_importance(self):
        # get oob errors
        def get_err_oob(X, y, forest, missing_s, T, change_feat=None):
            X, y = X.copy(), y.copy()
            if change_feat:
                print(f'!!-- changing values of feature named {change_feat}')
                prev_vals = X[change_feat]

                new_vals = X[change_feat].sample(frac=1).values
                X[change_feat] = new_vals
                print('values were changed!')

            out_of_bag_preds, visited_idx = [], []
            for ind in X.index:
                if len(missing_s[ind]) > T:
                    visited_idx.append(ind)
                    print(visited_idx[:6])

                    out_of_bag_preds_tree = []
                    for ind_tree in missing_s[ind]:
                        curr_tree = forest[ind_tree]

                        out_of_bag_preds_tree.append(curr_tree.predict(X.iloc[[ind]]).values[0])

                    s = np.sum(out_of_bag_preds_tree) / len(out_of_bag_preds_tree)
                    res_pred_after_vote = int(s > 0.5)

                    out_of_bag_preds.append(res_pred_after_vote)

            y_true_cut = y.iloc[visited_idx]
            print(f'y_true_cut {len(y_true_cut)}, out_of_bag_preds {len(out_of_bag_preds)}')

            err_oob = np.sum(np.array(y_true_cut) == np.array(out_of_bag_preds)) / len(y_true_cut)

            return err_oob

        # PERMUTATION
        feat_imp_dict = {}

        for j in self.train_df_feat.columns:
            err_oob_true = get_err_oob(self.train_df_feat, self.train_labels,
                                       self.forest, self.missing_subsets, T=self.n // 2)

            err_oob_j = get_err_oob(self.train_df_feat, self.train_labels, self.forest,
                                    self.missing_subsets, T=self.n // 2, change_feat=j)

            print(f'err_oob {err_oob_true}')
            print(f'err_oob_j {err_oob_j}')

            feat_imp_dict[j] = err_oob_j - err_oob_true

        return feat_imp_dict


# Grid Search
def grid_search(n_est, max_d, clf, test_df, test_labels):
    all_scores = []

    for est in n_est:
        for d in max_d:
            curr_pred = clf.predict_less_trees_depth(
                test_df.reset_index(drop=True),
                n_trees=est,
                new_depth=d)

            score = roc_auc_score(np.array(test_labels), curr_pred[1])

            all_scores.append({'scores': score,
                               'n_est': est,
                               'max_d': d})

    all_scores = sorted(all_scores, key=lambda el: el['scores'], reverse=True)

    return all_scores


# My implementations

def my_train_test_split(X, y, ratio=0.6, seed=17):
    np.random.seed(seed)

    # finding the size of train and test
    train_size = int(X.shape[0] * ratio)

    train_idx = np.random.choice(X.index, size=train_size, replace=False)
    test_idx = [x for x in X.index if x not in train_idx]

    return X.iloc[train_idx], X.iloc[test_idx], y.iloc[train_idx], y.iloc[test_idx]


# Get Metrics and graphs

def get_precision_recall_accuracy_f(y_pred, y_true, main_class, method):
    def get_stats(y_pred, y_true, value):
        TP = FP = FN = TN = 0
        for i in range(y_pred.shape[0]):
            # print(i)
            if y_pred[i] == value and y_true[i] == value:
                TP += 1
            elif y_pred[i] != value and y_true[i] == value:
                FN += 1
            elif y_pred[i] == value and y_true[i] != value:
                FP += 1
            else:
                TN += 1

        precision = TP / (TP + FP)
        recall = TP / (TP + FN)

        accuracy = (TP + TN) / (TP + FP + FN + TN)
        f1 = 2 * precision * recall / (precision + recall)
        return precision, recall, accuracy, f1

    prec, rec, acc, f1 = get_stats(y_pred, y_true, main_class)

    if method == 'print':
        print('Precision: {:.3f}\nRecall: {:.3f}\nAccuracy: {:.3f}\nF1-score: {:.3f}'.format(prec, rec, acc, f1))
    elif method == 'get':
        return prec, rec, acc, f1


#     print(y_pred_label, y_pred_prob, type(y_pred_label))

def print_roc_auc(y_true, y_pred):
    y_pred_label, y_pred_prob = y_pred[0], y_pred[1]
    #     y_pred, y_true = np.array(y_pred), np.array(y_true)
    y_true, y_pred_label, y_pred_prob = np.array(y_true), np.array(y_pred_label), np.array(y_pred_prob)

    roc_auc = dict()
    fpr, tpr, _ = roc_curve(y_true, y_pred_prob)
    roc_auc[1] = auc(fpr, tpr)

    #     plt.figure(figsize=(8, 8))
    plt.rcParams["figure.figsize"] = (8, 8)
    plt.figure()
    lw = 2
    plt.plot(fpr, tpr, color='blue',
             lw=lw, label='ROC curve (area = %0.2f)' % roc_auc[1])
    plt.plot([0, 1], [0, 1], color='navy', lw=lw, linestyle='--')
    plt.xlim([0.0, 1.0])
    plt.ylim([0.0, 1.05])
    plt.xlabel('False Positive Rate')
    plt.ylabel('True Positive Rate')
    plt.title('Receiver operating characteristic example')
    plt.legend(loc="lower right")
    plt.show()

    print(f'Your Rocking awesome AUC score is {roc_auc_score(y_true, y_pred_prob)}')
    print(f'Your Rocking awesome Accuracy score is {accuracy_score(y_true, y_pred_label)}')
