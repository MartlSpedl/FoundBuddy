import os
import sys
from huggingface_hub import HfApi

token = os.environ.get("HF_TOKEN")
if not token:
    print("ERROR: HF_TOKEN environment variable not set")
    sys.exit(1)

commit_sha = os.environ.get("GITHUB_SHA", "unknown")

api = HfApi()

print(f"Uploading FoundBuddy-backend to HuggingFace Space (commit: {commit_sha[:8]})...")

api.upload_folder(
    folder_path="FoundBuddy-backend",
    repo_id="MartlSpedl/foundbuddy-backend",
    repo_type="space",
    token=token,
    ignore_patterns=[
        "bin/**",
        "build/**",
        "uploads/**",
        "*.class",
        "*.jar",
        "src/main/resources/firebase-key.json",
        ".idea/**",
        ".gradle/**",
    ],
    commit_message=f"Auto-deploy from GitHub commit {commit_sha[:8]}",
)

print("Upload complete!")
