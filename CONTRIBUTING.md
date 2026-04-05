# Contributing

## Branch Strategy

- `main`：生产可发布分支，只接受经过评审和验证的合并。
- `develop`：日常集成分支，功能开发默认从该分支切出。
- `feature/<name>`：功能分支，例如 `feature/member-crud`。
- `fix/<name>`：缺陷修复分支，例如 `fix/material-stock-update`。
- `hotfix/<name>`：线上紧急修复，优先从 `main` 切出并回合到 `develop`。

## Local Workflow

1. 同步最新分支：

```bash
git checkout develop
git pull
```

2. 创建功能分支：

```bash
git checkout -b feature/your-change
```

3. 开发完成后先自检：

```bash
git status
git diff --stat
```

4. 提交时使用清晰的前缀：

- `feat:` 新功能
- `fix:` 缺陷修复
- `refactor:` 重构
- `docs:` 文档
- `chore:` 工程或依赖调整
- `test:` 测试相关

示例：

```bash
git commit -m "feat: add material issue workflow"
```

5. 推送分支并发起 Pull Request：

```bash
git push -u origin feature/your-change
```

## Pull Request Rules

- PR 必须聚焦单一主题，避免混入不相关改动。
- PR 标题应与最终 squash commit 一致。
- 提交 PR 前至少说明：
  - 改动目标
  - 影响范围
  - 测试情况
  - 是否涉及数据库、接口或配置变更
- 对于数据库迁移，必须在描述中点明迁移文件名。
- 涉及 API 变化时，必须同步更新文档。

## Review Expectations

- 优先关注行为回归、数据一致性、权限与风控逻辑。
- 闸机、库存、人工放行、支付相关改动必须重点审查异常路径。
- 审核通过前不要直接向 `main` 推送。

## Recommended Branch Protection

- `main`：
  - 禁止直接推送
  - 至少 1 个 PR 审核通过
  - 必须通过 CI
  - 使用 squash merge
- `develop`：
  - 禁止强推
  - 至少 1 个 PR 审核通过
  - 建议通过基础 CI

