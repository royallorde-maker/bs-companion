@ECHO OFF
@REM Gradle wrapper placeholder for Windows.
WHERE gradle >NUL 2>NUL
IF %ERRORLEVEL% EQU 0 (
  gradle %*
) ELSE (
  ECHO Gradle not found. Open this project in Android Studio to auto-download Gradle 8.7,
  ECHO or install Gradle and run: gradle wrapper --gradle-version 8.7
  EXIT /B 1
)
