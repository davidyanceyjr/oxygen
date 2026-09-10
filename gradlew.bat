@echo off
setlocal
set DIR=%~dp0
if exist "%DIR%gradle\wrapper\gradle-wrapper.jar" (
  if defined JAVA_HOME (set JAVA_EXE=%JAVA_HOME%\bin\java.exe) else (set JAVA_EXE=java.exe)
  "%JAVA_EXE%" -Dorg.gradle.appname=gradlew -classpath "%DIR%gradle\wrapper\gradle-wrapper.jar" org.gradle.wrapper.GradleWrapperMain %*
  exit /b %ERRORLEVEL%
)
echo gradle-wrapper.jar is not bundled in this generated archive.
echo On Windows, restore gradle\wrapper\gradle-wrapper.jar or run Gradle 9.7.0 directly.
exit /b 1
