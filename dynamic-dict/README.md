# Project Dynadictum

Dynamic dictionary for C++ in which you can store any type that can be copied.

### Usage:
#### 1. Initializer list
```cpp
dynadictum new_dict =
{
    {"key_1", 7},
    {"key_2", false},
    {"key_3_as_dict",
        {
            {"key_3_1", string("sss")},
            {"key_3_2", 342.333}
        }
    }
};
```

#### 2. Put & Get
```cpp
dynadictum first_dict;

first_dict.put("key_1", 1243);
auto num_1243 = first_dict.get<int>("key_1"); // 1243

```

