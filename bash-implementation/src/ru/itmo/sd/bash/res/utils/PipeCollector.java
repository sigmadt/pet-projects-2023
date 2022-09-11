package ru.itmo.sd.bash.res.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class PipeCollector implements Collector<Token, List<List<Token>>, List<List<Token>>> {

    @Override
    public Supplier<List<List<Token>>> supplier() {
        return () -> new ArrayList<>(List.of(new ArrayList<>()));
    }

    @Override
    public BiConsumer<List<List<Token>>, Token> accumulator() {
        return (
                (lists, token) -> {
                    if (token.getType() != Token.Type.PIPE_SYMBOL) {
                        var currPlace = lists.size() - 1;
                        lists.get(currPlace).add(token);
                    } else {
                        lists.add(new ArrayList<>());
                    }
                });
    }

    @Override
    public BinaryOperator<List<List<Token>>> combiner() {
        return ((firstList, secondList) -> {
            firstList.get(firstList.size() - 1).addAll(secondList.get(0));

            secondList.remove(0);

            firstList.addAll(secondList);
            return firstList;
        });

    }

    @Override
    public Function<List<List<Token>>, List<List<Token>>> finisher() {
        return Function.identity();
    }

    @Override
    public Set<Characteristics> characteristics() {
        return Set.of(Characteristics.IDENTITY_FINISH);
    }
}