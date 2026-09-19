#!/bin/bash
set -e

WIKI_REPO="https://github.com/merx666/hyperreal-journal.wiki.git"
TMP_DIR=$(mktemp -d)

echo "🔄 Sprawdzanie repozytorium GitHub Wiki: $WIKI_REPO"

if git clone "$WIKI_REPO" "$TMP_DIR" 2>/dev/null; then
    echo "✅ Repozytorium Wiki istnieje. Kopiowanie stron z katalogu wiki/..."
    cp -r wiki/* "$TMP_DIR/"
    cd "$TMP_DIR"
    git add .
    if git diff-index --quiet HEAD --; then
        echo "ℹ️ Brak zmian do zakomitowania (wiki jest aktualne)."
    else
        git commit -m "docs(wiki): sync official project wiki in Polish"
        git push origin master 2>/dev/null || git push origin main
        echo "🎉 Strony Wiki zostały pomyślnie zsynchronizowane i opublikowane na GitHubie!"
    fi
    rm -rf "$TMP_DIR"
else
    echo "⚠️ Repozytorium GitHub Wiki nie zostało jeszcze zainicjalizowane na serwerach GitHuba."
    echo "👉 Wejdź w przeglądarce pod adres: https://github.com/merx666/hyperreal-journal/wiki"
    echo "👉 Kliknij zielony przycisk 'Create the first page' i zapisz pierwszą stronę (Home)."
    echo "👉 Następnie uruchom ten skrypt ponownie (./scripts/sync-wiki.sh) – automatycznie wyśle wszystkie podstrony i spis treści!"
fi
