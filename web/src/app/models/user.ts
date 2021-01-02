export enum UserStatus {
  ONLINE = "ONLINE",
  OFFLINE = "OFFLINE",
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

  constructor({
    name = "",
    email = "",
    phone = "",
    customName = "",
    role = UserRole.EMPLOYEEE,
    status = UserStatus.OFFLINE,
    companyId = "",
    username = email,
  }) {
    this.name = name;
    this.email = email;
    this.phone = phone;
    this.role = role;
    this.status = status;
    this.customName = customName;
    this.username = username;
    this.companyId = companyId;
  }
}
