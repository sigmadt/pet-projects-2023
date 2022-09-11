#pragma once
#include "json.hpp"
#include "dynadictum.hpp"
#include <iostream>
#include <cmath>


namespace utils {

    class type_is_not_serial : public std::exception { };
    class type_is_not_deserial : public std::exception { };

    template <class E>
    bool equals(const std::type_info& curr) {
        return curr == typeid(E);
    }

    struct json_insider {
        dynadictum loader(const nlohmann::json& j) {
            if (j.is_null()) {
                return {};
            }

            dynadictum res_dict;

            for (const auto& elem : j.items()) {
                const auto& key = elem.key();
                auto value = elem.value();
                if (value.is_string()) {
                    auto put_val = value.get<std::string>();
                    res_dict.put(key, put_val);
                }
                else if (value.is_boolean()) {
                    auto put_val = value.get<bool>();
                    res_dict.put(key, put_val);
                }
                else if (value.is_number_integer()) {
                    auto put_val = value.get<int>();
                    res_dict.put(key, put_val);
                }
                else if (value.is_number_float()) {
                    auto put_val = value.get<double>();
                    res_dict.put(key, put_val);
                }
                else if (value.is_object()) {
                    auto new_dict = loader(value);
                    res_dict.put(key, new_dict);
                }
                else {
                    throw type_is_not_deserial();
                }
            }

            return res_dict;
        }

        nlohmann::json saver(const dict_t& dict) {
            nlohmann::json res_j;

            for (auto& [key, value] : dict) {
                const std::type_info& curr_type = value.inner_type();

                if (equals<bool>(curr_type)) {
                    res_j[key] = value.get_inner<bool>();
                }
                else if (equals<dynadictum>(curr_type)) {
                    res_j[key] = saver(value.get_inner<dynadictum>());
                }
                else if (equals<unsigned short>(curr_type)) {
                    res_j[key] = value.get_inner<unsigned short>();
                }
                else if (equals<std::string>(curr_type)) {
                    res_j[key] = value.get_inner<std::string>();
                }
                else if (equals<short>(curr_type)) {
                    res_j[key] = value.get_inner<short>();
                }
                else if (equals<unsigned int>(curr_type)) {
                    res_j[key] = value.get_inner<unsigned int>();
                }
                else if (equals<int>(curr_type)) {
                    res_j[key] = value.get_inner<int>();
                }
                else if (equals<unsigned long>(curr_type)) {
                    res_j[key] = value.get_inner<unsigned long>();
                }
                else if (equals<long>(curr_type)) {
                    res_j[key] = value.get_inner<long>();
                }
                else if (equals<unsigned char>(curr_type)) {
                    res_j[key] = value.get_inner<unsigned char>();
                }
                else if (equals<char>(curr_type)) {
                    res_j[key] = value.get_inner<char>();
                }
                else if (equals<float>(curr_type)) {
                    res_j[key] = value.get_inner<float>();
                }
                else if (equals<double>(curr_type)) {
                    res_j[key] = value.get_inner<double>();
                }
                else {
                    throw type_is_not_serial();
                }
            }

            return res_j;
        }
    };



    inline bool load_from_json(std::istream& is, dynadictum& dict) {
        try {
            nlohmann::json json_ = nlohmann::json::parse(is);
            dict = json_insider{}.loader(json_);
        } catch(...) {
            return false;
        }
        return true;

    }

    inline void save_to_json(std::ostream& os, const dynadictum& dict) {
        nlohmann::json json_ = json_insider{}.saver(dict);
        os << json_;
    }

}