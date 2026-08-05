# ClassApp

A SQLite-backed Java Swing app, built on top of your existing POJOs, following a
strict layering: **View -> Controller -> Application -> DAO -> Database**.

## Why this layering

- **POJOs** (`User`, `ClassRoom`, `Group`, `Task`/`ClassTask`/`GroupTask`,
  `Membership`, `TaskStatus`, `UserTaskStatus`, `GroupTaskStatus`) stay exactly as
  given - plain data holders, no behavior, no DB awareness, untouched.
- **DAO** (`dao/`) is the only layer that talks SQL. One DAO per table/entity.
- **Application** (`app/Application.java`) is a single class holding every
  business process from the use case diagram: register, login, create/join a
  class, create/join/leave a group, create/edit/delete/track status for both
  class tasks and group tasks. It's the only layer that calls DAOs, and the only
  place business rules live (e.g. "a user belongs to at most one group per
  class" is enforced in `Application.joinGroup`, not in the GUI).
- **Controller** (`controller/`) is the only layer that talks to `Application`,
  and the only layer that decides which window opens next. Views never
  instantiate other views themselves.
- **View** (`gui/`) is purely presentational Swing. Every button just calls a
  method on its controller and waits to be told what to display.

```
src/main/java/com/classapp/
    User.java, ClassRoom.java, Group.java, Task.java,          <- your POJOs, unchanged
    ClassTask.java, GroupTask.java, Membership.java,
    TaskStatus.java, UserTaskStatus.java, GroupTaskStatus.java

    db/Database.java              - SQLite connection + schema creation

    dao/
        UserDAO, ClassRoomDAO, GroupDAO, ClassTaskDAO, GroupTaskDAO,
        MembershipDAO, UserTaskStatusDAO, GroupTaskStatusDAO   - persistence
        Rows.java                 - small read-only DTOs for joined/display data
                                     (see note below)

    app/
        Application.java          - every business process / use case in one place

    controller/
        LoginController           - controls LoginFrame; the app's entry point
        MainController            - controls MainFrame (the user's class list)
        ClassController           - controls ClassFrame (one class's members/groups/tasks)
        GroupController           - controls GroupFrame (one group's members/tasks)
        ValidationException       - thrown by Application on bad input/broken rules

    gui/
        LoginFrame, MainFrame, ClassFrame, GroupFrame   - the four windows
        UiKit                      - shared colors/fonts/buttons so all four match
        TaskDialog                 - shared "title + description" add/edit dialog

    Main.java                     - entry point: builds Application, starts LoginController
```

## How navigation works ("the controller always selects the next view")

Nothing in `gui/` ever does `new SomeOtherFrame(...)`. Instead:

1. A button click calls a method on the view's controller, e.g.
   `controller.onOpenClass(selectedClass)`.
2. The controller calls `Application` if needed, then decides what happens next.
   To open a new window it constructs the *next* controller and calls its
   `start()` - e.g. `MainController.onOpenClass` creates a `ClassController`
   and calls `.start()`, which is what actually builds and shows `ClassFrame`.
3. The controller keeps the one `View` reference it owns and pushes fresh data
   into it (`view.setClasses(...)`, `view.setTasks(...)`, `view.showError(...)`)
   after every `Application` call.

Login -> Main -> (open a class) -> Class -> (open a group) -> Group, and
Main/Class windows are left open behind the ones opened from them, matching
how you'd expect a desktop app to behave (you can have several classes/groups
open at once).

## Note on `dao/Rows.java`

A few of the given POJOs intentionally don't expose getters for their
reference fields (`Group` has no `getClassRoom()`, `Membership` has no getters
at all). That's fine for inserting data, but the DAO can't read those
relationships back out of a POJO to display in a table or list. `Rows.java`
defines small immutable `record`s (`GroupRow`, `MembershipRow`, etc.) built
directly from SQL `JOIN`s, used only for display. All inserts/updates still go
through your POJOs' own constructors/setters where those exist.

## About the visual design

I didn't have a reference image to match, so `gui/UiKit.java` applies a plain,
consistent style (color palette, spacing, rounded buttons) across all four
windows. If you have a specific mockup in mind, share it and I can adjust
`UiKit` and the four frames to match more closely - the controller/Application
layering underneath won't need to change.

## How to run

### Option A - Maven (needs internet access to Maven Central)

```
mvn clean package
java -jar target/classapp.jar
```

### Option B - No Maven

The sqlite-jdbc driver is already included in `lib/`.

```
./compile.sh
./run.sh
```
or on windows
```
./compile.bat
./run.bat
```

(On Windows: `javac -cp lib\sqlite-jdbc-3.36.0.3.jar -d out (dir /s /b src\main\java\*.java)`
then `java -cp "out;lib\sqlite-jdbc-3.36.0.3.jar" com.classapp.Main`.)

The database file `classapp.db` is created automatically on first run.

## Requirements

- JDK 17+
- (Option A only) Maven, with internet access to Maven Central
