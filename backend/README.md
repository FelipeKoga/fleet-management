# Deploy

serverless deploy

# Deploy function

serverless deploy function --function sendMessage

# Test local

## USER

yarn test user get
yarn test user create
yarn test user remove
yarn test user list
yarn test user update

wscat -c wss://ljdjapabh7.execute-api.us-east-1.amazonaws.com/dev?username=teste2@gmail.com
wscat -c wss://ljdjapabh7.execute-api.us-east-1.amazonaws.com/dev?username=teste1@gmail.com

## CONNECTION

yarn test connection-handler connect
yarn test connection-handler disconnect

## CHAT

yarn test chat addMessage
yarn test chat createChat
yarn test chat createGroup
yarn test chat chats
yarn test chat messages

## WEBSOCKET

{"action": "SEND_MESSAGE", "data": { "chatId": "YCNzfqYoL0NvQqIPit1Yb", "username": "teste1@gmail.com", "message": "Ola from cmd" }}
