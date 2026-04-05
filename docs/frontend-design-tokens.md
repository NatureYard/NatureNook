# Frontend Design Tokens

## Purpose

This document defines the shared semantic design tokens used across the three frontend surfaces in this repository:

- `frontend/customer-mini`
- `frontend/merchant-mobile`
- `frontend/admin-web`

It is the standalone reference for token naming, semantic meaning, ownership, and cross-surface mapping. The product UX spec remains the higher-level policy document; this file is the implementation-facing token contract.

## Token Principles

- Name tokens by semantic purpose, not by hue or by one-off component usage.
- Reuse an existing token before introducing a new one.
- Add new tokens at the surface entry point first, not in page-level files.
- Keep the same semantic meaning across all three surfaces, even if exact values differ slightly by platform.
- Treat states, surfaces, text hierarchy, and emphasis as separate concerns.

## Token Families

### Brand

| Token | Meaning | Typical use |
| --- | --- | --- |
| `brand-primary` | Primary emphasis color | Main CTA, selected navigation, strongest action |
| `brand-primary-strong` | Highlight companion to primary | CTA gradients, emphasized fill transitions |
| `brand-secondary` | Secondary brand emphasis | Workflow emphasis, supporting action, operational highlight |
| `brand-secondary-soft` | Softer companion to secondary | Hero gradients, supportive accent panels |

### Text

| Token | Meaning | Typical use |
| --- | --- | --- |
| `text-primary` | Highest-priority readable text | Page titles, key metrics, primary content |
| `text-secondary` | Supporting readable text | Descriptions, helper copy, summaries |
| `text-tertiary` | Low-emphasis supporting text | Labels, captions, placeholders, secondary hints |

### Surface

| Token | Meaning | Typical use |
| --- | --- | --- |
| `surface-base` | Global page background | App/page base canvas |
| `surface-card` | Standard elevated container | Cards, list items, white panels |
| `surface-overlay` | Soft translucent overlay surface | Hero status panels, floating summaries |
| `surface-muted` | Muted input/supporting surface | Inputs, inactive panels, neutral fill |
| `surface-soft` | Secondary neutral surface | Secondary buttons, low emphasis chips |
| `surface-highlight-*` | Highlighted contextual surface | Summary cards, context panels, emphasized cards |

### State

| Token | Meaning | Typical use |
| --- | --- | --- |
| `state-success-bg` / `state-success-fg` | Success or available state | Success chips, pass states, completed work |
| `state-warning-bg` / `state-warning-fg` | Warning or pending state | Pending actions, operational reminders |
| `state-error-bg` / `state-error-fg` | Error or blocked state | Failures, exceptions, interruptions |
| `state-info-bg` / `state-info-fg` | Informational guidance state | Process hints, secondary guidance |
| `state-risk-bg` / `state-risk-fg` | High-risk operational state | Manual release risk, escalations, audit-heavy actions |

### Border, Focus, Shadow

| Token | Meaning | Typical use |
| --- | --- | --- |
| `border-subtle` | Low-contrast border | Section dividers, weak outlines |
| `border-strong` | High-contrast border | Selected containers, important inputs |
| `border-input` | Standard form border | Inputs and selectors |
| `focus-ring` | Visible focus or selection ring | Focused inputs, selected cards, high-visibility affordances |
| `shadow-soft` | Shared low-weight elevation | Cards and panels with gentle elevation |

## Surface Entry Points

Define or update tokens in these files first:

- `frontend/customer-mini/app.wxss`
- `frontend/merchant-mobile/App.vue`
- `frontend/admin-web/src/style.css`

Page files may consume tokens, but should not be the first place a token is invented.

## Current Cross-Surface Baseline

| Semantic token | Customer mini | Merchant mobile | Admin web |
| --- | --- | --- | --- |
| `brand-primary` | `#ff6b4a` | `#ff6b4a` | `#ff6b4a` |
| `brand-primary-strong` | `#ff8a5c` | `#ff8a5c` | `#ff8a5c` |
| `brand-secondary` | `#2f8f7d` | `#2f8f7d` | `#2f8f7d` |
| `text-primary` | `#2f2a24` | `#2f2a24` | `#2f2a24` |
| `text-secondary` | `#7d6d60` | `#7d6d60` | `#7d6d60` |
| `surface-card` | `rgba(255, 255, 255, 0.94)` | `#ffffff` | `rgba(255, 255, 255, 0.92)` |
| `surface-base` | warm gradient base | `#fff9f2` | warm-neutral gradient base |
| `state-success` | mint semantic pair | green semantic pair | mint semantic pair |
| `state-warning` | amber semantic pair | amber semantic pair | reserved |
| `state-error` / `state-risk` | rose/orange guidance pair | red semantic pair | red semantic pair |

## Authoring Rules

- Do not introduce tokens like `green-1`, `orangeLight`, `bgCard2`, or `mainColor`.
- If a token is platform-specific but still semantic, extend the family name instead of inventing a new naming system.
Example: `surface-highlight-start`, `surface-highlight-success`, `state-risk-fg`.
- If a page repeats the same color rule in two or more places and the usage is semantic, lift it to the surface entry point.
- If a page uses a new color only once for a one-off illustration or decorative flourish, document why it should remain local.
- Decorative gradients must not become the only place where semantic meaning lives.

## Review Checklist

- Does the token name describe purpose instead of appearance?
- Would another engineer know when to use it without seeing the exact color?
- Is the same token meaning preserved across mini-program, merchant mobile, and admin web?
- Was the token added to the correct entry point before page-level usage?
- Does the new token avoid overlapping meaning with an existing one?

## Relationship To UX Spec

Policy and rationale remain in `docs/pet-park-system-dev-spec.md`.
This file is the implementation-oriented companion for frontend token usage and maintenance.
