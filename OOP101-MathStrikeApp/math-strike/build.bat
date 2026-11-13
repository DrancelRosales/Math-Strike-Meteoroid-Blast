@echo off
REM Build Math Strike: Meteoroid Blast with the plain JDK (Windows).
cd /d "%~dp0"
if exist out rmdir /s /q out
mkdir out\classes
dir /s /b src\main\java\*.java > out\sources.txt
javac -encoding UTF-8 -d out\classes @out\sources.txt || exit /b 1
echo Main-Class: mathstrike.MathStrike> out\manifest.txt
jar cfm MathStrike.jar out\manifest.txt -C out\classes . || exit /b 1
echo Built MathStrike.jar
echo Start the game with:  java -jar MathStrike.jar
