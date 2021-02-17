import { User } from "../models/user";

export const getAvatar = (user: User) => {
  return `https://ui-avatars.com/api/?rounded=true&name=${user.name}&background=${user.color}&color=000`;
};
