#include <iostream>
#include <cstddef>
#include <functional>


namespace sigmadt {
#define FWD(arg) std::forward<decltype(arg)>(arg)

    template <class S>
    using no_ref = std::remove_reference_t<S>;

    template<size_t N, class Head, class... Tail>
    struct get_nth_type {
        using type = typename get_nth_type<N - 1, Tail...>::type;
    };

    template<class Head, class... Tail>
    struct get_nth_type<0, Head, Tail...> {
        using type = Head;
    };

    template <class Type>
    struct type_holder {
        Type value;
        type_holder() {}
        type_holder(Type&& el) : value(FWD(el)) {}
    };


    template <size_t N, class S>
    class base_holder {
    public:
        base_holder() { }
        base_holder(const S& el) : content(el) {}

        base_holder(S&& el) noexcept : content(std::move(el)) {}

        base_holder(const base_holder& other) : content(other.content) {}
        base_holder(base_holder&& other)  noexcept : content(FWD(other.content)) {}

        const bool empty = false;

        base_holder& operator=(S&& el) {
            content = std::move(el);
            return *this;
        }

        base_holder& operator=(const S& el) {
            content = el;
            return *this;
        }

        base_holder& operator=(base_holder&& other) noexcept (std::is_nothrow_move_assignable_v<S>) {
            content = std::move(other.content);
            return *this;
        }

        base_holder& operator=(const base_holder& other) {
            *this = base_holder<N, S>(other.content);
            return *this;
        }

        S& get() { return content; }
        S const& get() const { return content; }
    private:
        S content;

    };

    template <size_t N>
    class base_holder<N, void> {
        const bool empty = true;
    };

    template <size_t N, class... Tail>
    class recursive_tuple {
    public:
        void tuple_assign(const recursive_tuple<N, Tail...>&) {}
        void tuple_move(recursive_tuple<N, Tail...>&&) {}
    };

    template <size_t N, class Head, class... Tail>
    class recursive_tuple<N, Head, Tail...> : public base_holder<N, no_ref<Head>>,
                                              public recursive_tuple<N + 1, Tail...>
    {
    public:
        static constexpr base_holder<N, Head> & get_head(recursive_tuple& b) { return b; }
        static constexpr const base_holder<N, Head> & get_head(const recursive_tuple& b) { return b; }

        static constexpr recursive_tuple<N+1, Tail...> & get_tail(recursive_tuple& t) { return t; }
        static constexpr const recursive_tuple<N+1, Tail...> & get_tail(const recursive_tuple& t) { return t; }

//        static constexpr auto& get_tail() {}

        recursive_tuple() = default;

        recursive_tuple(const Head& first, const Tail&... rest) :
                base_holder<N, no_ref<Head>>(first),
                recursive_tuple<N + 1, Tail...>(rest...) {}

        recursive_tuple(Head&& first, Tail&&... rest) :
                base_holder<N, no_ref<Head>>(FWD(first)),
                recursive_tuple<N + 1, Tail...>(FWD(rest)...) {}

        template<typename... Tail_>
        void tuple_assign(const recursive_tuple<N, Tail_...>& other) {
            get_head(*this) = recursive_tuple::get_head(other);
            get_tail(*this).tuple_assign(recursive_tuple<N, Tail_...>::get_tail(other));
        }


        template<typename... Tail_>
        void tuple_move(recursive_tuple<N, Tail_...>&& other) {
            get_head(*this) = std::move(recursive_tuple::get_head(other));
            get_tail(*this).tuple_move( std::forward<recursive_tuple<N+1, Tail...>>(recursive_tuple<N, Tail_...>::get_tail(other)));
        }

//        template<std::enable_if_t<std::is_copy_assignable_v<Head>, bool> = true>
        recursive_tuple(const recursive_tuple& other) {
            this->tuple_assign(other);
        }

        recursive_tuple(recursive_tuple&& other)  noexcept(std::is_nothrow_move_constructible_v<Head>) {
            this->tuple_move(FWD(other));
        }

        recursive_tuple& operator=(const recursive_tuple& other) {
            this->tuple_assign(other);
            return *this;
        }

        recursive_tuple& operator=(recursive_tuple&& other) {
            this->tuple_move(FWD(other));
            return *this;
        }


    };


    template <typename... Tail>
    void void_func(Tail&&...) {
    }


    template <class... Xs>
    class tuple_impl {
        const bool empty = true;
    };



    template<class Head, class... Tail>
    class tuple_impl<Head, Tail...> : public recursive_tuple<0, Head, Tail...> {
    public:
        tuple_impl() = default;
        explicit tuple_impl(const Head& val, const Tail&... rest) : recursive_tuple<0, Head, Tail...>(val, rest...) {}
        explicit tuple_impl(Head&& val, Tail&&... rest) : recursive_tuple<0, Head, Tail...>(FWD(val), FWD(rest)...) {}

        template<class OtherHead, class... OtherTail>
        explicit tuple_impl(tuple_impl<OtherHead, OtherTail...>&& other) noexcept : recursive_tuple<0, OtherHead, OtherTail...>(FWD(other)) {}

        template<class OtherHead, class... OtherTail>
        explicit tuple_impl(const tuple_impl<OtherHead, OtherTail...>& other) : recursive_tuple<0, OtherHead, OtherTail...>(other) {}


        template<class... Rest>
        friend bool operator==(tuple_impl<Rest...> &t1, tuple_impl<Rest...> &t2);

        template<typename Head_, typename... Tail_>
        tuple_impl& operator=(const tuple_impl<Head_, Tail_...>& other) {
            recursive_tuple<0, Head_, Tail_...>::operator=(other);
            return *this;
        }

        template<typename Head_, typename... Tail_>
        tuple_impl& operator=(tuple_impl<Head_, Tail_...>&& other) {
            recursive_tuple<0, Head_, Tail_...>::operator=(FWD(other));
            return *this;
        }
    };




    template<class... Xs>
    tuple_impl(Xs... xs)->tuple_impl<Xs...>;




    template<size_t N, class... Xs>
    auto& get(tuple_impl<Xs...> &t) {
        using holder = base_holder<N, typename get_nth_type<N, Xs...>::type>;
        return (static_cast<holder&>(t)).get();
    }

    template<size_t N, class... Xs>
    auto& get(const tuple_impl<Xs...> &t) {
        using holder = base_holder<N, typename get_nth_type<N, Xs...>::type>;
        return (static_cast<holder const &>(t)).get();
    }

    template <typename T, typename... Xs>
    constexpr size_t find_type() {
        constexpr size_t sz = sizeof...(Xs);
        constexpr bool found_arr[sz] = { std::is_same_v<T, Xs>... };

        size_t res = sz;

        for (size_t ind = 0; ind < sz; ++ind) {
            if (found_arr[ind]) {
                if (res < sz) {
                    return sz;
                }
                res = ind;
            }


        }
        return res;

    }

    template <typename T, typename... Xs>
    const T& get(const tuple_impl<Xs...> &t) {
        constexpr size_t idx = find_type<T, Xs...>();
        static_assert(idx < sizeof...(Xs), "type not found");
        return get<idx>(t);

    }




    template <class T>
    struct unwrap_refwrapper
    {
        using type = T;
    };

    template <class T>
    struct unwrap_refwrapper<std::reference_wrapper<T>>
    {
        using type = T&;
    };

    template <class T>
    using special_decay_t = typename unwrap_refwrapper<typename std::decay<T>::type>::type;

    template <class... Types>
    auto make_tuple(Types&&... args)
    {
        return tuple_impl<special_decay_t<Types>...>(std::forward<Types>(args)...);
    }


    template<size_t N, class... Xs>
    bool equals_helper(tuple_impl<Xs...> &t1, tuple_impl<Xs...> &t2) {
        if constexpr (N == 0) {
            return get<N>(t1) == get<N>(t2);
        }
        else {
            return get<N>(t1) == get<N>(t2) && equals_helper<N - 1>(t1, t2);
        }
    }

    template<class... Xs>
    bool operator==(tuple_impl<Xs...> &t1, tuple_impl<Xs...> &t2) {
        return equals_helper<sizeof...(Xs) - 1>(t1, t2);
    }

    template<class S>
    struct tuple_size {};

    template <class... Tail>
    struct tuple_size<tuple_impl<Tail...>> : std::integral_constant<size_t, sizeof...(Tail)> {};

    template <typename F, typename... Xs>
    auto call_args(F func, tuple_impl<Xs...> t) {
        return std::invoke(func, get<Xs>(FWD(t))...);
    }


}