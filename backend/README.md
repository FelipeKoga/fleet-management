# Deploy

serverless deploy

# Deploy function

serverless deploy function --function sendMessage

# Test local

## USER

yarn test user get "-e USER_TABLE=tcc-user-dev -e CONNECTION_TABLE=tcc-connection-dev"
yarn test user create "-e USER_TABLE=tcc-user-dev -e CONNECTION_TABLE=tcc-connection-dev"
yarn test user remove "-e USER_TABLE=tcc-user-dev -e CONNECTION_TABLE=tcc-connection-dev"
yarn test user list "-e USER_TABLE=tcc-user-dev -e CONNECTION_TABLE=tcc-connection-dev"
yarn test user update "-e USER_TABLE=tcc-user-dev -e CONNECTION_TABLE=tcc-connection-dev"

wscat -c wss://ljdjapabh7.execute-api.us-east-1.amazonaws.com/dev?username=kosloski@email.com

## CHAT

yarn test chat addMessage "-e CHAT_TABLE=tcc-chat-dev -e USER_CHAT_TABLE=tcc-user-chat-dev -e USER_TABLE=tcc-user-dev"
yarn test chat createChat "-e CHAT_TABLE=tcc-chat-dev -e USER_CHAT_TABLE=tcc-user-chat-dev -e USER_TABLE=tcc-user-dev"
yarn test chat createGroup "-e CHAT_TABLE=tcc-chat-dev -e USER_CHAT_TABLE=tcc-user-chat-dev -e USER_TABLE=tcc-user-dev "
yarn test chat chats "-e CHAT_TABLE=tcc-chat-dev -e USER_CHAT_TABLE=tcc-user-chat-dev -e USER_TABLE=tcc-user-dev"
yarn test chat messages "-e CHAT_TABLE=tcc-chat-dev -e USER_CHAT_TABLE=tcc-user-chat-dev -e USER_TABLE=tcc-user-dev"
