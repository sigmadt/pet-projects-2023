( numbers and math operations)
3 4 + .
12 12 / .
1360 23 - .
17 4 mod .

( string )
." regular forth string"

( constants )
false false and
true false =

( keywords and compile block )
: .less64 ( n -- n ) dup 64 > if ." Greater than 64!" else ." Less than 64!" then ;

( compile mod block )
: sqaure dup * ;