#!/bin/bash
echo "===================================================================="
echo " Compiling 'sixseven' Personal Expense Manager (3-Member Division)"
echo "===================================================================="

mkdir -p out

javac -encoding UTF-8 -cp ".:lib/ojdbc8.jar" -d out module_db_dao/*.java module_core_logic/*.java module_ui/*.java Main.java

if [ $? -ne 0 ]; then
    echo "[ERROR] Compilation failed!"
    exit 1
fi

echo "[SUCCESS] Build succeeded!"
echo "Launching sixseven Personal Expense Manager..."
java -cp "out:lib/ojdbc8.jar" Main
