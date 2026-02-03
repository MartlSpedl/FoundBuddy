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

COPY clip_service.py /app/

# IMPORTANT: No preload step here (Render Free OOM)
# RUN python -c "import open_clip; open_clip.create_model_and_transforms('ViT-B-16', pretrained='openai'); print('preloaded')"

EXPOSE 8001

# IMPORTANT: bind to Render's provided PORT
CMD ["sh", "-c", "uvicorn clip_service:app --host 0.0.0.0 --port ${PORT:-8001}"]
