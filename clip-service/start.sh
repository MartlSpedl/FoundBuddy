#!/bin/bash

# Railway verwendet automatisch den PORT aus der Environment
echo "Starting CLIP service on port ${PORT:-8000}"

# Installiere Abhängigkeiten
pip install -r requirements.txt

# Starte den Service
uvicorn clip_service:app --host 0.0.0.0 --port ${PORT:-8000}
