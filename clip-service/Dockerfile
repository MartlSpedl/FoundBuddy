FROM python:3.11-slim

WORKDIR /app
COPY requirements.txt /app/
RUN pip install --no-cache-dir -r requirements.txt

COPY clip_service.py /app/

EXPOSE 8001
CMD ["uvicorn", "clip_service:app", "--host", "0.0.0.0", "--port", "8001"]
