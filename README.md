
## How to run

windows
```
./run.bat
```

linux/mac
```
./run.sh
```

if already compiled
```
java -cp "out;lib\sqlite-jdbc-3.53.2.0.jar" com.classapp.Main
```




## Project structure

```
src/com/classapp/
  Main.java                  entry point
  Database.java              connection + CREATE TABLE statements (plumbing, not a DAO)
  User.java                  login/logout, createClass/joinClass, createGroup/joinGroup/leaveGroup, viewTasks
  Membership.java            the User <-> ClassRoom <-> Group join, keeps all three lists in sync
  ClassRoom.java              owns memberships/groups/tasks directly
  Group.java                 owns memberships/tasks directly, holds its ClassRoom reference
  Task.java                  abstract - editTask/deleteTask
  ClassTask.java              extends Task - updateUserStatus
  GroupTask.java               extends Task - updateGroupStatus
  UserTaskStatus.java          changeStatus (per user, per class task)
  GroupTaskStatus.java         changeStatus (one per group task)
  TaskStatus.java               enum: TODO / IN_PROGRESS / DONE
  gui/                        Swing windows (LoginFrame, MainFrame, ClassFrame, GroupFrame)
```
