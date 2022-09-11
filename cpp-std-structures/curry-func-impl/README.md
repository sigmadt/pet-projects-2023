# Currying in C++ is not a miracle

If you like functional paradigm you will find this implementation interesting...

## Usage:
### 1. Define function and pass args
```cpp
int id(int elem) {
    return elem;
}

auto new_id = curry(id);
auto result = new_id(13); // 13

// or simply
auto simple_res = curry(id)(19) // 19;
        
int math_opers(int x, int y, int z) {
    return x + 2 * y - 100 * z + 1243;
}

auto math_res = math_opers(1)(2)(3); // 948

// but this is a function
auto curried_math_func = math_opers(1)(2);
auto new_math_res = curried_math_func(3); // 948
```

### 2. Via functors
```cpp
auto mult = [](int x, int y) { return x * y; };
auto mult_res = mult(30)(11); // 330
```
