# Git Hooks

## Included Hooks

仓库内置了两个本地 Git hooks 模板，位于 `.githooks/`：

- `pre-commit`
- `commit-msg`

## Behavior

### `pre-commit`

- 检查暂存区中的空白问题
- 拦截冲突标记和格式错误

### `commit-msg`

要求提交信息遵循以下格式：

```text
<type>: <summary>
```

允许的 `type`：

- `feat`
- `fix`
- `refactor`
- `docs`
- `chore`
- `test`

示例：

```text
feat: add manual release submission flow
```

## Activation

执行以下命令启用仓库级 hooks：

```bash
git config core.hooksPath .githooks
chmod +x .githooks/pre-commit .githooks/commit-msg
```

## Notes

- hooks 只在本地生效，不替代 CI。
- CI 仍然负责分支合并前的基础验证。
