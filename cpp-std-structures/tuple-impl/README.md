# std::tuple implementation

## Usage:

### 1. Make tuple and size
```cpp
using sigmadt;

tuple<int, float, std::string> first_tuple = make_tuple<int, float, std::string>(17, 21.01, "cpp");

// compile-time size
static_assert(tuple_size<tuple<int, float, std::string>>::value == 3);

```

### 2. Get value by position
```cpp
auto num_17      = get<0>(first_tuple) // integer, value is 17
auto float_21_01 = get<1>(first_tuple) // float, value is 21.01
auto str_cpp     = get<2>(first_tuple) // string, value is "cpp"         

```

### 3. Get value by type
```cpp
auto num_17      = get<int>(first_tuple) // integer, value is 17
auto float_21_01 = get<float>(first_tuple) // float, value is 21.01
auto str_cpp     = get<std::string>(first_tuple) // string, value is "cpp"    
```

