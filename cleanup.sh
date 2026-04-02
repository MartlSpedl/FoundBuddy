#!/bin/bash

echo "🧹 FoundBuddy Repository aufräumen..."
echo ""

# Backend aufräumen
echo "📦 Backend wird aufgeräumt..."
rm -rf FoundBuddy-backend/.gradle
rm -rf FoundBuddy-backend/build
rm -rf FoundBuddy-backend/target
rm -rf FoundBuddy-backend/.settings
rm -rf FoundBuddy-backend/uploads
rm -f FoundBuddy-backend/.classpath
rm -f FoundBuddy-backend/.project
rm -f FoundBuddy-backend/.deploy

# Frontend aufräumen
echo "📱 Frontend wird aufgeräumt..."
rm -rf FoundBuddy-frontend/.idea
rm -rf FoundBuddy-frontend/.kotlin

echo ""
echo "✅ Cleanup abgeschlossen!"
echo "💡 Jetzt kannst du mit 'git add -A' und 'git commit' die Änderungen committen"
