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
tokenizer = open_clip.get_tokenizer("ViT-B-32")

print("CLIP model loaded on", device)


class TextRequest(BaseModel):
    text: str


class ImageRequest(BaseModel):
    image_uri: str


@app.post("/embed/text")
def embed_text(req: TextRequest):
    tokens = tokenizer([req.text]).to(device)
    with torch.no_grad():
        embedding = model.encode_text(tokens)
        embedding = embedding / embedding.norm(dim=-1, keepdim=True)
    return embedding[0].cpu().tolist()


@app.post("/embed/image")
def embed_image(req: ImageRequest):
    if req.image_uri.startswith("http"):
        response = requests.get(req.image_uri)
        image = Image.open(BytesIO(response.content)).convert("RGB")
    else:
        image = Image.open(Path(req.image_uri)).convert("RGB")

    image = preprocess(image).unsqueeze(0).to(device)

    with torch.no_grad():
        embedding = model.encode_image(image)
        embedding = embedding / embedding.norm(dim=-1, keepdim=True)

    return embedding[0].cpu().tolist()


# start
# uvicorn clip_service:app --host 127.0.0.1 --port 8001