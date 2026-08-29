# Oxygen UI Development Workflow

This document defines the canonical screenshot-development workflow for visual
Compose work in Oxygen. The installed app running on the emulator through the
real presentation path is the authoritative rendering check. Compose previews
may later accelerate component development, but previews are not a replacement
for emulator verification.

This workflow is for bounded UI presentation work. It does not permit changing
weather semantics, provider behavior, repository behavior, navigation,
presentation values, or accessibility behavior just to achieve a visual result.

## Fast Visual Loop

Use this loop while visually developing a component or bounded visual concern:

```text
establish visual objective
-> capture baseline
-> inspect relevant Compose code
-> make one bounded UI change
-> compile/build as needed
-> install and launch
-> capture screenshot
-> inspect rendered result
-> revise
-> repeat until visual objective is satisfied
```

The purpose is rapid visual convergence. Do not require the full project
verification suite after every spacing, typography, shape, or composition
adjustment. Compilation confirms only that the code builds; it is not evidence
that the visual change succeeded.

The standard emulator capture command is:

```sh
scripts/capture-screen.sh
scripts/capture-screen.sh .codex/test-artifacts/<cycle-id>/home-after.png
```

## Verification Loop

Use this loop after the rendered visual result has converged:

```text
run focused tests/checks
-> install current build
-> render authoritative application state
-> capture final screenshot evidence
-> verify relevant layout/accessibility constraints
-> run broader regression checks appropriate to the slice
-> preserve final evidence
-> commit
```

Final screenshots are presentation evidence when they come from the installed
application through the real presentation path. Save required visual evidence
under `.codex/test-artifacts/<cycle-id>/` and record the project-local paths in
the normal Codex cycle files.

## UI Slice Acceptance Pattern

Future visual tasks should specify acceptance in three categories.

### Functional Invariants

Functional invariants are behavior and meaning that must not change during a
visual-only task. Examples:

- weather values
- condition identity
- source/provenance
- navigation behavior
- refresh behavior
- accessibility semantics
- provider/repository behavior

### Visual Objectives

Visual objectives describe the intended presentation outcome. They are goals,
not exact pixel specifications unless a task explicitly provides measurements.
Examples:

- establish clear visual hierarchy
- make one element visually dominant
- reduce visual weight of tertiary information
- differentiate semantic component types
- improve spacing rhythm and information grouping

### Layout Constraints

Layout constraints define where the result must remain usable. Examples:

- supported compact width
- long text/location names
- large font scaling
- no clipping or sibling overlap
- no unintended horizontal scrolling
- usable with effects disabled
