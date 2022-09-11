#pragma once
#include <iostream>
#include <string>
#include <exception>
#include <map>
#include <utility>
#include <vector>



namespace utils {
#define FWD(arg) std::forward<decltype(arg)>(arg)


    class empty_any_t_exception : public std::exception
    {};
    class invalid_type_exception : public std::exception
    {};
    class no_key_exception : public std::exception
    {};


    class any_t {
    private:
        class base {
        public:
            [[nodiscard]] virtual const std::type_info& type() const = 0;
            [[nodiscard]] virtual base* get_copy() const = 0;

            [[nodiscard]] virtual bool is_const() const = 0;
            virtual bool operator==(const base& wrapper) const = 0;

            virtual ~base() = default;
        };

        template <class S>
        class derived : public base {
        public:
            derived(const S& el) : content(el) {}

            derived(S&& el) noexcept : content(FWD(el)) {}

            [[nodiscard]] base* get_copy() const override {
                return new derived(content);
            }

            [[nodiscard]] bool is_const() const override {
                return std::is_const_v<S>;
            }

            [[nodiscard]] const std::type_info& type() const override {
                return typeid(content);
            }

            bool operator==(const base& wrapper) const override {
                if (wrapper.type() == typeid(S)) {
                    return dynamic_cast<const derived<S>&>(wrapper).content == content;
                }
                return false;
            }

        public:
            S content;
        };

    public:
        any_t() : inner(nullptr) {}

        any_t(const any_t& o) {
            if (o.inner == nullptr) {
                inner = nullptr;
            } else {
                inner = o.inner->get_copy();
            }
        }

        any_t(any_t&& o)  noexcept {
            inner = o.inner;
            o.inner = nullptr;
        }

        template<class E>
        any_t(const E& el) {
            inner = new derived<E>(el);
        }

        template<class E>
        any_t(E&& el) {
            inner = new derived<std::decay_t<E>>(FWD(el));
        }

        ~any_t() {
            delete inner;
            inner = nullptr;
        }

        any_t& operator=(any_t&& o) noexcept {
            if (!inner) {
                inner = o.inner;
                o.inner = nullptr;
            } else {
                delete inner;
                inner = o.inner;
                o.inner = nullptr;
            }

            return *this;
        }


        bool operator==(const any_t& o) const {
            return *o.inner == *inner;
        }

        [[nodiscard]] const std::type_info& inner_type() const {
            if (inner == nullptr) {
                throw empty_any_t_exception();
            }
            else {
                return inner->type();
            }
        }

        [[nodiscard]] bool is_empty() const {
            return inner == nullptr;
        }


        // any_t methods
    public:
        template <class S>
        S cast() const {
            if (inner == nullptr) {
                throw empty_any_t_exception();
            }

            if (inner->type() != typeid(S)) {
                throw invalid_type_exception();
            }

            return dynamic_cast<derived<S>*>(inner)->content;
        }

        template <class S>
        S& get_inner() {
            if (inner == nullptr) {
                throw empty_any_t_exception();
            }

            if (inner->type() != typeid(S) || inner->is_const() != std::is_const_v<S>) {
                throw invalid_type_exception();
            }

            return dynamic_cast<derived<S>&>(*inner).content;
        }

        template <class S>
        const S& get_inner() const {
            if (inner == nullptr) {
                throw empty_any_t_exception();
            }

            if (inner->type() != typeid(S) || inner->is_const() != std::is_const_v<S>) {
                throw invalid_type_exception();
            }

            return dynamic_cast<derived<S>&>(*inner).content;
        }

        template <class S>
        S* get_ptr() {
            if (inner == nullptr) {
                return nullptr;
            }

            if (inner->type() != typeid(S) || inner->is_const() != std::is_const_v<S>) {
                return nullptr;
            }

            return &(dynamic_cast<derived<S>*>(inner)->content);
        }

        template <class S>
        const S* get_ptr() const {
            if (inner == nullptr) {
                return nullptr;
            }

            if (inner->type() != typeid(S) || inner->is_const() != std::is_const_v<S>) {
                return nullptr;
            }

            return &(dynamic_cast<derived<S>*>(inner)->content);
        }

    private:
        base* inner;

    };


    class dynadictum {
    public:
        dynadictum() = default;

        dynadictum(const dynadictum& other) {
            storage = other.storage;
        }

        dynadictum(dynadictum&& other)  noexcept {
            storage = std::move(other.storage);
        }

        dynadictum& operator=(const dynadictum& other) = default;

        dynadictum& operator=(dynadictum&& other)  noexcept {
            storage = std::move(other.storage);
            return *this;
        }

        ~dynadictum() {
            if (!empty())
                clear();
        }

    public:
        bool operator==(const dynadictum& other) const {
            return storage == other.storage;
        }

        bool operator!=(const dynadictum& other) const {
            return !(*this == other);
        }

        [[nodiscard]] bool contains(const std::string& given_key) const {
            return storage.find(given_key) != storage.end();
        }

        bool remove(const std::string& given_key) {
            return storage.erase(given_key);
        }

        [[nodiscard]] size_t size() const {
            return storage.size();
        }

        [[nodiscard]] bool empty() const {
            return storage.empty();
        }

        void clear() {
            storage.clear();
        }

        template<class E>
        bool put(const std::string& key, E&& val) {
            auto [ptr, success] = storage.emplace(key, FWD(val));
            return success;
        }


        template <class S>
        S& get(const std::string& given_key)  {
            if (contains(given_key)) {
                return storage.at(given_key).get_inner<S>();
            } else {
                throw no_key_exception();
            }
        }

        template <class S>
        const S& get(const std::string& given_key) const  {
            if (contains(given_key)) {
                return storage.at(given_key).get_inner<S>();
            } else {
                throw no_key_exception();
            }
        }

        template <class S>
        S* get_ptr(const std::string& given_key)  {
            if (contains(given_key)) {
                return storage.at(given_key).get_ptr<S>();
            }
            return nullptr;
        }

        template <class S>
        const S* get_ptr(const std::string& given_key) const  {
            if (contains(given_key)) {
                return storage.at(given_key).get_ptr<S>();
            }
            return nullptr;
        }

        [[nodiscard]] bool is_dict(const std::string& given_key) const {
            if (contains(given_key)) {
                auto val = storage.at(given_key);
                return val.inner_type() == typeid(dynadictum);
            }
            return false;
        }

        [[nodiscard]] auto begin() const {
            return storage.begin();
        }

        [[nodiscard]] auto end() const {
            return storage.end();
        }


    private:
        class helper_list {
        public:
            template <class E>
            helper_list(const std::string& given_key, const E& el) {
                some_key = given_key;
                content = el;
            }

            helper_list(const std::string& given_key, const dynadictum& given_dict) {
                some_key = given_key;
                content = given_dict;
            }

            [[nodiscard]] std::string get_key() const {
                return some_key;
            }

            [[nodiscard]] any_t get_content() const {
                return content;
            }

        private:
            std::string some_key;
            any_t content;

        };


    private:
        std::map<std::string, any_t> storage;

    public:
        dynadictum(const std::initializer_list<helper_list>& given_data) {
            for (auto& h : given_data) {
                put(h.get_key(), h.get_content());
            }
        }


    };


}












