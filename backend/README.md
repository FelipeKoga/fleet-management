# Deploy

serverless deploy

# Deploy function

serverless deploy function --function sendMessage

# Test local

## USER

yarn test user get "-e USER_TABLE=tcc-user-dev"
yarn test user create "-e USER_TABLE=tcc-user-dev"
yarn test user delete "-e USER_TABLE=user"
yarn test user list "-e USER_TABLE=user"
yarn test user update "-e USER_TABLE=user"
