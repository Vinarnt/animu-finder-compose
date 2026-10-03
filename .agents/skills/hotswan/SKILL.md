---
name: hotswan
description: >-
  Use when hot-reloading a running Compose app with Compose HotSwan: editing
  composables, ViewModels, mappers or resources and applying them to the live
  app, or driving the app through the hotswan MCP tools (status, reload,
  screenshot, semantic tree, device logs). Covers what applies in place, what
  restarts the app with state loss, edits that are silently skipped, and the
  reload workflow. Not for general Compose authoring — use the compose skill.
---

# HotSwan Agent Skill

This project uses Compose HotSwan for hot reload. Save a Kotlin file and the change
is applied to the already-running app. The app does not restart, and navigation
state, scroll position and remember{} values survive.

HotSwan 2.x runs an interpreter inside the app, so it is not limited to swapping
function bodies. Adding and removing composables, changing branching, wrapping or
unwrapping layout, and replacing an entire screen all apply in place.

It reloads Android, the iOS simulator and Compose Desktop. One save updates every
target that is currently running.

## Applies in place, with state preserved

- Any function body, composable or not: text, colors, modifiers, layout, control
  flow, ViewModel methods, mappers, utilities
- Structural edits: adding, removing and re-nesting composable calls, new if/when
  branches, wrapping a subtree in a new container, rewriting a whole screen
- New top-level functions, classes, objects, enums and sealed classes
- New properties on a data class
- Extension functions and vararg functions
- Edits spread across several files in one save
- Values of existing resources (strings.xml, colors.xml, dimens.xml)

## Restarts the app automatically, so state is lost

- Changing a class's superclass, or adding or removing an interface it implements,
  when that class already existed in the previous build. Brand-new classes and
  deleted classes never cause this. HotSwan detects it while building and
  relaunches the app for you.

## Needs a full build

- A new resource identifier, such as a new R.string or R.drawable entry
- Edits to build.gradle.kts or a version catalog

## Edits that quietly do nothing

There is no error for either of these, so recognise them yourself:

- inline functions are expanded at their call sites, so there is no unit left to
  replace. Editing one has no effect until the next real build.
- The body of a named suspend fun stays native. Editing inside a LaunchedEffect,
  produceState or scope.launch block does reload. Editing suspend fun loadUser()
  itself does not.

## Rough edges to expect

- A changed function signature is not checked before it is applied. If the screen
  looks wrong or blank right after such an edit, save again: the next reload
  reports the incompatibility and restarts the app.
- Structural edits are applied a file at a time. If a re-nest changes the shape of
  a composable that is currently on screen, the whole file is held together, so an
  untouched composable in the same file can be affected too.
- Reordering sibling calls usually works, but a large reorder can disturb the slot
  table enough that the result does not come out right. Check the screen after one.

## How to work

- Make one focused edit at a time. Small changes reload faster, and when something
  looks off they are far easier to attribute.
- Put new composables wherever they belong. There is no same-file restriction, so a
  new composable and its caller can live in different files.
- Save the file. HotSwan picks the change up on its own and there is no command to run.
- If something looks wrong after a reload, a normal Run always returns you to a
  known good state.

## MCP tools

You have HotSwan MCP tools for hot reloading a running Compose app on Android, the
iOS simulator or Compose Desktop. Use them to edit code and then see, on the real
screen, what your edit did.

HotSwan 2.x applies structural changes in place, so do not restrict yourself to
editing values inside a function body. Rewriting a screen is a normal edit.

### Session

- hotswan_get_status() - targets attached, app running, watcher state.
- hotswan_start() - start watching files. Required before the first reload.
- hotswan_build_and_install() - full Gradle build, install and start the watcher.
- hotswan_list_devices() / hotswan_select_devices(...) - choose targets.
- hotswan_get_connected_devices() - what HotSwan would reload right now.

### Reload

- hotswan_reload(filePaths) - apply your edits. Read its verdict carefully.
- hotswan_revert_change(filePaths) - restore files to git HEAD and reload.
- hotswan_get_reload_health() - whether the last reload degraded silently.
- hotswan_get_dispatch_stats() - interpreter telemetry.

### See the screen

- hotswan_take_screenshot() - capture the current screen.
- hotswan_list_snapshots() - which recorded snapshot ids are actually diffable.
- hotswan_screen_delta(snapshotId) - did the screen change across that reload.
- hotswan_start_snapshot() / hotswan_stop_snapshot() - snapshot capture mode.

### Read the UI

- hotswan_get_semantic_tree() - on-screen elements with stable nodeIds and bounds.
- hotswan_get_hierarchy() - the composition slot table. Android only. Desktop
  answers an empty tree instead of refusing, so 0 groups there means "not
  available", not "nothing is composed".

### Drive the app (Android only, the other targets refuse by name)

- hotswan_click(nodeId), hotswan_long_click(nodeId), hotswan_type_text(...),
  hotswan_scroll(...), hotswan_scroll_to_index(...)

Node ids come from hotswan_get_semantic_tree. HotSwan re-resolves a node's bounds
at the moment of the action, so an id stays usable after a reload moves things.

### Theme and color

- hotswan_explore_palette(...) - generate theme variants and apply them live.
- hotswan_show_palette_grid(), hotswan_palette_status(),
  hotswan_apply_palette_variant(...)

### Preview and history

- hotswan_run_preview(...) - run one @Preview in isolation. Android only.
- hotswan_capture_previews(...) - screenshot previews in bulk.
- hotswan_select_variant(...), hotswan_get_decision_history(),
  hotswan_clear_decisions()
- hotswan_get_logs() - HotSwan's own compile and reload log, for when a RELOAD failed.

### Read the device log

- hotswan_get_device_logs(target?, count?, filter?) - your app's own log, where stack
  traces live. Use it when the app crashed or misbehaved AFTER a reload landed.
  Android reads logcat scoped to the app's pid, and widens to a device-wide read
  when the process is gone, which is exactly the case after a crash. The iOS
  simulator reads a time window. Compose Desktop is refused by name, so read the
  console you launched it from.

### Workflow

1. hotswan_get_status(), then hotswan_start().
2. Drive the app to the screen you are about to edit: hotswan_get_semantic_tree()
   then hotswan_click(nodeId). Reloading onto the right screen is what lets you
   see the change.
3. Edit the files.
4. hotswan_reload([...files]).
5. Read the verdict, then verify: hotswan_screen_delta(snapshotId) using the id the
   reload returned, or hotswan_take_screenshot().
6. Repeat from 3.

### Rules

- Call hotswan_start() before the first reload, and edit files BEFORE calling
  hotswan_reload, not after.
- health=unknown means HotSwan measured NOTHING for that call. It is not failure.
  Never revert an edit on the strength of it. Screenshot, or call
  hotswan_get_reload_health(), and find out what is actually on screen.
- When hotswan_reload returns a snapshotId, use it. That is the only id for that
  reload. hotswan_list_snapshots() finds ids from earlier ones.
- A before/after pair is captured over adb only, so on a Desktop-only or
  iOS-simulator session no snapshot is diffable. Take a screenshot instead.
- Screenshot a finished change, not every intermediate edit.
- If an edit seems to do nothing at all, check whether you edited an inline
  function or the body of a named suspend fun. Both are silently skipped until the
  next real build. Editing inside LaunchedEffect, produceState or scope.launch
  does reload.
- If the screen looks wrong or blank right after you changed a function signature,
  reload again. The second reload reports the incompatibility and restarts the app.
- Split the two log tools by question. Reload did not apply: hotswan_get_logs().
  Reload applied and then the app crashed or went wrong: hotswan_get_device_logs().
- A device-log leg reporting zero lines means the read worked and nothing matched.
  It is NOT evidence the app is fine. Widen or drop the filter before concluding.
- If the app is stuck, hotswan_build_and_install() gives you a clean baseline.
