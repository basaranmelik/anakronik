from fastapi import FastAPI
from controllers.upload_controller import router as upload_router
from controllers.query_controller import router as query_router

app = FastAPI()
app.include_router(upload_router, tags=["Upload"])
app.include_router(query_router, tags=["Query"])
