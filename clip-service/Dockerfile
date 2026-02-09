FROM python:3.11-slim

WORKDIR /app

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1

# Optional Cache-Ordner (okay so)
ENV HF_HOME=/app/.cache/huggingface
ENV TORCH_HOME=/app/.cache/torch
ENV TRANSFORMERS_CACHE=/app/.cache/huggingface

COPY requirements.txt /app/

# Install torch CPU + dependencies
RUN pip install --upgrade pip && \
    pip install --default-timeout=600 --retries 20 \
      torch torchvision --index-url https://download.pytorch.org/whl/cpu && \
    pip install --default-timeout=600 --retries 20 -r requirements.txt

# Setze Environment Variables
ENV PYTHONPATH=/app
ENV TRANSFORMERS_CACHE=/app/.cache
ENV HF_HOME=/app/.cache

# Expose Port (Railway setzt dies automatisch)
EXPOSE 8000

# Starte den Service
CMD ["uvicorn", "clip_service:app", "--host", "0.0.0.0", "--port", "8000"]
