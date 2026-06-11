---
description: Push current changes to git repository
---

Follow these steps to commit and push the current changes to the Git repository:

1. **Check status**
Check the current state of the working directory and staging area.
// turbo
```bash
git status
```

2. **Stage all changes**
Add all modified and untracked files to the staging area.
// turbo
```bash
git add .
```

3. **Review changes (Optional)**
If you need to review the changes before committing to generate a message, you can run `git diff --cached`.

4. **Commit changes**
Generate a concise and descriptive commit message explaining the changes made. Run the commit command.
```bash
git commit -m "<your_generated_commit_message>"
```

5. **Push to the remote repository**
Push the committed changes to the current branch on the remote repository.
```bash
git push origin HEAD
```
