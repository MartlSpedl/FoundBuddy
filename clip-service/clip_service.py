from fastapi import FastAPI
from pydantic import BaseModel
import open_clip
import torch
import requests
from PIL import Image
from io import BytesIO
from pathlib import Path

app = FastAPI()

device = "cuda" if torch.cuda.is_available() else "cpu"

model, _, preprocess = open_clip.create_model_and_transforms(
    model_name="ViT-B-16",
    pretrained="openai"
)
tokenizer = open_clip.get_tokenizer("ViT-B-16")

model = model.to(device)
model.eval()

print("CLIP model loaded on", device)


class TextRequest(BaseModel):
    text: str


class ImageRequest(BaseModel):
    image_uri: str


@app.get("/health")
def health():
    return {"status": "ok", "device": device}


@app.post("/embed/text")
def embed_text(req: TextRequest):
    tokens = tokenizer([req.text]).to(device)
    with torch.no_grad():
        embedding = model.encode_text(tokens)
        embedding = embedding / embedding.norm(dim=-1, keepdim=True)
    return embedding[0].cpu().tolist()


@app.post("/embed/image")
def embe

