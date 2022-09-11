# Constexpr std::optional
This implementation allows you to use optional in runtime and compile time as well.

## Usage:
### 1. Run-time
Pretty similar to `std::optional`:
```cpp
optional<int> const first_opt{17};
auto num_17 = first_opt.value();
first_opt.reset(); 

auto is_empty = !first_opt; // true
```

### 2. Compile-time
```cpp
constexpr optional<int> const second_opt{221};
constexpr auto num_221 = second_opt.value_or(17);

static_assert(num_221 == 221);
```