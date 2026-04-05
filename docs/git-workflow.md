# Git Workflow

## Default Flow

日常开发流程建议采用 `develop + feature branch` 模式：

1. `main` 保持随时可发布
2. `develop` 作为开发集成分支
3. 业务需求从 `develop` 切出 `feature/*`
4. 缺陷修复从 `develop` 切出 `fix/*`
5. 线上问题从 `main` 切出 `hotfix/*`

## Merge Policy

- `feature/* -> develop`
- `fix/* -> develop`
- `hotfix/* -> main`，然后再回合 `develop`
- `develop -> main` 仅在形成可发布版本时进行

## Commit Convention

- `feat:`
- `fix:`
- `refactor:`
- `docs:`
- `chore:`
- `test:`

## PR Checklist

- 代码与文档是否同步
- 是否包含数据库迁移
- 是否影响接口协议
- 是否影响权限、库存、闸机、人工放行或风控
- 是否说明本地验证方式

## Suggested GitHub Settings

### Branch protection for `main`

- Require a pull request before merging
- Require approvals: `1`
- Dismiss stale pull request approvals when new commits are pushed
- Require status checks to pass before merging
- Restrict direct pushes
- Require linear history

### Branch protection for `develop`

- Require a pull request before merging
- Require approvals: `1`
- Restrict force push
- Require status checks when CI is available

