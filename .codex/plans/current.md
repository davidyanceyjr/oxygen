# Slice 30D1 — Appearance Control Semantics

**Status:** planned
**Cycle ID:** `2026-09-13-slice-30d1-appearance-control-semantics`
**Prerequisite:** Gate 30C3 — Alert Accessibility Evidence and Documentation Sync

## Slice objective

Give the existing Settings / Appearance Theme, Layout, Effects, and Contrast
controls deliberate group and choice semantics through the real `OxygenApp`
path. Preserve the existing preference transaction state, confirmed-write
behavior, failed-write retry behavior, preference independence, and no-refetch
contracts.

This is the next specified candidate selected after Gate 30C3 closure; no 30D1
production or test work has started in this plan.

## Acceptance boundary

The slice is ready only when each existing Appearance group and choice exposes
meaningful label, role, selected/disabled, pending, success, and read/write
failure semantics without color-only meaning; retry and Back actions are named,
operable, and at least 48dp; and confirmed-write, failed-target retention,
retry, preference independence, and no-refetch behavior remain observable.

## Intended files and evidence

- Review and, if required, edit the existing Appearance preference state and
  control components under `app/src/main/kotlin/`.
- Extend the existing preference transaction JVM tests and add no more than five
  named connected semantic/action cases through `OxygenApp`.
- Run the focused JVM/connected evidence selected for the changed boundary,
  then applicable Android compile, unit, assembly, and `git diff --check` checks.

## Out of scope

Large-font/RTL layout matrix, new preferences, automatic system contrast, Full
effects, icon packs, persistence formats, Home/alert behavior, and TalkBack
service traversal.

**Next action:** discover the existing Appearance preference state and control
semantics, then establish the focused red/baseline evidence boundary.
