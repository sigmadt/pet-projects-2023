# Navigation page

Pet-projects are sorted  and filtered by _programming languages/tools._
# C++
## 1. Dynamic dictionary
Dictionary with keys as strings and values of any object that can be copied. 

Serialization for `std::vector`, `std::map` 
and its combinations is supported. 

Read/write JSON is available for this structure as well.

You can check out my implementation in 
[`dynamic-dict`](https://github.com/sigmadt/pet-projects-2023/tree/master/dynamic-dict) folder.

## 2. Various std:: like structures
### 2.1 Constexpr optional
`std::optional`, but you can work with it in compile-time.

Check my implementation [`here`](https://github.com/sigmadt/pet-projects-2023/tree/master/cpp-std-structures/constexpr-optional-impl).
### 2.2 Currying in C++ is not a joke?
Currying is the technique that represents breaking  down a function 
that takes multiple arguments into a series of functions that each take only one argument.

See my implementation [`here`](https://github.com/sigmadt/pet-projects-2023/tree/master/cpp-std-structures/curry-func-impl).
### 2.3 Tuple
`std::tuple` implementation using variadic templates, SFINAE and multiple inheritance.

[`Implementation`](https://github.com/sigmadt/pet-projects-2023/tree/master/cpp-std-structures/tuple-impl).

# Java
## 1. Shell
Default UNIX shell implementation. Available commands are: `cat`, `echo`, `grep`, `pwd`, `wc`. 
Pipes and several flags for commands are supported as well.

For mor information click [`here`](https://github.com/sigmadt/pet-projects-2023/tree/master/bash-implementation).

## 2. Rogue-like game
Fully functional 2-D game using `AWT` and `Swing`.

See project [`source code`](https://github.com/sigmadt/pet-projects-2023/tree/master/rogue-like).

## 3. Version Constrol System
VCS à la `git` named `smit`. You can add files, commit changes, traverse commit tree using `reset` and `checkout`, create branches, merge branches and more! 

It is simple to launch on your local machine, colorized display is supported.

See project [`source code`](https://github.com/sigmadt/pet-projects-2023/tree/master/vcs-smit). 

## 4. Torrent Implementation
Torrent protocol implementation using TCP. `Tracker` and `Client` send each other messages using sockets in Java. Messages are de/serialized using [`Protobuf`](https://protobuf.dev/).

See project [`source code`](https://github.com/sigmadt/pet-projects-2023/tree/master/torrentino). 


# Python
## 1. Machine learning algorithms
### 1.1 Decision tree
Simple decision tree [`implementation`](https://github.com/sigmadt/pet-projects-2023/blob/master/ml-algorithms/decision-tree/decision_tree_impl.py).

For more details and in-depth explanation you can read my [`article`](https://github.com/sigmadt/pet-projects-2023/blob/master/ml-algorithms/decision-tree/article/decision_tree_article.ipynb) about decision tree.

### 1.2 Random Forest
See implementation of random forest [`here`](https://github.com/sigmadt/pet-projects-2023/blob/master/ml-algorithms/random-forest/article/module/random_forest_impl.py).

Also, I wrote an [`article`](https://github.com/sigmadt/pet-projects-2023/blob/master/ml-algorithms/random-forest/article/random_forest_article.ipynb) about this implementation. 
Comparison with `XGBoost`, `CatBoost` and `LightGBM` is provided as well.

### 1.3 Linear Regression
I wrote an [`article`](https://github.com/sigmadt/pet-projects-2023/blob/master/ml-algorithms/linear-regression/article/linear_regression_article.ipynb) for this topic.

Method was applied on housing market data.

### 1.4 Single-layer Neural Network
Alternatively this method is called `Perceptron`.
Check out [`article`](https://github.com/sigmadt/pet-projects-2023/blob/master/ml-algorithms/linear-regression/article/linear_regression_article.ipynb) 
and [`source code`](https://github.com/sigmadt/pet-projects-2023/blob/master/ml-algorithms/neural-network/article/module/perceptron.py) for this method.

## 2. Telegram Bot
Telegram bot which purpose is to help with preparing for algorithm interviews in big tech companies.

See project [`source code`](https://github.com/sigmadt/pet-projects-2023/tree/master/interview-assistant). 


# 3. Web service
Simple web service which can define level of toxicity of a certain message.

See project [`source code`](https://github.com/sigmadt/pet-projects-2023/tree/master/is-toxic-web-service).	


# Big Data
[Big data projects](https://github.com/sigmadt/pet-projects-2023/tree/master/big-data)
