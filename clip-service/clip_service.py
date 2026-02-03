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

_model = None
_preprocess = None
_tokenizer = None


def get_model():
    """
    Lazy-load CLIP model on first request.
    Helps the service start and bind the port before loading heavy weights.
    """
    global _model, _preprocess, _tokenizer

    if _model is None:
        model, _, preprocess = open_clip.create_model_and_transforms(
            model_name="ViT-B-16",
            pretrained="openai"
        )
        tokenizer = open_clip.get_tokenizer("ViT-B-16")

        model = model.to(device)
        model.eval()

        _model = model
        _preprocess = preprocess
        _tokenizer = tokenizer

        print("CLIP model loaded on", device)

    return _model, _preprocess, _tokenizer


class TextRequest(BaseModel):
    text: str


class ImageRequest(BaseModel):
    image_uri: str


@app.get("/health")
def health():
    # Do NOT load model here – keep it lightweight.
    return {"status": "ok", "device": device, "modelLoaded": _model is not None}


@app.post("/embed/text")
def embed_text(req: TextRequest):
    model, preprocess, tokenizer = get_model()

    tokens = tokenizer([req.text]).to(device)
    with torch.no_grad():
        embedding = model.encode_text(tokens)
        embedding = embedding / embedding.norm(dim=-1, keepdim=True)
    return embedding[0].cpu().tolist()


@app.post("/embed/image")
def embed_image(req: ImageRequest):
    model, preprocess, tokenizer = get_model()

    if req.image_uri.startswith("http"):
        r = requests.get(req.image_uri, timeout=20)
        r.raise_for_status()
        image = Image.open(BytesIO(r.content)).convert("RGB")
    else:
        image = Image.open(Path(req.image_uri)).convert("RGB")

    image_t = preprocess(image).unsqueeze(0).to(device)

    with torch.no_grad():
        embedding = model.encode_image(image_t)
        embedding = embedding / embedding.norm(dim=-1, keepdim=True)

    return embedding[0].cpu().tolist()