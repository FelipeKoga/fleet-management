import { Location } from "./location";

export enum UserStatus {
  ONLINE = "ONLINE",
  OFFLINE = "OFFLINE",
  DISABLED = "DISABLED",
}

export enum UserRole {
  ADMIN = "ADMIN",
  OPERATOR = "OPERATOR",
  EMPLOYEEE = "EMPLOYEE",
}

export class User {
  name: string;
  email: string;
  username: string;
  phone: string;
  role: string;
  status: UserStatus;
  customName: string;
  companyId: string;
  avatar: string;
  avatarUrl?: string;
  location?: Location;
  locationUpdate: number;
  color: string;

  constructor({
    name = "",
    email = "",
    phone = "",
    customName = "",
    role = UserRole.EMPLOYEEE,
    status = UserStatus.OFFLINE,
    companyId = "",
    username = email,
    avatar = "",
    avatarUrl = "",
    location = null,
    locationUpdate = 0,
    color = "#2196f3",
  }) {
    this.name = name;
    this.email = email;
    this.phone = phone;
    this.role = role;
    this.status = status;
    this.customName = customName;
    this.username = username;
    this.companyId = companyId;
    this.avatar = avatar;
    this.avatarUrl = avatarUrl;
    this.location = location;
    this.locationUpdate = locationUpdate;
    this.color = color;
  }
}
