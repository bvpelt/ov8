# Git

## Remove a branch

### Local remove a branch

```bash

# This command will only delete the branch if it has been fully merged into its upstream branch (usually main or develop).
git branch -d <branch-name>

# Use the uppercase -D to force the deletion of the branch, even if it has unmerged changes.
git branch -D <branch-name>
```

### Remote remove a branch

```bash

git push origin --delete <branch-name>

# Alternative use the colon prefix
git push origin :<branch-name>
```

Other collaborators will need to run git fetch --prune or git prune to clean up their local tracking branches.