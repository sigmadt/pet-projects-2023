# Version Control System SMIT

VCS implementation named `Smit` with Command Line Interface.

## Usage

* `init` -- initialize smit repository
* `add <files>` -- add files
* `rm <files>` -- remove files. Acts similar to `git rm --cached <files>`  
* `status` -- display staged/untracked files. Colorized output is supported
* `commit -m <message>` -- commit changes with specified message
* `reset <to_revision>` -- reset to revision. Acts similar to `git reset --hard`
* `log [from_revision]` -- display commit history for current branch. Colorized output is supported
* `checkout <option>`
    * Options for `option`:
        * `commit hash` -- hash of specific commit
        * `master` -- restore initial state for current branch
        * `HEAD~N`, where `N` -- number of parent in commit tree
* `checkout -- <files>` -- rest changes in files
* `branch-create <name>` -- create branch
* `branch-remove <branch>` -- remove specified branch
* `show-branches` -- display all available branches. Colorized output is supported
* `merge <branch>` -- merch `<branch>` into current branch

_Note._ Argument `<inner>` is required, `[smth]` is optional.

## Launch Instructions
Run shell script `run.sh` with specified arguments, for example:

```shell
./run.sh init
./run.sh add Readme.md
./run.sh status
```

