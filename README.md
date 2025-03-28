# 1. Video 

- s3 bucket-> config permissions -> setup IAM user with all persmissions for upload,fetch and del
- use AWS Element Media Convert -> for different resolution , store diff res in diff folder
- HLS (index.m3u8)--> for auto selecting resolution
# Upload
- Take multipart file in controller
- Generate unique name 
- upload to S3 -> PutObject API
- now store metadata in Postgresql
- return pre-signed URL

# Fetch

- generate and return a pre-signed URL to frontend for temp access
- use CDN (clodFront) for fast streaming
- return HLS url (index.m3u8) instead or .mp4

# Delete video from  S3

- use S3 key (file name )
- Now del using DeleteObject API
- now remove metadata from PostgreSQL