#!/usr/bin/env bash
# Script to synchronize live Room SQLite database from Android emulator/device to hyperreal_journal.db

PACKAGE_NAME="info.hyperreal.journal"
DB_NAME="hyperreal_journal_db"
LOCAL_DEST="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)/hyperreal_journal.db"

echo "Checking connected devices..."
adb devices

echo "Attempting to pull database from $PACKAGE_NAME..."
if adb exec-out run-as "$PACKAGE_NAME" cat "databases/$DB_NAME" > "$LOCAL_DEST.tmp" 2>/dev/null && [ -s "$LOCAL_DEST.tmp" ]; then
    mv "$LOCAL_DEST.tmp" "$LOCAL_DEST"
    echo "Successfully synced live database to $LOCAL_DEST"
    sqlite3 "$LOCAL_DEST" "SELECT count(*) FROM ingestions;" >/dev/null 2>&1 && echo "Database verified (valid SQLite file)."
else
    rm -f "$LOCAL_DEST.tmp"
    echo "Could not pull using run-as (app might not be installed in debug mode or not yet launched)."
    echo "Alternatively, you can pull manually via: adb pull /data/data/$PACKAGE_NAME/databases/$DB_NAME $LOCAL_DEST"
fi
