Clone a GitHub repository to a specified folder and checkout to a specific commit

Ask the user for:
1. Repository URL (e.g., https://github.com/user/repo.git)
2. Target folder name
3. Specific commit hash (optional, if not provided, clone the default branch)

Execute the following steps:

1. Clone the repository:
   ```bash
   git clone <repository-url> <target-folder>
   ```

2. If a specific commit is provided, checkout to that commit:
   ```bash
   cd <target-folder>
   git checkout <commit-hash>
   ```

3. Add the cloned folder to .gitignore to prevent it from being tracked:
   ```bash
   echo "<target-folder>/" >> .gitignore
   ```

Example usage:
- Repository: https://github.com/liyupi/yu-ai-code-mother.git
- Target folder: EcomAIGen-fronted
- Commit: 695d8096d536dc3e9b5b6dd63214b96f7e303d33

Make sure to:
1. Verify the repository URL is valid
2. Check if the target folder already exists
3. Confirm network connectivity to GitHub
4. Update .gitignore to exclude the cloned project
