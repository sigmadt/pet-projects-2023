#include <experimental/type_traits>
#include <tuple>
#include <utility>
#include <iostream>


namespace sigmadt {
    template <typename F, typename... X>
    class curried;

    namespace insider {
        // для проверки что нет аргументов
        template<class S>
        using no_args_decl = decltype(std::declval<S>()());

        template<class U>
        constexpr bool no_args = std::experimental::is_detected_v<no_args_decl, U>;

        // вызываемость
        template<typename U, typename... Y>
        constexpr bool is_callable = std::is_invocable_v<std::decay_t<U>, Y...>;

        // основная функция для каррирования в рекурсии
        template <typename F, typename... X, std::enable_if_t<is_callable<F, X...>, bool> = true>
        constexpr auto curry_recursive(F&& f, std::tuple<X...>&& xs) {
            return std::apply(std::forward<F>(f), std::move(xs));
        }

        template <typename F, typename... X, std::enable_if_t<!is_callable<F, X...>, bool> = true>
        constexpr auto curry_recursive(F&& f, std::tuple<X...>&& xs) {
            return curried(std::forward<F>(f), std::move(xs));
        }

    }

    // структурка чтобы все аккуратно запаковывать
    template <typename F, typename... X>
    class curried final {
    private:
        F func;
        std::tuple<X...> xs;

    public:
        constexpr curried(F f) : func(std::move(f)) {}

        constexpr curried(F f, std::tuple<X...> zs)
                : func(std::move(f)), xs(std::move(zs)) {}

        // call operators для отложенных вычислений
        constexpr auto operator()() && {
            return insider::curry_recursive(std::move(func), std::move(xs));
        }

        template <typename Y>
        constexpr auto operator()(Y&& y) && {
            return insider::curry_recursive(std::move(func),
                                            std::tuple_cat(std::move(xs),
                                                           std::forward_as_tuple(std::forward<Y>(y))));

        }

        template <typename Y, typename... Z>
        constexpr auto operator()(Y&& y, Z&&... zs) && {
            return std::move(*this)(std::forward<Y>(y))(std::forward<Z>(zs)...);
        }

        template <typename... Z>
        constexpr auto operator()(Z&&... zs) const & {
            return curried(*this)(std::forward<Z>(zs)...);
        }

        template <typename... Z>
        constexpr auto operator()(Z&&... zs) & {
            return insider::curry_recursive(this->func,
                                            std::tuple_cat(this->xs, std::forward_as_tuple(std::forward<Z>(zs)...))
                                            );
        }
    };

    // ну и наконец сама функция каррирования (c++14 moment)
    template <typename F, std::enable_if_t<insider::no_args<F>, bool> = true>
    constexpr auto curry(F&& func) {
        return std::forward<F>(func);
    }

    template <typename F, std::enable_if_t<!insider::no_args<F>, bool> = true>
    constexpr auto curry(F&& func) {
        return insider::curry_recursive(std::forward<F>(func), {});
    }

    template <typename F, typename Y, typename... X>
    constexpr auto curry(F&& func, Y&& y, X&&... xs) {
        return curry(std::forward<F>(func))(std::forward<Y>(y), std::forward<X>(xs)...);
    }
}

