# Shell project

This is my bash implementation. I did my best in order to make this shell work. Enjoy using this tool!

## Available commands

* [`cat`](src/ru/itmo/sd/bash/res/commands/CatCommand.java) – concatenate files or input stream if no files is given
    * Usage:  
      `cat [file]...`
* [`echo`](src/ru/itmo/sd/bash/res/commands/EchoCommand.java) – write arguments to the standard output
    * Usage:  
      `echo [string]...`
* [`pwd`](src/ru/itmo/sd/bash/res/commands/PwdCommand.java) – return working directory name
* [`wc`](src/ru/itmo/sd/bash/res/commands/WcCommand.java) – word, line, character, and byte count
    * Usage:  
      `wc [file]...`
* [`grep`](src/ru/itmo/sd/bash/res/commands/GrepCommand.java) – file pattern searcher
    * Usage:  
      `grep [option]... [pattern] [file]`
    * Options:  
      `-i`  perform case-insensitive matching  
      `-c`  only a count of selected lines is written to standard output  
      `-l`  only the names of files containing selected lines are written to standard output  
      `-w`  the expression is searched for as a word   
      `-A num`  print num lines of trailing context after each match  
## Launch instructions
Simply clone repository and run `./run.sh` script file from the root. Enjoy! 💲💲💲
