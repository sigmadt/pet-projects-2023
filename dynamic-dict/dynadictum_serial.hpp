#pragma once
#include <vector>
#include "dynadictum.hpp"


namespace utils {

    template <class S>
    struct insider {
        S write_func(const S& el) {
            return el;
        }

        S read_func(const any_t& el) {
            return el.cast<S>();
        }
    };

    template <class S>
    struct insider<std::vector<S>> {
        dynadictum write_func(const std::vector<S> given_vec) {
            dynadictum d;

            for (size_t ind = 0; ind < given_vec.size(); ++ind) {
                d.put(std::to_string(ind), insider<S>{}.write_func(given_vec[ind]));
            }

            return d;
        }

        std::vector<S> read_func(const any_t& el) {
            std::vector<S> res_vec;
            auto inner_dict = el.cast<dynadictum>();

            res_vec.resize(inner_dict.size());


            for (auto& p : inner_dict) {
                size_t curr_ind = std::stoi(p.first);
                res_vec[curr_ind] = insider<S>{}.read_func(p.second);
            }

            return res_vec;
        }


        std::vector<S> read_func(const dynadictum& d) {
            std::vector<S> res_vec;

            res_vec.resize(d.size());


            for (auto& p : d) {
                size_t curr_ind = std::stoi(p.first);
                res_vec[curr_ind] = insider<S>{}.read_func(p.second);
            }

            return res_vec;
        }
    };

    template <class S>
    struct insider<std::map<std::string, S>> {
        dynadictum write_func(const std::map<std::string, S>& given_map) {
            dynadictum d;

            for (const auto& [key, value] : given_map) {
                auto wrap_val = insider<S>{}.write_func(value);
                d.put(key, wrap_val);
            }

            return d;
        }

        std::map<std::string, S> read_func(const dynadictum& d) {
            std::map<std::string, S> res_map;

            for (auto& p : d) {
                res_map.insert_or_assign(p.first, insider<S>{}.read_func(p.second));
            }

            return res_map;
        }

        std::map<std::string, S> read_func(const any_t& el) {
            std::map<std::string, S> res_map;

            auto inner_dict = el.cast<dynadictum>();
            for (auto& p : inner_dict) {
                res_map.insert_or_assign(p.first, insider<S>{}.read_func(p.second));
            }

            return res_map;
        }
    };

    template <class S>
    void write(dynadictum& d, const S& elem) {
        d = insider<S>{}.write_func(elem);
    }

    template <class S>
    void read(const dynadictum& d, S& elem) {
        elem = insider<S>{}.read_func(d);
    }


}


