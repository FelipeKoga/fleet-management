export enum UserStatus {
  ONLINE = "ONLINE",
  OFFLINE = "OFFLINE",
  DISABLED = "DISABLED",
}

export enum UserRole {
  ADMIN = "ADMIN",
  OPERATOR = "OPERATOR",
  EMPLOYEEE = "EMPLOYEEE",
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
  }
}
