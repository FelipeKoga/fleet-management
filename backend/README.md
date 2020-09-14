# Deploy

serverless deploy

# Deploy function

serverless deploy function --function sendMessage

# Test local

serverless invoke local --function user --path functions/user/tests/get.json

serverless invoke local --function chat --path functions/chat/tests/chats.json -e CHAT_TABLE=chat -e USER_TABLE=user

serverless invoke local --function connection-handler --path functions/connection-handler/tests/connect.json -e USER_TABLE=user
