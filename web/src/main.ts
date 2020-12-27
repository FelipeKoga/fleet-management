import { enableProdMode } from "@angular/core";
import { platformBrowserDynamic } from "@angular/platform-browser-dynamic";

import { AppModule } from "./app/app.module";
import { environment } from "./environments/environment";

import Amplify from "aws-amplify";
import stack from "./stack.json";

if (environment.production) {
  enableProdMode();
}

Amplify.configure({
  Auth: {
    mandatorySignIn: true,
    region: stack.Region,
    userPoolId: stack.UserPoolId,
    identityPoolId: stack.IdentityPoolId,
    identityPoolRegion: stack.Region,
    userPoolWebClientId: stack.UserPoolClientId,
  },
  aws_appsync_region: stack.Region,
  aws_appsync_authenticationType: "AMAZON_COGNITO_USER_POOLS",
});

platformBrowserDynamic()
  .bootstrapModule(AppModule)
  .catch((err) => console.error(err));
